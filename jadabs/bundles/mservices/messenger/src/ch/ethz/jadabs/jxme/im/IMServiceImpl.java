/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.im;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * @author andfrei
 * 
 */
public class IMServiceImpl implements IMService, DiscoveryListener, Listener
{
    private Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.im.IMServiceImpl");
    
    //---------------------------------------------------
    // Constants
    //---------------------------------------------------
    
    private static final String SIP_ADDRESS_TO = "sipaddressto";
    
    private static final String SIP_ADDRESS_FROM = "sipaddressfrom";
    
    private static final String IM_MESSAGE_TAG = "immessage";
    
    private static final String IM_PIPE_NAME = "impipe-open";
    
    private static final String IM_TYPE = "imtype";
    
    private static final String IM_TYPE_MSG = "msg";
    
    private static final String IM_TYPE_REG = "reg";
    
    private static final String IM_TYPE_UNREG = "unreg";
    
    private static final String IM_TYPE_ALIVE = "alive";
        
    private static final String IM_STATUS = "status";
    
    
    //---------------------------------------------------
    // Default Fields
    //---------------------------------------------------
    
    private static int SearchPipeTimeout = 3000;
    private static int ndiscosleep = 5000;
    
    //---------------------------------------------------
    // Fields
    //---------------------------------------------------
    private GroupService groupsvc; 
    
    private String sipaddress;
    
    private int imstatus = IM_STATUS_OFFLINE;
    
    private boolean registered = false;
    
    private Pipe impipe;
    
    private IMListener imlistener;
        
    private Hashtable neighbours = new Hashtable();
    
    private boolean running = false;
    
    private NeighbourThread nthread;
    
    public IMServiceImpl(GroupService groupsvc, String sipaddress) throws IOException
    {
        this.groupsvc = groupsvc;
        this.sipaddress = sipaddress;
        
        init();
        
    }
        
    private void init() throws IOException
    {       
        // get or create an IMPipe
        setupIMPipe();
        
        groupsvc.listen(impipe, this);
        
    }
    
    
    
