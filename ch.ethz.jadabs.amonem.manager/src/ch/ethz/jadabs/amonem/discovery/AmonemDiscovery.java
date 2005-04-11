/*
 * Created on Nov 18, 2004
 *
 * This class discovers peers and groups and adds them to the discoveryDAG.
 * Since there is only one group, this functionality is not completely implemented.
 * 
 */
package ch.ethz.jadabs.amonem.discovery;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.amonem.manager.AmonemManagerImpl;
import ch.ethz.jadabs.amonem.manager.DAGBundle;
import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGIterator;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.remotefw.BundleInfoListener;
import ch.ethz.jadabs.remotefw.Framework;
import ch.ethz.jadabs.remotefw.FrameworkManager;
import ch.ethz.jadabs.remotefw.RemoteFrameworkListener;
import ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener;


/**
 * 
 * This class discovers peers and groups and adds them to the discoveryDAG.
 * Since there is only one group, this functionality is not completely implemented.
 *  
 * @author bam
 */
public class AmonemDiscovery
{

    private Logger LOG = Logger.getLogger(AmonemDiscovery.class.getName());

    static AmonemDiscovery amonemDiscovery;
    
    // DAG root element
    private DAGGroup rootGroup;
    
    // Name of this peer
    private String myName;
    
    protected AmonemManagerImpl amonemManager;
    private FrameworkManager FWManager;
    
    // Vector that contains all GroupServices we know (one for each group)
    private Vector gServices = new Vector();
    
    // Vector that contains all groups we know
    private Vector foundGroups = new Vector();
    
    // The group we belong to
//    private PeerGroup myGroup;
    
    // Contains the PW needed to join a group ("Master-PW" since we need to join every group)
    private final String GROUP_JOIN_PASSWORD = "";	// no security implemented yet...
    
    AmonemServiceAdvertisementListener advListener;
    
    /**
     * 
     * @param root The DAGGroup that builds the root of the discovery-DAG
     * @param amonemManager The AmonemManager instantiating this class
     * @param FWManager The ch.ehtz.jadabs.remotefw.FrameworkManager
     */
    public AmonemDiscovery(DAGGroup root, AmonemManagerImpl amonemManager, FrameworkManager FWManager) {
        this.rootGroup = root;
        this.amonemManager = amonemManager;
        this.FWManager = FWManager;
        
        advListener = new AmonemServiceAdvertisementListener();
    }
    
    public ServiceAdvertisementListener getServiceAdvertisementListener()
    {
        return advListener;
    }
    
