/*
 * Created on Jan 12, 2005
 *
 */
package ch.ethz.jadabs.im.bridge;

import org.apache.log4j.Logger;
import org.codehaus.nanning.AspectInstance;
import org.codehaus.nanning.Invocation;
import org.codehaus.nanning.MethodInterceptor;
import org.codehaus.nanning.Mixin;
import org.codehaus.nanning.config.P;
import org.codehaus.nanning.config.Pointcut;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.im.api.IMContact;
import ch.ethz.jadabs.im.api.IMListener;
import ch.ethz.jadabs.im.api.IMService;
import ch.ethz.jadabs.osgiaop.AOPContext;
import ch.ethz.jadabs.osgiaop.AOPService;


/**
 * @author andfrei
 * 
 */
public class IMServiceBridgeActivator implements BundleActivator
{

    private static Logger LOG = Logger.getLogger(IMServiceBridgeActivator.class.getName());
    
    Pointcut pointcut;
    
    IMService newIMsvc;
    IMService oldIMsvc;
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        
        String imsvcname = bc.getProperty("ch.ethz.jadabs.im.bridge.imservice");
        
        // get IMService with the given imsvcname
        ServiceReference[] sref = bc.getServiceReferences(IMService.class.getName(), "(impl=sip)");
        
        if (sref.length == 0)
        {
            LOG.error("could not find the service specified: "+imsvcname);
            return;
        }
        
        newIMsvc = (IMService)bc.getService(sref[0]);
        
		ServiceReference[] impref = bc.getServiceReferences(IMService.class.getName(), "(impl=jxme)");
		
        if (impref.length == 0)
        {
            LOG.error("could not find the service specified: "+"(impl=jxme)");
            return;
        }
		
		if (impref[0] != null)
		{
		    ((AOPContext)bc).getAOPService(impref[0]);
		    
		    AOPService imaopsvc = ((AOPContext)bc).getAOPService(impref[0]);
	        
	        AspectInstance ai = imaopsvc.getAspectInstance();
	      	
	        Mixin imsvcmixin = ai.getMixinForInterface(IMService.class);
	        
	      	// get old target
	      	oldIMsvc = (IMService)imsvcmixin.getTarget();
	      
	      	imsvcmixin.setTarget(newIMsvc);
	      	
//	      	// crosscut with sendmessage
//	      	MethodInterceptor interceptor = new IMServiceInterceptorSendMessage();
//	      	pointcut = P.methodName("sendMessage.*");
//	      	pointcut.advise(ai, interceptor);
//	      	
//	      	// crosscut with disconnect
//	      	MethodInterceptor interceptorDiscon = new IMServiceInterceptorDisconnect();
//	      	pointcut = P.methodName("disconnect.*");
//	      	pointcut.advise(ai, interceptorDiscon);
//	      	
//	      	// crosscut with getBuddies
//	      	MethodInterceptor interceptorGetBuddies = new IMServiceInterceptorGetBuddies();
//	      	pointcut = P.methodName("getBuddies.*");
//	      	pointcut.advise(ai, interceptorGetBuddies);
//	      	
//	      	// get listener from old imsvc and register this imservice
//	      	IMListener iml = oldIMsvc.getListener();
	      	
	      	newIMsvc.setListener(oldIMsvc.getListener());
	      	newIMsvc.connect();
		}
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
        
    }

    class IMServiceInterceptorSendMessage implements MethodInterceptor
    {
        
        public Object invoke(Invocation invocation) throws Throwable
        {
            
            String tosip = (String)invocation.getArgument(0);
            String msg = (String)invocation.getArgument(1);
 
            LOG.debug("invoked SendMessage interceptor: "
                    + tosip + ":"+msg);
                       
            newIMsvc.sendMessage(tosip, msg);
            
            Object result = invocation.invokeNext();

            return result;
        }
    }
    
    class IMServiceInterceptorDisconnect implements MethodInterceptor
    {
        
        public Object invoke(Invocation invocation) throws Throwable
        {
                     
            LOG.debug("invoked disconnect interceptor");
            
            newIMsvc.disconnect();
            
            Object result = invocation.invokeNext();

            return result;
        }
    }
    
    class IMServiceInterceptorGetBuddies implements MethodInterceptor
    {
        
        public Object invoke(Invocation invocation) throws Throwable
        {
                     
            LOG.debug("invoked getBuddies interceptor");
            
            Object[] newlist = newIMsvc.getBuddies();
//            Object[] oldlist = oldIMsvc.getBuddies();
            
//            Object[] nexts = invocation.invokeNext();
            
//            Object[] result = new Object[oldlist.length+newlist.length];

//            int k = 0;
//            for(int i=0; i<newlist.length; i++)
//                result[k++] = newlist[i];
//            
//            for(int i=0; i<oldlist.length; i++)
//                result[k++] = oldlist[i];
            
            return newlist;
        }
    }
}
