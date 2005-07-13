/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.im.jxme;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.ioapi.IOProperty;
import ch.ethz.jadabs.im.ioapi.MessageCons;
import ch.ethz.jadabs.im.api.IMContact;
import ch.ethz.jadabs.im.api.IMListener;
import ch.ethz.jadabs.im.api.IMService;
import ch.ethz.jadabs.im.api.IMSettings;
import ch.ethz.jadabs.im.common.CommonIMSettings;
import ch.ethz.jadabs.im.common.UserList;
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
public class IMServiceImpl implements IMService, DiscoveryListener, Listener, IMSettings
{
    private Logger LOG = Logger.getLogger("ch.ethz.jadabs.im.jxme.IMServiceImpl");
    
    //---------------------------------------------------
    // Constants
    //---------------------------------------------------
    
    private static final String IM_PIPE_NAME = "impipe-open";
    
//    private static final String IM_TYPE_ALIVE = "alive";
        
    
    
    //---------------------------------------------------
    // Default Fields
    //---------------------------------------------------
    
    private static int SearchPipeTimeout = 5000;
    private static int ndiscosleep = 3000;
    
    //---------------------------------------------------
    // Fields
    //---------------------------------------------------
    private GroupService groupsvc; 
    
    private int imstatus = MessageCons.IM_STATUS_OFFLINE;
    
    private String imtype = MessageCons.IM_JXME;
    
    private boolean registered = false;
    
    private Pipe impipe;
    
    private IMListener imlistener;
        
    private Hashtable neighbours = new Hashtable();
    
//    private UserList userList;
    
    private boolean running = false;
    
    private boolean initialized = false;
    
    private CommonIMSettings settings;
//    private NeighbourThread nthread;

    private boolean gateway = false;

    private UserList userList;
    
    public IMServiceImpl(GroupService groupsvc, Pipe pipe, IOProperty prop, IOProperty buddies) throws IOException
    {
        this.settings = new CommonIMSettings(prop, "IM Settings");
        this.impipe = pipe;
        this.groupsvc = groupsvc;
        buddies.setPath(getBuddyListPath());
        this.userList = new UserList(buddies);
        
//        initialized = true;
//        init();
    }
        
