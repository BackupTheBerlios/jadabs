package ch.ethz.iks.evolution.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResource;
import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResourceFactory;
import ch.ethz.iks.evolution.cop.UpgradeableComponentLoader;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResourceFactory;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.IClassLoaderWithClassPath;
import ch.ethz.iks.proxy.IProxy;
import ch.ethz.iks.proxy.TransparentProxyFactory;
import ch.ethz.iks.proxy.WrappingException;
import ch.ethz.iks.proxy.cop.ProxyComponentResource;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;
import ch.ethz.iks.proxy.cop.ProxyLoader;
import ch.ethz.iks.utils.Decode;

/**
 * A generic adapter to map method invocations on proxies to their hidden
 * objects. An adapter is registered using the <code>IProxy.setInvocationHandler</code>
 * setter. This default adapter supports three kinds of invocations from proxies:
 * <code>-adapt reflect</code> uses reflection to find a method that has the same namehash
 * as the one called on the proxy and the arguments passed are compatible with the methods 
 * parameter types. To avoid perfomance penalty, it is recommended that each public class defines its
 * own custom adapter and use the 
 * <code>-adapt name</code> invocation method. This is based on the name of the method and its declaring class
 * to identify the method to call. Note that the proxy must pass the correct class name (the name of the class it hides).
 * The third kind of invocations accepted by proxies is <code>-adapt hash</code>. To avoid String comparisons, a precalclated integer
 * is used to identify the method to call. see <code>TransparentProxyFactory.getMethodHash</code>
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class DefaultAdapter implements IAdapter {

	protected DefaultAdapter() {
		super(); 
	}

	public static DefaultAdapter Instance() {
		return handler; // singleton
	}

	private static DefaultAdapter handler = new DefaultAdapter();
	private static ProxyRegistry proxies = new ProxyRegistry();
	//	keeps track of all proxy objects passed to the outside the declaring component (e.g. as return values of methods to client components or arguments to methods of dependency components)
	
	private static Logger LOG = Logger.getLogger(DefaultAdapter.class);
	//private InvocationBuffer pendingInvocations = new InvocationBuffer(); // unused, to buffer invocations during evolution
	private static Vector originalsWithoutAProxy = new Vector(); // objects waiting to be hidden, queueing for a proxy
	private static Logger bmLog = Logger.getLogger("ProxyBenchmark"); // benchmark logger (proxy_bm.html)
	protected Object hidden;

	// pre-calculated method hashes of Object.class
	private static Method toString;
	private static Method equals;
	private static Method hashCode;

	static {
		try {
			toString = Object.class.getMethod("toString", null);
			equals = Object.class.getMethod("equals", new Class[] { Object.class });
			hashCode = Object.class.getMethod("hashCode", null);
		} catch (NoSuchMethodException e) {
			LOG.error(e);
		}

	}

	/******************************************* handling of proxy method invocations **************************************************/
	
	/**
	 * <code>-adapt hash</code>
	 *
	 * handles methods on transpparent proxies like proxy.method(args) by invoking the corresponding method on the hidden object.
	 * Instead of the method object or name, a hash value is passed for perfomance reasons.
	 * If neccessary, it wraps the return value in a proxy before passing it back.
	 */
	public final synchronized Object invoke(Object proxy, int methodCode, String declaringClass, Object[] args) throws Throwable {
		Object originalReturnedObject = null;
		
		// hidden object
		Object callee;
		if (this.hidden == null) {
			LOG.error(" no hidden object assigned to adapter "+this);
			callee = proxies.getHiddenBy((IProxy) proxy);
		} else {
			callee = this.hidden;
		}
		
		// arguments
		prepareArgs(declaringClass, args);

		// call
		if (proxy == null) { // static method
			//LOG.info("adaptStatic in custom adapter " + this.getClass() + " @ " + this.getClass().getClassLoader());
			DefaultAdapter customAdapter = (DefaultAdapter) getCustomAdapter(declaringClass);
			originalReturnedObject = customAdapter.adaptStatic(methodCode, declaringClass, args);
		} else {
			originalReturnedObject = adapt(callee, methodCode, args);
		}
		
		// return value
		if (originalReturnedObject == null) {
			return null;
		} 
		Object retval = prepareReturnValue(declaringClass, originalReturnedObject);
		return retval;
	}


	/**
	 * <code>-adapt name</code>
	 * 
	 * Redirects an invocation to a method of a transparent proxy object to its adapter
	 * Checks if the Object returned by the method also needs to be hidden and returns
	 * a proxy of it if required. 
	 * 
	 * @param proxy
	 * @param method
	 * @param args
	 * @return
	 */
	public final synchronized Object invoke(Object proxy, String methodName, String declaringClass, Object[] args) throws Throwable {
		Object originalReturnedObject = null;
		
		// hidden object
		Object callee;
		if (hidden == null) {
			callee = proxies.getHiddenBy((IProxy) proxy);
		} else {
			callee = hidden;
		}
		
		// arguments
		prepareArgs(declaringClass, args);

		// call
		if (proxy == null) { // static method
			//LOG.info("adaptStatic in custom adapter " + this.getClass() + " @ " + this.getClass().getClassLoader());
			DefaultAdapter customAdapter = (DefaultAdapter) getCustomAdapter(declaringClass);
			originalReturnedObject = customAdapter.adaptStatic(methodName, declaringClass, args);
		} else {
			originalReturnedObject = adapt(callee, methodName, args);
		}
		
		// return value
		if (originalReturnedObject == null) {
			return null;
		} 
		Object retval = prepareReturnValue(declaringClass, originalReturnedObject);
		return retval;
		
	}


	/**
	 * handles invocations from transparent as well as <em>dynamic</em> proxies of the form 
	 * <code>-adapt reflect</code>
	 * cares about wrapping and unwrapping of arguments and return value
	 * delegates execution of the invocation to 
	 * <code>adapt(Object callee, String methodName, Object[] args)</code> 
	 * 
	 * A custom adapter may override adapt() to map the invocation to a different callee, method or arguments.
	 */
	public final synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (proxy instanceof Proxy) {
			// dynamic proxy
			Class c = method.getDeclaringClass();
			boolean isExternal = c.isInterface() && !ComponentEvolutionMain.doHideIfc();
			String declaringClass = c.getName();
			Object originalReturnedObject = null;

			// implement methods of Object.class to avoid endless recursion
			if (c == Object.class && method.equals(hashCode)) {
				return new Integer(System.identityHashCode(proxy));
			} else if (c == Object.class && method.equals(equals)) {
				return new Boolean(proxy == args[0]);
			} else if (c == Object.class && method.equals(toString)) {
				return proxy.getClass().getName();
			}

			Object callee = proxies.getHiddenBy((Proxy) proxy);

			for (int i = 0; (args != null) && (i < args.length); i++) {
				if (args[i] instanceof Proxy) {
					args[i] = proxies.getHiddenBy((Proxy) args[i]);
					//LOG.info("unwrapping proxy arg, hidden = "+args[i]);
				} else if (isExternal && TransparentProxyFactory.isExternal(args[i], declaringClass)) {
					// original instance of an external cop => hide it from callee's cop
					// hide arg for callbacks behing a proxy defined inside the hidden cop or its customizer
					/*LOG.info(
						"dynproxy: hide external arg from callee cop for callbacks: "
							+ args[i]
							+ " passed as an arg to "
							+ declaringClass
							+ " "
							+ method.getName()
					);*/

					Proxy dynProxy = createDynamicProxy(args[i]);
					//(Proxy) Proxy.newProxyInstance(dynProxyLoader, args[i].getClass().getInterfaces(), Instance());
					// create a DYNAMIC proxy 

					Class param = method.getParameterTypes()[i];
					if (!param.isInterface()) {
						LOG.error(
							"assigning dynamic proxy as arg to non ifc param "
								+ param
								+ " in method "
								+ method.getName());
					}
					args[i] = dynProxy; // assign dynamic proxy as argument

				}
			}

			if (proxy == null) { // static method
				LOG.error(
					"dynamic proxy: static resOfProxy("
						+ declaringClass
						+ ") is "
						+ ProxyComponentResourceFactory.getResourceOfProxy(declaringClass)
						+ " dynamic proxies must be created using a static method outside the component the hidden class is declared in (is loaded and accessed unprotected by client otherwise) or by a return value of a non-static method of a class that is already hidden behind a proxy. Currently, the getExtObj() is the way to solve this bootstrap problem");
				throw new WrappingException(
					"dynamic proxy: static resOfProxy("
						+ declaringClass
						+ ") is "
						+ ProxyComponentResourceFactory.getResourceOfProxy(declaringClass)
						+ " dynamic proxies must be created using a static method outside the component the hidden class is declared in (is loaded and accessed unprotected by client otherwise) or by a return value of a non-static method of a class that is already hidden behind a proxy. Currently, the getExtObj() is the way to solve this bootstrap problem");

				//originalReturnedObject = adaptStatic(method.getName(), declaringClass, args);
			} else {
				originalReturnedObject = adapt(callee, method.getName(), args);
			}

			if (originalReturnedObject == null) {
				return null;
			} else if ((originalReturnedObject instanceof Proxy)) {
				if (!proxies.containsProxy((Proxy) originalReturnedObject)) {
					WrappingException f = new WrappingException(" method returning a proxy that is not registered");
					LOG.error(originalReturnedObject, f);
					throw f;
				} else {
					return originalReturnedObject;
				}
			} else if (TransparentProxyFactory.needsProxy(originalReturnedObject, declaringClass)) {
				Proxy dynProxy = createDynamicProxy(originalReturnedObject);
				//LOG.debug(" handler returning dynamic proxy "+dynProxy+" hiding "+originalReturnedObject);
				return dynProxy;
			} else {
				return originalReturnedObject; // no proxy needed
			}

		} // is dynamic proxy

		try {
			// transparent proxy
			return invoke(proxy, method.getName(), method.getDeclaringClass().getName(), args);
			// TODO: if two hidden original are referentially equal (obj1 == obj2), their proxies must be the same, too... => loadtime manipulation: replace == by .equals
		} catch (Exception i) {
			WrappingException f = new WrappingException(i);
			LOG.error(proxy, f);
			throw f;
		}
	}

	


	


	/************************************ check args and return value if needs wrapping or unwrapping ***********************************/
	


	private Object prepareReturnValue(String declaringClass, Object originalReturnedObject) throws WrappingException, Exception {
		if ((originalReturnedObject instanceof IProxy)) {
			if (!proxies.containsProxy((IProxy) originalReturnedObject)) {
				WrappingException f = new WrappingException(" method returning a proxy that is not registered");
				LOG.error(originalReturnedObject, f);
				throw f;
			} else {
				return originalReturnedObject;
			}
		} else {
			Object proxy4retval = proxies.getStableProxyOf(originalReturnedObject);
			if (proxy4retval instanceof IProxy) {
				return (IProxy) proxy4retval;
			} else if (TransparentProxyFactory.needsProxy(originalReturnedObject, declaringClass)) {
				return wrapOutgoingObject(originalReturnedObject, declaringClass);
			} else {
				return originalReturnedObject; // no proxy needed
			}
		}
	}

	private void prepareArgs(String declaringClass, Object[] args) throws Exception {
		// check if arguments have to be unwrapped (matched to hidden objects)
		// check if arguments have to be wrapped (incoming external objects)
		for (int i = 0;(args != null) && (i < args.length); i++) {
			Object arg = args[i];
			if (arg instanceof IProxy) {
				args[i] = proxies.get(arg);
				LOG.info("unwrapping proxy arg, hidden = "+args[i]);
			} else if (arg instanceof Proxy) {
				LOG.error("only transparent proxies are supported with hash value method matching");
			} else {
				// cache arg2proxy
				Object proxy4arg = proxies.getUnstableProxyOf(arg);
				if (proxy4arg instanceof IProxy) {
					args[i] = (IProxy) proxy4arg;
				} else if (TransparentProxyFactory.isExternal(args[i], declaringClass)) {

					// callback: if declaringClass.isInterface() && !doHideIfc => wrap 
					// don;'t care if doHideingIfc == true: interface is in stable proxy cop, arg in its hidden cop

					// original instance of an external cop => hide it from callee's cop
					//  hide arg for callbacks behing a proxy defined inside the hidden cop or its customizer
					if (LOG.isInfoEnabled()) {
						LOG.info(">>>>>");

						LOG.info(
							"(0) [UNSTABLE] proxy for arg needed: "
								+ args[i]
								+ " passed as an arg to "
								+ declaringClass
							//	+ " method hash = "
							//	+ methodCode
						);
					}
					args[i] = wrapIncomingObject(arg, declaringClass);
					// create an UNSTABLE proxy (and adapter) in external cop's customizer cop (memory only).
				}
			}
		}
	}



	
	/************************************ default impl. of method execution on hidden object: after arg wrapping & before return value wrapping ***********************************/
	
	
	/**
	 * <code>-adapt name</code>
	 * 
	 * maps the <em>static</em>invocation <code>declaringClass.method(args[0],...,args[n]) on the proxy to
	 * the class with the same name on its hidden component. Executes the method with the same name and matching parametertypes.
	 * @param method
	 * @param declaringClass
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	protected Object adaptStatic(String method, String declaringClass, Object[] args) throws Throwable {
		Object originalReturnedObject;

		// may neither access proxy nor original object: n/a, use method.getDeclaringClass() to access proxy class
		String className = declaringClass; // proxy and original share name
		UpgradeableComponentResource origCop = null;
		if (!ComponentEvolutionMain.doHideIfc()) {
			LOG.error(
				"dynamic proxy: static resOfProxy("
					+ className
					+ ") is "
					+ ProxyComponentResourceFactory.getResourceOfProxy(className)
					+ " dynamic proxies must be created using a static method outside the component the hidden class is declared in (is loaded and accessed unprotected by client otherwise) or by a return value of a non-static method of a class that is already hidden behind a proxy. Currently, the getExtObj() is the way to solve this bootstrap problem");
			throw new WrappingException(
				"dynamic proxy: static resOfProxy("
					+ className
					+ ") is "
					+ ProxyComponentResourceFactory.getResourceOfProxy(className)
					+ " dynamic proxies must be created using a static method outside the component the hidden class is declared in (is loaded and accessed unprotected by client otherwise) or by a return value of a non-static method of a class that is already hidden behind a proxy. Currently, the getExtObj() is the way to solve this bootstrap problem");
		} else {
			origCop =
				(UpgradeableComponentResource) ProxyComponentResourceFactory
					.getResourceOfProxy(className)
					.getOriginalComponent();
			// cop containing hidden class
		}
		UpgradeableComponentLoader cl = (UpgradeableComponentLoader) origCop.getClassLoader();
		Class origClass = cl.loadClass(className);

		Method m = Decode.getMatchingMethod(method, origClass, args);
		originalReturnedObject = m.invoke(null, args); // call original method

		return originalReturnedObject;
	}

	/**
	 * <code>-adapt hash</code>
	 * Custom adapters should override this method to handle <em>static</em> invocations
	 * @param methodCode
	 * @param declaringClass
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	protected Object adaptStatic(int methodCode, String declaringClass, Object[] args) throws Throwable {
		//hack
		LOG.error("ERROR: Use custom adapter to call static method with hash " + methodCode + " in " + declaringClass);
		return adaptStatic(IComponent.factoryMethod, declaringClass, args);
	}

	protected Object adaptStatic(Method m, Object[] args) throws Throwable {
		return adaptStatic(m.getName(), m.getDeclaringClass().getName(), args);
	}



	/**
	 * Custom adapters should override this method to handle <em>instance</em> method invocations
	 * based on <code>-adapt hash</code> 
	 * @param callee
	 * @param methodCode
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	protected Object adapt(Object callee, int methodCode, Object[] args) throws Throwable {
		RuntimeException rt = new RuntimeException("DefaultAdapter.adapt("+callee+", "+methodCode+", args) is NOT YET IMPLEMENTED (use subclass)");
		rt.fillInStackTrace();
		LOG.error("DefaultAdapter.adapt(Object, int, Object[]) is NOT YET IMPLEMENTED (use subclass)", rt);
		//
		// Method m = Decode.getMethodByHash(hash); // TODO: have to cache hashes to allow reverse lookup hash -> method(name)
		throw rt;
		
	}

	/**
	 * <code>-adapt name</code> 
	 * 
	 * generic method invocation handling by matching method names using reflection techniques
	 * maps the invocation <code>callee.method(args[0],...,args[n]) on the proxy to
	 * the class with the same name on its hidden component. Executes the <em>instance</em> method with the same name and matching parametertypes.
	 * @param callee
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	protected Object adapt(Object callee, String method, Object[] args) throws Throwable {
		Object originalReturnedObject;

		Method m = Decode.getMatchingMethod(method, callee.getClass(), args);
		originalReturnedObject = m.invoke(callee, args);
		//LOG.info(" adapt(dyn) invoked "+callee+"."+method);

		return originalReturnedObject;
	}
	
	/**
	 * <code>-adapt reflect</code>
	 */
	protected Object adapt(Object callee, Method m, Object [] args[]) throws Throwable {
		return adapt(callee, m.getName(), args);
	}
	

	/************************************ proxy creation / wrapping of args/return values ***********************************/

	/**
	 * transparent proxy creation in case <code>-proxy ifc</code> and hidden is returnvalue of a method
	 *
	 * Create a proxy instance (if none exists) for the object and return the proxy instead
	 * The Class loader of the objects components proxy component is used to load the proxy class.
	 * Do NOT invoke any method (e.g. toString) on the proxy before registration has been finished!
	 * 
	 * Assumes that the proxy class for the given object already exists in the proxy component of the given object
	 * 
	 * @param objectToBeHidden
	 * @return a proxy instance that hides the existing Object given
	 * @throws Exception
	 */
	public final IProxy wrapOutgoingObject(Object objectToBeHidden, String declaringClass) throws Exception {
		// search proxy for this value // same proxy object for the same original object,reference comparison == supported

		Object proxy = proxies.getStableProxyOf(objectToBeHidden);
		if (proxy instanceof IProxy) {
			if (LOG.isInfoEnabled()) {
				LOG.info(
					"(1) [STABLE]     wrapOutgoing: found EXISTING proxy "
						+ ((IProxy) proxy).dump()
						+ " @ "
						+ proxy.getClass().getClassLoader());
				// + " for outgoing " + objectToBeHidden);
			}
			return (IProxy) proxy;

		}
		UpgradeableComponentResource hiddenCop =
			(UpgradeableComponentResource) UpgradeableComponentResourceFactory.getComponentResourceByContent(
				declaringClass);

		ClassLoader cl = hiddenCop.getProxyLoader();
		originalsWithoutAProxy.add(objectToBeHidden);
		if (LOG.isInfoEnabled()) {
			LOG.info("(1) [STABLE]     wrapOutgoing: creating a proxy for original " + objectToBeHidden);
			/// + " in cop " + proxyCop);
		}
		//create a proxy object using the proxy class' "copy constructor" new $proxyClassName(Object) -> register
		IProxy newProxy = TransparentProxyFactory.newProxyInstance(cl, objectToBeHidden);

		return newProxy;

	}

	/**
	 * transparent proxy creation in case <code>-proxy ifc</code> and hidden is argument to a method invocation
 	 *
	 * Wrap objects that do not belong to the callee's component into transparent proxies.
	 * This includes arguments that are passed to the callee, e.g. a client may implement
	 * an interface declared in callee's component and the callee offers a method to receive
	 * objects that implement this interface. To avoid given external references away,
	 * the argument is hidden behind a transparent proxy that implements the same interface(s).
	 * 
	 * @param objectToBeHidden
	 * @paramincomingClassName - the name of the class of the callee (becomes the name of the proxy class)
	 * @return
	 * @throws Exception
	 */
	public final Object wrapIncomingObject(Object objectToBeHidden, String incomingClassName) throws Exception {
		// hiddenCop = cop of callee
		IComponentResource hiddenCop =
			UpgradeableComponentResourceFactory.getComponentResourceByContent(incomingClassName);
		AdapterComponentResource migrCop = AdapterComponentResourceFactory.getAdapterComponentFor(null, hiddenCop);
		ClassLoader loader = migrCop.getClassLoader();

		// unstable proxy for client objects that must implement the current version of a service interface
		// on upgrade of the client cop, there exist already a suitable proxy object (the one hiding the old client obj)
		// Just have to get this proxy and assign new version client object as its hidden object. 

		// check if arguments are assignable to formal parameters 
		// This is not the case if an external object (e.g.client) implements an interface
		// and that Object is passed as argument: The interface implemented by the client
		// is declared in the proxy cop while the formal parameter is declared to be of 
		// the current version of the interface (e.g. escop v3))
		// Must insert an adapter in between to map calls on the current interface methods to the one
		// in the proxy interface to guarantee version independence and type safety.
		// Otherwise, a ClassCastException is thrown in the (custom) adapter.adapt() method

		// search proxy for this value // same proxy object for the same original object,reference comparison == supported
		Object proxy = proxies.getUnstableProxyOf(objectToBeHidden);
		if (proxy instanceof IProxy) {
			if (LOG.isInfoEnabled()) {
				LOG.info(">>>>>");
				LOG.info(
					"(1) [UNSTABLE]: wrapIncoming: found EXISTING proxy "
						+ ((IProxy) proxy).dump()
						+ " @ "
						+ proxy.getClass().getClassLoader()
						+ " for incoming "
						+ objectToBeHidden);
			}
			return (IProxy) proxy;

		}

		String ifcsToString = new String();
		String cn = objectToBeHidden.getClass().getName();
		// use existing adapter
		IAdapter handler = getCustomAdapter(cn);

		Vector newIfcs = new Vector();
		Class[] ifcs = Decode.getInterfaces(objectToBeHidden.getClass());
		// contains proxy interfaces and client defined ones (unknown to the service)
		for (int i = 0; i < ifcs.length; i++) {
			ProxyComponentResource proxyCop = ProxyComponentResourceFactory.getResourceOfProxy(ifcs[i].getName());
			if (null != proxyCop) {
				// proxy interface, exchange by hidden one

				try {
					Class hiddenIfc = migrCop.loadClass(ifcs[i].getName(), true);
					
					if (ifcs[i] == hiddenIfc) {
						LOG.error("[FAILED] to load hidden interface "+ifcs[i]+" from adapter "+migrCop);
					} else {
						LOG.info(
												"wrapIncoming "
													+ objectToBeHidden
													+ ": Replaced "
													+ hiddenIfc.getName()
													+ " @ "
													+ ifcs[i].getClassLoader()
													+ " by the one @ "
													+ hiddenIfc.getClassLoader()
						);
					}
					ifcs[i] = hiddenIfc; // should be visible from migrCop (deps = hiddenCop)
					newIfcs.add(hiddenIfc);
				} catch (Exception c) {
					// ifc is unknown to the loader, skip it
					//LOG.info("skipping unknown proxy ifc " + ifcs[i] + " for loader of cop " + migrCop);
				}
			} else {
				//if (ifc is in callee's cop)
				// interface is declared in the component of the objectToBeHidden, is unknown to the loader, skip it
				//LOG.info("skipping unknown ifc " + ifcs[i] + " for loader of cop " + migrCop);
			}
		}
		Class[] replacedIfcs = null;
		if (newIfcs.size() != ifcs.length) {
			// some were excluded

			//LOG.info(newIfcs.size() + " afterwards: interfaces = " + newIfcs.toString());
			replacedIfcs = new Class[newIfcs.size()];
			Iterator iter = newIfcs.iterator();
			int i = 0;
			while (iter.hasNext()) {
				replacedIfcs[i] = (Class) iter.next();
				ifcsToString += replacedIfcs[i].toString() + " , ";
				i++;
			}
			//LOG.info("removed unknown ifcs, remaining "+ifcs.length);
		} else {
			replacedIfcs = ifcs;
			for (int i = 0; i < replacedIfcs.length; i++) {
				ifcsToString += replacedIfcs[i].toString() + " , ";
			}
		}
		if (replacedIfcs != null && replacedIfcs.length > 0) {
			if (LOG.isInfoEnabled()) {
				LOG.info("(1) [UNSTABLE]: wrapIncoming: creating incoming proxy ("
				/* + loader + " , " + ifcsToString + " " + handler+*/
				+"" + ") for object " + objectToBeHidden);
			}

			DefaultAdapter.originalsWithoutAProxy.add(objectToBeHidden);

			// compiles, loads, instantiates and registers the proxy
			IProxy dynProxy =
				TransparentProxyFactory.newProxyInstance((IClassLoaderWithClassPath) loader, replacedIfcs, objectToBeHidden);
			
			return dynProxy;
		} else {
			LOG.error("ERROR: dynamic proxy: interface list is null of " + objectToBeHidden);
			return objectToBeHidden;
		}
	}

	
	/**
	 * creates dynamic proxies (java dynamic proxy API) instead of transparent proxies if
	 * <code>-proxy ifc</code> is <em>NOT</em> specified
	 *
	 * @param toBeHidden
	 * @return
	 * @throws IllegalArgumentException
	 */
	public final Proxy createDynamicProxy(Object toBeHidden) throws IllegalArgumentException {

		Proxy dynProxy = proxies.getProxyOf(toBeHidden);
		if (dynProxy == null) {
			Class c = toBeHidden.getClass();
			Class [] impl = Decode.getInterfaces(c);
			// begin debugging
			//Vector ifcs = new Vector();
			//for (int i = 0; i < impl.length; i++) {
			//	ifcs.add(impl[i]);
			//}
			//LOG.info("Proxy.newProxyInstance("+toBeHidden.getClass().getClassLoader()+", "+ifcs.toString()+", "+toBeHidden+")");
			// end of debugging
			IAdapter adapter = getCustomAdapter(c.getName());
			
			dynProxy =
				(Proxy) Proxy.newProxyInstance(
					toBeHidden.getClass().getClassLoader(),
					impl,
					adapter);
			proxies.register(dynProxy, toBeHidden);
			//LOG.info("created a dynamic proxy for "+toBeHidden);
		}
		return dynProxy;
	}


	/************************************ proxy registration ***********************************/

	/**
	 * initializes the mapping from a proxy object to its original object
	 * an original object corresponding to the given proxy object is created if n/a
	 * 
	 * This method should be called before the first invocation on a method of a proxy occurs 
	 * (e.g. in its constructor). Otherwise, the adapter or hidden object may not yet have been 
	 * initialized and assoicated to the proxy object.
	 * 
	 * @param proxy
	 * @param initializationArgs - the arguments passed to the constructor of the proxy
	 */
	public final void register(IProxy proxy, Object[] initializationArgs) throws WrappingException {
		Object original = null;
		String args = new String();

		if ((initializationArgs != null)
			&& (initializationArgs.length == 1)
			&& originalsWithoutAProxy.contains(initializationArgs[0])) {
			// new Proxy(original)

			original = initializationArgs[0];
			boolean contained = originalsWithoutAProxy.remove(original);

			if (!contained) {
				WrappingException w =
					new WrappingException(" cannot create a proxy for an existing original that is not in originalsWithoutAProxy");
				LOG.error(this, w);
				throw w;
			}
			// be careful: initArg of copy constructor may be a proxy, too: new FilterImpl(new StringEvent())
			if (original instanceof IProxy) {
				original = proxies.get(original); 
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("(2)             register: wrapping EXISTING object " + original + " ...");
			}
			// proceed to proxy registration
		} else {
			// on creation of a proxy, need to create original (hidden) object, too
			String className = proxy.getClass().getName();
			IComponentResource cop = ProxyComponentResourceFactory.getResourceOfProxy(className);
			if (cop instanceof ProxyComponentResource) {
				/*LOG.info(
					"registration of proxy "
						+ className
						+ " in cop "
						+ ((ProxyComponentResource) cop).getExtResLocation()
						+ cop.getCodeBase());
				*/
				UpgradeableComponentResource origCop = ((ProxyComponentResource) cop).getOriginalComponent();
				if (origCop.contains(className)) { // look for a class having the same name
					Class originalClass;
					try {
						ClassLoader loader = origCop.getClassLoader();
						originalClass = loader.loadClass(className);
						//LOG.info("register proxy: Loading original from "+origCop.getClassPath()+", version "+origCop.getVersion());
						if (initializationArgs == null || initializationArgs.length == 0) {

							original = originalClass.newInstance();

						} else {
							// check if arguments have to be unwarpped (matched to hidden objects), too
							for (int i = 0; initializationArgs != null && i < initializationArgs.length; i++) {
								if (initializationArgs[i] == null) {
									throw new RuntimeException(className + " register: arg is null " + i);
								}
								if (initializationArgs[i] instanceof IProxy) {
									ClassLoader cl = initializationArgs[i].getClass().getClassLoader();
									if (cl instanceof ProxyLoader) {
										//int hash = ((IProxy) initializationArgs[i]).dump();
										initializationArgs[i] = proxies.get(initializationArgs[i]);
										
										/*LOG.info(
											" found EXISTING outgoing proxy (hash = "
												+ hash
												+ ") @ "
												+ cl
												+ " for initArg["
												+ i
												+ "]: replaced by hidden "
												+ initializationArgs[i]);*/
									} else {
										LOG.error(
											" found EXISTING incoming proxy @ " + cl + " for initArg[" + i + "] ???");
									}
								}
							}
							Class[] params = Decode.getClasses(initializationArgs);
							
							Constructor init = Decode.getMatchingConstructor(originalClass, params);

							original = init.newInstance(initializationArgs);
							// create original object to be hidden by the proxy

						}
						if (LOG.isInfoEnabled()) {
							LOG.info(
								"(2)             register: created new() original = "
									+ original
									+ " @ "
									+ loader
									+ " for proxy "
									+ proxy.dump()
									+ " @ "
									+ cop.toString()
									+ " ...");
						}
					} catch (Exception e) {
						WrappingException f = new WrappingException(e);
						LOG.error(this, e);
						throw f;
					}
				} else {
					WrappingException f = new WrappingException("Original class not found in proxy");
					LOG.error(proxy, f);
					throw f;
				}
			} else {
				WrappingException f = new WrappingException("ProxyCop not found");
				LOG.error(proxy, f);
				throw f;
			} // ProxyCop
		} // copy

		// registration 
		proxies.register(proxy, original);
		IAdapter redirectToOriginal = getCustomAdapter(proxy);
		redirectToOriginal.setOriginal(original); // avoid lookup proxy -> hidden
		proxy.setInvocationHandler((IAdapter) redirectToOriginal); // proxy -> adapter
		if (original instanceof IProxy) {
			LOG.error(" original must NOT be a proxy itself!!!");
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("(3)       ...   Registration finished: proxy " + proxy.dump() + " -> adapter "
			//+ redirectToOriginal
			+" -> " + proxies.get(proxy) + "\n");
		}
	}

	public final void unregister(IProxy proxy) {
		proxies.remove(proxy);
	}


	/************************************ custom adapter ***********************************/

	/**
	 * looks for a custom adapter class for the given proxy and returns a new instance of it.
	 * This method may return different objects depending on the hidden
	 * component version of the proxy component that p is defined in.
	 * @param p
	 * @return
	 */
	private IAdapter getCustomAdapter(IProxy p) {

		String name = p.getClass().getName();
		return getCustomAdapter(name);
	}

	private IAdapter getCustomAdapter(String name) {

		// load adapter class
		ProxyComponentResource proxyCop = ProxyComponentResourceFactory.getResourceOfProxy(name);
		UpgradeableComponentResource cop;
		if (proxyCop != null) {
			cop = proxyCop.getOriginalComponent();
		} else {
			cop =
				(UpgradeableComponentResource) UpgradeableComponentResourceFactory.getComponentResourceByContent(name);
		}

		try {
			Class adapter = cop.getCustomAdapter(name);
			if ((adapter != null) && (IAdapter.class.isAssignableFrom(adapter))) {
				IAdapter customAdapter = (IAdapter) adapter.newInstance();
				/*LOG.info(
					"found custom adapter "
						+ customAdapter
						+ " for original cop "
						+ cop.getCodeBase()
						+ " version "
						+ cop.getVersion());*/
				return customAdapter;
			} else if (adapter != null) {
				LOG.error(
					"CustomAdapter "
						+ adapter.getName()
						+ " does not implement the "
						+ IAdapter.class.getName()
						+ " interface, using default adapter");
				return Instance();
			} else {
				LOG.error("CustomAdapter is null");
				return Instance();
			}
		} catch (ClassNotFoundException e) {
			LOG.error("No CustomAdapter found for " + name + ", using the default adapter");
			return Instance();
		} catch (InstantiationException e) {
			LOG.error("Could not instantiate the custom adapter, using the default adapter", e);
			return Instance();
		} catch (IllegalAccessException e) {
			LOG.error("Could not access the custom adapter, using the default adapter");
			return Instance();
		} catch (OnlineUpgradeFailedException e) {
			LOG.error("No migr cop found for " + name + ", using the default adapter");
			return Instance();
		}
	}

	/**
	 * updates the mapping from proxy to hidden object oldObj by replacing it by newObj
	 * Main objs, just for use of IEvolutionManager 
	 * @param oldObj
	 * @param newObj
	 */
	public void updateOriginal(Object oldObj, Object newObj) {
		if (oldObj instanceof IProxy || newObj instanceof IProxy)
			throw new WrappingException(" original onjects may not be proxy objects themselves");
		if (this.hidden == oldObj) {
			this.hidden = newObj;
		}

		if (ComponentEvolutionMain.doHideIfc()) {
			IProxy proxy = null; // transparent proxy
			// TODO: How to match NON singleton objects (how to find corresponding objects in both version, how to access these)
			// original may occur more than once
			Iterator mapping = proxies.entrySet().iterator();
			while (mapping.hasNext()) {
				Map.Entry element = (Map.Entry) mapping.next();
				if (element.getValue() == oldObj) {
					element.setValue(newObj);
					proxy = (IProxy) element.getKey();
					LOG.info(" proxy " + proxy.dump() + " for " + oldObj + " now redirects to " + newObj);
					IAdapter redirectToOriginal = getCustomAdapter(proxy);
					proxy.setInvocationHandler(redirectToOriginal);
					redirectToOriginal.setOriginal(newObj);
				}
			}
			if (proxy == null) {
				LOG.info(" (cop was not in use?) unknown proxy for " + oldObj);
				return;
			}
		} else {
			// dynamic proxy
			proxies.updateHiddenObject(oldObj, newObj);
		}

	}
	
	/************************************ proxy registry update ********************************************/

	/**
	 * on runtime evolution, after state migration
	 * Given a mapping function from old version objects to their new version counterparts
	 * (as filled by <code>DefaultUpgradeStrategy.transferState</code>), replaces the
	 * object hidden by the given proxy by its new version object
	 * @param p -  proxy that emains stable during current evolution step
	 * @param old2new - object mapping
	 */
	public static final void evolveMapping(IProxy p, HashMap old2new) {
		Object oldHidden = proxies.remove(p);
		if (oldHidden == null) {
			LOG.error(
				"evolveMapping: hidden object not found of proxy "+ p.dump());
			return;
		}
		Object newHidden = old2new.get(oldHidden);
		if (newHidden == null) {
			LOG.info(
				"[UDPATE]   evolveMapping: no object corresponding to old hidden object "
					+ oldHidden
					+ " found. Is proxy "
					+ p.dump()
					+ " already up to date? ");
			return;
		}
		
		IAdapter adapter = Instance().getCustomAdapter(p);
		p.setInvocationHandler(adapter);
		adapter.setOriginal(newHidden);
		//
		LOG.info(
			" [UDPATE]   replaced "
				+ oldHidden
				+ " @ "
				+ oldHidden.getClass().getClassLoader()
				+ " BY "
				+ newHidden
				+ " @ "
				+ newHidden.getClass().getClassLoader()
				+ " in proxy "
				+ p.dump());
		proxies.register(p, newHidden);

	}

	/**
	 * In case of runtime evolution, it may occur that an unstable proxy has to be created anew, while the hidden object remains valid.
	 * This is the case when the hidden object belongs not to the component evolving, but is stored in a variable inside the evolving component, for example.
	 * 
	 * @param registeredProxy
	 * @return
	 * @throws Exception
	 */
	public static final IProxy updateUnstableProxy(IProxy registeredProxy, IComponentResource evolvingCop) throws Exception {
		Object hidden = proxies.removeProxy(registeredProxy);
		if (hidden == null) {
			LOG.error("[FAILED]   to update unstable proxy "+registeredProxy+": hidden is NULL or proxy is NOT registered ");
			//throw new OnlineUpgradeFailedException("[FAILED]   to update unstable proxy "+registeredProxy+": hidden is NULL ");
			return null;
		}
		LOG.info(
			"[UDPATE]   updateUnstableProxy("
				+ registeredProxy.dump()
				+ "): creating new transparent proxy to hide stable object "
				+ hidden);
		IProxy newProxy = (IProxy) Instance().wrapIncomingObject(hidden, evolvingCop.getClassName());
		// debug only
		String impl ="";
		Class[] ifcs = newProxy.getClass().getInterfaces();
		for (int i = 0; i < ifcs.length; i++) {
			impl += ","+ifcs[i]+" @ "+ifcs[i].getClassLoader();
		}
		//LOG.info("[ ??? ]   are these Hidden IFCS? "+impl);
		// end of debug
		IAdapter adapter = Instance().getCustomAdapter(newProxy);
		adapter.setOriginal(hidden);
		newProxy.setInvocationHandler(adapter);
		proxies.register(newProxy, hidden);
		return newProxy;
	}

	public static final Proxy updateUnstableProxy(Proxy registeredDynamicProxy) throws Exception {
		Object hidden = proxies.remove(registeredDynamicProxy);
		LOG.info(
			"[UDPATE]   updateUnstableProxy("
				+ registeredDynamicProxy.hashCode()
				+ "): creating new dynamic proxy to hide stable object "
				+ hidden);
		return (Proxy) Instance().createDynamicProxy(hidden);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.proxy.IProxyInvocationHandler#setOriginal(java.lang.Object)
	 */
	public void setOriginal(Object original) {
		this.hidden = original;
	}

	/**
	 * @return an iterator over a clone of all registered proxies 
	 */
	public static Iterator getRegisteredProxies() {
		if (ComponentEvolutionMain.doHideIfc()) {
			ProxyRegistry reg = (ProxyRegistry) proxies.clone();
			return reg.keySet().iterator();
		} else {
			return proxies.dynamicProxies();
		}
	}
	
	/**
	 * for debugging purposes only
	 * prints a list of all proxies currently registered
	 * along with their hidden objects to the logger given
	 */
	public static void printProxies(Logger log, String identifier) {
		final int MAX_LENGTH = 30;
		log.info("\n------+--------------------------------+--------------------------------+");
		log.info("      | "+System.currentTimeMillis()+": ProxyRegistry content "+identifier+"           |");
		log.info(" kind |           proxy object         |      hidden object             |");
		log.info("------+--------------------------------+--------------------------------+");
		ProxyRegistry clone = (ProxyRegistry) proxies.clone();
		Iterator mappings = clone.entrySet().iterator();
		while (mappings.hasNext()) {
			Map.Entry proxy2hidden = (Map.Entry) mappings.next();
			String key = ""+((IProxy)proxy2hidden.getKey()).dump();
			String val = ""+proxy2hidden.getValue().toString();
			if (key.length() >= MAX_LENGTH) {
				key = key.substring(key.length() - MAX_LENGTH, key.length());
			} else {
				for (int i = key.length(); i < MAX_LENGTH; i++) {
					key += " ";	
				}
			}
			if (val.length() >= MAX_LENGTH) {
				val = val.substring(val.length() - MAX_LENGTH, val.length());
			} else {
				for (int i = val.length(); i < MAX_LENGTH; i++) {
					val += " ";	
				}
			}
						
			log.info("      | "+key+" | "+val);
		}
		
		/*try {
			File dir = new File("dump");
			if (!dir.exists()) {
				dir.createNewFile();
			}
			File file = new File(dir,"ProxyRegistry");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileOutputStream stream = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(stream);
			out.writeObject(proxies);
		} catch (IOException io) {
			LOG.error(io+" occurred on dumping ProxyRegistry");
		}*/
	}
	
	

}
