package ch.ethz.iks.proxy;

/**
 * Not in use as the realizing class TransparentProxyFactory declares this methods as static.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface IProxyFactory {
	
	public IProxy newProxyInstance(IClassLoaderWithClassPath loader, Class toHide, Object[] initArgs) throws Exception;
	
	public IProxy newProxyInstance(ClassLoader loader, Object toHide) throws Exception;
	
	public IProxy newProxyInstance(IClassLoaderWithClassPath loader, Class[] ifcsToImpl, Object toHide) throws Exception;
	
	public Class getProxyClass(IClassLoaderWithClassPath loader, Class toImplementAProxyFor) throws WrappingException;
	
	public Class getProxyClass(IClassLoaderWithClassPath loader, Class[] ifcsToImpl, Class toImplementAProxyFor)
	throws WrappingException;
	
	

}
