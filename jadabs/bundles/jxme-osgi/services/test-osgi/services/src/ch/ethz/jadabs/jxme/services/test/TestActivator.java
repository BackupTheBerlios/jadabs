/*
 * Created on Jul 22, 2004
 *
 */
package ch.ethz.jadabs.jxme.services.test;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.ID;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * @author andfrei
 *
 */
public class TestActivator 
	implements BundleActivator
{

    Logger LOG = Logger.getLogger(TestActivator.class);
    
    static BundleContext bc;
    Search search;
    
    String peername;
    
    GroupService groupsvc;
    GroupService testgroupsvc;
    
    Peer1Listeners peer1listeners;
    Peer2Listeners peer2listeners;
    Peer3Listeners peer3listeners;
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        TestActivator.bc = bc;
        
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        groupsvc = (GroupService)bc.getService(sref);
        
        sref = bc.getServiceReference(PeerNetwork.class.getName());
        PeerNetwork peernetwork = (PeerNetwork)bc.getService(sref);
        
        peername = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
        
        
        
//        search = new Search(); 
        
//        groupsvc.addDiscoveryListener(search);
        // search for peers
//        if (peername.equals("peer2"))
//            search.testPositive("search for each other's peer",
//                    groupsvc, NamedResource.PEER, "");
//            
//        if (peername.equals("peer1"))
//            search.testPositive("search for each other's peer",
//                groupsvc, NamedResource.PEER, "peer2");
        
        // search for groups
//        search.testPositive("search for groups",
//                groupsvc, NamedResource.GROUP, "");
        
        // testpipe
        if (peername.equals("peer1"))
        {
            peer1listeners = new Peer1Listeners();
            testPipePeer1();
        } 
        else if (peername.equals("peer2"))
        {
            peer2listeners = new Peer2Listeners();
            groupsvc.remoteSearch(NamedResource.PEER, "Name", "", 1, peer2listeners);
            testPipePeer2();
        } 
        else if (peername.equals("peer3"))
        {
            
            //groupsvc.remoteSearch(NamedResource.PEER, "Name", "", 1, this);
            
            
            PeerGroup testgroup = new PeerGroup("TestGroup",new ID("urn:jxta:uuid-1:2:03"),"for test purpose");
        
            testgroupsvc = groupsvc.join(testgroup, null);
            
            peer3listeners = new Peer3Listeners();
            testgroupsvc.remoteSearch(NamedResource.PEER, "Name", "", 1, peer3listeners);
            
            testPipePeer3();
        
        }
            
    }


    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
    }
    
    
    public void testPipePeer1()
    {
        // propagation pipe
        Pipe proppipe = (Pipe)groupsvc.create(NamedResource.PIPE, 
                "testpipe", null,Pipe.PROPAGATE);
        
        groupsvc.remotePublish(proppipe);
        
        // unicast pipe
//        Pipe unicpipe = (Pipe)groupsvc.create(NamedResource.PIPE, 
//                "testpipe", null,Pipe.UNICAST);
//        groupsvc.remotePublish(unicpipe);
        
        try
        {
            groupsvc.listen(proppipe, peer1listeners);
//            groupsvc.listen(unicpipe, new UnicastListener());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }
    
    public void testPipePeer2()
    {
        try
        {
            groupsvc.remoteSearch(NamedResource.PIPE, "Name", "", 1, peer2listeners);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void testPipePeer3()
    {
        try
        {
            testgroupsvc.remoteSearch(NamedResource.PIPE, "Name", "", 1, peer3listeners);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    


    class Peer1Listeners implements Listener
    {
       
	    public void handleMessage(Message message, String listenerId)
	    {
	        LOG.debug("PropagationListener: "+ message.toXMLString());
	    }

        /* (non-Javadoc)
         * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleSearchResponse(NamedResource namedResource)
        {
            LOG.debug("called Peer1Listeners handleSearchResponse");
            
        }
	    
    }

    
    class Peer2Listeners implements DiscoveryListener, Listener
    {
        
        public void handleSearchResponse(NamedResource namedResource)
        {
            LOG.debug("found namedresource: " + namedResource.getName());
            
            LOG.debug("group: "+namedResource.getID().getGroupID());
            
            if (namedResource instanceof Pipe)
            {
                Pipe pipe = (Pipe)namedResource;
                
                try
                {
                    groupsvc.resolve(pipe,100000);
                    
	                Element[] elms = new Element[]{new Element("testa","testval",Message.JXTA_NAME_SPACE)};
	                
	                groupsvc.send(pipe,new Message(elms));
                    
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

	    public void handleMessage(Message message, String listenerId)
	    {
	        LOG.debug("PropagationListener: "+ message.toXMLString());
	    }

        /* (non-Javadoc)
         * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleNamedResourceLoss(NamedResource namedResource)
        {
            LOG.info("namedresouce lost: " + namedResource.getName());
        }
        
    }
    
    class Peer3Listeners implements DiscoveryListener, Listener
    {
        
        public void handleSearchResponse(NamedResource namedResource)
        {
            LOG.debug("found namedresource: " + namedResource.getName());
            
            LOG.debug("group: "+namedResource.getID().getGroupID());
            
            if (namedResource instanceof Pipe)
            {
                Pipe pipe = (Pipe)namedResource;
                
                try
                {
                    groupsvc.resolve(pipe,100000);
                    
                    groupsvc.listen(pipe, this);
                    
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

	    public void handleMessage(Message message, String listenerId)
	    {
	        LOG.debug("PropagationListener: "+ message.toXMLString());
	    }

        /* (non-Javadoc)
         * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleNamedResourceLoss(NamedResource namedResource)
        {
            LOG.info("namedresouce lost: " + namedResource.getName());
            
        }
    
    }
    
    class UnicastListener implements Listener
    {

        /* (non-Javadoc)
         * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message, java.lang.String)
         */
        public void handleMessage(Message message, String listenerId)
        {
            LOG.info("UnicastListener: "+message.toXMLString());
            
        }

        /* (non-Javadoc)
         * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleSearchResponse(NamedResource namedResource)
        {
            // TODO Auto-generated method stub
            
        }
        
    }
}
