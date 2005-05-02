package ch.ethz.jadabs.jxmews;


import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.microservices.MicroElement;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleActivator;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleImpl;
import ch.ethz.jadabs.jxme.microservices.MicroListener;
import ch.ethz.jadabs.jxme.microservices.MicroMessage;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;

/**
 * SBBMIDlet is the main class for the SBB application on the mobile phone.
 * It implements the BundleActivator class as well as Listeners to handle
 * user events and the incoming message from the SBB Server. For the
 * different kind of information presented to the user some forms and lists
 * are created initially.
 *
 * @author Franz Maier
 */

public class JxmeWSMIDlet extends MIDlet implements BundleActivator, MicroListener, CommandListener {

    private static Logger LOG = Logger.getLogger("SBBMIDlet");
    
    private JxmeWSMIDlet instance;
    private Display display;
    
    protected static final String SOAP_PIPE = "soappipe";
    protected static final String PIPE_NAME = "localpipe";
    
    private ServiceReference fServiceReference;

    public String fResponseFromSBBServer = "";

    /* commands */
    private Command regCmd;
    private Command unregCmd;
    private Command logCmd;
    private Command exitCmd;
    
    /** MicroGroupService service bundle actiavtor */
    private MicroGroupServiceBundleImpl groupService;

    private String pipeId;

    /**
     * Constructor
     */
    public JxmeWSMIDlet() {

        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.peeralias", this.getAppProperty("ch.ethz.jadabs.jxme.peeralias"));
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.microservices.bundleport", 
                this.getAppProperty("ch.ethz.jadabs.microservices.bundleport"));
        //        osgicontainer.setProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer", this.getAppProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer"));
        osgicontainer.startBundle(new LogActivator());
//        osgicontainer.startBundle(new JxmeActivator());
        
        // start bundle part of MicroGroupService 
        MicroGroupServiceBundleActivator mgsActivator = new MicroGroupServiceBundleActivator();        
        osgicontainer.startBundle(mgsActivator);
        groupService = mgsActivator.getService();   
        groupService.wakeupCore(); 
                
        osgicontainer.startBundle(this);

        instance = this;
        LOG = Logger.getLogger("JxmeWSMIDlet");
    }

    public void start(BundleContext bundleContext)
    {

    }

    public void stop(BundleContext bundleContext) 
    {
        
    }

    /**
     * Handle starting the MIDlet
     */
    public void startApp() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke startApp()");
        }

        display = Display.getDisplay(this);
        regCmd = new Command("Register", Command.SCREEN, 1);
        unregCmd = new Command("Unregister", Command.SCREEN, 1);
        logCmd = new Command("Log", Command.SCREEN, 3);
        exitCmd = new Command("Beenden", Command.EXIT, 1);

        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(Logger.getLogCanvas());
        Logger.getLogCanvas().setCommandAndListener(new Command[] {
                regCmd, unregCmd, exitCmd}, this);
        display.setCurrent(Logger.getLogCanvas());
        
        try {            
	        // create pipe to communicate with core
	        String ns[] = groupService.localSearch(NamedResource.PIPE, "Name", PIPE_NAME, 1 );
	        
	        if (ns.length > 0)
	        {
	            pipeId = ns[0];
	            LOG.debug("found: "+pipeId);
//		        pipeId = groupService.create(NamedResource.PIPE, PIPE_NAME, "urn:jxta:uuid-0002:0001:04", Pipe.PROPAGATE);
//		        groupService.publish(NamedResource.PIPE, PIPE_NAME, pipeId);
	                
		        groupService.listen(pipeId, this);
	        }
	        else
	            LOG.debug("no local-loopback");
	        
        } catch(IOException e) {
           LOG.error("Error while registering listener to pipe '"+pipeId+"': "+e.getMessage());
        }
    }

    /**
     * Handle pausing the MIDlet
     */
    public void pauseApp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke pauseApp()");
        }
    }

    /**
     * Handle destroying the MIDlet
     */
    public void destroyApp(boolean unconditional) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke destroyApp()");
        }
    }

    /**
     * Quit the MIDlet
     */
    private void quitApp() {
        instance.destroyApp(true);
        instance.notifyDestroyed();
        instance = null;
        display = null;
    }


    /**
     * Handle user action
     *
     * @param c GUI command
     * @param d GUI display object that triggered the command
     */
    public void commandAction(Command c, Displayable d) {
        if (c == regCmd) {
            sendLocalJxmeMessage("reg","opipe","soappipe");
        } else if (c == unregCmd) {
            sendLocalJxmeMessage("unreg", null, null);
        } else if (c == exitCmd) {
            quitApp();
        }
    }

    public void sendSoapString(String soapMsg)
    {
//        if (configForm.getConnectionType() == ConfigurationForm.CONTYPE_HTTP)
//            sendHttpSoap(soapMsg);
//        else
        
//            sendJxmeMessage(soapMsg);
            
        MicroElement elms[] = new MicroElement[1];
        elms[0] = new MicroElement("SOAP_REQUEST_TAG", "test", "jxta");
        MicroMessage mmsg = new MicroMessage(elms);
        
        LOG.debug("sending JXTA message over pipe.");
        try {
            groupService.send(pipeId, mmsg);
        } catch(IOException e) {
            LOG.error("Error while sending JXTA message: "+e.getMessage());
        }
    }
    
    private void sendLocalJxmeMessage(String message, String name, String value) 
    {

//        Element[] elms = new Element[1];
//        elms[0] = new Element("SOAP_REQUEST_TAG", message, Element.TEXTUTF8_MIME_TYPE);
  
        MicroElement[] elms;
        if (name != null)
            elms = new MicroElement[1];
        else
        {
            elms = new MicroElement[2];
            elms[1] = new MicroElement(name, value, "jxta");
        }
        
        
        elms[0] = new MicroElement("type", message, "jxta");
        
        
        MicroMessage mmsg = new MicroMessage(elms);
        
        LOG.debug("sending JXTA message over pipe.");
        try {
            groupService.send(pipeId, mmsg);
        } catch(IOException e) {
            LOG.error("Error while sending JXTA message: "+e.getMessage());
        }
        
    }

    public void handleSearchResponse(NamedResource namedResource) {
    }

    public void handleMessage(MicroMessage message, String listenerId)
    {
        LOG.debug("incoming message \""+message.toXMLString()+"\", listenerId="+listenerId);                    

    }	

}