    private void setupIMPipe()
    {
        // try first to find impipe
        
        try
        {
            groupsvc.remoteSearch(NamedResource.PEER, "Name", 
                    "", 1, this);
            groupsvc.remoteSearch(NamedResource.PIPE, "Name", 
                    IM_PIPE_NAME, 1, this);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        // wait for finding pipe
        try
        {
            Thread.sleep(SearchPipeTimeout);
        } catch (InterruptedException e1)
        {
            e1.printStackTrace();
        }
        
        // if no IMPipe found create one
        if (impipe == null)
        {
            // propagation pipe
            impipe = (Pipe)groupsvc.create(NamedResource.PIPE, 
                    IM_PIPE_NAME, null, Pipe.PROPAGATE);
            
            groupsvc.remotePublish(impipe);
        }
        
    }
    
    public int getStatus()
    {
        return imstatus;
    }
    
    public String getSipAddress()
    {
        return sipaddress;
    }
    
    //---------------------------------------------------
    // Implement IMSerivce methods
    //---------------------------------------------------
    
    /**
     * Subscribe with a listener to get notified about messages.
     * Use status to choose your subscription type.
     * 
     * @param imlistener listener to gent notified about other IMs
     * @param status use one of the possible IM_STATUS.
     */
    public void register(IMListener imlistener, int status)
    {
        // start NeighbourThread
        if (!running)
        {
            running = true;
            
            if (nthread == null)
                nthread = new NeighbourThread();
            
        	nthread.start();
        }
        
        this.imstatus = status;
        this.imlistener = imlistener;
        this.registered = true;
        
        Element[] elms = new Element[3];
        
        elms[0] = new Element(IM_TYPE, IM_TYPE_REG, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(SIP_ADDRESS_FROM, sipaddress, Message.JXTA_NAME_SPACE);
        elms[2] = new Element(IM_STATUS, Integer.toString(status), Message.JXTA_NAME_SPACE);
        
        try
        {            
            groupsvc.send(impipe,new Message(elms));
        } catch (IOException e)
        {            
//            LOG.error("could not subscribe, no other IM running");
//            throw new IMException("could not subscribe, no other IM running");           
        }
        
    }

    /*
     */
    public void unregister()
    {
        
        // stop the NeighbourThread
        if (running)
            running = false;
        
        Element[] elms = new Element[2];
        
        elms[0] = new Element(IM_TYPE, IM_TYPE_UNREG, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(SIP_ADDRESS_FROM, sipaddress, Message.JXTA_NAME_SPACE);
    
        try
        {
            groupsvc.send(impipe,new Message(elms));
        } catch (IOException e)
        {
            LOG.debug("could not unsubscribe");
//            throw new IMException("could not unsubscribe", e);
        }
        
        this.registered = false;
    }

    /*
     */
    public void sendMessage(String tosipaddress, String message) throws IMException
    {
        if (impipe == null)
            throw new IMException("could not send message");
        
        Element[] elms = new Element[4];
            
        elms[0] = new Element(IM_TYPE, IM_TYPE_MSG, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(SIP_ADDRESS_TO, tosipaddress, Message.JXTA_NAME_SPACE);
        elms[2] = new Element(SIP_ADDRESS_FROM, sipaddress, Message.JXTA_NAME_SPACE);
        elms[3] = new Element(IM_MESSAGE_TAG, message, Message.JXTA_NAME_SPACE);
        
        
        try
        {
            groupsvc.send(impipe,new Message(elms));
        } catch (IOException e)
        {
            throw new IMException("could not send message", e);
        }
    }
    
    public NeighbourTuple[] getNeighbours()
    {
        
        NeighbourTuple[] ntuples = new NeighbourTuple[neighbours.size()];
        
        int i = 0;
        for (Enumeration en = neighbours.elements(); en.hasMoreElements();)
        {
            ntuples[i++] = (NeighbourTuple)en.nextElement();
        }
        
        return ntuples;
               
    }
    
    //---------------------------------------------------
    // Implement Listener
    //---------------------------------------------------
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message, java.lang.String)
     */
    public void handleMessage(Message message, String listenerId)
    {
        String fromaddress = new String(message.getElement(SIP_ADDRESS_FROM).getData());
        
        String type = new String(message.getElement(IM_TYPE).getData());
        
        // IM_TYPE_MSG
        if (type.equals(IM_TYPE_MSG))
//                (addressto.equals(sipaddress) || address.equals("any")))
        {
            
            String toaddress = new String(message.getElement(SIP_ADDRESS_TO).getData());
            
            if (toaddress.equals(sipaddress) || 
                    toaddress.equals(SIP_ADDRESS_ANY))
            {
                String msg = new String(message.getElement(IM_MESSAGE_TAG).getData());
            
                imlistener.process(fromaddress, msg);
            }
        }
        // IM_TYPE_REG
        else if (type.equals(IM_TYPE_REG))
        {
            int status = Integer.parseInt(new String(message.getElement(IM_STATUS).getData()));
            
            NeighbourTuple ntuple = new NeighbourTuple(fromaddress, status);
            neighbours.put(fromaddress,ntuple);
            
            imlistener.imRegistered(fromaddress, status);
        }
        // IM_TYPE_UNREG
        else if (type.equals(IM_TYPE_UNREG))
        {
            imlistener.imUnregistered(fromaddress);
            
            neighbours.remove(fromaddress);
        }
        // IM_TYPE_NACK
        else if (type.equals(IM_TYPE_ALIVE))
        {
            int status = Integer.parseInt(new String(message.getElement(IM_STATUS).getData()));
        
            if (!neighbours.containsKey(fromaddress))
            {
                NeighbourTuple ntuple = new NeighbourTuple(fromaddress,status);
                neighbours.put(fromaddress, ntuple);
            }
        }
        // not known...
        else
            LOG.debug("message type not known: "+type);
    }

    /**
     * Implements DiscoveryListener to discover IMPipe and Listener
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        if (namedResource instanceof Pipe)
        {
            impipe = (Pipe)namedResource;
            LOG.debug("found pipe: " + impipe.toString());
            
            try
            {
                groupsvc.resolve(impipe, 10000);
                
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    class NeighbourThread extends Thread
    {
        
        public NeighbourThread()
        {
            super("NeighbourThread");
        }
        
        public void run()
        {
            
            while(running)
            {
                try
                {
                    Thread.sleep(ndiscosleep);
                } catch (InterruptedException e1)
                {
                    LOG.error("NeighbourThread stopped");
                }
                
	            if (impipe == null)
	                continue;
	            
	            if (registered)
	            {
	                Element[] elms = new Element[3];
	                
	                elms[0] = new Element(IM_TYPE, IM_TYPE_ALIVE, Message.JXTA_NAME_SPACE);
	                elms[1] = new Element(SIP_ADDRESS_FROM, sipaddress, Message.JXTA_NAME_SPACE);
	                elms[2] = new Element(IM_STATUS, Integer.toString(imstatus), Message.JXTA_NAME_SPACE);
		            
		            try
		            {
		                groupsvc.send(impipe,new Message(elms));
		            } catch (IOException e)
		            {
//	                	LOG.debug("could not send message");
		            }
	            }
	            
	            
            }
        }
        
    }// end NeighbourThread

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleNamedResourceLoss(NamedResource namedResource)
    {
        LOG.info("namedresouce lost: " + namedResource.getName());
        
    }
    
    
}
