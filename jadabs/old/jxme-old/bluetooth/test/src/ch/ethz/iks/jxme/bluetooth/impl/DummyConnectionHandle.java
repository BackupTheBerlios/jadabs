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

/**
 * @author Daniel Kaeppeli, jdan[at]kaeppe.li
 *  
 */
public class DummyConnectionHandle implements IConnectionHandle {

	private static int idCounter = 0;
	private static int ridCounter = 0;
	
	private String remoteBTAddress = null;
	private String identifer = null;

	private boolean isMaster = false;

	public DummyConnectionHandle(boolean isMaster) {
		this.isMaster = isMaster;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#getConnection()
	 */
	public StreamConnection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#getIdentifier()
	 */
	public String getIdentifier() {
		if(identifer == null){
			identifer = getIdentifierString();
		}
		return identifer;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#openOutputStream()
	 */
	public OutputStream openOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#openInputStream()
	 */
	public InputStream openInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#getRemoteBTAddress()
	 */
	public String getRemoteBTAddress() {
		if(remoteBTAddress == null){
			remoteBTAddress = getRemoteBTAddressString();
		}
		return remoteBTAddress;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#getLocalBTAddress()
	 */
	public String getLocalBTAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#close()
	 */
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#getRemoteDevice()
	 */
	public RemoteDevice getRemoteDevice() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#isMaster()
	 */
	public boolean isMaster() {
		return isMaster;
	}

	protected String getIdentifierString(){
		return "id" + idCounter++;
	}
	
	protected String getRemoteBTAddressString(){
		return "remoteId" + ridCounter++;
	}

}