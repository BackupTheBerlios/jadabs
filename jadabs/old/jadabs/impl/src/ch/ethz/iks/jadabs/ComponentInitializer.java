/*
 * Created on Mar 11, 2003
 *
 * $Id: ComponentInitializer.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ch.ethz.iks.logger.ILogger;
import ch.ethz.iks.logger.Logger;

/**
 * @author andfrei
 */
public class ComponentInitializer extends MultiClassLoader {

	private static ILogger LOG = Logger.getLogger(ComponentInitializer.class);

	private ComponentResource extRes;

	private JarResources jarResources;
	
	public ComponentInitializer(ComponentResource extRes, String jarName){
	
		this.extRes = extRes;
	
		jarResources = new JarResources( jarName );
	}

	protected Class loadClassFromDependency(String className,
		boolean resolveIt) throws ClassNotFoundException{
		
		if (extRes != null)
			return extRes.loadClassFromDependency(className, resolveIt);
		else
			return null;
	}

	protected byte[] loadClassBytes(String className){
		
		className = formatClassName( className);
	
		return (jarResources.getResource(className));
	}

	public InputStream getResourceAsStream(String className){
				
		byte[] clAsBytes = jarResources.getResource(className);
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Loading class: " + className);
		}
		
		return new ByteArrayInputStream(clAsBytes);
	}
	
    protected void stop()
    {
        jarResources = null;
    }
	
}
