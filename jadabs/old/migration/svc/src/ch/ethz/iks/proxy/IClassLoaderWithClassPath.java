package ch.ethz.iks.proxy;

/**
 * ClassLoaders may implement this interface to expose their classpath
 * where they load binaries from. 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface IClassLoaderWithClassPath {
	
	public String getClassPath();
	
	public Class loadClass(String className) throws ClassNotFoundException;

}
