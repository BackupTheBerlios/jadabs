/*
 * Created on Nov 14, 2003
 * 
 * $Id: TCPPeerNetwork.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jxme.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.concurrent.LinkedQueue;
import ch.ethz.iks.jxme.IElement;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.impl.Element;
import ch.ethz.iks.jxme.impl.ElementParseException;
import ch.ethz.iks.jxme.impl.Message;
import ch.ethz.iks.jxme.impl.MessageParseException;
import ch.ethz.iks.jxme.impl.PeerNetwork;

/**
 * <code>TCPPeerNetwork</code> represents a server which can receive messages
 * on a specific port. To limit the number of generated incomming connections a
 * pool mechanism is used whereas on the startup of the TCPPeerNetwork it can be
 * decided how many thread should be allowed.
 * 
 * Following threads are started: -<code>MainServer</code> thread to accept
 * new connection, -<code>SocketThread</code> depends on the requested
 * number, initialy one, -<code>InMessageHandler</code> processes the
 * incomming message -<code>TCPPeerNetwork</code> thread is responsible for
 * the outgoing messages
 * 
 * @author andfrei
 * @version CVS $Revision: 1.1 $
 */
public class TCPPeerNetwork extends PeerNetwork
{

    private static Logger LOG = Logger.getLogger(TCPPeerNetwork.class.getName());

    private String peername;

    LinkedQueue indpQ = new LinkedQueue();

    LinkedQueue outdpQ = new LinkedQueue();

    private Thread pnetThread;

    private LinkedQueue socketPool = new LinkedQueue();

    // MainServer to listen for incomming connection
    private MainServer mainServer;

    // number of Server threads, how many sockets can be open at the same time.
    private int nofServerThreads = 1;

    private Vector socketThreads = new Vector();

    // InMessage Handler
    private InMessageHandler inMsgHandler;

    // OutMessage Handler
    private OutMessageHandler outMsgHandler;

    // out, in message queue
    private LinkedQueue inMsgQ = new LinkedQueue();

    private LinkedQueue outMsgQ = new LinkedQueue();

    private boolean first = true;

    /** Default port to start the server */
    public static int defaultport = 4162;

    public static final String elementTCPSender = "SenderTCPAddress";

    public TCPPeerNetwork(String peername)
    {

        super();
        this.peername = peername;
    }

    /**
     * @param peername
     * @param nofst
     */
    public TCPPeerNetwork(String peername, int nofst)
    {

        this(peername);
        this.nofServerThreads = nofst;
    }

