/*
 * Created on Mar 23, 2004
 *
 */
package ch.ethz.iks.jxme.udp;

import java.io.IOException;

import ch.ethz.iks.jadabs.BootstrapConstants;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IMessageListener;
import ch.ethz.iks.jxme.IPeerNetwork;
import ch.ethz.iks.jxme.impl.Element;
import ch.ethz.iks.jxme.impl.Message;


/**
 * @author andfrei
 * @version $revision$
 */
public class TestPeerNetwork extends Thread implements IComponent, IMessageListener
{
    
    IPeerNetwork pnet;
    boolean running = true;
    String peername;
    

    public static TestPeerNetwork createComponentMain(){
        return new TestPeerNetwork();
    }

    //---------------------------------------
    // Implements: Runnable
    //---------------------------------------
    public void run()
    {
        int number = 0;
        while (running){
            
            IMessage msg = new Message(new Element("elementname", "value_" + number++));
            
            try {
                pnet.send(null, msg);
                System.out.println("Message sent");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        
    }

    //---------------------------------------
    // Implements: IComponent
    //---------------------------------------

    public void init(IComponentContext context) {
        
        pnet = (IPeerNetwork)
            context.getComponent("ch.ethz.iks.jxme.udp.UDPPeerNetwork");
        
        peername = (String)context.getProperty(BootstrapConstants.PEERNAME);
        

    }

    public void startComponent(String[] args) {
        if (peername.equals("peer1"))
            start();            
        else
            pnet.addMessageListener(this);
    }

    public void stopComponent() {
        running = false;
        
        pnet.removeMessageListener(this);
    }

    public void disposeComponent() {
        
    }

    //---------------------------------------
    // Implements: IMessageListener
    //---------------------------------------
    public void processMessage(IMessage message) {
        System.out.println(message.toXMLString());
    }

    
}
