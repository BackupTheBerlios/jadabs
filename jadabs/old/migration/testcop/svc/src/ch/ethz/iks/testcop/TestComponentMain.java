/*
 * Created on Sep 3, 2003
 *
 */
package ch.ethz.iks.testcop;

import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentContext;


/**
 * @author andfrei
 *
 */
public class TestComponentMain extends Thread implements IComponent, ITestCop {

	private static Logger LOG = Logger.getLogger(TestComponentMain.class);
	private static final int version = ComponentRepository.Instance().getComponentResourceByClassname(TestComponentMain.class.getName()).getVersion();
	private boolean running = true;
	private static TestComponentMain tc = new TestComponentMain();
	
	private TestComponentMain() {
		super();
		LOG.info("called constructor "+this);
	}
	
	Vector outer = new Vector();
	{
		outer.setSize(2);
	}

	public static void main(String[] args){
		
	}

	/**
	 * @see ch.ethz.iks.cop.IComponent#initComponent()
	 */
	public void init(IComponentContext ctx) {
		LOG.info("called initComponent");
		LOG.info("---> class file version "+version);
	}

	/**
	 * @see ch.ethz.iks.cop.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {
		LOG.info("called startComponent "+this);
		
		start();
	}

	public void run(){
		
		while(running){
			
			LOG.info(createString());
		if (listener != null) {
						listener.react(tc);
					}
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
	}

	public String createString(){
		
		Vector tmp = new Vector();
		tmp.setSize(2);
		tmp.setElementAt(version+": test string",0);
		
		outer = tmp;
		
		String result = version+": this is the original string";
		
		return (String)outer.elementAt(0);
	}

	/**
	 * @see ch.ethz.iks.cop.IComponent#stopComponent()
	 */
	public void stopComponent() {
		
		running = false;
		LOG.info("called stop component");
	}

	/**
	 * @return the singleton
	 */
	public static ITestCop createComponentMain() {
		return tc; // singleton
	}
	
	public void finalize() {
		LOG.error("********* trashing "+this);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
		
	}

	/**
	 * methods for benchmarking only
	 */
	public void nop() {
		this.running = false; // empty body seems to be optimized by JIT, do something (nonsense)
	}

	
	public ITestCop getInternalObj() {
		
		return tc;
	}
	
	private ITestListener listener;
	
	public void subscr(ITestListener l) {
		
		//LOG.info("subscribing external client... ");
		//LOG.info("listener = "+ l.getClass().getName()+" loaded by "+l.getClass().getClassLoader());
		this.listener = l;
	}
	

}
