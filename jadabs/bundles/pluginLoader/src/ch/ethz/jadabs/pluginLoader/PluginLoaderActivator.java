/*
 * Copyright (c) 2003-2005, Jadabs project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the Jadabs project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Created on Nov 28, 2004
 *
 */

package ch.ethz.jadabs.pluginLoader;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.bundleLoader.api.BundleLoader;
import ch.ethz.jadabs.http.HttpSite;
import ch.ethz.jadabs.http.PageHandler;
import ch.ethz.jadabs.pluginLoader.api.PluginLoader;


/**
 * Activator of the PluginLoader
 * @author rjan
 */
public class PluginLoaderActivator implements BundleActivator
{

    protected static Logger LOG = Logger.getLogger(PluginLoaderActivator.class.getName());    
    
    protected static BundleContext bc;
    protected static BundleLoader bloader;
    protected static PluginLoaderImpl ploader;
    protected static PageHandler pageHandler;
    protected static HttpSite httpSite;
    
    /**
     * start the bundle, this method is called by the OSGi framework
     * @param bc the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void start(BundleContext bc) throws Exception
    {
        PluginLoaderActivator.bc = bc;
        ServiceReference sref;
              
        if (LOG.isDebugEnabled())
            LOG.debug("starting Plugin Loader ... ");        
        
        // get BundleLoader
        sref = bc.getServiceReference(BundleLoader.class.getName());
        if (sref != null)
        {
            LOG.debug("Connected to BundleLoader ");
            PluginLoaderActivator.bloader = (BundleLoader) bc.getService(sref);
        } else
        {
            LOG.debug("BundleLoader is not running, cannot start PluginLoader");
            throw new BundleException("Cannot start PluginLoader, BundleLoader is not running");
        }

        ploader = PluginLoaderImpl.getInstance();
        
        bc.registerService(PluginLoader.class.getName(), ploader,null);
                
        String starter = PluginLoaderActivator.bc
        	.getProperty("ch.ethz.jadabs.pluginloader.starter");

        if (starter == null)
            starter = "init.starter";
	  
        PluginLoaderImpl.getInstance().init(starter);
        
        
        // register PageHandler
        pageHandler = new PluginLoaderPageHandler();
        sref = bc.getServiceReference(HttpSite.class.getName());
        
        httpSite = (HttpSite)bc.getService(sref);
        
        httpSite.registerPageHandler("Plugins", pageHandler);
    }
    
    /**
     * stops the bundle, this method is called by the OSGi framework.
     * @param bc the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void stop(BundleContext bc) throws Exception
    {
       ploader = null;
       httpSite.unregisterPageHandler("Plugins", pageHandler);
    }
}
