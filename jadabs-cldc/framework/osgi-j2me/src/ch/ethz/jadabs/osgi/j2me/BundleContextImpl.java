/*
 * Created on Jul 15, 2004
 * $Id: BundleContextImpl.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.osgi.j2me;

//java.io.File does not exist on J2ME (CLDC-1.0/MIDP-2.0)
//import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;


/**
 * This is the implementation of the OSGi BundleContext. It is basically
 * a restrivted implementation of the OSGI BundleContext interface. Only 
 * those methods required by Jadabs/JXME on J2ME/CLDC are implement.
 * The implementation of the remaining method is empty.   
 * 
 * <p><b>Note:</b> The OSGi API uses <code>java.util.Dictionary</code> 
 * instead of <code>java.util.Hashtable</code> in some method arguments
 * but this had to be changed since in J2ME a Hashtable is only an
 * Object but not a Dictionary. There is no Dictionary class in J2ME. </p> 
 * 
 * @author andfrei
 * @author Ren&eacute; M&uuml;ller
 */
public class BundleContextImpl implements BundleContext
{
    /** there is only one single OSGi container in the implementation */
    static OSGiContainer osgicontainer = OSGiContainer.Instance();
    
    /**
     * Returns the value of the specified property.
	  * If the key is not found in the Framework properties, the system
	  * properties are then searched. The method returns
	  * <tt>null</tt> if the property is not found.
	  * 
     * @param key The name of the requested property.
	  * @return The value of the requested property, or <tt>null</tt> if the 
	  * property is undefined. 
     */
    public String getProperty(String key)
    {
        return osgicontainer.getProperty(key);
    }

    /**
     * <b>Not implemented</b>: Returns the <tt>Bundle</tt> object for this 
     * context bundle.
     * 
     * @return always <code>null</code>
     */
    public Bundle getBundle()
    {
        return null;
    }
    
    /**
     * <b>Not implemented</b>: Installs the bundle from the specified location 
     * string.
     * 
     * @param location The location identifier of the bundle to install.
     * @return always <code>null</code>  
     */
    public Bundle installBundle(String location) throws BundleException
    {
        return null;
    }

    /**
     * <b>Not implemented</b>: Installs the bundle from the specified 
     * <tt>InputStream</tt> object.
     * 
     * @param location The location identifier of the bundle to install.
	  * @param in The <tt>InputStream</tt> object from which this bundle will be read.
     * @return always <code>null</code>
     */
    public Bundle installBundle(String location, InputStream in) throws BundleException
    {
        return null;
    }

    /**
     * <b>Not implemented</b>: Returns the bundle with the specified identifier.
	  *
	  * @param id The identifier of the bundle to retrieve. 
     * @return always <code>null</code>
     */
    public Bundle getBundle(long id)
    {
        return null;
    }

    /**
     * <b>Not implemented</b>: Returns a list of all installed bundles. 
     * @return always <code>null</code>
     */
    public Bundle[] getBundles()
    {
        return null;
    }
   
    /**
     * <b>Not implemented</b>: Adds the specified <tt>ServiceListener</tt> 
     * object with the specified <tt>filter</tt> to this context bundle's 
     * list of listeners.
     *
     * @param listener The <tt>ServiceListener</tt> object to be added.
	  * @param filter The filter criteria. 
     */
    public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException
    {
        // not implemented!
    }

    /**
     * <b>Not implemented</b>: Adds the specified <tt>ServiceListener</tt> 
     * object to this context bundle's list of
	  * listeners.
	  *
     * @param listener The <tt>ServiceListener</tt> object to be added. 
     */
    public void addServiceListener(ServiceListener listener)
    {
        // not implemented!
    }

    /**
     * <b>Not implemented</b>: Removes the specified <tt>ServiceListener</tt> 
     * object from this context bundle's list of listeners.
	  * See {@link #getBundle}for a definition of context bundle.
	  * 
	  * @param listener The <tt>ServiceListener</tt> to be removed.
     */
    public void removeServiceListener(ServiceListener listener)
    {	
        // not implemented!
    }

    /**
     * <b>Not implemented</b>: Adds the specified <tt>BundleListener</tt> 
     * object to this context bundle's list of listeners if not already 
     * present.
     * 
     * @param listener The <tt>BundleListener</tt> to be added.
     */
    public void addBundleListener(BundleListener listener)
    {
        // not implemented!
    }

    /**
     * <b>Not implemented</b>: Removes the specified <tt>BundleListener</tt> 
     * object from this context bundle's list of listeners.
     * 
     * @param listener The <tt>BundleListener</tt> object to be removed.
     */
    public void removeBundleListener(BundleListener listener)
    {
        // not implemented!
    }

