/*
 * Created on Oct 1, 2003
 *
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
package ch.ethz.iks.testclient;

import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.testcop.ITestCop;
import ch.ethz.iks.testcop.ITestListener;
import ch.ethz.iks.utils.DependencyInspector;

/**
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class ComponentUsingAnotherOne extends Thread implements IComponent, DependencyInspector, ITestListener {

	private boolean stopped;
	private static ComponentUsingAnotherOne copWithDependency;
	private IComponent externalComponent;

	public static void main(String[] args) {
		if (copWithDependency != null && !copWithDependency.isAlive()) {
			// already initialized
			copWithDependency.startComponent(args);
			return;
		}
		// ordinary main
	}
	
	private ComponentUsingAnotherOne() {
		super();
	}

	public static ComponentUsingAnotherOne createComponentMain() {
		if (copWithDependency == null) copWithDependency = new ComponentUsingAnotherOne();
		return copWithDependency;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#initComponent()
	 */
	public void init(IComponentContext ctx) {
		this.stopped = false;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {
		copWithDependency.start();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#stopComponent()
	 */
	public void stopComponent() {
		this.stopped = true;

	}
 
	public void run() {
		while (!stopped) {
			
			ComponentResource copRes;
			IComponent cop = createTCService();
			System.out.println(" ***** TCM = "+cop);
			
			//ComponentResource copVersion = (ComponentResource) ComponentRepository.Instance().getComponentResourceByCodebase("testcop.jar");
			// using dynamic proxy ITestComponentMain t = (ITestComponentMain) Proxy.newProxyInstance(copVersion.getClassLoader(), new Class [] {ITestComponentMain.class}, ComponentEvolution.getUpgradeManager(copVersion, null));
			print(cop);
			ITestCop tc = ((ITestCop)cop);
			tc.nop();
			System.out.println(" ***** getInternalObj returns = "+tc.getInternalObj());
			tc.subscr((ITestListener)this);
			
			//this.externalComponent = TestComponentMainWrapper.newExternalInstance();
			//print();
			try {
				Thread.sleep(11000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			
		}

	}

	private IComponent createTCService() {
		//IComponent cop = TestComponentMain.Instance();
		ComponentResource copRes = (ComponentResource) ComponentRepository.Instance().getComponentResourceByClassname("ch.ethz.iks.testcop.TestComponentMain");
		IComponent cop = copRes.getExtObject();
		return cop;
	}

	public void print(IComponent cop) {
		System.out.println(
			"ComponentUsingAnotherOne: external Component is of Type " + cop.getClass().getName() +" loaded by ClassLoader "+ cop.getClass().getClassLoader().toString());

	}

	/**
	 * @return
	 */
	public IComponent getExternalReference() {
		//IComponent cop = TestComponentMain.Instance();
		IComponent cop = createTCService();
		return cop;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.test_cop.DependencyInspector#getClassLoaderOfExternalReference()
	 */
	public ClassLoader getClassLoaderOfExternalReference() {
		return createTCService().getClass().getClassLoader();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.testcop.ITestListener#react(ch.ethz.iks.testcop.ITestCop)
	 */
	public void react(ITestCop tc) {
		System.out.println(
					" ***** ComponentUsingAnotherOne: reacted on external Type " + tc.getClass().getName() +" loaded by ClassLoader "+ tc.getClass().getClassLoader().toString());

	}
	
	

}
