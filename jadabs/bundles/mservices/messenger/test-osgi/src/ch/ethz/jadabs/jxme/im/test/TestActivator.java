/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.im.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.im.IMException;
import ch.ethz.jadabs.jxme.im.IMListener;
import ch.ethz.jadabs.jxme.im.IMService;
import ch.ethz.jadabs.jxme.im.IMServiceImpl;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * @author andfrei
 * 
 */
public class TestActivator implements BundleActivator
{
   
    static String test1sipaddress = "sip:test1@wlab.ethz.ch";
    static String test2sipaddress = "sip:test2@wlab.ethz.ch";
    
    static GroupService groupsvc = null;
    
    static IMService imservice = null;
    
    String peername;
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        
        peername = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
        
        // get GroupService
        ServiceReference sref = bc.getServiceReference(
                "ch.ethz.jadabs.jxme.services.GroupService");
        
        if (sref == null)
            throw new Exception("could not properly initialize IM, GroupService is missing");
    
        groupsvc = (GroupService)bc.getService(sref);
        
        // create IMServiceImpl and do testrun
        if (peername.equals("peer1"))
        {
	        imservice = new IMServiceImpl(groupsvc, test1sipaddress);
	        
	        testrunPeer1();
        }
        else
        {
            imservice = new IMServiceImpl(groupsvc, test2sipaddress);
         
            testrunPeer2();
        }
        
        
        // register IMService
        //bc.registerService("ch.ethz.jadabs.jxme.im.IMService", imservice, null);
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {

    }

    
    public void testrunPeer1()
    {
//        try
//        {
            // first subscribe
            imservice.register(new Peer1IMListener(), IMService.IM_STATUS_ONLINE);
            
            // send IM-Message
//            imservice.sendMessage(test2sipaddress, "testmessage");
//            
//            
//            
//        } catch (IMException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }
    
    
    public void testrunPeer2()
    {
        try
        {
            // first subscribe
            imservice.register(new Peer2IMListener(), IMService.IM_STATUS_ONLINE);
            
            // send IM-Message
            imservice.sendMessage(test1sipaddress, "testmessage");
            
            
            
        } catch (IMException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    class Peer1IMListener implements IMListener
    {
    	public void imRegistered(String sipaddress, int status)
        {
            System.out.println("IM joined: "+sipaddress);
        }
        
        public void imUnregistered(String sipaddress)
        {
            System.out.println("IM left: "+sipaddress);
        }
        
        public void process(String sipaddress, String msg)
        {
            System.out.println("IM sent: "+sipaddress+":"+msg);
        }
    }
    
    class Peer2IMListener implements IMListener
    {
    	public void imRegistered(String sipaddress, int status)
        {
            System.out.println("IM joined: "+sipaddress);
        }
        
        public void imUnregistered(String sipaddress)
        {
            System.out.println("IM left: "+sipaddress);
        }
        
        public void process(String sipaddress, String msg)
        {
            System.out.println("IM sent: "+sipaddress+":"+msg);
        }
    }
}
