package ch.ethz.jadabs.amonem.manager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.amonem.AmonemManager;
import ch.ethz.jadabs.amonem.BundleListener;
import ch.ethz.jadabs.amonem.GroupListener;
import ch.ethz.jadabs.amonem.PeerListener;
import ch.ethz.jadabs.amonem.deploy.AmonemDeploy;
import ch.ethz.jadabs.amonem.deploy.AmonemDeploySkeleton;
import ch.ethz.jadabs.amonem.discovery.AmonemDiscovery;
import ch.ethz.jadabs.servicemanager.ServiceReference;

/*
 * Created on 23.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author barbara
 *
 * 
 */
public class AmonemManagerImpl implements AmonemManager 
{
    

    
    /*
    * --- CONFIG SECTION ---
    * JARPATH is the path (on the filesystem of the machine running the amonem-app)
    * where the deploy finds it's *.jar files.
    */
//    private final String JARPATH = "/local/bam/.maven/repository/jadabs/jars/"; 
    private final String REPOPATH = "/local/bam/.maven/repository/"; 
    private final String LOCAL_DEPLOY_PATH = "/local/bam/amonem-deploy/";
    private final String XARGS_TEMPLATE = "/home/bam/ew/jadabs/new_dummy_init.xargs";
    private final String TMP_DOWNLOAD_DIR = "/tmp";
    private String temp_FOLDER;
    private String FileSeperator= System.getProperty("file.separator");

    private String DOWNLOAD_dir;
	private String DEPLOY_path;
	private String xargs_TEMPLATE;
	
    private final String REMOTE_DEPLOY_PATH = "/usr/local/amonem-deploy/";
    
    /*
    * --- END CONFIG SECTION ---
    */
    
    /**
     *     
     */
	private DAGGroup deployROOT;
	private DAGGroup discoveryROOT;
	
    private static AmonemManagerImpl instance;

    private Logger LOG = Logger.getLogger(AmonemDiscovery.class.getName());

    private static AmonemDiscovery amonemDisc;
    private static AmonemDeploy amonemDepl;

    private static Vector groupListeners = new Vector();
    private static Vector peerListener = new Vector();
    private static Vector bundleListener = new Vector();
    
	public AmonemManagerImpl() {
	}
	
	/**
	 * starts the Manager and prepares the default state
	 */
	public void start()
	{
        // root for DAGs

		deployROOT= new DAGGroup("rootGroup");
		discoveryROOT= new DAGGroup("rootGroup");
        discoveryROOT.setResource(AmonemManagerActivator.pGroup);
        
        this.setROOT(discoveryROOT);
        
        // create discovery
        amonemDisc = new AmonemDiscovery(discoveryROOT, this, AmonemManagerActivator.fwManager);
        amonemDisc.start(AmonemManagerActivator.pGroup, AmonemManagerActivator.gServ);
        
        // create deploy
        amonemDepl = new AmonemDeploy(this);
        
//        doAndreasTest();

	}
	
	public static AmonemManagerImpl Instance()
	{
	    if (instance == null)
	        instance = new AmonemManagerImpl();
	    
	    return instance;
	}
	
//	public void start(PeerGroup pGroup, GroupService gServ, FrameworkManager FWManager) {
//        // root for DAG
//        DAGGroup myDAG = new DAGGroup("rootGroup");
//        myDAG.setResource(pGroup);
//        
//        // create discovery
//        amonemDisc = new AmonemDiscovery(myDAG, this, FWManager);
//        amonemDisc.start(pGroup, gServ);
//        
//        // create deploy
//        amonemDepl = new AmonemDeploy(JARPATH);
//        AmonemDeploySkeleton ads = amonemDepl.getSkeleton("AmonemPeer1");
//        ads.addJar("amonem-discovery-0.1.0.jar");
//        ads.addJar("amonem-discovery-0.7.0.jar");
//        ads.addJar("amonem-manager-0.1.0.jar");
//        ads.addJar("amonem-manager-0.1.0.jar");
//        amonemDepl.deployLocal(ads);
//	}
	
    /**
     *  @param Root of the discovery DAG
     */
	public void setROOT(DAGGroup root) {
		discoveryROOT= root;
	}
	
    /**
     *  @return Root of the deploy DAG
     */
	public DAGGroup getDeployROOT(){
		return deployROOT;
	}
	
	//---------------------------------------------------
    // Implements AmonemManager Interface
    //---------------------------------------------------
	
