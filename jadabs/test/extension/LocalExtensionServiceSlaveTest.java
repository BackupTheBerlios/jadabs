/*
 * Created on Jul 8, 2003
 *
 */
package ch.ethz.iks.extension;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ch.ethz.iks.bootstrap.DefaultBootStrap;

/**
 * @author andfrei
 */
public class LocalExtensionServiceSlaveTest extends TestCase {

	private static Logger LOG = Logger.getLogger(LocalExtensionServiceSlaveTest.class);
	{
		PropertyConfigurator.configure("./log4j.properties");
	}
	
	private static DefaultBootStrap bootstrap;

	/**
	 * Constructor for LocalExtensionServiceTest.
	 * @param arg0
	 */
	public LocalExtensionServiceSlaveTest(String arg0) {
		super(arg0);
		
		String[] args = new String[10];
		args[0] = "-name";
		args[1] = "peer2";
		
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
	public void testInsertExtensionResourceVector() {
				
		
		DefaultBootStrap.remoteWait();
	}

}