    /**
     * 
     * @param pGroup The ch.ethz.jadabs.jxme.PeerGroup object
     * @param gServ The ch.ethz.jadabs.jxme.services.GroupService object
     */
    public void start(PeerGroup pGroup, GroupService gServ) {

        setMyPeername(FWManager);
        
//        this.myGroup = pGroup;
        foundGroups.add(pGroup);
        
//        discoverRemoteGroups(gServ);
        discoverLocalGroups(gServ);
        discoverRemotePeers(FWManager);
        
//        printDAG();
    }
    
    
    /**
     * 
     * This method can be used to discover all groups within range (e.g. all groups within the worldgroup). It works recursively. It is not tested, because groups are not implemented yet! Use at your own risk.
     * 
     * @param gServ The ch.ethz.jadabs.jxme.services.GroupService that will be uses to discover groups in. Groups within this groupservices group will be joined and searched recursively.
     * 
     * 
     * TODO groups should be added to the discoveryDAG
     * 
     */
    private void discoverLocalGroups(GroupService gServ) {
        NamedResource[] tmpGroups;
        PeerGroup tmpGroup;
        GroupService tmpGServ;
        int i;
        
        addGroupService(gServ);
        
        try {
            // GroupService.localSearch("WHAT", "ATTRIBUT?", "VALUE?", THRESHOLD) searches
            // the *local* DB for known groups/peer/pipes
            tmpGroups = gServ.localSearch(NamedResource.GROUP, "Name", "", 1);
            // What if gServ can not find a group? tmpGroups = null?
            // If so, we should catch a NullPointerException (is this the name?)?
            for (i = 0; i < tmpGroups.length; i++) {
                tmpGroup = (PeerGroup) tmpGroups[i];
                if (!knownGroup(tmpGroup)) {
                    // if we found a group we don't know of yet, add it to knownGroups...
                    foundGroups.add(tmpGroup);
                    // ...and join it to find contained groups recursively
                    tmpGServ = gServ.join(tmpGroup, GROUP_JOIN_PASSWORD);
                    // what exactly is this tmpGServ?
	                if (!tmpGServ.equals(gServ)) {
						discoverLocalGroups(tmpGServ);
	                }
                }
            }
            for (i = 0; i < foundGroups.size(); i++){
                LOG.debug("Gruppe " + ((PeerGroup) foundGroups.elementAt(i)).hashCode() + " gefunden");
            }
        }
        catch (IOException e) {
            LOG.debug("IOError bei local Search in AmonemDiscovery: " + e.getMessage());
        }
    }
    
    
    /**
     * This method should discover remote groups (= groups that exist on nodes which are not the Amonem App).
     * 
     * Since such groups do not exist yet, we have not implemented this functionality.
     * There is not even a DiscoveryListener implemented.
     * 
     * @param gServ The ch.ethz.jadabs.jxme.services.GroupService that will be uses to discover groups in.
     */
    private void discoverRemoteGroups(GroupService gServ) {
        
        addGroupService(gServ);
        
        try {
            // GroupService.remoteSearch("WAS", "ATTRIBUT?", "VALUE?", THRESHOLD, LISTENER) searches
            // the *remotely* for groups/peer/pipes and notifies the listener if something
            // changes. The listener has to have a handleSearchResponse(NamedResource) method.
            // Notifications will be sent to the listener until GroupService.cancelSearch(listener) is called.
            gServ.remoteSearch(NamedResource.GROUP, "NAME", "*", 0, null);
        }
        catch (IOException e) {
            LOG.debug("IOError bei remote Search in AmonemDiscovery: " + e.getMessage());
        }
    }

    
    /**
     * 
     * @param FWManager The ch.ethz.jadabs.remotefw.FrameworkManager that will be uses to discover peers.
     */
    private void discoverRemotePeers(FrameworkManager FWManager) {
        // Enumeration contains ch.ethz.jadabs.remotewf.impl.RemoteFramework
        // which can be addressed as Framework
        Enumeration frameworks = FWManager.getFrameworks();
        Framework fw;
        DAGPeer tmpDAGPeer;
        
        /*
         * add a RemoteFrameworkListener to the FrameworkManager in order to be informed
         * when a peer pops up (enterFramework event) or dies (leaveFramework event).
         */
        RemoteFrameworkListener RFWListener = new AmonemRFWListener(this);
        FWManager.addListener(RFWListener);
        
        // register the framework of each peer in the DAG to be able to e.g. stop a bundle
        while (frameworks.hasMoreElements()) {
            fw = (Framework)frameworks.nextElement();
            tmpDAGPeer = addPeerToDAG(fw);
        }
    }
    
    
    /**
     * discoverLocalPeer returns the framework of the Amonem App. This method is not used at the moment.
     * 
     * @param FWManager The ch.ethz.jadabs.remotefw.FrameworkManager that will be uses to discover peers.
     */
    private void discoverLocalPeer(FrameworkManager FWManager) {
        Framework framework = FWManager.getLocalFramework();
        
        LOG.debug("Local FW: " + framework.toString());
    }

    
    /**
     * Get all the information needed in the DAG from a Framework and add it
     * to the corresponding DAGPeer Element
     * 
     * @param fw ch.ethz.jadabs.remotefw.Framework
     * @param dp ch.ethz.jadabs.amonem.DAGPeer
     */
    private void analysePeerFW(Framework fw, DAGPeer dp) {
        int i;
        long[] bundles;
        DAGBundle DAGBund;
        
        /*
         * this is a asynchronous call, meaning that if we do not have a copy of the bundles
         * of this peer in the local cache, null is returned and a request is sent to
         * the peer to send his bundle info.
         * 
         * when the answer to this request arrives, we (should) get an allBundlesChanged
         * event. as the "should" implies, this seems not to work correctly yet. 
         */
        bundles = fw.getBundles();
        
        if(bundles != null) {
            LOG.debug("Bundles fuer Peer (" + fw.getPeername() + ") nicht null, kommen in den DAG");
            for (i = 0; i < bundles.length; i++) {
                // if we have bundles, gather the usual info and save it with the peer
                DAGBund = new DAGBundle();
                DAGBund.setBundleID(bundles[i]);
                DAGBund.setName(fw.getBundleName(bundles[i]));
                DAGBund.setState(fw.getBundleState(bundles[i]));
                dp.setBundle(DAGBund);
            }
        }
        else {
            LOG.debug("Bundles fuer Peer (" + fw.getPeername() + ") null, kommen nicht in den DAG");
        }
    }
    
/**
 *
 * @param fw ch.ethz.jadabs.remotefw.Framework of the peer to add to the discovery-DAG
 *
 * TODO Not only insert into "root group" (= worldgroup)
 */
    protected DAGPeer addPeerToDAG(Framework fw) {
        DAGIterator iterator = new DAGIterator(rootGroup);
        DAGPeer tmpDAGPeer = null;
        boolean exists = false;
        
        /*
         * check if peer (or a peer with the same name) already exists
         * we used to check only for the same fw but if peers dis- and then reappear
         * fast enough this leads to inconsistencies.
         * 
         * fast dis- and reappearing peers are not really handled correctly through only
         * this test here but it helps to keep the picture clean(er)   
         */
        while (iterator.hasMorePeers() && !exists) {
            tmpDAGPeer = (DAGPeer)iterator.getNextPeer();
            if (tmpDAGPeer.getName().equals(fw.getPeername())) {
                exists = true;
            }
        }
        
        if (!exists) {
            LOG.debug("Peer gefunden, der noch nicht im DAG war: " + fw.getPeername());
            tmpDAGPeer = new DAGPeer(fw.getPeername());
            
            tmpDAGPeer.setFramework(fw);
            
            // add a bundleinfolistener to the new peer so we are informed if bundles change
            BundleInfoListener BIListener = new AmonemBundleInfoListener(amonemManager);
            fw.addBundleInfoListener(BIListener);
            
            /*
             * save the listener to be able to remove it when the peer leaves
             * 
             * if we do not remove the listener when a peer leaves and this peer is
             * still running it can send bundleChanged events which can not be handled
             * correctly since we removed the peer from the discoveryDAG.
             */
            tmpDAGPeer.setListener(BIListener);
            // don't call analyzePeerFW because eventsystem does not seem to work
//            analysePeerFW(fw, tmpDAGPeer);
          
            rootGroup.addChild(tmpDAGPeer);
            
            // trigger event
            amonemManager.childAddedInDiscovery(tmpDAGPeer);
            
        }
        else {
            LOG.debug("Peer gefunden, der schon im DAG war: " + fw.getPeername());
            // don't call analyzePeerFW because eventsystem does not seem to work
//            analysePeerFW(fw, tmpDAGPeer);
        }
        
        printDAG();
        return tmpDAGPeer;
    }
    
    
    /**
     * 
     * @param fw ch.ethz.jadabs.remotefw.Framework of the peer to remove from the discovery-DAG
     */
    protected void removePeerFromDAG(Framework fw) {
        DAGIterator iterator = new DAGIterator(rootGroup);
        DAGPeer tmpDAGPeer = null;
        boolean found = false;
        
        tmpDAGPeer = (DAGPeer)rootGroup.getElement(fw.getPeername());
        if(tmpDAGPeer != null && fw == tmpDAGPeer.getFramework()) {
            found = true;
        }
        else if(tmpDAGPeer != null && fw != tmpDAGPeer.getFramework()) {
            // the reason why we can get here is unknown to me but i know that it is possible...
            LOG.info("Achtung: Peer (" + tmpDAGPeer.getName() + ") mit anderem FW im DAG gefunden.");
            LOG.info("   -> Loesche ihn aufgrund des identischen Namens!");
            found = true;
        }
        else {
            found = false;
        }
        
        if (found) {
            BundleInfoListener bil = tmpDAGPeer.getListener();
            
            /*
             * remove the listener, otherwise events for this peer can arrive
             * after removing it from the discoveryDAG which is bad.
             */
            fw.removeBundleInfoListener(bil);
            rootGroup.removeChild(tmpDAGPeer);
            
            // event to the manager
            amonemManager.childRemoved(tmpDAGPeer);
            LOG.debug("Peer (" + tmpDAGPeer.getName() + ") erfolgreich aus dem DAG entfernt...");
        }
        else {
            LOG.warn("Peer (" + tmpDAGPeer.getName() + ") zum entfernen nicht im DAG gefunden...");
        }
        
        printDAG();
    }
    
    
    /**
     * 
     * @param FWManager ch.ethz.jadabs.remotefw.FrameworkManager of the amonem-dicovery (e.g. the one passed to the constructor)
     */
    private void setMyPeername(FrameworkManager FWManager) {
        this.myName = (FWManager.getLocalFramework()).getPeername();
    }

    
    /**
     * 
     * @param gServ ch.ethz.jadabs.jxme.services.GroupService
     */
    private void addGroupService(GroupService gServ) {
        if (!knownGroupService(gServ)) {
            gServices.add(gServ);
        }
    }
    
    
    /**
     * 
     * @param pGroup ch.ethz.jadabs.jxme.NamedResource
     * @return True, if group was found before, False otherwise
     */
    private boolean knownGroup(NamedResource pGroup) {
        return foundGroups.contains(pGroup);
    }

    
    /**
     * 
     * @param gServ ch.ethz.jadabs.jxme.services.GroupService
     * @return True, if GroupService was found before, False otherwise
     */
    private boolean knownGroupService(GroupService gServ) {
        return gServices.contains(gServ);
    }

    
    /**
     * Outputs a ASCII-art graph of the DAG, only works while there is only one group
     * (the WorldGroup).
     *
     */
    private void printDAG() {
        DAGIterator iter = new DAGIterator(rootGroup);
        
        // this is a hack and will only work as long as there is only one group...
        /**
         * TODO make printDAG work with arbitrary number of groups/peers
         */
        LOG.debug("Here's your DAG:");
        while (iter.hasMoreGroups()) {
            LOG.debug("GROUP: " + iter.getNextGroup().getName());
            LOG.debug("    \\  ");
            LOG.debug("     |");
            while (iter.hasMorePeers()) {
                LOG.debug("     +-> " + iter.getNextPeer().getName());
            }
        }
    }
}
