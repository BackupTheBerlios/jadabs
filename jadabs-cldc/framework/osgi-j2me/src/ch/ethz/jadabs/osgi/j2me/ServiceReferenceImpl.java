/*
 * Created on Jul 15, 2004
 * $Id: ServiceReferenceImpl.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.osgi.j2me;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;


/**
 * This is a stripped down implementation of the OSGi API 
 * ServiceReference interface.
 * 
 * <p><b>Note:</b>Only class name reference is implemented.
 * Properties are <b>not</b> implemented.</p>
 * 
 * @author andfrei
 * @author Ren&eacute; M&uuml;ller
 */
public class ServiceReferenceImpl implements ServiceReference
{
    /** class name of the referenced service */
    String clname;
    
    /**
     * Create new service reference associtated to
     * the service given the classname.
     * 
     * @param clname Classname of the referenced service 
     */
    public ServiceReferenceImpl(String clname)
    {
        this.clname = clname;
    }
    
    /**
     * <b>Not implemented</b>: Returns the property value 
     * to which the specified property key is mapped in the
     * properties <tt>Dictionary</tt> object of the service 
     * referenced by this <tt>ServiceReference</tt> object.
     * 
     * @param key The property key.
     * @return always <code>null</code> 
     */
    public Object getProperty(String key)
    {
        return null;
    }

    /**
     * <b>Not implemented</b>: Returns an array of the keys in 
     * the properties <tt>Dictionary</tt> object of the
     * service referenced by this <tt>ServiceReference</tt> object.
     * 
     * @return always <code>null</code> 
     */
    public String[] getPropertyKeys()
    {
        return null;
    }

    /**
     * <b>Not implemented</b>: Returns the bundle that registered 
     * the service referenced by this <tt>ServiceReference</tt> object.
     * 
     * @return always <code>null</code> 
     */
    public Bundle getBundle()
    {
        return null;
    }

    /**
     * <b>Not implemented</b>: Returns the bundles that are using 
     * the service referenced by this <tt>ServiceReference</tt> object.
     * 
     * @return always <code>null</code> 
     */
    public Bundle[] getUsingBundles()
    {
        return null;
    }
}
