/* 
 * Created on Aug 4, 2004
 * 
 * $Id: HandyguiMIDlet.java,v 1.1 2005/04/11 08:27:43 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import java.util.Hashtable;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.im.api.IMService;
import ch.ethz.jadabs.im.api.IMSettings;
import ch.ethz.jadabs.im.jxme.IMServiceActivator;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.JxmeActivator;
//import ch.ethz.jadabs.jxme.bt.BTActivator;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;
import ch.ethz.jadabs.api.MessageCons;

/**
 * This is the MIDlet class of the Messenger application.
 * 
 * @author Janneth Malibago
 * @version 1.0
 */
public class HandyguiMIDlet extends MIDlet 
                                  implements CommandListener, BundleActivator
{    
    /** Reference to Log4j window */
    private static Logger LOG;
    
    /** shared static variables */
    private HandyguiMIDlet instance;

    /** display of this midlet application */
    private Display display;       
    
    /* the I/O handler */
    private RMSIOProperty rmsioProperty;
    
    /** Reference to IMService**/
	private IMService imService;
	
	/** Reference to IMSettings**/
	private IMSettings imSettings;
    
	/* the different screens */
	private AboutScreen aboutScreen;
    private NeighboursList neighboursList;
    private BuddiesList buddiesList;
    private AddBuddyForm addBuddyForm;
    private RemoveBuddyForm removeBuddyForm;
    private HistoryForm historyForm;
    private SelectReceiverList selectReceiverList;
    private MessageScreen messageScreen;    
    private SettingsForm settingsForm;
    
    /** Reference to the end-point service */
    private EndpointService endptsvc;
    
    /** Reference to SMS Gateway instance */
//    private SMSGatewayService smsgateway;    
    
    /* reference to tha previous screen, used for cancelCmd */
    private Displayable previousScreen;
    
    /* hashtable where the possible receivers are stored which are returned by the IMService */
    private Hashtable possibleReceivers;
    
    /* variable to store the address of the current receiver */
    private String currentReceiver = "testNr";    
    /* variable to store the message which will be sent with sendMessage() */
    private String currentMessage = "testMessage";
    
    /* variable to store the name of the buddy which should be added or removed */
    private String buddyName = "testBuddy";
    
    /* Images */
	public static Image GREEN;
	public static Image ORANGE;
	public static Image RED;
	public static Image BLUE;
	
	/* commands */
    private Command showNeighboursCmd;
    private Command showBuddiesCmd;
    private Command showHistoryCmd;
    private Command showSettingsCmd;  
    private Command connectCmd;
    private Command disconnectCmd;
    private Command cancelCmd;
    private Command addBuddyCmd;
    private Command addCmd;
    private Command removeBuddyCmd;
    private Command removeCmd;    
    private Command sendMessageCmd;
    private Command selectCmd;
    private Command sendCmd;
    private Command saveCmd; 
    private Command getStatusCmd;
    private Command logCmd;
    private Command aboutCmd;
    private Command exitCmd; 
    

    /** Constructor */
    public HandyguiMIDlet()
    {    
//      Carefully!! crashes if LOG in constructor!!!!      
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("invoke HandyguiMIDlet()");
//        }
        
        // do some initialization stuff
        // this corresponds to the init.xargs descriptor from knopflerfish
        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.peeralias", this.getAppProperty("ch.ethz.jadabs.jxme.peeralias"));
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer", 
                                  this.getAppProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer"));        
        osgicontainer.startBundle(new LogActivator());
        osgicontainer.startBundle(new JxmeActivator());
//        osgicontainer.startBundle(new BTActivator());  
        osgicontainer.startBundle(new IMServiceActivator());  
        
        osgicontainer.startBundle(this);
        instance = this;
        
        LOG = Logger.getLogger("SmartMessengerMIDlet"); 
        
        possibleReceivers = new Hashtable();
        
        // load images
        try {           
	        GREEN = Image.createImage("/res/green.png"); 
        } catch (java.io.IOException err) {
            LOG.debug("failed loading image green.png");
        } 
        try {           
	        ORANGE = Image.createImage("/res/orange.png"); 
        } catch (java.io.IOException err) {
            LOG.debug("failed loading image orange.png");
        }  
        try {           
	        RED = Image.createImage("/res/red.png"); 
        } catch (java.io.IOException err) {
            LOG.debug("failed loading image red.png");
        }  
        try {           
	        BLUE = Image.createImage("/res/blue.png"); 
        } catch (java.io.IOException err) {
            LOG.debug("failed loading image blue.png");
        }  
    }

    /** Handle starting the MIDlet */
    public void startApp()
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke startApp()");
        }

        // obtain reference to Display singleton
        display = Display.getDisplay(this);   
        
		/*
		 *For example, a MIDP implementation for a mobile phone could use the following algorithm: 
		 * 
		 * Find any commands that map to hard buttons on the device, and assign them to those buttons.
		 * The highest priority command of type Back maps to the Back hard button.
		 * The highest priority command of type Exit maps to the End Call button.
		 * The highest priority command of type Help maps to the Help button.
		 * Order the abstract commands by type in the following sequence: Item, Screen, Back, OK, Cancel, Stop, Help, and Exit.
		 * Within type, order the abstract commands by priority, highest to lowest.
		 * Put the highest-priority command of type Item on the left soft button.
		 * Put any remaining commands on a system menu and put the label �Menu� on the right soft button. * 
		 * 
		 */     
        addBuddyCmd = new Command("Add Buddy", Command.SCREEN, 1);
        removeBuddyCmd = new Command("Remove Buddy", Command.SCREEN, 1);
        sendMessageCmd = new Command("Send Message", Command.SCREEN, 2);  
        connectCmd = new Command("Connect", Command.SCREEN, 3);
        disconnectCmd = new Command("Disconnect", Command.SCREEN, 3); 
        showNeighboursCmd = new Command("Neighbours", Command.SCREEN, 4);
        showBuddiesCmd = new Command("Buddies", Command.SCREEN, 4);
        showHistoryCmd = new Command("History", Command.SCREEN, 4);
        showSettingsCmd = new Command("Settings", Command.SCREEN, 4); 
        getStatusCmd = new Command("Get Status", Command.SCREEN, 5);
        aboutCmd = new Command("About", Command.SCREEN, 6);       
        logCmd = new Command("Log", Command.SCREEN, 7);
        cancelCmd = new Command("Cancel", Command.BACK, 1);
        selectCmd = new Command("Select", Command.OK, 1);
        sendCmd = new Command("Send", Command.OK, 1);
        saveCmd = new Command("Save Settings", Command.OK, 1);        
        addCmd = new Command("Add", Command.OK, 1);                
        removeCmd = new Command("Remove", Command.OK, 1);                  
        exitCmd = new Command("Exit", Command.EXIT, 1);
        
        // initialize the screens
        aboutScreen = new AboutScreen(this, new Command[] {connectCmd, disconnectCmd, 
                showNeighboursCmd, showBuddiesCmd, showHistoryCmd, showSettingsCmd, sendMessageCmd, getStatusCmd, logCmd, exitCmd});  
        neighboursList = new NeighboursList(this, new Command[] {connectCmd, disconnectCmd, 
                showBuddiesCmd, showHistoryCmd, showSettingsCmd, sendMessageCmd, getStatusCmd, aboutCmd, logCmd, exitCmd});  
        buddiesList = new BuddiesList(this, new Command[] {connectCmd, disconnectCmd, addBuddyCmd, removeBuddyCmd,
                showNeighboursCmd, showHistoryCmd, showSettingsCmd, sendMessageCmd, getStatusCmd, aboutCmd, logCmd, exitCmd});        
        addBuddyForm = new AddBuddyForm(this, new Command[] {addCmd, cancelCmd});
        removeBuddyForm = new RemoveBuddyForm(this, new Command[] {removeCmd, cancelCmd});        
        historyForm = new HistoryForm(this, new Command[] {connectCmd, disconnectCmd, 
                showNeighboursCmd, showBuddiesCmd, showSettingsCmd, sendMessageCmd, getStatusCmd, aboutCmd, logCmd, exitCmd}); 
        selectReceiverList = new SelectReceiverList(this, new Command[] {selectCmd, cancelCmd});
        messageScreen = new MessageScreen(this, new Command[] {sendCmd, cancelCmd, 
                showNeighboursCmd, showBuddiesCmd, showHistoryCmd, showSettingsCmd, getStatusCmd, aboutCmd, logCmd, exitCmd});
        settingsForm = new SettingsForm(this, new Command[] {connectCmd, disconnectCmd, 
                showNeighboursCmd, showBuddiesCmd, showHistoryCmd, sendMessageCmd, saveCmd, getStatusCmd, aboutCmd, logCmd, exitCmd});     
        
        // initialize Logger
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(buddiesList);   
      
        // initialize GUI       
        Image statusImage = null;
        try {
            // load the status image
	        statusImage = Image.createImage("/res/blue.png"); 
        } catch (java.io.IOException err) {
            LOG.debug("failed loading image blue.png");
        }
        settingsForm.initSettings("", "", "", "");       
        initPossibleReceivers();
              
        display.setCurrent(buddiesList);		
        		
    }

    /** Handle pausing the MIDlet */
    public void pauseApp()
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke pauseApp()");
        }
    }

    /** Handle destroying the MIDlet */
    public void destroyApp(boolean unconditional)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke destroyApp()");
        }
    }

    /** Quit the MIDlet */
    private void quitApp()
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke quitApp()");
        }
        instance.destroyApp(true);
        instance.notifyDestroyed();
        instance = null;
        display = null;
    }
    
    
    /**
     * Handle user action from SmartMessenger application
     * 
     * @param c
     *            GUI command 
     * @param d
     *            GUI display object that triggered the command
     */
    public void commandAction(Command c, Displayable d)
    {   
        if (c == cancelCmd) {
	        if (LOG.isDebugEnabled()) 
	        {
		        LOG.debug("invoke cancelCmd");
		    } 
	        display.setCurrent(previousScreen);	 
        } else if (c == showNeighboursCmd) {
	        if (LOG.isDebugEnabled()) 
	        {
		        LOG.debug("invoke showNeighboursCmd");
		    } 
            display.setCurrent(neighboursList);
            previousScreen = d;
        } else if (c == showBuddiesCmd) {
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke showBuddiesCmd");
    	    } 
            display.setCurrent(buddiesList);
            previousScreen = d;
        } else if (c == showHistoryCmd) {
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke showHistoryCmd");
    	    } 
            display.setCurrent(historyForm);
            previousScreen = d;
        } else if (c == showSettingsCmd) {
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke showSettingsCmd");
    	    } 
            display.setCurrent(settingsForm);
            previousScreen = d;
        } else if (c == connectCmd) {
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke connectCmd");
    	    } 
            if (getImService() != null)
			{	        
                imService.connect(new Listener(this));
			}
	        else
	        {
	            LOG.debug("IMService == null");
	        }     
