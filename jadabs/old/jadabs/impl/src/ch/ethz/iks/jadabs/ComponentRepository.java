/*
 * Created on Sep 3, 2003
 * 
 * $Id: ComponentRepository.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import ch.ethz.iks.logger.ILogger;
import ch.ethz.iks.logger.Logger;

/**
 * The dynamic Component Repository (dCR) contains all the components, in its
 * not loaded, initialized, started, stopped phases. Query the dCR to get
 * information about the components.
 * 
 * Initialize the dCR with the ComponentManager if you want to use the Local
 * Component Loader, which loads components from the persitent component
 * directory. Use the dCR directly if you want to write your own component
 * loader.
 * 
 * @author andfrei
 * @version $Revision: 1.1 $
 *  
 */
public class ComponentRepository implements IComponentRepository
{

    private static ILogger LOG = Logger.getLogger(ComponentRepository.class);

    private static ComponentRepository coprep;

    private Hashtable coprescrs = new Hashtable(); // list of (String codebase,
                                                   // IComponentResource copres)

    private ComponentRepository() {

    }

    public static ComponentRepository Instance()
    {

        if (coprep == null) {
            coprep = new ComponentRepository();
            ComponentRepository.coprep
                    .setResourceFactory(new ComponentResourceFactory());
        }
        return ComponentRepository.coprep;
    }

    /**
     * Insert new component or service by its codebase which resides in
     * the repository (pcoprep) directory.
     * 
     * Follows the lifecycle of init, start for a service.
     * 
     * @param codebase - a .jar file in the pcoprep
     */
    public void insert(String codebase)
    {
        
        String path = "";
        try {
            File pcoppath = new File((String)Jadabs.Instance().getProperty(Jadabs.PCOPREP));
            String absjarfilename = pcoppath.getAbsolutePath() + File.separatorChar + codebase;

            //BUGFIX: problems with deleting the file under windows, use a URL
            URL urlfile= new URL("file:/"+absjarfilename); 
            path = urlfile.getFile();
            
            if (LOG.isDebugEnabled())
                LOG.debug("jarfile to load: "+path);
            
            JarFile jarfile = new JarFile(path);
            
            IComponentResource copres = null;
            String urnid = null;
                            
            
//            copres = ComponentRepository.Instance().createResource( codebase, null, 0);
                            
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
                } else {
                    copres = ComponentRepository.Instance().createResource( codebase, null, 0);
                }
            } else
                copres = ComponentRepository.Instance().createResource( codebase, null, 0);
            
            
            // cleanup resources
            jarfile.close();
            
