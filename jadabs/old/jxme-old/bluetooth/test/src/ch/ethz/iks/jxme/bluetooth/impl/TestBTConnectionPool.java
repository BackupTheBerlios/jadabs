/*
 * Created on 18.02.2004, ETH Zurich
 * 
 */
package ch.ethz.iks.jxme.bluetooth.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

import ch.ethz.iks.jxme.bluetooth.IConnectionHandle;
import ch.ethz.iks.jxme.bluetooth.IConnectionPool;
import junit.framework.TestCase;

/**
 * @author Daniel Kaeppeli, jdan[at]kaeppe.li
 *
 */
public class TestBTConnectionPool extends TestCase {

	private IConnectionPool pool = BTConnectionPool.getConnectionPool();
	
	// must be an even number!
	private static final int NUMBER_OF_CONNECTIONS = 2*50;
	
	public void testCreation(){
		assertNotNull(pool);
	}
	
	public void testAddConnectionMaster(){
		for(int i = 0; i < NUMBER_OF_CONNECTIONS; i++){
			pool.addConnection(new DummyConnectionHandle(true));
		}
		
		assertEquals("Number of connections", NUMBER_OF_CONNECTIONS, pool.size());
		assertEquals("masters", IConnectionPool.MASTER, pool.getRole());
	}
	
	public void testAddConnectionMixed(){
		boolean flag = true;
		for(int i = 0; i < NUMBER_OF_CONNECTIONS; i++){
			pool.addConnection(new DummyConnectionHandle(flag));
			flag = !flag;
		}
		
		assertEquals("Number of connections", 2 * NUMBER_OF_CONNECTIONS, pool.size());
		assertEquals("masters", IConnectionPool.MASTER_AND_SLAVE, pool.getRole());
	}
	

	
}
