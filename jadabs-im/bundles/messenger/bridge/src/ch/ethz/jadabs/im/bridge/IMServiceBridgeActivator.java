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

import ch.ethz.jadabs.im.api.IMService;
import ch.ethz.jadabs.osgiaop.AOPContext;
import ch.ethz.jadabs.osgiaop.AOPService;


/**
 * @author andfrei
 * 
 */
public class IMServiceBridgeActivator implements BundleActivator
{

    private static Logger LOG = Logger.getLogger(IMServiceBridgeActivator.class);
    
    Pointcut pointcut;
    
    IMService imsvc;
    
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        
        String imsvcname = bc.getProperty("ch.ethz.jadabs.im.bridge.imservice");
        
        // get IMService with the given imsvcname
        ServiceReference sref = bc.getServiceReference("(impl equals imsvcname)");
        
        if (sref == null)
        {
            LOG.error("could not find the service specified: "+imsvcname);
            return;
        }
        
        imsvc = (IMService)bc.getService(sref);
        
		ServiceReference impref = bc.getServiceReference(IMService.class.getName());
		
		if (impref != null)
		{
		    ((AOPContext)bc).getAOPService(impref);
		    
		    AOPService imaopsvc = ((AOPContext)bc).getAOPService(impref);
	        
	        AspectInstance ai = imaopsvc.getAspectInstance();
	      	
	        Mixin imsvcmixin = ai.getMixinForInterface(IMService.class);
	        
	      	// get old target
	      	IMService establishedIMService = (IMService)imsvcmixin.getTarget();
	      
	      	// crosscut with sendmessage
	      	MethodInterceptor interceptor = new IMServiceInterceptorSendMessage();
	      	pointcut = P.methodName("sendMessage.*");
	      	pointcut.advise(ai, interceptor);
	      	
	      	// crosscut with unregister
	      	MethodInterceptor interceptorUnreg = new IMServiceInterceptorUnregister();
	      	pointcut = P.methodName("unregister.*");
	      	pointcut.advise(ai, interceptorUnreg);
	      	
	      	// get listener from old imsvc and register this imservice
	      	imsvc.connect(establishedIMService.getListener());
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
                        
            imsvc.sendMessage((String)invocation.getArgument(0),
                    			(String)invocation.getArgument(1));
            
            Object result = invocation.invokeNext();

            return result;
        }
    }
    
    class IMServiceInterceptorUnregister implements MethodInterceptor
    {
        
        public Object invoke(Invocation invocation) throws Throwable
        {
                        
            imsvc.disconnect();
            
            Object result = invocation.invokeNext();

            return result;
        }
    }
}
