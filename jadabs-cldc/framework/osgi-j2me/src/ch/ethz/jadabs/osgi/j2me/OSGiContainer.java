/*
 * Created on Jul 15, 2004
 * $Id: OSGiContainer.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.osgi.j2me;

import java.util.Hashtable;
import java.util.Vector;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;


/**
 * This OSGiContainer has two maintasks. First, be Interface
 * compliant for registering bundles as in normal OSGi. Second, provide
 * an IoC like container for VMs without normal Classloading.
 * 
 * @author andfrei
 * @author Ren&eacute; M&uuml;ller
 */
public class OSGiContainer
{
    /** there will be only one container */ 
    private static OSGiContainer osgicontainer;
    
    /** all installed, started, resolved bundles */
    private static Vector bundles = new Vector();
    
    /** create only one instance, for all bundles the same */
    private static BundleContext rootbc = new BundleContextImpl();
    
    /**
     * keep registered services in a hastable, for now we only
     * support one key
     */
    private static Hashtable services = new Hashtable();
    
    /**
     * up to now there is only one property table for the
     * entire the container
     */ 
    private static Hashtable properties = new Hashtable();

    /**
     * Constructor is private because OSGiContainer is a singleton
     * the static method Instace() should be used to obtain an
     * instance. 
     */
    private OSGiContainer()
    {
       // empty
    }
    
    /**
     * Static method to obtain a singleton instance.
     * @return single instance of the OSGiContainer
     */
    public static OSGiContainer Instance()
    {
        if (osgicontainer != null)
            return osgicontainer;
        else
            osgicontainer = new OSGiContainer();
       
        return osgicontainer;
    }
    
    /**
     * Installs and starts the bundle specified by the 
     * given activator 
     * @param activator Activator that belongs to the bundle 
     * to be started.
     */
    public void startBundle(BundleActivator activator)
    {
        // should query first if this bundle is already installed
        installBundle(activator);
        
        try
        {
            // should check if not yet started
            activator.start(rootbc);
            // should set a flag for this activator that it has been started
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Install a bundle specified bundle (ie activator).
     * 
     * @param obj bundle (i.e. activator of the bundle) to be installed 
     */
    public void installBundle(Object obj)
    {
        bundles.addElement(obj);
        // should set a flag for this bundle that it has been installed
    }
    
    /**
     * Register service in the bundles hash table
     * @param classname Name of service class 
     * @param obj Reference to service install to be registered  
     * @param dict hashtable with properties of the service. 
     * @return always <code>null</code>
     */
    public ServiceRegistration registerService(String classname, Object obj, Hashtable dict)
    {
        services.put(classname, obj);
        
        return null;
    }
    
    /**
     * Return ServiceReference to the service specified by the classname
     * @param classname ANme of service class
     * @return A ServiceReference to the service of the given class.
     */
    public ServiceReference getServiceReference(String classname)
    {
        return new ServiceReferenceImpl(classname);
    }
    
    /**
     * Obtain reference to service from ServiceReference instance.  
     * @param sref ServiceReference instance that points to service
     * @return Object the ServiceReference points to
     */
    public Object getService(ServiceReference sref)
    {
        String clname = ((ServiceReferenceImpl)sref).clname;
        return services.get(clname);
    }
    
    /**
     * Return value of property specified by key.
     * @param key Key of the property whose value is to be obtained. 
     * @return value associated to property key.
     */
    public String getProperty(String key)
    {
        return (String)properties.get(key);
    }
    
    /**
     * Set value of property specified by key.
     * @param key Key of the property whose value is to be set.
     * @param value Value of the property to be set.
     */
    public void setProperty(String key, String value) 
    {
        properties.put(key, value);
    }
}