    /**
     *  @param Name of the Peer
     *  @param Name of the Bundle to be started
     */
	public void startBundle(String PeerName, String BundleName){
		//discoveryDAG
		//amonemDepl.startBundle(Framework fw, long bid)
		DAGPeer peer= (DAGPeer) discoveryROOT.getElement(PeerName);
		if (peer!=null){
			DAGBundle bundle= peer.getBundle(BundleName);
			if (bundle!=null){
				amonemDepl.startBundle(peer.getFramework(), bundle.getBundleID());
			}
		}
	}
	
    /**
     *  @param Name of the Peer
     *  @param Name of the Bundle to be stopped
     */
	public void stopBundle(String PeerName, String BundleName){
		//discoveryDAG
		//amonemDepl.stopBundle(Framework fw, long bid)
		DAGPeer peer= (DAGPeer) discoveryROOT.getElement(PeerName);
		if (peer!=null){
			DAGBundle bundle= peer.getBundle(BundleName);
			if (bundle!=null){
				amonemDepl.stopBundle(peer.getFramework(), bundle.getBundleID());
			}
		}
	}

    /**
     *  @param Name of the Peer
     *  @param Name of the Bundle to be installed
     */
	public void installBundle(String PeerName, String PathName){
		//discoveryDAG
		//amonemDepl.installBundle(Framework fw, String pathToBundle)
		DAGPeer peer= (DAGPeer) discoveryROOT.getElement(PeerName);
		if (peer!=null){
			amonemDepl.installBundle(peer.getFramework(), PathName);
		}
	}
	
    /**
     *  @param Name of the Peer
     *  @param Name of the Bundle to be removed
     */
	public void removeBundle(String PeerName, String BundleName){
		//discoveryDAG
		//amonemDepl.removeBundle(Framework fw, long bid)
		DAGPeer peer= (DAGPeer) discoveryROOT.getElement(PeerName);
		if (peer!=null){
			DAGBundle bundle= peer.getBundle(BundleName);
			if (bundle!=null){
				amonemDepl.removeBundle(peer.getFramework(), bundle.getBundleID());
			}
		}
	}
	
	public void getServiceAdvertisements(String peerName, String filter)
	{
	    AmonemManagerActivator.serviceManager.getServiceAdvertisements(
	            peerName, filter, amonemDisc.getServiceAdvertisementListener());
	}
	
    /**
     *  @param Bundlelistener
     */
	public void addBundleListener(BundleListener bundlelistener){
		bundleListener.add(bundlelistener);
	}
	
    /**
     *  @param Grouplistener
     */
    public void addGroupListener(GroupListener grouplistener)
    {
        groupListeners.add(grouplistener);
    }
    
    /**
     * saves the current configuration, stored in the deploy DAG.
     *  
     * @param Location, where the configuration has to be saved
     * @param Name of the configuration
     * @return value of success
     */ 
	public boolean exportDAG(String locationName, String ConfigName){
		boolean success=true;
		ConfigurationSaver Saver = new ConfigurationSaver();
		//System.out.println(" save: "+ locationName + ConfigName);
  		success= Saver.save(deployROOT, locationName, ConfigName);
  		return success;
	}
	
	/**
	 * saves one peer (of the deployDAG)
	 * 
	 * @param full name of the location-file
	 * @param name of the peer to be saved
	 */
    public void exportPeer(String locationName, String PeerName){
    	DAGPeer expPeer= (DAGPeer) deployROOT.getElement(PeerName);
    	if (expPeer!=null){
    		ConfigurationSaver Saver= new ConfigurationSaver();
    		Saver.saveOnlyPeer(locationName, expPeer);
    	}else{
    		System.out.println("Peer "+ PeerName + " ist nicht im deploy DAG");
    	}
    }
    
