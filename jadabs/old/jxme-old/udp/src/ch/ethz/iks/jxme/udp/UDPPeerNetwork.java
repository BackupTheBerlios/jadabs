/*
 * Created on Jul 31, 2003
 * 
 * $Id: UDPPeerNetwork.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jxme.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import ch.ethz.iks.concurrent.LinkedQueue;
import ch.ethz.iks.jxme.IElement;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IPeerNetwork;
import ch.ethz.iks.jxme.impl.Element;
import ch.ethz.iks.jxme.impl.ElementParseException;
import ch.ethz.iks.jxme.impl.Message;
import ch.ethz.iks.jxme.impl.MessageParseException;
import ch.ethz.iks.jxme.impl.PeerNetwork;

/**
 * UDPConnection implements the PeerConnection. By using UDP MulticastSocket
 * DatagramPackets are sent multicast to all listening nodes.
 * 
 * @author andfrei
 */
public class UDPPeerNetwork extends PeerNetwork
{

    private static Logger LOG = Logger.getLogger(UDPPeerNetwork.class);
    {
        LOG.debug("ch.ethz.iks.jxme.udp.UDPPeerNetwork initialized");
    }

    /** UDPPeerNetwork designed as a singleton. */
    private static UDPPeerNetwork udpnet;

    private byte TTL = (byte) 1;

    private InetAddress group = null;

    private int portrec;

    private int portsend;

    private MulticastSocket mcreceiver = null;

    private MulticastSocket mcsender = null;

    /* connection arguments */
    private String type;

    private String mcastadr = "224.0.0.1";

    private String mcastport = "4160";

    private String peername;

    LinkedQueue indpQ = new LinkedQueue();

    LinkedQueue outdpQ = new LinkedQueue();

    private PnetThread udpPnetThread;

    private InPacketThread inpacketThread;

    private OutPacketThread outpacketThread;

    private UDPPeerStreaming udpstream;

    private UDPPeerNetwork()
    {
        udpstream = new UDPPeerStreaming(this);
        peername = (String)Activator.bc.getProperty(
                IPeerNetwork.JXME_PEERNAME);
    }

    public static UDPPeerNetwork Instance()
    {
        if (udpnet == null) 
            udpnet = new UDPPeerNetwork();
        
        return udpnet;
    }

    public void setPeername(String peername)
    {
        this.peername = peername;
    }

    /**
     * Create is used here to create a multicast connection, similar to
     * propagation pipe.
     * 
     * @param type
     *            used for PIPE
     * @param name
     *            multicast address
     * @param arg
     *            multicast port
     *  
     */
    public int create(String type, String name, String arg)
    {

        if (LOG.isDebugEnabled()) LOG.debug("created new UDPPeerNetwork");

        this.type = type;
        this.mcastadr = name;
        this.mcastport = arg;

        return 0;
    }

    /**
     * Send a String to the multicast address.
     * 
     * @param id
     *            the peer or pipe id to which data is to be sent.
     * @param data
     *            a {@link IMessage}containing an array of {@link IElement}s
     *            which contain application data that is to be sent
     * 
     * @return query id that can be used to match responses, if any
     * 
     * @throws IOException
     *             if there is a problem in sending
     * @throws IllegalArgumentException
     *             if id is null
     */
    public void send(String id, IMessage msg) throws IOException
    {
        // set peername to indicate sender, this allows to have multiple UDP
        // instances on the same machine
        Element el = new Element(peername.getBytes(), Message.SENDER);
        msg.setElement(el);

        // create bytestream, should be checked for its length as a
        // datagrampacket has only 8kb
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        msg.writeMessage(out);
        out.flush();
        byte[] bytedata = out.toByteArray();
        out.close();

        if (bytedata.length >= 8000)
        {
            udpstream.sendData(msg);

            if (LOG.isDebugEnabled())
                    LOG.debug("message bigger then 8kB, stream the message over udp");
        } else
        {

            Element strtag = new Element(FileSplitter.FS_STRTAG, 0);
            msg.setElement(strtag);

            // create bytestream, should be checked for its length as a
            // datagrampacket has only 8kb
            out = new ByteArrayOutputStream();
            msg.writeMessage(out);
            out.flush();
            bytedata = out.toByteArray();
            out.close();

            DatagramPacket dp = new DatagramPacket(bytedata, bytedata.length,
                    group, portrec);

            // put it into the outsender queue
            try
            {
                outdpQ.put(dp);
            } catch (InterruptedException ie)
            {
                LOG.error("LinkedQueue reported interrupted exception", ie);
            }

            //			if (LOG.isDebugEnabled()){
            //				LOG.debug("sent message: " + msg.toXMLString());
            //			}
        }

    }

