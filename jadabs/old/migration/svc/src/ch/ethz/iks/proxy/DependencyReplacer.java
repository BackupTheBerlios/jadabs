package ch.ethz.iks.proxy;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;
import ch.ethz.iks.proxy.cop.ProxyLoader;

/**
 * Scans class files being loaded from inter component dependencies (see superclass).
 * An object used outside of the component it declares is called an external object.
 * External references found are hidden behind a transparent proxy (see TransparentProxyFactory).
 * 
 * The task of this class is to get the component the external object is referenced in (user), 
 * the one declaring the external object (owner) and the proxy component of the owner (proxy).
 * 
 * It then manipulates the dependency specification of the user component by replacing 
 * owner by proxy. Before referenciation of the external object, the owner thus loades
 * the proxy class instead of the hidden class in the owner.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class DependencyReplacer extends ExternalReferenceScanner {
	
	private static Logger LOG = Logger.getLogger(DependencyReplacer.class);
	
	
	public DependencyReplacer(String cp, int version) {
		super(cp, version);
	}
	/* (non-Javadoc)
	 * @see ch.ethz.iks.proxy.ExternalConstructorReplacer#foundExternalReference(ch.ethz.iks.cop.IComponentResource, java.lang.String, ch.ethz.iks.evolvable.cop.UpgradeableComponentResource)
	 */
	/**
	 * Called upon detection of an external reference of a type named <code>externalClassName</code> inside the component <code>insideCop</code>.
	 * 
	 * 
	 * @param insideCop
	 * @param externalClassName
	 * @param copOfExternalClass
	 */
	protected void foundExternalReference(IComponentResource insideCop, String externalClassName, UpgradeableComponentResource copOfExternalClass)  {
		super.foundExternalReference(insideCop, externalClassName, copOfExternalClass);
		IComponentResource proxyCop = null;
		try {
			ProxyLoader proxyLoader = copOfExternalClass.getProxyLoader();
			ClassLoader origLoader = copOfExternalClass.getClassLoader();
			if (proxyLoader == null) 
				LOG.error(" -> found external "+externalClassName+" @ "+origLoader+" , proxyloader = "+proxyLoader);
			hide(proxyLoader, origLoader.loadClass(externalClassName) );
			String cb = ProxyComponentResourceFactory.getProxyCodebase(copOfExternalClass);
			proxyCop = ComponentRepository.Instance().getComponentResourceByCodebase(cb);
			replaceDependency(insideCop, copOfExternalClass, proxyCop);
			
		} catch (WrappingException w) {
			LOG.error("foundExtRef caught",w);
			throw w; // throw up to ECL.loadClassBytes
		} catch (ClassNotFoundException e) {
			LOG.error("foundExtRef FAILED to load class to be hidden "+externalClassName+" from "+copOfExternalClass.getClassLoader(),e);
		}
		
	}
	
	/**
	 * replaces the dependency <code>oldDependency</code> of component <code>copWithDependency</code> by <code>newDependency</code>.
	 * Thus, the <code>copWithDependency</code> will delegate the loading of classes from dependency to its new dependency component (that is\
	 * a proxy for the old one normally) from now on.
	 * 
	 * @param copWithDependency
	 * @param oldDependency
	 * @param newDependency
	 */
	private void replaceDependency(IComponentResource copWithDependency, IComponentResource oldDependency, IComponentResource newDependency) {
		if (copWithDependency instanceof UpgradeableComponentResource) {
			if (ComponentEvolutionMain.doHideIfc()) {
				((UpgradeableComponentResource)copWithDependency).replaceComponentDependency(oldDependency.getCodeBase(), newDependency.getCodeBase() );
				//LOG.error(" -> Replaced dependency "+oldDependency.getCodeBase()+" by "+newDependency.getCodeBase()+" in component "+copWithDependency.getCodeBase());
			} 
		}
	}
	
	/**
	 * initiates the creation of a proxy class for the class to be hidden and returns the (porxy) component resource the proxy class is loaded in.
	 * 
	 * @param proxyLoader
	 * @param toHide
	 * @return
	 * @throws WrappingException
	 */
	public void hide(ProxyLoader proxyLoader, Class toHide) throws WrappingException {
		if (ComponentEvolutionMain.doHideIfc()) {
			Class proxy = TransparentProxyFactory.getProxyClass(proxyLoader, toHide);
		} else {
			///Class dynProxy = Proxy.getProxyClass(proxyLoader, toHide.getInterfaces());
		}
		//return ProxyComponentResourceFactory.getResourceOfProxy(proxy.getName()); 
	}
	
	protected void manipulate(String classname, CtClass toManipulate, int extRefCount) throws NotFoundException, CannotCompileException {}

}