    /**
     * loads a stored Configuration. if load=false then it's only load in 
     * the deployDAG. load=true means that all Peers are also started.
     * 
     * @param full pathname of the xml-file
     * @param load
     */
	public void importDAG(String locationName, boolean Start){
		if ((DOWNLOAD_dir==null) | (xargs_TEMPLATE==null)){
			Start=false;
		}
		if ((temp_FOLDER==null) | (DEPLOY_path==null)){
			Start=false;
		}
		//Start= true;
		DAGParser P= new DAGParser();
		Vector Peers= P.parse(locationName);
		DAGPeer curPeer= null;
		Enumeration PeerIterator= Peers.elements();
		if (Peers.size()==1){
			while (PeerIterator.hasMoreElements()){
				curPeer= (DAGPeer) PeerIterator.nextElement();
				DAGPeer sameName= (DAGPeer) deployROOT.getElement(curPeer.getName());
				if (sameName!=null){
					amonemDepl.killPeer(sameName);
					deployROOT.removeChild(sameName);
				}
				if (Start){
					AmonemDeploySkeleton skel= getDeploySkeleton(curPeer, curPeer.getName(), DOWNLOAD_dir, DEPLOY_path,xargs_TEMPLATE);
					amonemDepl.deployLocal(skel);
				} else {
					curPeer.setDeployed(false);
					deployROOT.addChild(curPeer);
					childAddedInDeploy(curPeer);
				}
			}
		} else {
			deleteDeployDag();
			while (PeerIterator.hasMoreElements()){
				curPeer= (DAGPeer) PeerIterator.nextElement();
				if (Start){
					AmonemDeploySkeleton skel= getDeploySkeleton(curPeer, curPeer.getName(), DOWNLOAD_dir, DEPLOY_path,xargs_TEMPLATE);
					amonemDepl.deployLocal(skel);
				} else {
					curPeer.setDeployed(false);
					deployROOT.addChild(curPeer);	
					childAddedInDeploy(curPeer);
				}
			}
		}
	}
    
    /**
     *  @param Skeleton
     */
	public void newPeer(AmonemDeploySkeleton Skeleton) {
		amonemDepl.deployLocal(Skeleton);
	}
	
    /**
     *  @param Name of the Peer
     *  @param path of the download directory
     *  @param path of the deploy directory
     *  @param full pathname of the file containing the dummy xargs template
     *  @return Skeleton of the new Peer
     */
	public AmonemDeploySkeleton getSkeleton(String name, String download_dir, String deploy_path, String xargs_template){
		return amonemDepl.getSkeleton(name, download_dir, deploy_path, xargs_template);
	}
	
	
	/**
	 * returns a Skeleton for the given Peer
	 * 
	 * @param DAGPeer
	 * @param PeerName
	 * @param Download Directory
	 * @param Deploy Path
	 * @param xargs Template
	 * @return Skeleton
	 * 
	 */
	 public AmonemDeploySkeleton getDeploySkeleton(DAGPeer Peer, String name, String download_dir, String deploy_path, String xargs_template){
	 	AmonemDeploySkeleton skel= amonemDepl.getSkeleton(name, download_dir, deploy_path, xargs_template);
		skel.setJavaPath(System.getProperty("java.home") + "/bin/java");
		
		String PlatformURL= Peer.getPlatform();
		Enumeration bundles= Peer.getBundles().elements();
		while (bundles.hasMoreElements()){
			DAGBundle curBundle= (DAGBundle) bundles.nextElement();
			if (curBundle.getUUID().equals(PlatformURL)){
				//System.out.println(curBundle.getUpdateLocation());
				skel.setPlatform(Peer.getPlatform(), curBundle.getUpdateLocation());
			} else {
				skel.addJar(curBundle.getUUID(), curBundle.getUpdateLocation());
			}
		}
	 	return skel;
	 }
	 
    /**
     *  @return Root of the discoovery DAG
     */
	public DAGGroup getDiscoveryROOT() {
		if (discoveryROOT == null) { 
			discoveryROOT = new DAGGroup("ROOT");
		}
		return discoveryROOT;
	}
    
    /**
     *  sets default Variables
     *  @param download directory
     *  @param path of the deploy directory
     *  @param path of the xargs-template
     *  @param Path of the Temp-folder
     */
	public void setFolders(String download_dir, String deploy_path, String xargs_template, String temp_folder) {
		temp_FOLDER= temp_folder;
		DOWNLOAD_dir= download_dir;
		DEPLOY_path= deploy_path;
		xargs_TEMPLATE= xargs_template;
	}

    /**
     *  @param Peerlistener
     */
    public void addPeerListener(PeerListener peerlistener)
    {
        peerListener.add(peerlistener);
    }
    
    /**
     *  @param added child
     */
    public void childAddedInDeploy(DAGMember dagmember){
    	Enumeration Update= groupListeners.elements();
    	while (Update.hasMoreElements()){
    	    try {
        		GroupListener cur= (GroupListener) Update.nextElement();
    			cur.childAddedInDeploy(dagmember);
    	    }
    	    catch (Exception e) {
    	        System.out.println("Exception ocurred: " + e.getMessage());
    	        e.printStackTrace();
    	    }
    	}
    }
    
    /**
     * @param added child
     */
    public void childAddedInDiscovery(DAGMember dagmember){
    	Enumeration Update= groupListeners.elements();
    	while (Update.hasMoreElements()){
    	    try {
        		GroupListener cur= (GroupListener) Update.nextElement();
    			cur.childAddedInDiscovery(dagmember);
    	    }
    	    catch (Exception e) {
    	        System.out.println("Exception ocurred: " + e.getMessage());
    	        e.printStackTrace();
    	    }
    	}
    }
    
