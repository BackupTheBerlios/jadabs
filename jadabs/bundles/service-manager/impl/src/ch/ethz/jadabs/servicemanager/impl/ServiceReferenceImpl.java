/*
 * Created on Feb 4, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.impl;

import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceReferenceImpl implements ServiceReference
{

    private String serviceAdv;
    
    private String peer;
    
    private String rptype;
    
    private String uuid;
    
    String group;
    String name;
    String version;
    
    public ServiceReferenceImpl(String uuid)
    {
        this.uuid = uuid;
    }
    
    public ServiceReferenceImpl(String id, String serviceAdv, String peer, String rptype)
    {
        this.serviceAdv = serviceAdv;
        this.peer = peer;
        this.rptype = rptype;
        this.uuid = id;
        
        group = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        name = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        version = id.substring(0,id.indexOf(":"));
//        String type = id.substring(id.indexOf(":")+1); 
    }


    public String getAdvertisement()
    {
        return serviceAdv;
    }

    
    public String getID()
    {
        return uuid;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getGroup()
    {
        return group;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public String getRPType()
    {
        return rptype;
    }
    
    public String getPeer()
    {
        return peer;
    }
    
    public String getDownloadURL()
    {
        return null;
    }
    
    public String getProperty(String name)
    {
        return null;
    }
}