    /**
     * Create a server socket to listen to incomming connections, use connect to
     * start listening.
     * 
     * @param type
     *            not used
     * @param name
     *            not used
     * @param arg
     *            port where the server is listening
     * 
     * @return 0 if could be started, -1 otherwise
     */
    public int create(String type, String name, String arg)
    {

        if (arg != null) defaultport = Integer.parseInt(arg);

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
    private void send(String address, int port, IMessage msg)
            throws IOException
    {

        // set peername to indicate sender, this allows to have multiple UDP
        // instances on the same machine
        Element el = new Element(peername.getBytes(), Message.SENDER);
        msg.setElement(el);

        try
        {
            outMsgQ.put(new OutMessage(address, port, msg));
        } catch (InterruptedException ie)
        {
            LOG.error("could not put message into outqueue", ie);
            throw new IOException("could not put message into outqueue");
        }

    }

    /**
     * Send a <code>IMessage</code> to a remote machine, use as address
     * following schema. Throws a NoPeerAvailableException otherwise.
     * 
     * @param address
     *            "host:port", whereas host has to be a resolveable ip number,
     *            e.g. "127.0.0.1:4160","localhost:4160"
     * @param msg
     *            IMessage
     */
    public void send(String address, IMessage msg) throws IOException
    {

        String ip = null;
        int port = defaultport;
        // take the address apart, which is of the form ip:port
        StringTokenizer strtokinzer = new StringTokenizer(address, ":");
        if (strtokinzer.hasMoreTokens())
            ip = strtokinzer.nextToken();
        else
            throw new IOException("malformed address, should be ip:port");

        if (strtokinzer.hasMoreTokens())
            port = Integer.parseInt(strtokinzer.nextToken());
        else
            throw new IOException("malformed address, should be ip:port");

        LOG.debug("parsed addres: " + ip + "," + port);

        send(ip, port, msg);
    }

    /**
     * Connect this peer to a MulticastAddress.
     * 
     * @param jadabsport
     *            port for connection
     * @return
     */
    private boolean startServer()
    {

        // start main server to accept new connection
        try
        {
            mainServer = new MainServer(defaultport);
            mainServer.start();
        } catch (IOException ioe)
        {
            LOG.error("could not start server socket", ioe);
            return false;
        }

        // start socket threads
        for (int i = 1; i <= nofServerThreads; i++)
        {
            SocketThread peercon = new SocketThread();
            socketThreads.add(peercon);

            peercon.start();
        }

        // start message handler
        inMsgHandler = new InMessageHandler();
        inMsgHandler.start();

        // start OutMessageHandler
        outMsgHandler = new OutMessageHandler();
        outMsgHandler.start();

        LOG.info("created server socket on:" + defaultport);

        return true;
    }

    /**
     * Start the connection, use first create(String, String, String) to create
     * a Server Socket to listen to. If connect is not preceded by a
     * <code>create</code> only the outgoing message connection is enabled.
     *  
     */
    public void connect() throws IOException
    {
        startServer();
        System.out.println("Started TCPService on port: " + defaultport
                + " ...");
    }

    /**
     * Close stops the tcp server and any still avtive connections.
     */
    public int close(String name, String id, String type)
    {

        super.close(name, id, type);

        System.out.println("close tcp connection");

        // stop MainServer
        if (mainServer != null)
        {
            //			mainServer.running = false;
            //			mainServer.interrupt();
            mainServer.stopServer();
        }

        // stop all server threads, first let them finish
        if (!socketThreads.isEmpty())
        {
            for (Enumeration en = socketThreads.elements(); en
                    .hasMoreElements();)
            {
                SocketThread serverThread = (SocketThread) en.nextElement();
                serverThread.running = false;
                serverThread.interrupt();
            }
        }

        // stop the InMessageHandler
        if (inMsgHandler != null)
        {
            inMsgHandler.running = false;
            inMsgHandler.interrupt();
        }

        // stop the outgoing Message Handler
        if (outMsgHandler != null)
        {
            outMsgHandler.running = false;
            outMsgHandler.interrupt();
        }

        if (LOG.isDebugEnabled()) LOG.debug("closed tcp connections.");

        return 1;
    }

    /**
     * Struct for the outgoing messages.
     *  
     */
    class OutMessage
    {

        String receiver;

        IMessage msg;

        int port;

        public OutMessage(String receiver, int port, IMessage msg)
        {
            this.receiver = receiver;
            this.msg = msg;
            this.port = port;
        }
    }

    /**
     * Handles the incomming messages by calling the message message listener.
     *  
     */
    class OutMessageHandler extends Thread
    {

        boolean running = true;

        public OutMessageHandler()
        {
            setName("TCPPeerNetwork:OutMessageHandler");
        }

        public void run()
        {

            while (running)
            {
                OutMessage outmsg;
                try
                {
                    outmsg = (OutMessage) outMsgQ.take();

                    if (LOG.isDebugEnabled())
                            LOG.debug("try to send message over tcp to: "
                                    + outmsg.receiver + ":" + outmsg.port);

                    Socket socket = new Socket(outmsg.receiver, outmsg.port);
                    OutputStream out = socket.getOutputStream();
                    outmsg.msg.writeMessage(out);
                    out.flush();
                    out.close();

                    // close socket, we are finished with sending
                    // TODO: maybe we want to have an open stream, could be done
                    // by creating the concept of a pipe
                    socket.close();

                } catch (InterruptedException e)
                {
                    LOG.warn(e);
                } catch (UnknownHostException e)
                {
                    LOG.warn(e);
                } catch (IOException e)
                {
                    LOG.warn(e);
                }
            }

        }

    }

    /**
     * Handles the incomming messages by calling the message message listener.
     *  
     */
    class InMessageHandler extends Thread
    {

        boolean running = true;

        public InMessageHandler()
        {
            setName("TCPPeerNetwork:InMessageHandler");
        }

        public void run()
        {

            while (running)
            {

                try
                {
                    IMessage msg = (IMessage) inMsgQ.take();
                    processMessage(msg);

                } catch (InterruptedException e)
                {
                    LOG.warn(e);
                }

            }
        }

    }

    /**
     * The <code>MainServer</code> Thread listens ont he serverport and
     * forwards new connection to the <code>ServerThread</code>s.
     *  
     */
    class MainServer extends Thread
    {

        private ServerSocket serversocket;

        boolean running = true;

        int jadabsport;

        public MainServer(int jadabsport) throws IOException
        {
            this.jadabsport = jadabsport;

            serversocket = new ServerSocket(jadabsport);
            setName("TCPPeerNetwork:MainServer");
        }

        public void run()
        {

            while (running)
            {

                try
                {
                    Socket connection = serversocket.accept();
                    socketPool.put(connection);

                    if (LOG.isDebugEnabled())
                            LOG.debug("new connection established");
                } catch (IOException ioe)
                {
                    LOG.warn("lost connection after accept");
                } catch (InterruptedException ie)
                {
                    LOG.warn("socket pool insertion interrupted");
                }

            }

            LOG.info("server socket has been stopped");
        }

        public void stopServer()
        {
            running = false;

            try
            {
                serversocket.close();
            } catch (IOException e)
            {
                LOG.warn("closed server socket connection");
            }
        }

    }

    /**
     * <code>ServerThread</code> handels the established socket connections.
     */
    class SocketThread extends Thread
    {

        boolean running = true;

        Socket socket;

        public void run()
        {

            while (running)
            {
                try
                {

                    socket = (Socket) socketPool.take();
                    IMessage msg = Message.read(socket.getInputStream());

                    // put tcp info into message elements for convenient
                    // 'replies'
                    msg.setElement(new Element(elementTCPSender, socket
                            .getInetAddress().toString()));

                    inMsgQ.put(msg);

                } catch (IOException ioe)
                {
                    LOG.warn("lost connection on socket");
                } catch (MessageParseException mpe)
                {
                    LOG.warn("message parse exception");
                } catch (ElementParseException epe)
                {
                    LOG.warn("element parse exception");
                } catch (InterruptedException ie)
                {
                    LOG.warn("could not read socket from socket pool");
                }
            }

        }

    }

}