//            TODO or only Alert in connectOk()?
            Alert alert = new Alert("Information",
                    "Connecting ...", null,
                    AlertType.INFO);
	        alert.setTimeout(3000);                                
	        display.setCurrent(alert, d);
        } else if (c == disconnectCmd) {
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke disconnectCmd");
    	    } 
            if (getImService() != null)
			{	        
                imService.disconnect();
			}
	        else
	        {
	            LOG.debug("IMService == null");
	        }   
//          TODO or only Alert in disconnectOk()?
            Alert alert = new Alert("Information",
                    "Disconnecting ...", null,
                    AlertType.INFO);
	        alert.setTimeout(3000);                                
	        display.setCurrent(alert, d); 	        
        } else if (c == addBuddyCmd) {
            // show the addBuddyForm where the user can type in the buddyname and the host
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke addBuddyCmd");
    	    }             
            display.setCurrent(addBuddyForm);
            previousScreen = d;
        } else if (c == addCmd) {
            // add the buddy
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke addCmd");
    	    } 
            if (getImService() != null)
			{	        
                imService.addSipBuddy(addBuddyForm.getBuddyName()+"@"+addBuddyForm.getHost());
        		initPossibleReceivers();
        	}
	        else
	        {
	            LOG.debug("IMService == null");
	        }  
