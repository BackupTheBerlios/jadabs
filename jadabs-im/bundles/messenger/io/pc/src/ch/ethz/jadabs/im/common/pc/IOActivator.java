/*
 * Created on 26 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.ethz.jadabs.im.common.pc;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.api.IOProperty;

/**
 * @author Franz Terrier
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IOActivator  implements BundleActivator {
	static BundleContext bc;
	
	FileIOProperty ioProp;
	
	public void start(BundleContext bc) throws Exception {
		IOActivator.bc = bc;
		
		String configfile = bc.getProperty("ch.ethz.jadabs.im.configfile");
		System.out.println(configfile);
		
		ioProp = new FileIOProperty(configfile);
		
		Hashtable dict = new Hashtable();
		dict.put("buddy","false");
		bc.registerService(IOProperty.class.getName(), ioProp, dict);
		
		ioProp = new FileIOProperty();
		dict.clear();
		dict.put("buddy", "true");
		bc.registerService(IOProperty.class.getName(), ioProp, dict);
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
