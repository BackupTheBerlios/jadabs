/*
 * Created on Jul 8, 2003
 *
 * $Id: LocalComponentLoader.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import ch.ethz.iks.logger.ILogger;
import ch.ethz.iks.logger.Logger;

/**
 * @author andfrei
 *
 */
public class LocalComponentLoader implements Suspendable {

	private static ILogger LOG = Logger.getLogger(LocalComponentLoader.class);

	private static int INSERT 	    = 0;	// used to specify the event for outgoing (Master)
	private static int WITHDRAW		= 0;	// or if the event was received as ingoing (Slave)

	protected static String m_pcoprep = BootstrapConstants.PCOPREP_DEFAULT;
	protected static File pcoppath = new File(m_pcoprep);

	//---------------------------------------------
	// state
	//---------------------------------------------

	private PCopRepChecker pcoprepThread;

	private Object suspendguard = new Object();
	private boolean suspended = false;

	//---------------------------------------------
	// constructor
	//---------------------------------------------

	/**
	 * Create an ExtensionManager with given PeerGroup.
	 * Takes default extension directory ./bin/pcoprep
	 * 
	 * @param peergroup
	 */
	public LocalComponentLoader()
    {

	}

	/**
	 * Create an ExtensionManager with given PeerGroup and a download directory for extensions.
	 * 
	 * @param pcoprepdir
	 */
	public LocalComponentLoader(String pcoprepdir)
    {		
		// set fileSaveLoc for Extensions to a new download directory
		if ( pcoprepdir != null){
			m_pcoprep = pcoprepdir + File.separatorChar;
			pcoppath = new File(m_pcoprep);
		}
	}

	/**
	 * Specify the timeinterval to check the repository for package changes. In
	 * case of 0 do only one check at startup.
	 * 
	 * @param timeinterval
     * @return boolean if the loader has been started successfully
	 */
	public boolean startLoader(int timeinterval){
		
		if (pcoprepThread == null) {
			
			pcoprepThread = new PCopRepChecker(timeinterval);                
            
            if (!pcoppath.exists())
            {
                LOG.info("repository does not exist: " + pcoppath.getAbsolutePath());
                return false;
            }
			else if (timeinterval != 0)
            {
				pcoprepThread.setName("PCopRepChecker ");
		
				pcoprepThread.threadRunning = true;
				pcoprepThread.start();
                
				return true;
			}
			else 
            {
				pcoprepThread.doOneCheck();
                return false;
            }
		}
		else
			return true;
    }

	public void stopLoader(){
		
		pcoprepThread.threadRunning = false;
		pcoprepThread = null;
	}

