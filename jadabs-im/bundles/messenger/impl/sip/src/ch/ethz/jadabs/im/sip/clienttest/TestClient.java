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
public class TestClient 
{

    private Logger LOG = Logger.getLogger(TestClient.class);
    
	static Logger logger = Logger.getLogger(TestClient.class);
	/* (non-Javadoc)
	 * @see SIPUserAgentListener#processRegister(javax.sip.ResponseEvent)
	 */
	public void processRegister(ResponseEvent e) {
		//logger.debug(e.getResponse());
	}

	
	public static void main(String[] args) {
				
		logger.info("Client Started");
		
		SIPUserAgentClient sipUAClient = null; //= new SIPUserAgentClient("/home/franz/workspace/jadabs-im/bundles/messenger/sip/src/ch/ethz/jadabs/im/sip/clienttest/config.conf");

		TestClient test = new TestClient();
		
		try {
            sipUAClient.start();
    		sipUAClient.connect(new SIPIMListener());
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

		
		try {
			Thread.sleep(10000);
		}
		catch  (Exception e){
			e.printStackTrace();
		}
		
//		sipUAClient.getSIPPublishClient().sendPublish("online");
		
		
		try {
			Thread.sleep(10000);
		}
		catch  (Exception e){
			e.printStackTrace();
		}
//		
//		sipUAClient.getSIPSubscribeClient().sendSubscribe(sipUAClient.getLocalURI(),"sip:chris@localhost", 10000);
		
	
		
//		sipUAClient.unregister();
		
	//	sipUAClient.getSIPInviteClient().invite("sip:chris@127.0.0.1:5066");
	
//		UserList users = new UserList();
//		users.getFromFile("bundles/im-client/src/buddies.xml");
		
//		logger.debug(((User)buddies.iterator().next()).getName());
//		logger.debug((((User)buddies.iterator().next()).getSIPAddresses().iterator().next()).toString());
		
//		logger.debug(users.getUsers().iterator().next());
		
//		logger.debug(users);
	}

}