            // insert now the copres
            insert(copres);
                                    
        } catch (IOException e) {
            LOG.error("JarFile = "+path,e);
        }
    }
    
    /**
     * Insert a new component by its ComponentResource.
     * Follows the lifecycle of init, start for a service.
     */
    public void insert(IComponentResource copRes)
    {

        try {

            IComponentResource dcopres = getComponentResourceByCodebase(copRes
                    .getCodeBase());
            if (dcopres != null) copRes = dcopres;

            initComponent(copRes);

            String[] args = Jadabs.Instance().getCommandlineArguments();
            copRes.startComponent(args);

            LOG.info("component started: " + copRes.getClassName());

            // start components main class with an independent thread
            //			ComponentStarter extRunner = new ComponentStarter(copRes);
            //			extRunner.start();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * Removes a ComponentResource by first stopping if not already
     * stopped, desposing and removing it from the runtime repository.
     * 
     */
    public void withdraw(IComponentResource copRes)
    {

        if (copRes != null) {
            String copcname = copRes.getCodeBase();

            if (copRes.isService() && copRes.isStarted())
            {
                LOG.info("stopping component: " + copcname);
                copRes.stopComponent();
            }
            
            copRes.disposeComponent();

            // remove extension from the list
            // micha: codebase is not unique (component evolution version x and
            // y have same codebase)
            if (coprescrs.get(copcname) == copRes) {
                coprescrs.remove(copcname);
            } else {
                // wrong copRes, find the key of the copRes to be withdrawn
                Object key = null;
                Iterator cops = coprescrs.entrySet().iterator();
                while (cops.hasNext()) {
                    Map.Entry entry = (Map.Entry) cops.next();
                    if (entry.getValue() == copRes) {
                        // found entry
                        key = entry.getKey();
                        break;
                    }
                }
                if (key != null) coprescrs.remove(key);
            }

            LOG.debug("component removed from repository: " + copcname);
        }

    }

    /**
     * Removes all components from the runtime repository. 
     *
     */
    protected void withdrawAll()
    {

        for (Enumeration en = coprescrs.elements(); en.hasMoreElements();) {
            IComponentResource copres = (IComponentResource) en.nextElement();
            String copcname = copres.getCodeBase();

            withdraw(copres);

            if (LOG.isInfoEnabled())
                    LOG.info("stopped component: " + copcname);
        }

    }

    public Hashtable getComponentResources()
    {

        return coprescrs;
    }

    public IComponentResource getComponentResourceById(String copID)
    {

        for (Enumeration en = coprescrs.elements(); en.hasMoreElements();) {
            IComponentResource copres = (IComponentResource) en.nextElement();

            if (copres.getCopID().equals(copID)) return copres;
        }

        return null;
    }

    public IComponentResource getComponentResourceByClassname(String classname)
    {

        for (Enumeration en = coprescrs.elements(); en.hasMoreElements();) {
            IComponentResource copres = (IComponentResource) en.nextElement();
            if (classname.equals(copres.getClassName())) // micha:
                                                         // copres.getClassName().equals(classname)
                                                         // may lead to
                                                         // Nullpointer if no
                                                         // main class is
                                                         // specified in
                                                         // manifest (e.g. proxy
                                                         // components)
                    return copres;
        }
        return null;
    }

    public IComponentResource getComponentResourceByCodebase(String codebase)
    {

        return (IComponentResource) coprescrs.get(codebase);
    }

    /**
     * Test if Component is already initialized.
     * 
     * @param codebase
     * @return
     */
    public boolean isComponentInitializedOrStarted(String codebase)
    {

        IComponentResource copres;
        /*
         * if ((copres = (IComponentResource) coprescrs.get(codebase)) != null)
         * return copres.isInitialized() || copres.isStarted(); else return
         * false;
         */
        return (coprescrs.get(codebase) != null);
    }

    public void initComponent(IComponentResource copRes)
            throws InstantiationException, IllegalAccessException
    {
        // micha: switched statements 1. put, 2. init
        // need the component a class belongs to
        // in order to get its bytecode for loadtime AOP
        // bytecode instrumentation (javassist).
        // Knowing the component of the class that is being loaded (e.g. the
        // copRes.getClass())
        // allows to get its codebase and thus to create a classpool for this
        // loaction (Jar).
        // A classpool is required to perform byte code instrumentation with
        // javassist
        coprescrs.put(copRes.getCodeBase(), copRes);

        copRes.initComponent();
        LOG.debug("initialized component: " + copRes.getCodeBase()
                + " mainclass: " + copRes.getClassName());

    }

    private IResourceFactory copFactory;

    /**
     * Factory to create <code>IComponentResource</code> objects managed by
     * the repository TODO: use IDFactory to create copID (BUT loadFromDep needs
     * codebase in dep Vector)
     */
    public IComponentResource createResource(String codebase, String classname)
    {
        String urnid = codebase;//IDFactory.Instance().newExtensionID(codebase);

        int index = 0;
        if ((index = urnid.lastIndexOf(".jar")) > 0)
                urnid = urnid.substring(0, index);

        return copFactory.createResource(urnid, codebase, classname);
    }

    public IComponentResource createResource(String urnid, String codebase,
            String classname)
    {
        return copFactory.createResource(urnid, codebase, classname);
    }

    public IComponentResource createResource(String codebase, String classname,
            int version)
    {

        String urnid = codebase;

        int index = 0;
        if ((index = urnid.lastIndexOf(".jar")) > 0)
                urnid = urnid.substring(0, index);

        return copFactory.createResource(urnid, codebase, classname, version);
    }

    public IComponentResource createResource(String urnid, String codebase,
            String classname, int version)
    {
        return copFactory.createResource(urnid, codebase, classname, version);
    }

    public void setResourceFactory(IResourceFactory factory)
    {
        this.copFactory = factory;
    }

    /*
     * public IComponentContext createComponentContext(IComponentResource
     * copRes) { return this.copFactory.createContext(copRes); }
     */

}