//            TODO only add buddy if IMService != null?
            if ((addBuddyForm.getBuddyName() == "") || (addBuddyForm.getHost() == "")) {
                Alert alert = new Alert("Error",
                        "Not correct format!", null,
                        AlertType.ERROR);
    	        alert.setTimeout(3000);                                
    	        display.setCurrent(alert, d); 
            }
            else
            {
                buddiesList.append(addBuddyForm.getBuddyName()+"@"+addBuddyForm.getHost(),RED);
                addBuddyForm.setBuddyName("");
                addBuddyForm.setHost("");
                display.setCurrent(buddiesList);
                previousScreen = d;                
            }            
        } else if (c == removeBuddyCmd) {
            // ask if the user really want to remove the selected buddy
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke removeBuddyCmd");
    	    } 
            removeBuddyForm.setBuddyName(buddiesList.getString(buddiesList.getSelectedIndex()));
            display.setCurrent(removeBuddyForm);
            previousScreen = d;
        } else if (c == removeCmd) {
            // remove the selected buddy
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke removeCmd");
    	    } 
            if (getImService() != null)
			{	        
        		imService.removeSipBuddy(buddiesList.getString(buddiesList.getSelectedIndex()));
        		initPossibleReceivers();
        	}
	        else
	        {
	            LOG.debug("IMService == null");	            
	        }   
