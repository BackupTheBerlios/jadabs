/*
 * Created on Feb 12, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.micro;

import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceReferenceImpl implements ServiceReference
{

    private String group;
    private String name;
    private String version;
    private String peer;
    private String provider;
    
    
    public ServiceReferenceImpl(String group, String name, String version)
    {
        this(group, name, version, null, null);
    }
    
    public ServiceReferenceImpl(String group, String name, String version, 
            String peer, String provider)
    {
        this.group = group;
        this.name = name;
        this.version = version;
        this.peer = peer;
        this.provider = provider;
    }
    
    
    /*
     */
    public String getName()
    {
        return name;
    }

    /*
     */
    public String getVersion()
    {
        return version;
    }

    /*
     */
    public String getGroup()
    {
        return group;
    }

    /*
     */
    public String getPeer()
    {
        return peer;
    }

    /*
     */
    public String getID()
    {
        return group+":"+name+":"+version+":";
    }

    /*
     */
    public String getAdvertisement()
    {
        return null;
    }

    public String getProvider()
    {
        return provider;
    }
}
