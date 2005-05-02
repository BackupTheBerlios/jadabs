/*
 * Created on Jul 22, 2004
 * $Id: BTTransport.java,v 1.4 2005/05/02 06:28:08 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.Transport;

/**
 * BTTransport implements the Transport functionaly for the Bluetooth-Connection
 * from a CLDC-1.0/MIDP-2.0 Mobile Device. It uses a BT-RFCOMM commlink that is
 * established *to* the Mobile Device from a Rendez-Vous Peer.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class BTTransport implements Transport
{

    /** UUID of the JxmeBtServer */
    static final UUID JXME_BT_UUID = new UUID("41d53d8182c04f0e8e5cc52cae0415c3", false);

    /** Name of this service to be written into SDDB */
    private static final String SERVICE_NAME = "JxmeBtServer";

    /**
     * Protocol Service UUID code for PublicBrowseGroup (see Bluetooth Assigned
     * Numbers document
     * https://www.bluetooth.org/foundry/assignnumb/document/service_discovery)
     */
    private static final UUID PUBLIC_BROWSE_GROUP = new UUID(0x1002);

    /**
     * Service Attribute identifier code for BrowseGroupList (see Bluetooth
     * Assigned Numbers document
     * https://www.bluetooth.org/foundry/assignnumb/document/service_discovery)
     */
    private static final int BROWSE_GROUP_LIST = 0x0005;

    /** log4j logger for BTTransport */
    private static Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.bt.BTTransport");

    /** remove connection from connection list after backoffconnection times */
    private static int backoffconnection = 3;
    
    /** EndpointService that gets notified when a new message arrives */
    private Listener listener;

    /** Endpoint URL of this interface */
    private EndpointAddress myEndpointURL;

    /** local BT device */
    private LocalDevice localDevice;

    /** reference to DiscoveryAgent that is used */
    private DiscoveryAgent agent;

    /** BT-Server socket */
    private StreamConnectionNotifier service;

    /** true if Bluetooth connection needs to be authenticated */
    private boolean authenticate = false;

    /** only if authenticated: true if Bluetooth connection is encrypted */
    private boolean encrypt = false;

    /**
     * only if authenticated: true if Bluetooth connection requires
     * authorization by the user.
     */
    private boolean authorize = false;

    /** URL of BT service */
    private String serviceURL;

    private boolean openStreamConnectionNotifier;

    /**
     * Hashtable containing client connections to this BT Service. It stores
     * EndpointAddress instances as keys and BTConnnection instances as values.
     */
    Hashtable connections = new Hashtable();
    
    
    Hashtable errorConnections = new Hashtable();

    /** connection listener that accepts new connection */
    private ConnectionListener connectionListener;

    /**
     * Constructor of BTTransport.
     * 
     * @param startService
     *            If set to true this host sets up the service. It is thus
     *            becomes a slave peer that rendez-vous peers can connect to. If
     *            false no service entry is created, this host thus is a
     *            rendez-vous peer that can connect to other non-rendezvous
     *            peers while taking the Bluetooth Master role
     *  
     */
    public BTTransport(boolean startService)
    {
        openStreamConnectionNotifier = startService;
        if (LOG.isDebugEnabled())
        {
            if (startService)
            {
                LOG.debug("starting BTTransport as a PEER");
            } else
            {
                LOG.debug("starting BTTransport as a RENDEZ-VOUS PEER");
            }
        }
    }

    /**
     * Initialize Transport-Layer
     * 
     * @param myURL
     *            Endpoint-Address of this BT-Interface (argument is ignored,
     *            use <code>null</code> instead, it will be automatically
     *            determined on the BT-MAC address of the BT-Interface within
     *            this method)
     * @param listener
     *            EvenpointService that will be notified if a new message is
     *            received.
     * @see ch.ethz.jadabs.jxme.Transport#init(ch.ethz.jadabs.jxme.EndpointAddress,
     *      ch.ethz.jadabs.jxme.Listener)
     */
    public void init(EndpointAddress myURL, Listener listener) throws IOException
    {
        this.listener = listener;
        localDevice = LocalDevice.getLocalDevice();

        // Set port number =0 since we do not know how to handle portnumbers in
        // BT
        myEndpointURL = new EndpointAddress("btspp", localDevice.getBluetoothAddress());

        agent = localDevice.getDiscoveryAgent();
        serviceURL = "btspp://localhost:" + JXME_BT_UUID.toString() + ";authenticate=" + authenticate + ";authorize="
                + authorize + ";encrypt=" + encrypt + ";name=" + SERVICE_NAME;

        // Set this BT device to discoverable
        try
        {
            localDevice.setDiscoverable(DiscoveryAgent.GIAC);
        } catch (BluetoothStateException e)
        {
            LOG.error("cannot set device to GIAC discoverable mode.");
            e.printStackTrace();
        }

        if (openStreamConnectionNotifier)
        {
            // start listening to connections
            connectionListener = new ConnectionListener();
            Thread connectionListenerThread = new Thread(connectionListener);
            connectionListenerThread.start();
        }
    }

    /**
     * Send specified message to the destination.
     * 
     * @param message
     *            message to be sent
     * @param destURI
     *            destination the message has to be sent to
     * @throws IOException
     *             if the specified destination is not listed in the connection
     *             table or if the underlying <code>OutputStream</code> throws
     *             a corresponding exception.
     * @see ch.ethz.jadabs.jxme.Transport#send(ch.ethz.jadabs.jxme.Message,
     *      ch.ethz.jadabs.jxme.EndpointAddress)
     */
    public void send(Message message, EndpointAddress destURI) throws IOException
    {

        if (!connections.containsKey(destURI.getHost()))
        { 
            throw new IOException("Destionation " + destURI.getHost()
                + " not in connections table."); 
        }
        BTConnection connection = (BTConnection) connections.get(destURI.getHost());

        // translate message into byte array
        ByteArrayOutputStream bout = new ByteArrayOutputStream(message.getSize());
        DataOutputStream dout = new DataOutputStream(bout);
        message.write(dout);

        // This call does not work on both J2ME and J2SE.
        // - Using the J2ME-Classes the compiler resolves the call to close()
        //   to DataOutputStream.close() in Bytecode
        // - Using the J2SE-Classes the compiler resolves the call to close()
        //   to FilterOutputStream.close() in Bytecode
        //  Consequence: If compiled for one it will not run on the other
        //    --> we leave that out
        //dout.close();
        bout.close();

        // send byte array
        try {
            connection.sendMessage(bout.toByteArray(), connection.getRemoteBTAddress(), false);
        } catch(IOException ioe)
        {
            handleErrorInConnection(connection);
            throw ioe;
        }
    }

    /**
     * 
     * @param message
     * @throws IOException
     * @see ch.ethz.jadabs.jxme.Transport#propagate(ch.ethz.jadabs.jxme.Message)
     */
    public void propagate(Message message) throws IOException
    {

        // check if there is at least one connection
        if (connections.size() == 0)
        {
            // there are not connections thus the message cannot be propagated

            throw new IOException("no connections found to propagate message");
        }

        // translate message into byte array
        ByteArrayOutputStream bout = new ByteArrayOutputStream(message.getSize());
        DataOutputStream dout = new DataOutputStream(bout);
        message.write(dout);

        // This call does not work on both J2ME and J2SE.
        // - Using the J2ME-Classes the compiler resolves the call to close()
        //   to DataOutputStream.close() in Bytecode
        // - Using the J2SE-Classes the compiler resolves the call to close()
        //   to FilterOutputStream.close() in Bytecode
        //  Consequence: If compiled for one it will not run on the other
        //    --> we leave that out
        //dout.close();
        bout.close();
        byte[] messagebuffer = bout.toByteArray();

        Enumeration econs = connections.elements();
        boolean rememberException = false;
        while (econs.hasMoreElements())
        {
            BTConnection connection = (BTConnection) econs.nextElement();
            try
            {
                connection.sendMessage(messagebuffer, connection.getRemoteBTAddress(), true);
            } catch (IOException e)
            {
                handleErrorInConnection(connection);
                // we catch IO Exception here because propage would
                // fail if only one client connection is broken.
                // All nodes that would be address after the broken
                // link would not get the message. Thus we catch
                // the exception and continue until we are through
                rememberException = true;
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("cannot forward multicast message to " + connection.getRemoteBTAddress());
                }
            }
        }
        if (rememberException) { throw new IOException("The message could not be forwarded to "
                + "at least one client."); }
    }

    private void handleErrorInConnection(BTConnection btc)
    {        
        if (!errorConnections.containsKey(btc))
            errorConnections.put(btc, new Integer(1));
        else
        {
            int value = ((Integer)errorConnections.get(btc)).intValue();
            
            if (value >= backoffconnection)
            {
                connections.remove(btc.getRemoteBTAddress());
                errorConnections.remove(btc);
            }
            else
                errorConnections.put(btc, new Integer(value+1));
        }
    }
    
    /**
     * Closes Service and all client connection that currently exist.
     * 
     * @see ch.ethz.jadabs.jxme.Transport#stop()
     */
    public void stop()
    {
        connectionListener.close();
    }

    /**
     * Return the EndpointAddress of this BT-Interface
     * 
     * @return EndointAddress of this BT-Interface
     * @see ch.ethz.jadabs.jxme.Transport#getEndpointAddress()
     */
    public EndpointAddress getEndpointAddress()
    {
        return myEndpointURL;
    }

    /**
     * Callback from BTConnection that informs BTtransport that a new message
     * has arrived that should be delivered to the upper layers.
     * 
     * @param receiver
     *            BT device address of receiver (i.e. this host)
     * @param sender
     *            BT device address of sender
     * @param message
     *            byte array that contains the message itself
     * @param multicast
     *            flag indiciating if this is a broadcast message and has to be
     *            relayed further.
     */
    void deliverMessage(String receiver, String sender, byte[] message, boolean multicast)
    {
        LOG.debug("deliverMessage()");

        ByteArrayInputStream bin = new ByteArrayInputStream(message);
        DataInputStream din = new DataInputStream(bin);
        Message msg = null;
        try
        {
            msg = Message.read(din);
        } catch (IOException e)
        {
            // a byte array input stream never throws a IOException
        }

        LOG.debug("  JXME-Message deserialized from BT stream");

        // propagage multicast message
        if (multicast)
        {
            Enumeration econs = connections.elements();
            while (econs.hasMoreElements())
            {
                BTConnection connection = (BTConnection) econs.nextElement();
                String destination = connection.getRemoteBTAddress();
                if (!destination.equals(sender))
                {
                    // only forward message to links other than the
                    // originating host
                    try
                    {
                        connection.sendMessage(message, destination, true);
                    } catch (IOException e)
                    {
                        LOG.error("Cannot forward multicast message to " + destination + ": " + e.getMessage());
                    }
                }
            }
        }

        LOG.debug("handleMessage(msg,null)");
        // deliver message to higher layer
        listener.handleMessage(msg, null);

    }

    /**
     * Adds connection to the connections table registered under remoteBTAddress
     * (Note: this method does *not* start the connection worker)
     * 
     * @param connection
     *            refernence to the object that encapsulates this connection
     */
    public void addConnection(BTConnection connection)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("adding connection to " + connection.getRemoteBTAddress() + " into pool.");
        }
        connections.put(connection.getRemoteBTAddress(), connection);
    }

    /**
     * Checks if a connection to device with the specified Bluetooth address
     * already exists in the connection pool
     * 
     * @param btAddress
     *            12-Byte hex-String Bluetooth Address
     * @return true of the connection to this device already exists
     */
    public boolean containsConnectionTo(String btAddress)
    {
        return connections.containsKey(btAddress);
    }

    /**
     * Callback from BTConnection where BTTransport is informed that a
     * connection to a BT client was closed
     * 
     * @param closedConn
     *            reference to the closed connection
     */
    void connectionClosed(BTConnection closedConn)
    {
        // removed connection just closed from connections table
        connections.remove(closedConn.getRemoteAddress());
    }

    /**
     * This class implements the read that accepts new connections to clients
     * (i.e. Rendez-Vous Peers).
     * 
     * @author Ren&eacute; M&uuml;ller
     */
    class ConnectionListener implements Runnable
    {

        /** true of the connection is about to be aborted */
        private boolean aborting;

        /**
         * Run body of ConnectionListenerThread
         * 
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            aborting = false;
            try
            {
                // open service
                service = (StreamConnectionNotifier) Connector.open(serviceURL);

                // Add the service to the 'Public Browse Group'
                ServiceRecord rec = localDevice.getRecord(service);
                DataElement element = new DataElement(DataElement.DATSEQ);
                element.addElement(new DataElement(DataElement.UUID, PUBLIC_BROWSE_GROUP));
                rec.setAttributeValue(BROWSE_GROUP_LIST, element);
            } catch (IOException e)
            {
                LOG.fatal("Cannot open StreamConnectionNotifier: " + e.getMessage());
                e.printStackTrace();
            }
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Created Service " + serviceURL);
            }

            while (!aborting)
            {
                try
                {
                    LOG.info("Waiting for connections.");

                    // This method call blocks until a client opens a
                    // StreamConnection to this service.
                    // Important: As long as a thread is within acceptAndOpen
                    // the service record of the corresponding service is
                    // presend into the SDDB. Howoever it vanishes as soon as
                    // a client connects and this method returns. It thus
                    // has to make sure that this thread reenters acceptAndOpen
                    // as soon as possible.
                    StreamConnection conn = service.acceptAndOpen();

                    // add connection and dispatch worker on that connection
                    BTConnection connection = new BTConnection(conn, RemoteDevice.getRemoteDevice(conn),
                            BTTransport.this);

                    LOG.debug("added connection: " + connection.getRemoteAddress().getHost());

                    connections.put(connection.getRemoteAddress().getHost(), connection);
                    connection.startWorker();

                } catch (IOException e)
                {
                    if (!aborting)
                    {
                        LOG.error("Cannot open connection: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * Close StreamConnectionNotifier (i.e. ServerSocket)
         */
        public void close()
        {
            if (aborting)
            {
                // do nothing if we are already aborting...
                return;
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("closing service.");
            }

            // This is kind of a hack, since we hope that closing the
            // stream connection listener wakes up the blocked thread
            // waiting on acceptAndOpen().
            // Howoever the Guys from Nokia recommend this method in
            // "Introduction To Developing Networked MIDlets Using
            // Bluetooth".
            synchronized (this)
            {
                aborting = true;
            }
            try
            {
                service.close();
            } catch (IOException e)
            {
                // hmm, there is nothing we can do about that here
            }

            // closing still open connections
            Enumeration econs = connections.elements();
            while (econs.hasMoreElements())
            {
                BTConnection connection = (BTConnection) econs.nextElement();
                connection.close();
            }
        }
    }
}