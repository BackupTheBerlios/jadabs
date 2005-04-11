package ch.ethz.jadabs.im.sip.clienttest;

import javax.sip.ResponseEvent;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.sip.SIPIMListener;
import ch.ethz.jadabs.im.sip.SIPUserAgentClient;

/*
 * Created on Nov 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestClient2 {

	static Logger logger = Logger.getLogger(TestClient2.class);
	/* (non-Javadoc)
	 * @see SIPUserAgentListener#processRegister(javax.sip.ResponseEvent)
	 */
	public void processRegister(ResponseEvent e) {
		//logger.debug(e.getResponse());
	}

	
	public static void main(String[] args) {
				
		logger.info("Client Started");
		
		SIPUserAgentClient sipUAClient = null;//= new SIPUserAgentClient("127.0.0.1");
//		sipUAClient.setPort(5067);

		TestClient2 test = new TestClient2();
		
		try {
            sipUAClient.start();
            sipUAClient.connect(new SIPIMListener());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
//
//		try {
//			sipUAClient.sendMessage("sip:jxme@localhost","HELLO");
//			Thread.sleep(10000);
//			
//		}
//		catch  (Exception e){
//			e.printStackTrace();
//		}
	
//		sipUAClient.unregister();
		
//		sipUAClient.getSIPInviteClient().invite("sip:franz@localhost");
	
//		UserList users = new UserList();
//		users.getFromFile("bundles/im-client/src/buddies.xml");
		
//		logger.debug(((User)buddies.iterator().next()).getName());
//		logger.debug((((User)buddies.iterator().next()).getSIPAddresses().iterator().next()).toString());
		
//		logger.debug(users.getUsers().iterator().next());
		
//		logger.debug(users);
	}
}
