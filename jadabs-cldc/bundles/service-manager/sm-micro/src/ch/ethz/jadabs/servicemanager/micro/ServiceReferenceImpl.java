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

    protected String group;
    protected String name;
    protected String version;
    protected String peer;
    protected String provider;
    protected String durl;
    protected String port;
    
    public ServiceReferenceImpl(String uuid)
    {
        this(uuid, null, null);
    }
    
    public ServiceReferenceImpl(String id, String peer, String provider)
    {
        this.peer = peer;
        this.provider = provider;
        
        group = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        name = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        version = id.substring(0,id.indexOf(":"));
    
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
    
    public String getPeer()
    {
        return peer;
    }
    
    public String getDownloadURL()
    {
        return durl;
    }
    
    public String getProperty(String name)
    {
        if (name.equals("port"))
            return port;
        else
            return null;
    }
}
