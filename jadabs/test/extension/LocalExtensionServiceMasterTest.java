/*
 * Created on Jul 8, 2003
 *
 */
package ch.ethz.iks.extension;

import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ch.ethz.iks.bootstrap.DefaultBootStrap;
import ch.ethz.iks.cop.ComponentRepository;
import ch.ethz.iks.cop.ComponentResource;
import ch.ethz.iks.cop.IComponentRepository;
import ch.ethz.iks.cop.eventsystem.ExtensionFactory;
import ch.ethz.iks.cop.eventsystem.PeerListAdvertisement;
import ch.ethz.iks.id.IDFactory;

/**
 * @author andfrei
 */
public class LocalExtensionServiceMasterTest extends TestCase {

	private static Logger LOG = Logger.getLogger(LocalExtensionServiceMasterTest.class);

	{
		BasicConfigurator.configure();
//		PropertyConfigurator.configure("/home/andfrei/workspace/jadabs/log4j.properties");
	}
	
	private static DefaultBootStrap bootstrap;

	/**
	 * Constructor for LocalExtensionServiceTest.
	 * @param arg0
	 */
	public LocalExtensionServiceMasterTest(String arg0) {
		super(arg0);
		
		String[] args = new String[2];
		args[0] = "-name";
		args[1] = "peer1";
		
		DefaultBootStrap.main(args);
		
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test for void insert(ExtensionResource, Vector)
	 */
	public void testInsertExtension() {
		
		IComponentRepository extSvc = ExtensionFactory.Instance().getExtensionService();
		
		String codebase = "udpext.jar";
		String classname = "ch.ethz.iks.jxme.udp.aop.UDPPeerNetworkPE";
		
		String extid = IDFactory.Instance().newExtensionID(classname);
		ComponentResource extRes = (ComponentResource) ComponentRepository.Instance().createResource(extid, codebase, classname);
		
		Vector peers = new Vector();
//		peers.add("ikpaq3");
		peers.add("peer2");

		extSvc.insert(extRes, new PeerListAdvertisement(peers));
//		extSvc.insert(extRes);
		
//		extSvc.withdraw(extRes);
//		extSvc.withdraw(extRes, new PeerListAdvertisement(peers));
		
		DefaultBootStrap.remoteWait();
	}

//	public void testWithdrawExtFrom() {
//	}

}