    /**
     * @param removed dagmember
     */
    public void childRemoved(DAGMember dagmember){
    	Enumeration Update= groupListeners.elements();
    	while (Update.hasMoreElements()){
    		GroupListener cur= (GroupListener) Update.nextElement();
			cur.childRemoved(dagmember);
    	}
    }
    
    /**
     * not jet used by the plugin
     * @param added dagpipe
     */
    public void pipeAdded(DAGPipe dagpipe){
    	Enumeration Update= peerListener.elements();
    	while (Update.hasMoreElements()){
    		PeerListener cur= (PeerListener) Update.nextElement();
			cur.pipeAdded(dagpipe);
    	}
    }
    
    /**
     * not jet used by the plugin
     * @param removed dagpipe
     */
    public void pipeRemoved(DAGPipe dagpipe){
    	Enumeration Update= peerListener.elements();
    	while (Update.hasMoreElements()){
    		PeerListener cur= (PeerListener) Update.nextElement();
			cur.pipeRemoved(dagpipe);
    	}
    }
   
    public void serviceReferenceAdded(ServiceReference sref)
    {
    	Enumeration Update= peerListener.elements();
    	while (Update.hasMoreElements()){
    		PeerListener cur= (PeerListener) Update.nextElement();
			cur.serviceReferenceAdded(sref);
    	}
    }
    
    /**
     *  @param name of the peer owing the changed bundle
     */
    public void bundleChanged(String PeerName){
    	DAGPeer peer= (DAGPeer) discoveryROOT.getElement(PeerName);
    	if (peer!=null){
    		Enumeration Update= bundleListener.elements();
    		while (Update.hasMoreElements()){
    			BundleListener cur= (BundleListener) Update.nextElement();
    			cur.bundleChanged(peer);
    		}
    	}
    }
    
    /**
     *  @param name of the peer which has to be removed
     */
    public void kill(String PeerName){
    	DAGPeer peer= (DAGPeer) deployROOT.getElement(PeerName);
    	if (peer!=null){
    		amonemDepl.killPeer(peer);
    	}
    }
    
    /**
     * 
     * @param name of the peer which has to be deleted
     */
	public void deletePeer(String PeerName) {
		DAGPeer delPeer= (DAGPeer) deployROOT.getElement(PeerName);
		if (delPeer.isDeployed()){
			amonemDepl.killPeer(delPeer);
		}
		delPeer.removeMyself();
    	Enumeration Update= groupListeners.elements();
    	while (Update.hasMoreElements()){
    	    try {
        		GroupListener cur= (GroupListener) Update.nextElement();
    			cur.childDeleted(delPeer);
    	    }
    	    catch (Exception e) {
    	        System.out.println("Exception ocurred: " + e.getMessage());
    	        e.printStackTrace();
    	    }
    	}
	}
    
    /**
     * @param URL's of the repositories (separated by ";")
     */
    public Vector getRepository(String URLString){
    	String Filename="";
    	RepoParser MyParser= new RepoParser();
    	Vector AllJars= new Vector();
    	StringTokenizer tokenizer= new StringTokenizer(URLString, ";");
    	while (tokenizer.hasMoreElements()){
        	String nextToken= tokenizer.nextToken();
        	Filename= getFilenameOfParseFile(nextToken);
        	//System.out.println("try to parse " + Filename);
            AllJars.addAll(MyParser.parse(Filename));
    	}
    	return AllJars;
    }
    
    //* not part of the interface*//
    

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.amonem.AmonemManager#prepareDirPath(java.lang.String)
     */
    public String prepareDirPath(String path)
    {
        String dirPath = prepareFilePath(path);
        if(!dirPath.endsWith(File.separator)) {
            dirPath += File.separator;
        }
        return dirPath;
    }
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.amonem.AmonemManager#prepareFilePath(java.lang.String)
     */
    public String prepareFilePath(String path)
    {
        String filePath = path.replace('\\', File.separatorChar);
        filePath = path.replace('/', File.separatorChar);
        return filePath;
    }

    private String getFileName(String url){
    	String Filename="";
    	StringTokenizer tokenizer= new StringTokenizer(url, "/");
    	while (tokenizer.hasMoreElements()){
    		Filename= tokenizer.nextToken();
    	}
    	return Filename;
    }

