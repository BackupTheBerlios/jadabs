package ch.ethz.iks.evolution.state;

import java.lang.reflect.Method;

import ch.ethz.iks.utils.Decode;

/**
 * NOT YET IN USE - Planned to hold service requests
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class MethodInvocation {
	
	public MethodInvocation(String methodName, Object[] args) {
		this.args = args;
		this.name = methodName;
	}
	
	public MethodInvocation(Method method, Object [] args) {
		this.args = args;
		this.name = method.getName();
	}
	
	public void setCallee(Object callee) {
		this.callee = callee;
	}
	
	public Object getCallee() {
		return this.callee;
	}
	
	public void execute (Object callee) throws Throwable {
		callee.getClass().getMethod(this.name, Decode.getClasses(this.args)).invoke(callee, this.args);
	}
	
	private Object [] args;
	private String name;
	private Object callee;

}
