/*
 * Created on May 4, 2004
 *
 */
package ch.ethz.iks.jxme.dispatcher.impl;

import org.codehaus.nanning.Invocation;
import org.codehaus.nanning.MethodInterceptor;

import ch.ethz.iks.jxme.IMessage;


/**
 * @author andfrei
 * 
 */
public class Dispatcher implements MethodInterceptor
{

    public Dispatcher()
    {
        
        
    }
    
    public Object invoke(Invocation invocation) throws Throwable
    {            
        System.out.println("called Dispatcher with args: ");
        
        Object[] args = invocation.getArgs();
        IMessage msg = (IMessage)args[1];
//        System.out.println(msg.toXMLString());
        System.out.println("called Dispatcher would send it now also through tcp");
        
        Object result = invocation.invokeNext();
        
        return result;
    }

}