    private String getFilenameOfParseFile(String Name){
    	String FilePath="";
    	System.out.println(Name);
    	if (Name.startsWith("http")){
    		URL nextURL;
			try {
				nextURL = new URL(Name);
				amonemDepl.downloadFile(nextURL, temp_FOLDER, true, false);
				String Filename= getFileName(Name);
				FilePath= temp_FOLDER + Filename;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
    	}
    	else if (Name.startsWith("file")){
    		URL nextURL;
			try {
				nextURL = new URL(Name);
				amonemDepl.downloadFile(nextURL, temp_FOLDER, true, false);
				String tmp_String= nextURL.getPath();
				int index = tmp_String.lastIndexOf("/");
				FilePath= temp_FOLDER + tmp_String.substring(index+1);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
    	}
    	else {
    		FilePath= Name;
    	}
    	System.out.println(FilePath);
    	return FilePath;
    }
     
    /**
     * deletes the whole deploy DAG
     *
     */
    private void deleteDeployDag(){
    	DAGPeer curPeer=null;
    	DAGIterator DagIter= new DAGIterator(deployROOT);
    	while (DagIter.hasMorePeers()){
    		curPeer= DagIter.getNextPeer();
    		amonemDepl.killPeer(curPeer);
    		deployROOT.removeChild(curPeer);
    	}
    }
    
    private void doAndreasTest() {
        AmonemDeploySkeleton ads = amonemDepl.getSkeleton("AmonemPeer1", TMP_DOWNLOAD_DIR, LOCAL_DEPLOY_PATH, XARGS_TEMPLATE);
        ads.setJavaPath(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        
        String repo = "file:///local/bam/.maven/repository/";

        boolean success = false;

        ads.setPlatform("osgi:framework:1.3.0:", repo + "osgi/jars/framework-1.3.0.jar");
        ads.addJar("swt:swt:3.0RC1-linux-osgi:", repo + "swt/jars/swt-3.0RC1-linux-osgi.jar");
        ads.addJar("xpp:xpp3:1.1.3.3_min-osgi:", repo + "xpp3/jars/xpp3-1.1.3.3_min-osgi.jar");
        ads.addJar("xstream:xstream:1.0.1-osgi:", repo + "xstream/jars/xstream-1.0.1-osgi.jar");
        ads.addJar("log4j:log4j:1.2.8-osgi:", repo + "log4j/jars/log4j-1.2.8-osgi.jar");
        ads.addJar("jadabs:concurrent:0.7.1:", repo + "jadabs/jars/concurrent-0.7.1.jar");
        ads.addJar("jadabs:jxme-osgi:0.7.1:", repo + "jadabs/jars/jxme-osgi-0.7.1.jar");
        ads.addJar("jadabs:jxme-udp:0.7.1:", repo + "jadabs/jars/jxme-udp-0.7.1.jar");
        ads.addJar("jadabs:jxme-services-api:0.7.1:", repo + "jadabs/jars/jxme-services-api-0.7.1.jar");
        ads.addJar("jadabs:jxme-services-impl:0.7.1:", repo + "jadabs/jars/jxme-services-impl-0.7.1.jar");
        ads.addJar("jadabs:remotefw-api:0.7.1:", repo + "jadabs/jars/remotefw-api-0.7.1.jar");
        ads.addJar("jadabs:remotefw-impl:0.7.1:", repo + "jadabs/jars/remotefw-impl-0.7.1.jar");
//        ads.addJar("jadabs:jadabs-maingui:0.1.7:", repo + "jadabs/jars/jadabs-maingui-0.7.1.jar" + ads.START);
        ads.addJar("jadabs:jadabs-maingui:0.7.1:", "http://n.ethz.ch/student/scherand/download/jadabs-maingui-0.7.1.jar");
        
        success = amonemDepl.deployLocal(ads);
        
        
//        DAGIterator iterator = new DAGIterator(ROOT);
//        DAGPeer peer;
//        Framework fw;
//        
//        while (iterator.hasMorePeers()) {
//            peer = iterator.getNextPeer();
//            if (peer.getName().equals("AmonemPeer1")) {
//                fw = peer.getFramework();
//                if (fw == null) {
//                    LOG.debug("Framework is null, should be fw of AmonemPeer1.");
//                }
//                else {
//                    fw.installBundle("jadabs/jars/concurrent-0.7.1.jar");
//                }
//            }
//        }
    }

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.amonem.AmonemManager#deletePeer(java.lang.String)
	 */


	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.amonem.AmonemManager#setTempFolder()
	 */


	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.amonem.AmonemManager#getDepolyROOT()
	 */

}
