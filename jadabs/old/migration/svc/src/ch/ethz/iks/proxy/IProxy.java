package ch.ethz.iks.proxy; 

import ch.ethz.iks.evolution.adapter.IAdapter;



/**
 * IProxy defines the interface that every replaceable component has to satisfy.
 * <code>setInvocationHandler(InvocationHandler)</code> assigns the object that is responsible for delegating
 * the method invocations on this proxy objet to its original (hidden) object.
 * The methods <code>hash(), same(IProxy) and initHash</code> are special in that these are the only
 * methods that MUST NOT be redirected by the proxy's invocation handler. They enable
 * non referencial comparison of proxy objects and not original objects.
 * <code>Object obj1, obj2; Object obj3 = obj2;
 *       IProxy p1 = TransparentProxyFactory.newProxyInstance(...,obj1,...);
 *       IProxy p2 = TransparentProxyFactory.newProxyInstance(...,obj1,...);
 *       IProxy p3 = TransparentProxyFactory.newProxyInstance(...,obj2,...);
 *       IProxy p4 = TransparentProxyFactory.newProxyInstance(...,obj3,...);
 * 		 p1 == p2 => true // TODO: to check if original is the same, then proxies should be, too 
 *       p1.equals(p2) => true (depends on impl of equals)
 *       p1.equals(p3) => false (depends on impl of equals)
 *       p2.equals(p4) => true (depends on impl of equals)
 *       p1.same(p2) => true // to check: if original is the same, then proxy should be, too 
 * 		 p1.same(p3) => false
 * </code>
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface IProxy extends IAdapter {

	public static final String proxyFolder = "proxies";
	public static final String proxyJarPrefix = "proxy4_";
	public static final String unknownMethod = "invoke";

	
    /*
     * collects the current internal state of the running component.
     * @return State - the current state of this component
 
    public StateOfComponent gatherCurrentState ();
    
	/*
	 * Applies the state given to the component.
	 * @param oldVersionState - the state to be migrated to this component
	 
    public void transferState (StateOfComponent oldVersionState) throws Exception;
    public void initUpgrade (); // enable buffer
    public void finalizeUpgrade (); // empty buffer
    */
    
    public void setInvocationHandler( IAdapter handler);
    

    
    public boolean same(IProxy anotherProxy); // Object.equals() do NOT forward this to handler object!!!
    
    public int hash(); // Object.hashCode()
    
    public String dump(); // Object.toString()
    

}