    /**
     * <b>Not implemented</b>: Adds the specified <tt>FrameworkListener</tt> 
     * object to this context bundle's list of listeners if not already present.
     * @param listener The <tt>FrameworkListener</tt> object to be added.
     * @see org.osgi.framework.BundleContext#addFrameworkListener(org.osgi.framework.FrameworkListener)
     */
    public void addFrameworkListener(FrameworkListener listener)
    {
        // not implemented!
    }

    /**
     * <b>Not implemented</b>: Removes the specified <tt>FrameworkListener</tt> 
     * object from this context bundle's list of listeners.
     * 
     * @param listener The <tt>FrameworkListener</tt> object to be removed.
     * @see org.osgi.framework.BundleContext#removeFrameworkListener(org.osgi.framework.FrameworkListener)
     */
    public void removeFrameworkListener(FrameworkListener listener)
    {
        // not implemented!
    }

    /**
     * <b>Not implemented</b>: Registers the specified service object 
     * with the specified properties under the specified class names 
     * into the Framework.
     * 
     * <p><b>Note:</b> The OSGi API uses <code>java.util.Dictionary</code> 
     * instead of <code>java.util.Hashtable</code>
     * but this had to be changed since in J2ME a Hashtable is only an
     * Object but not a Dictionary. There is no Dictionary class in J2ME. </p> 
     * 
     * @param clazzes The class names under which the service can be located.
	  * @param service The service object or a <tt>ServiceFactory</tt> object.
	  * @param properties The properties for this service.	    
     * @return always <code>null</code>
     */
    public ServiceRegistration registerService(String[] clazzes, Object service, Hashtable properties)
    {
        return null;
    }

    /**
     * 
     * 
     * <p><b>Note:</b> The OSGi API uses <code>java.util.Dictionary</code> 
     * instead of <code>java.util.Hashtable</code>
     * but this had to be changed since in J2ME a Hashtable is only an
     * Object but not a Dictionary. There is no Dictionary class in J2ME. </p>
     * 
     * @param classname The class name under which the service can be located.
     * @param obj The service object or a <tt>ServiceFactory</tt> object.
     * @param dict The properties for this service.
     * @return A <tt>ServiceRegistration</tt> object for use by the bundle
	  * registering the service to update the service's properties or to 
	  * unregister the service.
     */
    public ServiceRegistration registerService(String classname, Object obj, Hashtable dict)
    {       
        return osgicontainer.registerService(classname, obj, dict);       
    }

    /**
     * <b>Not implemented</b>: Returns a list of <tt>ServiceReference</tt> 
     * objects.
     * 
     * @param clazz The class name with which the service was registered, or
  	  * <tt>null</tt> for all services.
  	  * @param filter The filter criteria. 
     * @return always <code>null</code>
     */
    public ServiceReference[] getServiceReferences(String clazz, String filter)
    {
        return null;
    }

    /**
     * Returns a <tt>ServiceReference</tt> object for a service that 
     * implements, and was registered under, the specified class.
     * 
     * @param classname The class name with which the service was registered.
     * @return A <tt>ServiceReference</tt> object, or <tt>null</tt>
	  * if no services are registered which implement the named class.
     */
    public ServiceReference getServiceReference(String classname)
    {
        return osgicontainer.getServiceReference(classname);
    }

    /**
     * Returns the specified service object for a service.
     * 
     * @param sref A reference to the service.
     * @return A service object for the service associated with <tt>reference</tt>,
	  * or <tt>null</tt> if the service is not registered or does not implement the classes
	  * under which it was registered in the case of a Service Factory. 
     */
    public Object getService(ServiceReference sref)
    {
        return osgicontainer.getService(sref);
    }

    /**
     * <b>Not implemented</b>: Releases the service object referenced by the 
     * specified <tt>ServiceReference</tt> object.
     * 
     * @param reference A reference to the service to be released.
     * @return always <code>false</code>
     */
    public boolean ungetService(ServiceReference reference)
    {
        return false;
    }

    /**
     * Creates a <tt>File</tt> object for a file in the
	  * persistent storage area provided for the bundle by the Framework.
	  * 
	  * This method is not implemented at all! It is also removed from 
	  * the org.osgi.framework.BundleContext Interface that is implemented
	  * by this class. Why?
	  * 
	  * Why isn't there a java.io.File on J2ME/CLDC? Well, there isn't a 
	  * file system, that's the bloody point. 
	  * 
	  * @param filename A relative name to the file to be accessed.
	  * @return A <tt>File</tt> object that represents the requested file or
	  * <tt>null</tt> if the platform does not have file system support.
     */
/*  java.io.File does not exist
    public File getDataFile(String filename)
    {
        return null;
    }
*/
    /**
     * <b>Not implemented</b>: Creates a <tt>Filter</tt> object.
     *  
     * @param filter The filter string.
     * @return always <code>null</code>
     */
    public Filter createFilter(String filter)
    {
        return null;
    }
}
