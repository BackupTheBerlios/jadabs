package ch.ethz.iks.evolution.adapter;

import java.lang.reflect.InvocationHandler;

/**
 * The interface each proxy handler has to implement. 
 * Three alternative ways of invocation handling may be used (depending on the
 * <code>-adapt</code> command line argument value: 
 * <code>reflect</code>: invoke(Object proxy, Method method,                           , Object[] args)
 * <code>name</code>:    invoke(Object proxy, String methodName,  String declaringClass, Object[] args)
 * <code>hash</code>:    invoke(Object proxy, int methodCode,     String declaringClass, Object[] args)
 * 
 * The interface provides methods to map invocations on the proxy to the hidden object as well as 
 * assigning the hidden object to the proxy.
 * A proxy handler may be assigned to a proxy
 * using the <code>IProxy.setInvocationHandler</code> method.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface IAdapter extends InvocationHandler {
	
	public Object invoke(Object proxy, String methodName,  String declaringClass, Object[] args) throws Throwable;
	
	public Object invoke(Object proxy, int methodCode,  String declaringClass, Object[] args) throws Throwable;

	/**
	 * Assign the hidden object to its proxy
	 * @param original
	 */
	public void setOriginal(Object original);
	
}
