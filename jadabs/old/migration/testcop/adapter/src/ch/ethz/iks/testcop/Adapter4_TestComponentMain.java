package ch.ethz.iks.testcop;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.proxy.TransparentProxyFactory;
import ch.ethz.iks.testcop.ITestCop;
import ch.ethz.iks.testcop.ITestListener;
import ch.ethz.iks.testcop.TestComponentMain;

/**
 * adapter for TestComponentMain class implementation as in testcop component.
 * Matches incoming invocations through the transparent proxy of the TestComponentMain
 * to the current implementation of it. This adapter supports the mapping
 * of any kind of adapter as specified by the value of the <code>-adapt</code> command line
 * argument, including <code>reflect hash name</code>. 
 * => component adapter2_testcop/ for sample evolution of testcop
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class Adapter4_TestComponentMain extends DefaultAdapter {

	private static final Class adaptee = TestComponentMain.class;

	// constants representing the methods of the proxy and the adaptee object
	private static final int METHOD_NOP = TransparentProxyFactory.getMethodHash(adaptee, "nop",null);
	private static final int METHOD_GETINTERNALOBJ = TransparentProxyFactory.getMethodHash(adaptee, "getInternalObj",null);
	private static final int METHOD_TOSTRING = TransparentProxyFactory.getMethodHash(adaptee, "toString",null);
	private static final int METHOD_SUBSCR_ITESTLISTENER = TransparentProxyFactory.getMethodHash(adaptee, "subscr",new Class[] {ITestListener.class});
	private static final int METHOD_CREATECOMPONENTMAIN = TransparentProxyFactory.getMethodHash(adaptee, IComponent.factoryMethod, null);
	
	private static Logger LOG = Logger.getLogger(Adapter4_TestComponentMain.class);

	
	

	public Adapter4_TestComponentMain() {
            super();
    }
    
	/**
	 * adapts the invocation on the proxy of a TestComponentMain object to the hidden implementation.
	 * Uses the <code>hash</code> kind signature to match from the proxy method to the method of
	 * the hidden object. 
	 * TODO: finish implementation by adapting methods other than getInternalObj and toString
	 * 
	 * This implementation maps by just forwarding the invocations on the proxy to the associated hidden object
	 */
	protected Object adapt(Object callee, int methodCode, Object[] args) throws Throwable {
		if (methodCode == Adapter4_TestComponentMain.METHOD_NOP) {
			((ITestCop) callee).nop();
			return null;
		} else if (methodCode == Adapter4_TestComponentMain.METHOD_GETINTERNALOBJ) {
			return ((ITestCop)callee).getInternalObj();
		} else if (methodCode == Adapter4_TestComponentMain.METHOD_TOSTRING) {
			return callee.toString();
		} else if (methodCode == Adapter4_TestComponentMain.METHOD_SUBSCR_ITESTLISTENER) {
			 ((ITestCop)callee).subscr( (ITestListener) args[0]);
			 return null;
		} else {
			throw new NoSuchMethodException("unknown method hash: "+methodCode);
		}
	}
	
	
	
	protected Object adapt(Object callee, Method m, Object[] args) throws Throwable {
		return adapt(callee, m.getName(), args);
	}

	
	protected Object adapt(Object callee, String methodName, Object[] args) throws Throwable {
		if ("nop".equals( methodName ) ) {
			((ITestCop) callee).nop();
			return null;
		} else if ("getInternalObj".equals( methodName ) ) {
			return ((ITestCop)callee).getInternalObj();
		} else if ("toString".equals( methodName ) ) {
			return callee.toString();
		}  else if ("subscr".equals( methodName ) ) {
			 ((ITestCop)callee).subscr( (ITestListener) args[0]);
			 return null;
		} else {
			throw new NoSuchMethodException("unknown method named: "+methodName);
		}
	}

	
	protected Object adaptStatic(int methodCode, String declaringClass, Object[] args) throws Throwable {
		if (methodCode == METHOD_CREATECOMPONENTMAIN) {
			//LOG.info("adaptStatic: createComponentMain() ");
			return TestComponentMain.createComponentMain();
			
		} else {
			throw new NoSuchMethodException("unknown (static) method hash: "+methodCode);
		}	
	}

	
	protected Object adaptStatic(Method m, Object[] args) throws Throwable {
		return adaptStatic(m.getName(), m.getDeclaringClass().getName(), args);
	}

	
	protected Object adaptStatic(String methodName, String declaringClass, Object[] args) throws Throwable {
		if (IComponent.factoryMethod.equals( methodName ) ) {
			//LOG.info("adaptStatic: createComponentMain() ");
			return TestComponentMain.createComponentMain();
			
		} else {
			throw new NoSuchMethodException("unknown (static) method named: "+methodName);
		}	
	}

}
