/*
 * Messenger4Me.java
 * 
 * Created on Mar 1, 2004
 *
 */
package examples.messaging;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * @author Jean Deruelle <jean.deruelle@nist.gov>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class Messenger4Me extends MIDlet implements CommandListener,
													SipStateListener{
	//State in which can be the application
	private static final int SIGNED_OUT=0;
	private static final int CONFIGURATION=1;
	private static final int SIGNED_IN=2;
	private static final int ADD_BUDDY=3;
	private static final int CALLING=4;
	private static final int CALL=5;
	//MIDLets Attributes
  	protected int id;    
  	private RecordStore rs;
	private static Command exitCommand = new Command("Exit", Command.SCREEN, 1);			
    private static Command logoutCommand = new Command("Logout", Command.SCREEN, 1);
	private static Command configCommand = new Command("Configuration",Command.SCREEN, 2);
	private static Command signInCommand = new Command("Sign In", Command.SCREEN, 3);  	
	private static Command okCommand = new Command("Ok", Command.SCREEN, 1);	
	private static Command cancelCommand = new Command("Cancel", Command.SCREEN, 2);
	private static Command addBuddyCommand = new Command("Add Buddy", Command.SCREEN, 2);	
	private static Command removeBuddyCommand = new Command("Remove Selected Buddy", Command.SCREEN, 3);
	private static Command inviteBuddyCommand = new Command("Call Selected Buddy", Command.SCREEN, 4);
	private static Command messageBuddyCommand = new Command("IM with Selected Buddy", Command.SCREEN, 5);
	private static Command byeCommand = new Command("End Call", Command.SCREEN, 1);	
	private static Image offlineImg=null;
	private static Image onlineImg=null;
  	private Display display;    // The display for this MIDlet
	//Application attributes
	private int state;
	private String proxyAddress="129.6.50.19:4000";
	protected Vector buddyList=null;
	private CallListener callListener=null;
	
	static{
		try{
			offlineImg=Image.createImage("/examples/messaging/offline_messenger.png");
			onlineImg=Image.createImage("/examples/messaging/online_messenger.png");
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
		
	/**
	 * 
	 */
	public Messenger4Me() {		
		//Get the mobile screen
		display = Display.getDisplay(this);		
		state=SIGNED_OUT;		
	}

	/**************************** MIDLET METHODS*******************************/
    
	/**
	 *  Called by application manager to start the MIDlet.
	 */
	public void startApp() {		
		storeSipUri("sip:jean@nist.gov");		
		mainMenu();					
	}

	// A required method
	public void pauseApp() {

	}

	// A required method
	public void destroyApp(boolean unconditional){
		callListener.startUnRegister();
		try{
			Thread.sleep(1000);
		}
		catch(InterruptedException ie){
			ie.printStackTrace();	
		}
		callListener.close();
	}

	/**
	 * Implementation of the CommandListener interface
	 * Be careful, no network communication is allowed in this method
	 */
	public void commandAction(Command c, Displayable s) {		
		switch (state){
			case SIGNED_OUT : 				
				if (c == exitCommand) {
					destroyApp(false);
					notifyDestroyed();
			  	}
			  	else if(c==configCommand){
			  		state=CONFIGURATION;
			  		createConfigGUI();
			  	}
			  	else if(c==signInCommand){
					signIn();																 
			  	}			  
			  	break;
			case CONFIGURATION :				
				if(c==okCommand){
					Form form=(Form)display.getCurrent();
					proxyAddress=((TextField)form.get(0)).getString();
					String sipURI=((TextField)form.get(1)).getString();
					if(sipURI !=null && sipURI.length()>0)
						storeSipUri(sipURI);		
				}				
				state=SIGNED_OUT;
				mainMenu();
				break;
			case SIGNED_IN :
				if (c == exitCommand) {
					destroyApp(false);
					notifyDestroyed();
				}								
                if(c==logoutCommand)
                {
                    callListener.startUnRegister();
                }
				if(c==addBuddyCommand){
					state=ADD_BUDDY;
					addBuddyGUI();
				}
				if(c==inviteBuddyCommand){
					List buddyListGUI = (List)display.getCurrent();
					int selectedIndex=buddyListGUI.getSelectedIndex();
					String buddyURI=buddyListGUI.getString(selectedIndex);
					callListener.call(buddyURI);
				}
				if(c==removeBuddyCommand){
					List buddyListGUI = (List)display.getCurrent();
					int selectedIndex=buddyListGUI.getSelectedIndex();
					String buddyURI=buddyListGUI.getString(selectedIndex);
					removeBuddy(buddyURI);
					displayMessengerGUI();
				}
				break;
			case ADD_BUDDY :				
				if(c==okCommand){
					TextBox textBox=(TextBox)display.getCurrent();					
					addBuddy(textBox.getString());
				}
				state=SIGNED_IN;
				displayMessengerGUI();
				break;
			case CALLING :
				if(c==cancelCommand){
					//send a cancel
					callListener.cancelCall();
				}				
				state=SIGNED_IN;
				displayMessengerGUI();
				break;		
			case CALL :
				if(c==byeCommand){
					//send a bye
					callListener.endCall();
				}				
				state=SIGNED_IN;
				displayMessengerGUI();
				break;				
		}
	}

	/********************* SIP STATE LISTENER METHODS ************************/
	
	/**
	 * Implementation of the SipStateListener interface
	 */
	public void sipStateChanged(int applicationState){
		System.out.println("Registration State: "+applicationState);
		switch (applicationState){
			case SipStateListener.NOT_REGISTERED :
                state=SIGNED_OUT;
                callListener.close();
				mainMenu(); break;
			case SipStateListener.REGISTERED :
				try{		
					//Get the buddyList from the record store
					buddyList=new Vector();
					rs=RecordStore.openRecordStore("BuddyList",true);
					RecordEnumeration re=rs.enumerateRecords(null,null,false);
					while(re.hasNextElement()){							
						String sipURI=new String(re.nextRecord());
						buddyList.addElement(new Buddy(sipURI));
					}	
					rs.closeRecordStore();		 		
				}
				catch(RecordStoreException rse){
					rse.printStackTrace();
				}
				addBuddy("sip:oli@nist.gov");
				//Subscribe to the presence of our buddies
				/*for(int i=0;i<buddyList.size();i++){
					Buddy buddy=(Buddy)buddyList.elementAt(i);
					callListener.subscribe(buddy);
				}*/
				state=SIGNED_IN;
				displayMessengerGUI(); break; 
			case SipStateListener.CALLING : 
				state=CALLING;
				displayCallGUI("Call");break;
			case SipStateListener.INCOMING_CALL : 
				state=CALLING;
				displayCallGUI("Incoming call");break;
			case SipStateListener.IN_A_CALL :
				state=CALL; 
				Displayable screen=display.getCurrent();
				if(screen instanceof TextBox){
					TextBox callTextBox=(TextBox)screen;
					callTextBox.removeCommand(cancelCommand);
					callTextBox.addCommand(byeCommand);					
					callTextBox.insert(
						"In A Call with "+callListener.callee+"\n", 
						callTextBox.getCaretPosition());
					//Display the message
					display.setCurrent(callTextBox);
				}				
				break;
			case SipStateListener.IDLE :
				state=SIGNED_IN;
				displayMessengerGUI();									
		}
	}

	/**
	 * Implementation of the SipStateListener interface
	 */
	public void sipBuddyPresenceChanged(String presenceState,String buddyURI){
		for(int i=0;i<buddyList.size();i++){
			Buddy buddy=(Buddy)buddyList.elementAt(i);
			if(buddy.getSipUri().equals(buddyURI)){
				buddy.setPresence(presenceState);
				displayMessengerGUI();
				return;	
			}				
		}
	}

	/************************** APPLICATION METHODS **************************/

	/**
	 * Sign In with the Proxy
	 */
	private void signIn(){							
        callListener = new CallListener(this);
		callListener.start();
		callListener.startRegister(proxyAddress);		
	}
	
	/**
	 * Store the sip uri in parameter in the record store
	 * @param sipURI - the sip URI to store
	 */
	private void storeSipUri(String sipURI){
		try{		
			rs=RecordStore.openRecordStore("UserSipUri",true);   
			String s=new String(sipURI);
		  	byte[] b = s.getBytes();          
		  	id=rs.addRecord(b, 0, b.length);
			rs.closeRecordStore();
		}
		catch(RecordStoreException rse){
			rse.printStackTrace();
			destroyApp(true);
		}
	}
	
	/**
	 * Store the sip uri of the buddy in parameter in the record store
	 * @param sipURI - the sip URI to store
	 */
	private void addBuddy(String sipURI){
		try{		
			//Store into the record store the sip uri
			rs=RecordStore.openRecordStore("BuddyList",true);   
			String s=new String(sipURI);
			byte[] b = s.getBytes();          
			id=rs.addRecord(b, 0, b.length);
			rs.closeRecordStore();
			Buddy buddy=new Buddy(sipURI);
			buddyList.addElement(buddy);			
		}
		catch(RecordStoreException rse){
			rse.printStackTrace();
			destroyApp(true);
		}
	}
	
	/**
	 * Remove the sip uri of the buddy in parameter in the record store
	 * @param sipURI - the sip URI to remove
	 */
	private void removeBuddy(String sipURI){
		//Remove the buddy from the list
		Enumeration e=buddyList.elements();
		while(e.hasMoreElements()){
			Buddy buddy=(Buddy)e.nextElement();
			if(buddy.getSipUri().equals(sipURI)){
				buddyList.removeElement(buddy);
				break;
			}							
		}
		try{		
			//Remove from the record store the sip uri
			rs=RecordStore.openRecordStore("BuddyList",true);   
			RecordEnumeration recordEnumeration=
				rs.enumerateRecords(null,null,true);
			while(recordEnumeration.hasNextElement()){
				int recordId=recordEnumeration.nextRecordId();
				String recordSipURI=new String(rs.getRecord(recordId));
				if(recordSipURI.equals(sipURI)){
					rs.deleteRecord(recordId);					
					break;
				}									
			}
			recordEnumeration.destroy();
			rs.closeRecordStore();					
		}
		catch(RecordStoreException rse){
			rse.printStackTrace();
			destroyApp(true);
		}
	}
	
	/******************************** GUI METHODS ****************************/
	
	/**
	 * Create and display the GUI to configure the application
	 */
	private void createConfigGUI(){	
		TextField proxyIPField=new TextField("Proxy Address:",
											  null,
											  24,
											  TextField.ANY);		
		TextField sipUriField=new TextField("Sip URI:",
											null,
											64,
											TextField.ANY);																							 
		Form form=new Form("Messenger 4 Me", 
							null);
		
		form.addCommand(okCommand);
		form.addCommand(cancelCommand);
		form.append(proxyIPField);		
		form.append(sipUriField);
		form.setCommandListener(this);
		display.setCurrent(form);
	}
	
	/**
	 * Display the GUI which will allow one to do some messaging
	 */
	private void displayMessengerGUI(){
		//Output on the mobile screen
		List buddyListGUI=new List("Buddy List",List.EXCLUSIVE);
		buddyListGUI.addCommand(exitCommand);		
		buddyListGUI.addCommand(addBuddyCommand);		
        buddyListGUI.addCommand(logoutCommand);
		
		
		if(buddyList!=null && buddyList.size()>0){
			buddyListGUI.addCommand(removeBuddyCommand);
			buddyListGUI.addCommand(inviteBuddyCommand);
			buddyListGUI.addCommand(messageBuddyCommand);	
		}		
		for(int i=0;i<buddyList.size();i++){		
			Buddy buddy=(Buddy)buddyList.elementAt(i);
			if(buddy.getPresence().equals("offline"))
				buddyListGUI.append(buddy.getSipUri(),offlineImg);
			else			
				buddyListGUI.append(buddy.getSipUri(),onlineImg);
		}
		//User allowed to exit the MIDlet on this screen
		buddyListGUI.setCommandListener(this);
				
		display.setCurrent(buddyListGUI);		
	}
	
	/**
	 * Display the GUI which will allow one to do some call
	 */
	private void displayCallGUI(String callDescription){
		TextBox callTextBox=
			new TextBox(
				"Call Status",
				null,
				255,
				TextField.ANY);		
		callTextBox.addCommand(cancelCommand);
		callTextBox.setCommandListener(this);
		callTextBox.insert(
			callDescription+"\n",
			callTextBox.getCaretPosition());
		//Display the message
		display.setCurrent(callTextBox);
	}
	
	/**
	 * Display the GUI to add a buddy to our buddy list	 
	 */
	private void addBuddyGUI(){
		TextBox addBuddyTextBox=
			new TextBox(
				"Enter the sip address of the buddy",
				null,
				24,
				TextField.ANY);
		addBuddyTextBox.addCommand(okCommand);
		addBuddyTextBox.addCommand(cancelCommand);
		addBuddyTextBox.setCommandListener(this);
		display.setCurrent(addBuddyTextBox);
	}

	/**	  
	 * Display the main Menu of the application
	 */
	private void mainMenu(){
		//Output on the mobile screen
        Form form = new Form("Messenger 4 Me", null);
//		try{		
//			Image image=Image.createImage("/examples/messaging/logoNist.png");
//			ImageItem imageItem=new ImageItem(null,image,ImageItem.LAYOUT_CENTER,null);
//			form.append(imageItem);
//		}
//		catch(IOException ioe){
//			ioe.printStackTrace();							
//		}
	  	form.addCommand(exitCommand);
		form.addCommand(configCommand);
		form.addCommand(signInCommand);			
	  	//User allowed to exit the MIDlet on this screen
		form.setCommandListener(this);
	  	//Display the message
	  	display.setCurrent(form);		
        System.out.println("displaying main menu");
	}
}