//          TODO only remove buddy if IMService != null?
            buddiesList.delete(buddiesList.getSelectedIndex());
            removeBuddyForm.setBuddyName("");
	        display.setCurrent(buddiesList); 
        } else if (c == sendMessageCmd) {
            // show a list of possible receivers and ask the user to select one
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke sendMessageCmd");
    	    }
            selectReceiverList.initReceiverList();
            display.setCurrent(selectReceiverList);
            previousScreen = d;  
        } else if (c == selectCmd) {
            // the selected buddy becomes the current receiver 
            // i.d. the next message will be sent to this buddy
            // show a textbox where the message can be inserted
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke selectCmd");
    	    }
            currentReceiver = selectReceiverList.getString(selectReceiverList.getSelectedIndex());
            messageScreen.setString("");
            display.setCurrent(messageScreen);    
        } else if (c == sendCmd) {
            // send the message inserted into the textbox to the current receiver
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke sendCmd");
    	    }
            currentMessage = messageScreen.getString();
            messageScreen.setString("");
            sendMessage(currentReceiver, currentMessage,d);            
        } else if (c == saveCmd) {
            // save settings
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke saveCmd");
    	    } 
            Alert alert = new Alert("Information","Saving Settings", null,AlertType.INFO);
	        alert.setTimeout(3000);                                
	        display.setCurrent(alert, d);
        } else if (c == getStatusCmd) {
            // return the status of the IMService
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke getStatusCmd");
    	    } 
            String status = getStatus();
            Alert alert = new Alert("Information","Your Status: " + status, null,AlertType.INFO);
	        alert.setTimeout(3000);                                
	        display.setCurrent(alert, d);
        } else if (c == aboutCmd) {
            // show information about the Jadabs IM Group
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke aboutCmd");
    	    } 
            display.setCurrent(aboutScreen);
            previousScreen = d;
        } else if (c == logCmd) {
            // show log history
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke logCmd");
    	    } 
            Logger.getLogCanvas().setPreviousScreen(d);
            display.setCurrent(Logger.getLogCanvas()); 
        } else if (c == exitCmd) {
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("invoke exitCmd");
    	    } 
            quitApp();
        } else if (d == selectReceiverList){
            // abstract command, similar to selectCmd
            // the selected buddy becomes the current receiver 
            // i.d. the next message will be sent to this buddy
            // show a textbox where the message can be inserted
            if (LOG.isDebugEnabled()) 
            {
    	        LOG.debug("buddy in selectReceiverList slected");
    	    }
            currentReceiver = ((SelectReceiverList) d).getString(((SelectReceiverList) d).getSelectedIndex());           
            messageScreen.setString("");
            display.setCurrent(messageScreen);
        }
    
    }
         
    

    

    public void sendMessage(String phoneNumber, String message, Displayable d) 
    {
        if (LOG.isDebugEnabled()) {
	        LOG.debug("invoke sendMessage()");
	    }        
//        TODO use IMService
        if (getImService() != null)
		{	        
            LOG.debug("use IMService sendMessage");
            imService.sendMessage(phoneNumber, message);
		}
        else 
        {
            LOG.debug("IMService == null");
//	        Element[] elms = new Element[2];
//	        elms[0] = new Element("to", phoneNumber.getBytes(), 
//	                               null, Element.TEXTUTF8_MIME_TYPE);       
//	        elms[1] = new Element("body", message.getBytes(), 
//	                               null, Element.TEXTUTF8_MIME_TYPE);
//	        Message msg = new Message(elms);
//	        smsgateway.sendSM(msg);	        
        }
        historyForm.append("<< " + phoneNumber + ": " + message + "\n");
        Alert alert = new Alert("Information",
                "Sending message to "+currentReceiver+".", null,
                AlertType.INFO);
	    alert.setTimeout(3000);                                
	    display.setCurrent(alert, d);          
    }
    
    
    /**
     * Called by the OSGi container when this bundle is started.
     * @param bc reference to context data of this bundle
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke start()");
        }

        ServiceReference sref;
        
        // register IOProperty service     
        rmsioProperty = new RMSIOProperty(this);
    	
		Hashtable dict = new Hashtable();
		dict.put("impl","ch.ethz.jadabs.im.cldc.gui.handygui.RMSIOProperty");
		bc.registerService("ch.ethz.jadabs.im.api.IOProperty", rmsioProperty, dict);  	        
    
        sref = bc.getServiceReference("ch.ethz.jadabs.im.api.IMService");
        if (sref == null)
        {
            LOG.debug("could not properly initialize IM, IMService is missing");
        }
        else
        {
            if (LOG.isDebugEnabled()) {
                LOG.debug("init imservice and imsettings");
            }
	        imService = (IMService)bc.getService(sref);       	        
	        imSettings = (IMSettings)imService; 
	        
	        if (getImService() != null)
			{	
	            settingsForm.initSettings(imSettings.getUserName(), imSettings.getPassword(),
						imSettings.getRegistrar(), imSettings.getIpPort());
				buddiesList.initBuddiesList();
				neighboursList.initNeighbourList();
				initPossibleReceivers();
			}
	        else
	        {
	            LOG.debug("IMService == null");	            
	        }
        }  
           
        // get Endpoint service
        sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        endptsvc = (EndpointService)bc.getService(sref);   
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("end invoking start()");
        }
    }

    /**
     * Called by the OSGi container when this bundle is stopped.
     * @param bc reference to context data of this bundle
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) 
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke stop()");
        } 	
//        bc = null;
    }
	

	
	/*********************************************
	 * USER MODIFICATION
	 * -------------------------------------------
	 * IM EVENTS
	 *********************************************/
    
	/**
	 * If IMService.register is called, used to notify app about 
	 * status of registration process
	 */
	public void connectOk() {
	    
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke connectOk()");
        }
		initPossibleReceivers();
		Alert alert = new Alert("Confirmation",
                "Connecting successful", null,
                AlertType.CONFIRMATION);
        alert.setTimeout(3000);                                
        display.setCurrent(alert);
	}
	
	/**
	 * If IMService.unregister is called, used to notify app about 
	 * status of registration process
	 */
	public void disconnectOk() {
	    
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke disconnectOk()");
        }
	    possibleReceivers = null;
	    Alert alert = new Alert("Confirmation",
                "Disonnecting successful", null,
                AlertType.CONFIRMATION);
        alert.setTimeout(3000);                                
        display.setCurrent(alert);
	}
	
	/**
	 * If an instant message is received from the sender this method
	 * is called.
	 * 
	 * @param sipaddress from address, who sent the message
	 * @param msg instant message
	 */
	public void incomingMessage(String sipaddress, String msg) {
	    
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke incomingMessage()");
        }
		historyForm.append(">> " + sipaddress + ": " + msg + "\n");
		Alert alert = new Alert("Information",
                "New Message received!", null,
                AlertType.INFO);
	    alert.setTimeout(3000);                                
	    display.setCurrent(alert);
	}
	
	/**
	 * Used to notify app about a status change of a buddy
	 */
	public void buddyStatusChanged() {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke buddyStatusChanged()");
        }
		buddiesList.initBuddiesList();
	}
	
	/**
	 * Used to notify app about a status change of a neighbour
	 */
	public void neighbourListChanged() {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke neighbourListChanged()");
        }
		neighboursList.initNeighbourList();
		initPossibleReceivers();
	}
	
    /**
     * Once a gateway comes in the transmission field of our agent, this
     * method gets called... 
     * 
     * @param presence
     */
	public void gatewayEvent(boolean presence) {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke gatewayEvent()");
        }
