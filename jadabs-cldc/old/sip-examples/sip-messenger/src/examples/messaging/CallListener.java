/*
 * CallListener.java
 * 
 * Created on Mar 1, 2004
 *
 */
package examples.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import gov.nist.microedition.sip.SipConnector;
import nist.javax.microedition.sip.SipClientConnection;
import nist.javax.microedition.sip.SipClientConnectionListener;
import nist.javax.microedition.sip.SipConnectionNotifier;
import nist.javax.microedition.sip.SipDialog;
import nist.javax.microedition.sip.SipException;
import nist.javax.microedition.sip.SipServerConnection;
import nist.javax.microedition.sip.SipServerConnectionListener;

/**
 * @author Jean Deruelle <jean.deruelle@nist.gov>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class CallListener
	implements SipClientConnectionListener, 
			   SipServerConnectionListener,
			   Runnable {
	private Thread callListenerThread=null;
	private SipStateListener sipStateListener=null;
	private SipConnectionNotifier sipConnectionNotifier=null;
	private SipClientConnection sipClientConnection=null;
	private boolean running =true;	
	private String proxy=null;
	protected String callee=null;	
	private boolean startRegistration=false;
	private boolean startUnRegistration=false;
	private boolean startCall=false;
	private boolean cancelCall=false;
	private boolean endCall=false;
	private static String sdp="v=0\n" +						"o=oli 0 0 IN IP4 129.6.50.176\n" +						"s=-\n" +						"c=IN IPV4 129.6.50.176\n" +						"t=0 0\n" +						"m=audio 6378 RTP/AVP 4";
	private static String notifySdp=		"<?xml version=\"1.0\"?>\n"+		"<!DOCTYPE presence\n"+		"PUBLIC \"-//IETF//DTD RFCxxxx XPIDF 1.0//EN\" \"xpidf.dtd\">\n"+
		"<presence>\n"+
		"<presentity uri=\"oli@nist.gov\" />\n"+
		"<atom id=\"nist-sipId1000\" >\n" +		"<address uri=\"sip:129.6.50.176:5080\"  >\n" +		"<status status=\"open\" />\n" +		"<msnsubstatus substatus=\"online\" />\n" +		"</address>\n" +		"</atom>\n" +		"</presence>"; 
	private SipDialog sipDialogInvite=null;	
	
		
	/**
	 * 
	 */
	public CallListener(SipStateListener sipStateListener) {
		this.sipStateListener=sipStateListener;
	}

	public void start(){
		//if(callListenerThread==null)
			callListenerThread=new Thread(this);		
		callListenerThread.start();
	}

	/**
	 * @see nist.javax.microedition.sip.SipClientConnectionListener#notifyResponse(nist.javax.microedition.sip.SipClientConnection)
	 */
	public void notifyResponse(SipClientConnection sipClientConnection) {
		System.out.println("RECEIVE RESPONSE");	
		try{
			sipClientConnection.receive(0);
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}		
		int statusCode =sipClientConnection.getStatusCode();
		//Handle the busy here
		if(statusCode ==486){
			sipStateListener.sipStateChanged(SipStateListener.IDLE);
			try{
				sipClientConnection.close();	
			}
			catch(IOException ioe){
				ioe.printStackTrace();						
			}
		}		
		//Handle the Temporary Unvailable
		if(statusCode ==480){
			sipStateListener.sipStateChanged(SipStateListener.IDLE);
			try{
				sipClientConnection.close();	
			}
			catch(IOException ioe){
				ioe.printStackTrace();						
			}
		}							
		//Handle the OK	
		if(statusCode ==200){
			if(sipClientConnection.getHeader("CSeq").indexOf("SUBSCRIBE")!=-1){
				try{
					SipDialog sipDialog=sipClientConnection.getDialog();
					sipClientConnection.close();
					System.out.println("OK for Subscribe received");			
					String sdp="<?xml version=\"1.0\"?>\n"+
							   "<!DOCTYPE presence\n"+
							   " PUBLIC \"-//IETF//DTD RFCxxxx XPIDF 1.0//EN\""+
							   " \"xpidf.dtd\">\n<presence>\n" +							   "<presentity uri=\""+"oli@nist.gov"+"\" />\n"+
							   "<atom id=\"nist-sipId2000\" >\n"+
							   "<address uri=\"sip:129.6.50.176:5080\"  >\n"+
							   "<status status=\"open\" />\n"+
							   "<msnsubstatus substatus=\"online\" />\n"+
							   "</address>\n</atom>\n</presence>";		
					SipClientConnection sipClientConnectionNotify=
									sipDialog.getNewClientConnection("NOTIFY");
					sipClientConnectionNotify.setHeader("Event","presence");
					sipClientConnectionNotify.setHeader("Subscription-State","active");
					sipClientConnectionNotify.setHeader("Content-Length",""+sdp.length());
					sipClientConnectionNotify.setHeader("Content-Type","application/xpidf+xml");
					OutputStream os=sipClientConnectionNotify.openContentOutputStream();
					os.write(sdp.getBytes());
					os.close();
				}
				catch(IOException ioe){
					ioe.printStackTrace();
				}				
			}
			if(sipClientConnection.getHeader("CSeq").indexOf("INVITE")!=-1){				
				SipDialog sipDialog=sipClientConnection.getDialog();					
				System.out.println("OK for INVITE received");			
				String contentType =sipClientConnection.getHeader("Content-Type");
				String contentLength =sipClientConnection.getHeader("Content-Length");
				//int length = Integer.parseInt(contentLength);
				if(contentType.equals("application/sdp")){
					// handle SDP here                    
				}
				sendAck(sipClientConnection);							
			}
			if(sipClientConnection.getHeader("CSeq").indexOf("BYE")!=-1){												
				System.out.println("OK for BYE received");
				try{
					sipClientConnection.close();	
				}
				catch(IOException ioe){
					ioe.printStackTrace();						
				}																	
			}
			if(sipClientConnection.getHeader("CSeq").indexOf("CANCEL")!=-1){
				sipStateListener.sipStateChanged(SipStateListener.IDLE);												
				System.out.println("OK for CANCEL received");
				try{
					sipClientConnection.close();	
				}
				catch(IOException ioe){
					ioe.printStackTrace();						
				}
			}
		}
	}

	/**
	 * @see nist.javax.microedition.sip.SipServerConnectionListener#notifyRequest(nist.javax.microedition.sip.SipConnectionNotifier)
	 */
	public void notifyRequest(SipConnectionNotifier sipConnectionNotifier) {
		try{
			SipServerConnection ssc = sipConnectionNotifier.acceptAndOpen();
			if(ssc.getMethod().equals("INVITE")){
				sipDialogInvite=ssc.getDialog();
				sipStateListener.sipStateChanged(SipStateListener.INCOMING_CALL);
				//handle content                 
				String contentType = ssc.getHeader("Content-Type");
				String contentLength =ssc.getHeader("Content-Length");
				String fromURI =ssc.getHeader("From");
				callee=getCleanURI(fromURI);
				System.out.println("Content-Type Header : "
								+contentType);
				System.out.println("Content-Length Header : "
												+contentLength);
				int length = Integer.parseInt(contentLength);
				String sdpInvite= "";
				if(contentType.equals("application/sdp")){
					InputStream is=ssc.openContentInputStream();
					byte content[] = new byte[length];
					is.read(content);
					sdpInvite=new String(content);
					System.out.println("SDP Content : "+sdpInvite);														
				}
				//TODO :  check if already in a call, send a busy here
				//TODO :  ask the permission to the user to accept the call
				//initialize and send 100 TRYING response
				ssc.initResponse(100);
				ssc.setHeader("Content-Length","0");
				ssc.setHeader("Content-Type","application/sdp");
				ssc.send();
				//initialize and send 180 RINGING response
				ssc.initResponse(180);
				ssc.setHeader("Content-Length","0");
				ssc.setHeader("Content-Type","application/sdp");
				ssc.send();
				//initialize and send 200 response                     
				ssc.initResponse(200);
				ssc.setHeader("Content-Length",String.valueOf(sdp.length()));
				ssc.setHeader("Content-Type","application/sdp");									
				OutputStream os=ssc.openContentOutputStream();
				os.write(sdp.getBytes());
				os.close();											
			}			
			if(ssc.getMethod().equals("ACK")) {
				sipDialogInvite=ssc.getDialog();
				sipStateListener.sipStateChanged(SipStateListener.IN_A_CALL);							
			}
			if(ssc.getMethod().equals("BYE")) {											  
				//initialize and send 200 response                     
				ssc.initResponse(200);		  		
				ssc.send();				
				sipStateListener.sipStateChanged(SipStateListener.IDLE);															
			}
			if(ssc.getMethod().equals("CANCEL")){											  
				//initialize and send 200 response                     
				ssc.initResponse(200);		  		
				ssc.send();	
				sipStateListener.sipStateChanged(SipStateListener.IDLE);													  				
			}
			if(ssc.getMethod().equals("SUBSCRIBE")){
				//TODO : Keep a trace of the subscriber and of his dialog
				ssc.initResponse(200);
				ssc.setHeader("Content-Length","0");				
				ssc.send();
				SipDialog sipDialog=ssc.getDialog();		
				//TODO :  take the sip URI from the subscribe
				sendNotify(sipDialog);		
			}
			if(ssc.getMethod().equals("NOTIFY")){
				ssc.initResponse(200);
				ssc.setHeader("Content-Length","0");				
				ssc.send();	
				//handle content                 
				String contentType = ssc.getHeader("Content-Type");
				String contentLength =ssc.getHeader("Content-Length");
				System.out.println("Content-Type Header : "
								+contentType);
				System.out.println("Content-Length Header : "
												+contentLength);
				int length = Integer.parseInt(contentLength);
				String sdp= "";
				if(contentType.equals("application/sdp")){
					InputStream is=ssc.openContentInputStream();
					byte content[] = new byte[length];
					is.read(content);
					sdp=new String(content);
					System.out.println("SDP Content : "+sdp);														
				}
				if(sdp.indexOf("offline")!=-1)
					//TODO: get the real notifier
					sipStateListener.sipBuddyPresenceChanged(
															"offline",
															"sip:oli@nist.gov");
				else
					//TODO: get the real notifier
					sipStateListener.sipBuddyPresenceChanged(
															"online",
															"sip:oli@nist.gov");							
			}			
			ssc.close();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	/**
	 * Register the application with a proxy
	 * @param proxy - Address and port of the proxy 	 
	 */
	public void startRegister(String proxy){
		this.proxy=proxy;
		if(proxy==null){		
			sipStateListener.sipStateChanged(SipStateListener.REGISTERED);
			return;
		}
		startRegistration=true;
	}
	
	public void startUnRegister(){		
		startUnRegistration=true;
	}
	/**
	 * Starts the process of calling the contact whose the uri is in parameter
	 * @param uri - uri of the contact to call
	 */
	public void call(String uri){		
		callee=uri;
		sipStateListener.sipStateChanged(SipStateListener.CALLING);
		startCall=true;
	}
	
	/**
	 * Starts the process of cancelling the call	 
	 */
	public void cancelCall(){						
		cancelCall=true;
	}
	
	/**
	 * Starts the process of ending the call	 
	 */
	public void endCall(){						
		endCall=true;
	}
	
	/**
	 * Subscribe to the presence of the buddy in parameter
	 * @param buddy - the buddy 
	 */
	public void subscribe(Buddy buddy){
		try{
			SipClientConnection sipClientConnection=
					(SipClientConnection)SipConnector.open(buddy.getSipUri());
			sipClientConnection.setListener(this);
			sipClientConnection.initRequest("SUBSCRIBE",sipConnectionNotifier);
			sipClientConnection.addHeader("Route","sip:"+proxy+";transport=udp");
			sipClientConnection.setHeader("Expires","930");
			sipClientConnection.setHeader("Event","presence");
			sipClientConnection.addHeader("Accept","application/xpidf+xml");
			sipClientConnection.send();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Subscribe to the presence of the buddy in parameter
	 * @param buddy - the buddy 
	 */
	public void sendInvite(){
		try{
			SipClientConnection sipClientConnection=
					(SipClientConnection)SipConnector.open(callee);
			sipClientConnection.setListener(this);
			sipClientConnection.initRequest("INVITE",sipConnectionNotifier);
			sipClientConnection.addHeader("Route","sip:"+proxy+";transport=udp");			
			sipClientConnection.setHeader("Content-Length", ""+sdp.length());
			sipClientConnection.setHeader("Content-Type","application/sdp");
			OutputStream os = sipClientConnection.openContentOutputStream();			
			os.write(sdp.getBytes());			
			os.close();	
			this.sipClientConnection=sipClientConnection;							      		
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		startCall=false;
	}
	
	/**
	 * Send the ACK
	 * @param sipClientConnection
	 */
	public void sendAck(SipClientConnection sipClientConnection){
		try{
			sipClientConnection.initAck();
			sipClientConnection.send();					
			sipClientConnection.close();
		}	
		catch(SipException se){
			se.printStackTrace();
		}
		catch(InterruptedIOException ioe){
			ioe.printStackTrace();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}	
		SipDialog dialog=sipClientConnection.getDialog();
		sipStateListener.sipStateChanged(SipStateListener.IN_A_CALL);						
		sipDialogInvite=sipClientConnection.getDialog();
	}
	
	/**
	 * Send the CANCEL	 
	 */
	public void sendCancel(){
		try{
			SipClientConnection sipClientConnection=
				this.sipClientConnection.initCancel();
			sipClientConnection.setListener(this);								
			sipClientConnection.send();					      	
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		catch(NullPointerException npe){
			npe.printStackTrace();
		}			
		cancelCall=false;
	}
	
	/**
	 * Send a notify
	 * @param sipDialog - create and send the notify with this dialog
	 */
	public void sendNotify(SipDialog sipDialog){
		try{
			SipClientConnection sipClientConnection=
				sipDialog.getNewClientConnection("NOTIFY");			
			sipClientConnection.addHeader("Route","sip:"+proxy+";transport=udp");			
			sipClientConnection.setHeader("Content-Length", ""+sdp.length());
			sipClientConnection.setHeader("Content-Type","application/xpidf+xml");
			OutputStream os = sipClientConnection.openContentOutputStream();			
			os.write(notifySdp.getBytes());			
			os.close();								      	
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}				
	}
	
	/**
	 * Send the BYE	 
	 */
	public void sendBye(){
		try{
			SipClientConnection sipClientConnection=
				sipDialogInvite.getNewClientConnection("BYE");			
			sipClientConnection.setListener(this);			
			sipClientConnection.send();					      	
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		sipStateListener.sipStateChanged(SipStateListener.IDLE);	
		endCall=false;
	}
	
	/**
	 * Do the process of sending and receiving the messages needed to register 
	 * with the proxy 	 
	 */
	public void register(String expires){
		System.out.println("proxy Address to send the register is : "+proxy);
		boolean registered =false;
		try{		
			SipClientConnection sipClientConnection=
				(SipClientConnection)SipConnector.open(
										"sip:jean@"+proxy);			
			sipClientConnection.initRequest("REGISTER",sipConnectionNotifier);
			sipClientConnection.setCredentials("deruelle","nist-sip","nist.gov");
			sipClientConnection.setHeader("Expires",expires);
			sipClientConnection.setRequestURI(
				"sip:"+proxy+";transport=udp");					
			sipClientConnection.send();				
			while(!registered){
				try{
					sipClientConnection.receive(15000);
				}
				catch(IOException ioe){
					//ioe.printStackTrace();		  																    			
				}
				int statusCode =sipClientConnection.getStatusCode();				
				if(statusCode ==200){						
					sipClientConnection.close();
					registered=true;
				}				 		    
			}		    
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		catch(NullPointerException npe){
			npe.printStackTrace();
		}
		if(registered){
		if(expires.toLowerCase().trim().equals("0")){
			startUnRegistration=false;		
			sipStateListener.sipStateChanged(SipStateListener.NOT_REGISTERED);
			return;
		}
        else
        {
			sipStateListener.sipStateChanged(SipStateListener.REGISTERED);
        }
        }
			
		startRegistration=false;
	}

	/**
	 * The main routine of the running thread
	 */
	public void run(){
		try{
			sipConnectionNotifier=
				(SipConnectionNotifier)SipConnector.open("sip:5080");
			sipConnectionNotifier.setListener(this);
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		while(running){
			try{
				Thread.sleep(1);
				if(startRegistration)
					//start the process of registration
					register("3600");
				if(startUnRegistration)
					//start the process of un-registration
					register("0");
				if(startCall){
					//start the process of inviting
					sendInvite();
				}
				if(cancelCall){
					//start the process of cancelling
					sendCancel();
				}
				if(endCall){
					//start the process of ending the call
					sendBye();
				}	
			}
			catch(InterruptedException ie){
				ie.printStackTrace();
			}
		}
		try{		
			sipConnectionNotifier.close();
		}		
		catch(IOException ioe){
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Close the thread
	 */
	public void close(){		
		running=false;
	}

	/*********************          Utility methods       ********************/
	
	public String getCleanURI(String uri){
		if(uri.indexOf(';')==-1)
			return uri.substring(uri.indexOf('<')+1,uri.indexOf('>'));
		else
			return uri.substring(uri.indexOf('<')+1,uri.indexOf(';'));
	}

}
