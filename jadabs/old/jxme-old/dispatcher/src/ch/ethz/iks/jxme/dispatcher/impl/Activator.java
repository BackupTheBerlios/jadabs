/*
 * Created on May 4, 2004
 *
 */
package ch.ethz.iks.jxme.dispatcher.impl;

import org.codehaus.nanning.AspectInstance;
import org.codehaus.nanning.Mixin;
import org.codehaus.nanning.config.P;
import org.codehaus.nanning.config.Pointcut;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.iks.jxme.IPeerNetwork;
import ch.ethz.jadabs.osgiaop.AOPContext;
import ch.ethz.jadabs.osgiaop.AOPService;


/**
 * @author andfrei
 * 
 */
public class Activator implements BundleActivator
{

    private Dispatcher dispatcher;
    private AspectInstance ai;
    private Pointcut pointcut;
    
    /*
     */
    public void start(BundleContext context) throws Exception
    {
        ServiceReference sref = context.getServiceReference(
                IPeerNetwork.class.getName());
        
        IPeerNetwork pnet = (IPeerNetwork)context.getService(sref);
        
        // create Dispatcher
        dispatcher = new Dispatcher();
        
        // set Pointcut for IPeerNetwork
        AOPService aopsvc = ((AOPContext)context).getAOPService(sref);
       	ai = aopsvc.getAspectInstance();
      	Mixin foomixin = ai.getMixinForInterface(IPeerNetwork.class);
      	
      	pointcut = P.methodName("send.*");
//      	MethodInterceptor minterceptor = new BeforeLogMethodInterceptor();
      	pointcut.advise(ai, dispatcher);       
      	
    }

    /*
     */
    public void stop(BundleContext context) throws Exception
    {

        pointcut.clear(ai,dispatcher,null);
        pointcut = null;
    }

}