    private void init()
    {       
        // get or create an IMPipe
//        setupIMPipe();
        
        try{
//        	Thread.sleep(1000);
        	groupsvc.listen(impipe, this);
		
        	initialized = true;
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    
    
    private void setupIMPipe()
    {
        // try first to find impipe
        
        try
        {
//            groupsvc.remoteSearch(NamedResource.PEER, "Name", 
//                    "", 1, this);
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
            System.out.println("no pipe found, create one: "+ IM_PIPE_NAME);
            // propagation pipe
            impipe = (Pipe)groupsvc.create(NamedResource.PIPE, IM_PIPE_NAME, null, Pipe.PROPAGATE);
            
            groupsvc.remotePublish(impipe);
            System.out.println("ownerID of pipe: " + impipe.getOwnerId() + ", ID of pipe: " + impipe.getID());
        }
        
    }
    
    public int getStatus()
    {
        return imstatus;
    }

    public void setStatus(int imstatus) {
    	System.out.println("set status to " + imstatus);
        this.imstatus = imstatus;
        System.out.println("before status changed msg");
        
        try {
                    Element[] elms = new Element[3];
        
        elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.NOTIFY, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(MessageCons.FROM_HEADER, getLocalURI(), Message.JXTA_NAME_SPACE);
        elms[2] = new Element(MessageCons.IM_STATUS, Integer.toString(getStatus()), Message.JXTA_NAME_SPACE);
        
        try
        {            
            groupsvc.send(impipe,new Message(elms));
        } catch (IOException e)
        {            
        	e.printStackTrace();
//            LOG.error("could not subscribe, no other IM running");
//            throw new IMException("could not subscribe, no other IM running");
        	System.out.println("Could not send status changed!");
        }
            sendPublish();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public String getIMType()
    {
        return imtype;
    }

    public void setIMType(String imtype) {
        this.imtype = imtype;
    }

    //---------------------------------------------------
    // Implement IMSerivce methods
    //---------------------------------------------------
    
    public void setListener(IMListener listener)
    {
        this.imlistener = listener;
    }
    
    /**
     * Subscribe with a listener to get notified about messages.
     * Use status to choose your subscription type.
     * 
     * @param imlistener listener to gent notified about other IMs
     * @param status use one of the possible IM_STATUS.
     */
    public void connect()
    {
        
    	System.out.println("im registering");
    	// start NeighbourThread
//        if (!running)
//        {
//            running = true;
//            
//            if (nthread == null)
//                nthread = new NeighbourThread();
//            
//        	nthread.start();
//        }
    	if (!initialized) {
    		init();
    	}
        
//    	this.imlistener = imlistener;

        try {
            sendRegister();
            imlistener.connectOK();
            setStatus(MessageCons.IM_STATUS_ONLINE);        
            this.registered = true;
        } catch (IOException e) {
            e.printStackTrace();
            imlistener.operationFailed(IMListener.CONNECTION_FAILED, "could not send register");
        }
    }
    
    private void sendRegister() throws IOException{
        System.out.println("SENDING Register !");
        
        Element[] elms = new Element[4];
        
        elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.REGISTER, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(MessageCons.FROM_HEADER, getLocalURI(), Message.JXTA_NAME_SPACE);
        elms[2] = new Element(MessageCons.IM_STATUS, Integer.toString(imstatus), Message.JXTA_NAME_SPACE);
        elms[3] = new Element(MessageCons.PASSWORD, getPassword(), Message.JXTA_NAME_SPACE);
        
        groupsvc.send(impipe, new Message(elms));
    }

    private void sendPublish() throws IOException{
        System.out.println("SENDING Publish !");
        
        Element[] elms = new Element[3];
        
        elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.PUBLISH, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(MessageCons.FROM_HEADER, getLocalURI(), Message.JXTA_NAME_SPACE);
        elms[2] = new Element(MessageCons.IM_STATUS, Integer.toString(imstatus), Message.JXTA_NAME_SPACE);
//        elms[3] = new Element(MessageCons.PASSWORD, getPassword(), Message.JXTA_NAME_SPACE);
        
        groupsvc.send(impipe, new Message(elms));
    }
    
    
    /*
     */
    public void disconnect () {
    	System.out.println("im unregistering");
        // stop the NeighbourThread
//        if (running)
//            running = false;
        
        Element[] elms = new Element[2];
        
        elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.BYE, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(MessageCons.FROM_HEADER, getLocalURI(), Message.JXTA_NAME_SPACE);
    
        try
        {
            groupsvc.send(impipe,new Message(elms));
            setStatus(MessageCons.IM_STATUS_OFFLINE);
        } catch (IOException e)
        {
            LOG.debug("could not unsubscribe");
//            throw new IMException("could not unsubscribe", e);
//            throw new IMException("Could not unregister, check settings !", e);
        }
        
//        setStatus(MessageCons.IM_STATUS_OFFLINE);
        this.gateway = false;
        this.registered = false;
        neighbours.clear();
        imlistener.disconnectOK();
    }
    
    public IMListener getListener()
    {
        return imlistener;
    }

    /*
     */
    public void sendMessage(String tosipaddress, String message)
    {
        
        if (impipe == null)
            imlistener.operationFailed(IMListener.CANNOT_DELIVER_MESSAGE, "could not send message");
        
        Element[] elms = new Element[4];
            
        elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.MESSAGE, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(MessageCons.TO_HEADER, tosipaddress, Message.JXTA_NAME_SPACE);
        elms[2] = new Element(MessageCons.FROM_HEADER, getLocalURI(), Message.JXTA_NAME_SPACE);
        elms[3] = new Element(MessageCons.MESSAGE_VALUE, message, Message.JXTA_NAME_SPACE);
        
        
        try
        {
            
            groupsvc.send(impipe,new Message(elms));
        } catch (IOException e)
        {
            imlistener.operationFailed(IMListener.CANNOT_DELIVER_MESSAGE, "could not send message");
        }
    }
    
    public IMContact[] getNeighbours()
    {
        
        IMContact[] ntuples = new IMContact[neighbours.size()];
        
        int i = 0;
        for (Enumeration en = neighbours.elements(); en.hasMoreElements();)
        {
            ntuples[i++] = (IMContact)en.nextElement();
            System.out.println("Buddy: "+ntuples[i-1].getUsername());
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
        if(this.registered)
        {

            String fromaddress = new String(message.getElement(MessageCons.FROM_HEADER).getData());
            
            String type = new String(message.getElement(MessageCons.IM_TYPE).getData());
            
            // IM_TYPE_MSG
            if (type.equals(MessageCons.MESSAGE))
//                    (addressto.equals(getUserName()) || address.equals("any")))
            {
                
                String toaddress = new String(message.getElement(MessageCons.TO_HEADER).getData());
                
                if (toaddress.equals(getLocalURI()) || 
                        toaddress.equals(MessageCons.SIP_ADDRESS_ANY))
                {
                    String msg = new String(message.getElement(MessageCons.MESSAGE_VALUE).getData());
                
                    if (imlistener != null)
                        imlistener.incomingMessage(fromaddress, msg);
                }
            }
            else if (type.equals(MessageCons.GATEWAY)) {
                // TODO gateway detection
            	int status = Integer.parseInt(new String(message.getElement(MessageCons.IM_STATUS).getData()));
                
            	if (status == MessageCons.IM_STATUS_ONLINE) {
            		imlistener.gatewayEvent(true);
            		if (!gateway) {
            		    try {
            		        sendRegister();
            		        sendPublish();
            		    } catch (IOException e) {
            		        e.printStackTrace();
                    	}
            			gateway = true;
            		}
            	}
            	else {
            		imlistener.gatewayEvent(false);
            		gateway = false;
            	}
            }
            // IM_TYPE_REG
            else if (type.equals(MessageCons.REGISTER))
            {
                int status = Integer.parseInt(new String(message.getElement(MessageCons.IM_STATUS).getData()));
                
                IMContact ntuple = new IMContact(fromaddress, status);
                neighbours.put(fromaddress,ntuple); 
                
                imlistener.neighbourListChanged();
                
//              send Register ACK message 
                System.out.println("send reg_ack");
                Element[] elms = new Element[4];
                
                elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.REG_ACK, Message.JXTA_NAME_SPACE);
                elms[1] = new Element(MessageCons.TO_HEADER, fromaddress, Message.JXTA_NAME_SPACE);
                elms[2] = new Element(MessageCons.FROM_HEADER, getLocalURI(), Message.JXTA_NAME_SPACE);
                elms[3] = new Element(MessageCons.IM_STATUS, Integer.toString(this.imstatus), Message.JXTA_NAME_SPACE);
                            
                if (impipe != null)
                {
                    try
                    {
                        groupsvc.send(impipe,new Message(elms));
                    } catch (IOException e)
                    {
                        LOG.debug("could not send reg_ack");
//                        throw new IMException("could not unsubscribe", e);
                    }    
                }
                else
                {
                    System.out.println("handle reg, send reg_ack: impipe == null");
                }
            }
            // IM_TYPE_UNREG
            else if (type.equals(MessageCons.BYE))
            {
                System.out.println("removing neighbour: "+fromaddress);
                neighbours.remove(fromaddress);            
                imlistener.neighbourListChanged();        
            }
            // IM_TYPE_NACK
//            else if (type.equals(IM_TYPE_ALIVE))
//            {
//                int status = Integer.parseInt(new String(message.getElement(IM_STATUS).getData()));
//            
//                if (!neighbours.containsKey(fromaddress))
//                {
//                    NeighbourTuple ntuple = new NeighbourTuple(fromaddress,status);
//                    neighbours.put(fromaddress, ntuple);
//                }
//            }
            else if (type.equals(MessageCons.REG_ACK))
            {
    			String toaddress = new String(message.getElement(MessageCons.TO_HEADER).getData());
    			if (toaddress.equals(getLocalURI()))
    			{
    			    System.out.println("register_ack correct toaddress");    			    
    			    // only if new buddy then add to neighborhood
	    			if (!neighbours.containsKey(fromaddress))
	    			{
		    			System.out.println("register_ack add new buddy");
                    int status = Integer.parseInt(new String(message.getElement(MessageCons.IM_STATUS).getData()));
                    
                    IMContact ntuple = new IMContact(fromaddress, status);
                    neighbours.put(fromaddress,ntuple);
                    
                    imlistener.neighbourListChanged();
		    		}
	    			else
	    			{
	    			    System.out.println("register_ack not new buddy");    
	    			}
    			}
    			else
    			{
    			    System.out.println("register_ack wrong toaddress");    
    			}
            } 
            else if (type.equals(MessageCons.NOTIFY))
            {         
            	int status = Integer.parseInt(new String(message.getElement(MessageCons.IM_STATUS).getData()));

            	neighbours.remove(fromaddress);
            	
            	IMContact ntuple = new IMContact(fromaddress, status);
                neighbours.put(fromaddress,ntuple); 
            	
                // TODO
            	imlistener.buddyStatusChanged();        
            }
            // IM_TYPE_NACK
//            else if (type.equals(IM_TYPE_ALIVE))
//            {
//                int status = Integer.parseInt(new String(message.getElement(IM_STATUS).getData()));
//            
//                if (!neighbours.containsKey(fromaddress))
//                {
//                    NeighbourTuple ntuple = new NeighbourTuple(fromaddress,status);
//                    neighbours.put(fromaddress, ntuple);
//                }
//            }
           // not known...	
            else
                LOG.debug("message type not known: "+type);    
        }
        else
        {
            System.out.println("not registred yet, can not handle message");
        }
    }

    /**
     * Implements DiscoveryListener to discover IMPipe and Listener
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        if (namedResource instanceof Pipe)
        {
            impipe = (Pipe)namedResource;
            System.out.println("found pipe: " + impipe.toString());
            System.out.println("ownerID of pipe: " + impipe.getOwnerId() + ", ID of pipe: " + impipe.getID());
            try
            {
                groupsvc.resolve(impipe, 1000);
                
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    
    
    
//    class NeighbourThread extends Thread
//    {
//        
//        public NeighbourThread()
//        {
//            super("NeighbourThread");
//        }
//        
//        public void run()
//        {
//            
//            while(running)
//            {
//                try
//                {
//                    Thread.sleep(ndiscosleep);
//                } catch (InterruptedException e1)
//                {
//                    LOG.error("NeighbourThread stopped");
//                }
//                
//	            if (impipe == null)
//	                continue;
//	            
//	            if (registered)
//	            {
//	                Element[] elms = new Element[3];
//	                
//	                elms[0] = new Element(IM_TYPE, IM_TYPE_ALIVE, Message.JXTA_NAME_SPACE);
//	                elms[1] = new Element(SIP_ADDRESS_FROM, getUserName(), Message.JXTA_NAME_SPACE);
//	                elms[2] = new Element(IM_STATUS, Integer.toString(imstatus), Message.JXTA_NAME_SPACE);
//		            
//		            try
//		            {
//		                groupsvc.send(impipe,new Message(elms));
//		            } catch (IOException e)
//		            {
////	                	LOG.debug("could not send message");
//		            }
//	            }
//	            
//	            
//            }
//        }
//        
//    }// end NeighbourThread

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleNamedResourceLoss(NamedResource namedResource)
    {
        LOG.info("namedresouce lost: " + namedResource.getName());
        
    }

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#addSipBuddy(java.lang.String)
	 */
	public void addSipBuddy(String buddy) {
	    userList.addUser(new IMContact(buddy, MessageCons.IM_STATUS_UNKNOWN));
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#removeSipBuddy(java.lang.String)
	 */
	public void removeSipBuddy(String buddy) {
		userList.removeUser(buddy);
	}

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMService#getBuddies()
     */
    public IMContact[] getBuddies() {
        // TODO Auto-generated method stub
        return userList.getUsers();
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#getUserName()
     */
    public String getUserName() {
    	return settings.getUserName();
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#getPassword()
     */
    public String getPassword() {
        return settings.getPassword();
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#getRegistrar()
     */
    public String getRegistrar() {
        return settings.getRegistrar();
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#getIpPort()
     */
    public String getIpPort() {
        return settings.getIpAddress()+":"+settings.getPort();
    }
    
    public String getLocalURI() {
        return getUserName()+"@"+getRealm();
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#getBuddyListPath()
     */
    public String getBuddyListPath() {
        return settings.getBuddyListPath();
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#setBuddyListPath(java.lang.String)
     */
    public void setBuddyListPath(String path) {
        settings.setBuddyListPath(path);
    }
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMService#getReceivers()
     */
    public String[] getReceivers() {
        IMContact[] contacts1 = getNeighbours();
        IMContact[] contacts2 = getBuddies();
        
        Hashtable ht = new Hashtable();
        for (int i=0; i < contacts1.length; i++) {
            ht.put(contacts1[i].getUsername(), contacts1[i]);
        }
        if (gateway) {
            for (int i=0; i< contacts2.length; i++) {
                ht.put(contacts2[i].getUsername(), contacts2[i]);
            }
        }
        if (!ht.isEmpty()) {
            return (String[])(ht.keySet().toArray(new String[0]));
        }
        else {
            return new String[0];
        }
    }
    
    public void newSettings(String username, String password, String registrar, String ipPort) {
        settings.setUserName(username);
        settings.setPassword(password);
        settings.setRegistrar(registrar);
        settings.setIpPort(ipPort);
        // TODO username
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#getRealm()
     */
    public String getRealm() {
        return settings.getRealm();
    }
}