//	    TODO see ch.ethz.jadabs.im.gui.swtgui.MainComposite.java
//	    should behave similar
	    
//		gatewayFound = true;
//		if (imService.getIMType().equals(MessageCons.IM_JXME))
//		{
//			setUsernameField.setEditable(true);
//			saveConfigButton.setEnabled(true);
//			setPasswordField.setEditable(true);
//		}
	}
	
    /**
     * If an operation times out, for example, a message confirmation is not received.
     * 
     * @param message the timeout error message
     */
    public void operationFailed(int type, String message) {
        Alert alert = new Alert("Operation Failed!",message, null,AlertType.ERROR);
	    alert.setTimeout(3000);                                
	    display.setCurrent(alert);
    }
	
	/*
	 * other functions
	 */
	
    /**
     * init possibleReceivers with the list of possible receivers returned by the IMService
     */
	private void initPossibleReceivers() {
	    String old = currentReceiver;
	    boolean keep = false;
	    possibleReceivers.clear();
	    if (getImService() != null)
		{	        
	        String[] contacts = imService.getReceivers();
			for (int i=0; i<contacts.length; i++) {
			    possibleReceivers.put(contacts[i],contacts[i]);
				keep = keep || old.equals(contacts[i]);
			}
			if (keep) {
			    currentReceiver = old;
			}
		}
        else
        {
            LOG.debug("IMService == null");
//            TODO remove this testcode
            possibleReceivers.put("TestBuddy","TestBuddy");
            possibleReceivers.put("TestBuddy2","TestBuddy2");
        } 		
	}
	

	
    /**
     * @return Returns the imService.
     */
    public IMService getImService() {
        return imService;
    }
    
    
    /**
     * @return Returns the list of possible receivers.
     */
    public Hashtable getPossibleReceivers() {
        return possibleReceivers;
    }
    
    /**
     * @return Returns the status of the IMService.
     */
    private String getStatus() 
    {
        String status;
        if (getImService() != null)
        {
            if (imService.getStatus() == MessageCons.IM_STATUS_ONLINE) 
            {
                status = "online";
            }
            else if (imService.getStatus() == MessageCons.IM_STATUS_BUSY)
            {
                status = "busy";
            }
            else if (imService.getStatus() == MessageCons.IM_STATUS_OFFLINE)
            {
                status = "offline";
            }
            else
            {
                status = "unknown";
            }
        }
        else
        {
            status = "unknown";
        }
        return status;
    }
    
    
    
}