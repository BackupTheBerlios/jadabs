/*
 * Created on Feb 4, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.impl;

import ch.ethz.jadabs.pluginloader.OSGiPlugin;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceReferenceImpl implements ServiceReference
{

    private OSGiPlugin plugin;
    
    private String peer;
    
    
    public ServiceReferenceImpl(OSGiPlugin plugin, String peer)
    {
        this.plugin = plugin;
        this.peer = peer;
    }

    /*
     */
    public String getName()
    {
        return plugin.getName();
    }

    /*
     */
    public String getVersion()
    {
        return plugin.getVersion();
    }

    /*
     */
    public String getGroup()
    {
        return plugin.getGroup();
    }

    public String getAdvertisement()
    {
        return plugin.getAdvertisement();
    }
    
    public String getPeer()
    {
        return peer;
    }
    
    public String getID()
    {
        return plugin.getID();
    }
}

