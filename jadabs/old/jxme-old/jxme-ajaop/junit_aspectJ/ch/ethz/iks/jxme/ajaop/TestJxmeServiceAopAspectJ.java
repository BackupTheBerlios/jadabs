/*
 * Created on Dec 1, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.ethz.iks.jxme.ajaop;

import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ch.ethz.iks.jxme.*; 
import ch.ethz.iks.jxme.bluetooth.*;

/**
 * @author daniel
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestJxmeServiceAopAspectJ extends TestCase {

	private static Logger LOG = Logger.getLogger(TestJxmeServiceAopAspectJ.class);

	private JxmeService jxmeService = JxmeService.Instance();
	private BluetoothService btcop = BluetoothService.Instance();

	public TestJxmeServiceAopAspectJ(){
	}

	public void setUp(){
	}
	
	public void tearDown(){
		return;
	}

	public void testCreation(){
		LOG.info("JUNIT: testeCreation");
		assertTrue(true);
	}
	
	public void testInit(){
		LOG.info("JUNIT: init BluetoothComponent");
		assertTrue(true);
	}

	public void testStart(){
		jxmeService.initComponent();
		jxmeService.startComponent(null);
		jxmeService.addMessageListener(new TestListener());
		if(LOG.isDebugEnabled()){
			LOG.debug("adding IMessageListener to JxmeService");
		}
		
		LOG.info("JUNIT: init BluetoothComponent");
		btcop.initComponent();
		btcop.startComponent(null);
		assertTrue(true);
		if( btcop.isRendezVousPeer()){
			while(btcop.numberOfConnections()==0){		
				synchronized(this){
					try {
						this.wait(1000);
					} catch (InterruptedException e) {
						LOG.error("interrupted during waiting for incomming messages", e);
					}
				}
			}
			IElement[] elements = new Element[1];
			elements[0] = new Element("test", "test");
			IMessage msg = new Message(elements);
			
			try{
				jxmeService.send(null, msg);
				if(LOG.isDebugEnabled()){
					LOG.debug("============> sent message");
				}
			} catch(IOException e){
				LOG.fatal("IOException during sending message: ", e);
			} catch(NoPeerAvailableException e){
				LOG.fatal("NoPeerAvailableException during sending message: ", e);
			}
		} else {
			synchronized(this){
				try {
					this.wait(30000);
				} catch (InterruptedException e) {
					LOG.error("interrupted during waiting for incomming messages", e);
				}	
			}	
		}
		synchronized(this){
			try {
				this.wait(3000);
			} catch (InterruptedException e) {
				LOG.error("interrupted during waiting for incomming messages", e);
			}	
		}	
	}

	class TestListener implements IMessageListener{
	
		public void processMessage(IMessage msg){
			LOG.info("===>\t\tGOT MESSAGE:\n" + msg.toXMLString());
		}
	}

}