	/**
	 * Load Persistent Components from the persistent repository directory, only once.
	 * To load/unload dynamically use the PCopRepChecker, which checks the persistent component
	 * repository for changes.
	 *
	 */
	protected void loadPCopRep(){
		
		File[] files = pcoppath.listFiles();
		
		if (files != null){
			for (int i = 0; i < files.length; i++) {
				LOG.info(files[i].getAbsoluteFile().getPath());
                
				
				try {
					JarFile jarfile = new JarFile(files[i]);
					Attributes atts = jarfile.getManifest().getMainAttributes();
					
					String version = atts.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
					String classname = atts.getValue(Attributes.Name.MAIN_CLASS);
					String codebase = files[i].getName();
					LOG.info("initialize classname: " + classname + " codebase: " + codebase + " version: " + version);

					//micha: String extid = IDFactory.Instance().newExtensionID(classname);
					ComponentResource copres = (ComponentResource) ComponentRepository.Instance().createResource( codebase, classname);

					ComponentRepository.Instance().insert(copres);
									
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
	}

	public static IComponentResource getComponentResource(String codebase){
		String file = null;
		if( LOG.isDebugEnabled() ){
			LOG.debug("getComponentResource: " + codebase);
		}
		
        String path = "";
		try {
			file = pcoppath.getAbsolutePath() + File.separatorChar + codebase;
			LOG.info("loading jar "+file);
            
            //BUGFIX: problems with deleting the file under windows, use a URL
            String OS = System.getProperty("os.name").toLowerCase();
            if (OS.indexOf("windows") > -1)
            {
                URL urlfile= new URL("file:/"+file);
                file = urlfile.getPath();
            }
            LOG.info("loading from "+file);
            
			JarFile jarfile = new JarFile(file);
            
			IComponentResource copres = null;
			String urnid = null;
							
			
			copres = ComponentRepository.Instance().createResource( codebase, null, 0);
							
			Manifest manifest = jarfile.getManifest();
			if (manifest != null){
				Attributes atts = jarfile.getManifest().getMainAttributes();
								
				String content = atts.getValue(Attributes.Name.CONTENT_TYPE);
								
				if (content != null && content.equals(IComponent.COMPONENT_TYPE)){							
					
                    // TODO: changed version info, migration still assumes 0,1,2,...
                    // int version = new Integer(atts.getValue(Attributes.Name.IMPLEMENTATION_VERSION)).intValue();
                    int version = 0;
					String classname = atts.getValue(Attributes.Name.MAIN_CLASS);
					
					LOG.debug("generate copres: "+classname+ " " + codebase);
									
					copres = ComponentRepository.Instance().createResource(  codebase, classname, version);
				}
			}
			
            // cleanup resources
            jarfile.close();
            
			return copres;
									
		} catch (IOException e) {
			LOG.error("JarFile = "+path,e);
		}
		
		return null;
	}

    public File getRepository()
    {
        return pcoppath;
    }
    
	/**
	 * PCopRepChecker checks the persistent (in the filesystem) component repository for new or deleted components.
	 * To add, alter, or remove a component do the changes in the filesystem by normal file commands.
	 */
	class PCopRepChecker extends Thread {

		boolean threadRunning = true;
		boolean doonce = false;
		
		int timeinterval;
		
		/**
		 * Specify the timeinterval, if 0 do only once.
		 * 
		 * @param timeinterval
		 */
		public PCopRepChecker(int timeinterval){
		
			this.timeinterval = timeinterval;

		}	

		void doOneCheck(){
			LOG.info("scanning pcoprep...");
			Hashtable jads = new Hashtable(); // (String classname, IComponentResource copres)
			File[] files = pcoppath.listFiles();
		
			if (files != null){
				// put all files into a joinable form
				for (int i = 0; i < files.length; i++) {
//					LOG.info(files[i].getAbsoluteFile());
				
					String codebase = files[i].getName();
					IComponentResource copres = getComponentResource(codebase);
				
					jads.put(codebase, copres);

				}
					
				// join the running repository with the static rep in the directory
					
				IComponentRepository coprep = ComponentRepository.Instance();
					
				// remove cops from dCR which are not in the pCR anymore
				//micha: to be more general, look in the extResLocation if the file is not there
				// if this location is different from the folder scanned here (the pcoprep directory)
				Hashtable hdcoprescrs = coprep.getComponentResources();
				for (Enumeration ekeys = hdcoprescrs.keys(); ekeys.hasMoreElements();){
					String codebase = (String)ekeys.nextElement();
					String location = ((ComponentResource)hdcoprescrs.get(codebase)).getExtResLocation();
					File copBinary = new File(location + File.separatorChar + codebase);
					if (pcoppath.getPath().equals(location) && !jads.containsKey(codebase)){
						coprep.withdraw((IComponentResource)hdcoprescrs.get(codebase));
					} else if (! copBinary.exists()) {
						LOG.info(" withdrawing component: No binary found at "+copBinary);
						coprep.withdraw((IComponentResource)hdcoprescrs.get(codebase));
					}
				}
					
				Hashtable tobeinserted = new Hashtable();
					
				// check for cops which are not yet running in the dCR
				for (Enumeration en = jads.elements(); en.hasMoreElements(); ) {
					IComponentResource copres = (IComponentResource)en.nextElement();
						
					String codebase = copres.getCodeBase();
						
					// check if running in dCR and has the same version, else start it up
					IComponentResource dcopres;
					if ((dcopres=coprep.getComponentResourceByCodebase(codebase)) == null){
						coprep.insert(copres);
							
						LOG.info("inserted cop with classname: " + copres.getClassName() + " codebase: " + copres.getCodeBase() + " version: " + copres.getVersion());
					}
					else if (dcopres.getVersion() < copres.getVersion()){
						replace(dcopres, copres);
							
					}
						
				}
					
				// tobeinserted is now loaded and started beginning with the component where all 
				// dependend components are already loaded

					
			}
				
		}

		public void run(){
		
			while(threadRunning ){
				
				// suspend this thread if aquired from outside
				synchronized(suspendguard){
					while (suspended)
						try {
							LOG.info("suspended "+this);
							suspendguard.wait();
						} catch (InterruptedException e1) {
							LOG.warn("suspend has been interrupted");
							break;
						}
				}
				
				doOneCheck();
				
				// sleep the timeinterval
				try {
					Thread.sleep(timeinterval);
				} catch (InterruptedException e) {
					LOG.warn("PCopRepChecker has been interrupted.", e);
				}
			
			}
		}

	
	} // PCopRepChanges	
	
	protected void replace(IComponentResource oldCopres, IComponentResource newCopres) {
		// remove the old one, replace by the new one
		// TODO: create a replace method which handles a dynamic switching
		ComponentRepository.Instance().insert(newCopres);
		LOG.info("remove component classname: " + oldCopres.getClassName() + ", " + oldCopres.getCodeBase() + " v: " + oldCopres.getVersion());
		ComponentRepository.Instance().withdraw(oldCopres);
		LOG.info("insert component classname: " + newCopres.getClassName() + ", " + newCopres.getCodeBase() + " v: " + newCopres.getVersion());
	}

	//---------------------------------------------
	// Suspendable implementations
	//---------------------------------------------
	
	/**
	 * Supsend LocalComponentLoader, by suspending the CheckerThread to
	 * load new components.
	 */
	public void suspend() {
		
		synchronized(suspendguard){
			suspended = true;
			suspendguard.notify();
		}
	}

	/**
	 * Resume a suspended LocalComponentLoader, resume of a not suspended Thread
	 * will have no effect.
	 */
	public void resume() {
		synchronized(suspendguard){
			suspended = false;
			suspendguard.notify();
		}
	}
	
} // LocalComponentLoader