    /**
     * Send a String to the multicast address.
     * 
     * @param id
     *            the peer or pipe id to which data is to be sent.
     * @param data
     *            a {@link IMessage}containing an array of {@link IElement}s
     *            which contain application data that is to be sent
     * @param datagramsize
     *            set this to true if msg has already been checked for less than
     *            8kB
     * 
     * @return query id that can be used to match responses, if any
     * 
     * @throws IOException
     *             if there is a problem in sending
     * @throws IllegalArgumentException
     *             if id is null
     */
    protected int sendCheckedMsg(String id, IMessage msg) throws IOException,
            IllegalArgumentException
    {
        // set peername to indicate sender, this allows to have multiple UDP
        // instances on the same machine
        Element el = new Element(peername.getBytes(), Message.SENDER);
        msg.setElement(el);

        // create bytestream, should be checked for its length as a
        // datagrampacket has only 8kb
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        msg.writeMessage(out);
        out.flush();
        byte[] bytedata = out.toByteArray();
        out.close();

        DatagramPacket dp = new DatagramPacket(bytedata, bytedata.length,
                group, portrec);

        // put it into the outsender queue
        try
        {
            outdpQ.put(dp);
        } catch (InterruptedException ie)
        {
            LOG.error("LinkedQueue reported interrupted exception", ie);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("sent message: " + msg.toXMLString());
        }

        return -1;
    }

    /**
     * Connect this peer to a MulticastAddress.
     * 
     * @param mcastadr
     * @param mcastport
     * @return
     */
    private void openConnection() throws IOException
    {

        try
        {
            group = InetAddress.getByName(mcastadr);
            portrec = Integer.parseInt(mcastport);

            // receiver
            mcreceiver = new MulticastSocket(portrec);
            mcreceiver.setTimeToLive(TTL);
            mcreceiver.joinGroup(group);

            // sender
            portsend = portrec + 1;
            mcsender = new MulticastSocket(portsend);
            mcsender.setTimeToLive(TTL);
            mcsender.joinGroup(group);

            LOG.info("connection startet on multicast address, receiver: "
                    + group + ":" + mcastport);
            LOG.info("connection startet on multicast address, sender: "
                    + group + ":" + portsend);

        } catch (UnknownHostException e)
        {
            LOG.info("Could not get InetAddress out of mulitcastaddr", e);
            throw new IOException(
                    "could not create InetAddress with given name: " + mcastadr);
        }

    }

    public void connect() throws IOException
    {
        // open the connection with the values specified by create(...)
        openConnection();

        // create now the main thread
        udpPnetThread = new PnetThread();
        udpPnetThread.setName("udpPnetThread");
        udpPnetThread.start();

        inpacketThread = new InPacketThread(mcreceiver, indpQ);
        inpacketThread.setName("inpacketThread");
        inpacketThread.start();

        outpacketThread = new OutPacketThread(mcsender, outdpQ);
        outpacketThread.setName("outpacketThread");
        outpacketThread.start();

    }

    public int close(String name, String id, String type)
    {
        // stop referenced threads
        udpstream.stop();

        // stop all local Threads
        udpPnetThread.receiveOK = false;
        udpPnetThread.interrupt();
        udpPnetThread = null;

        inpacketThread.stopThread();
        inpacketThread.interrupt();
        inpacketThread = null;

        outpacketThread.stopThread();
        outpacketThread.interrupt();
        outpacketThread = null;

        // closes the connection and leave the multicast group.
        try
        {
            if (mcreceiver != null)
            {
                mcreceiver.leaveGroup(group);
                mcreceiver.close();
                mcreceiver = null;
            }
            if (mcsender != null)
            {
                mcsender.leaveGroup(group);
                mcsender.close();
                mcsender = null;
            }
        } catch (IOException e)
        {
        }

        return 1;
    }

    public void finalize()
    {
        LOG.debug("called finalize on UDPPeerNetwork");
    }

    /**
     * Listens for new incoming Datagram Packets, which have been received by
     * the <code>InPacketThread</code> and put into the <code>indpQ</code>.
     *  
     */
    class PnetThread extends Thread
    {

        private boolean receiveOK = true;

        /**
         * UDPConnection runs as a Thread and receives DatagramPackets.
         */
        public void run()
        {

            while (receiveOK)
            {

                try
                {

                    //					DatagramPacket dp = (DatagramPacket)indpQ.take();

                    // check if this peer sent the packet
                    //					if (LOG.isDebugEnabled())
                    //						LOG.debug("inetAdr: " + dp.getAddress());

                    byte[] data = (byte[]) indpQ.take();

                    ByteArrayInputStream in = new ByteArrayInputStream(data);
                    IMessage msg = Message.read(in);
                    in.reset();
                    in.close();
                    in = null;

                    // get Sender name
                    String remotePeerName = Message.getElementString(msg,
                            Message.SENDER);

                    if (LOG.isDebugEnabled())
                            LOG.debug("remotePeerName: " + remotePeerName
                                    + "; peername: " + peername);

                    if (remotePeerName.equals(peername)) continue;
                    //						processMessage(msg);

                    int streamtag = Message.getElementInt(msg,
                            FileSplitter.FS_STRTAG);

                    if (streamtag == 1)
                    {
                        if (LOG.isInfoEnabled())
                                LOG.info("received stream message: "
                                        + remotePeerName);
                        udpstream.receiveMessage(msg);
                    } else
                        processMessage(msg); // call here directly the instance
                                             // of JXMECop

                } catch (InterruptedException ie)
                {
                    LOG.warn("linkedQueue reported an interrupted Exception");
                } catch (IOException ioe)
                {
                    LOG.warn("could not receive DatagramPacket");
                } catch (MessageParseException mpe)
                {
                    LOG.warn("Message has wrong syntax");
                } catch (ElementParseException epe)
                {
                    LOG.warn("Element has wrong syntax");
                }
            }
        }

        public void finalize()
        {
            LOG.debug("called finalize on PnetThread");
        }

    } // end PnetThread

}