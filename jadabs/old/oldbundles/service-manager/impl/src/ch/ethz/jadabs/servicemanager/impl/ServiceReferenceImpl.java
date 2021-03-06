/*
 * Created on Feb 4, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.impl;

import ch.ethz.jadabs.bundleloader.ServiceAdvertisement;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceReferenceImpl implements ServiceReference
{

    private ServiceAdvertisement serviceAdv;
    
    private String peer;
    
    private String rptype;
    
    private String uuid;
    
    public ServiceReferenceImpl(String uuid)
    {
        this.uuid = uuid;
    }
    
    public ServiceReferenceImpl(ServiceAdvertisement serviceAdv, String peer, String rptype)
    {
        this.serviceAdv = serviceAdv;
        this.peer = peer;
        this.rptype = rptype;
        this.uuid = serviceAdv.getID();
    }

    /*
     */
    public String getName()
    {
        return serviceAdv.getName();
    }

    /*
     */
    public String getVersion()
    {
        return serviceAdv.getVersion();
    }

    /*
     */
    public String getGroup()
    {
        return serviceAdv.getGroup();
    }

    public String getAdvertisement()
    {
        return serviceAdv.getAdvertisement();
    }
    
    public String getPeer()
    {
        return peer;
    }
    
    public String getID()
    {
        return uuid;
    }
    
    public String getRPType()
    {
        return rptype;
    }
}

