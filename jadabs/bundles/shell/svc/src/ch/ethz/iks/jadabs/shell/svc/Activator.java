/*
 * Copyright (c) 2003-2004, Jadabs project
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


package ch.ethz.iks.jadabs.shell.svc;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import ch.ethz.jadabs.remotefw.FrameworkManager;
import ch.ethz.jadabs.bundleLoader.IBundleLoader;


/**
 * Jadabs Shell Reloaded 
 * @author rjan
 */
public class Activator implements BundleActivator
{

    protected static Logger LOG = Logger.getLogger(Activator.class.getName());    
    protected static BundleContext b_context;
    protected static String peerName;
    protected static FrameworkManager remotefw;
    protected static IBundleLoader bloader;
    protected static boolean running = true;
    private Shell shell;

    /**
     * start the bundle, this method is called by the OSGi implementation.
     * 
     * @param bc the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void start(BundleContext bc) throws Exception
    {
        Activator.b_context = bc;
        ServiceReference sref;
        
        if (LOG.isDebugEnabled())
            LOG.debug("starting Jadabs Shell ... ");

        // get RemoteFramework
        sref = bc.getServiceReference(FrameworkManager.class.getName());
        if (sref != null)
        {
            LOG.debug("Connected to RemoteFramework ");
        } else
        {
            LOG.debug("Can't start Jadabs Shell, RemoteFramework not running !");
            throw new BundleException("Can't start Jadabs Shell, RemoteFramework not running !");
        }
        Activator.remotefw = (FrameworkManager) bc.getService(sref);

        // get BundleLoader
        sref = bc.getServiceReference(IBundleLoader.class.getName());
        if (sref != null)
        {
            LOG.debug("Connected to BundleLoader ");
        } else
        {
            LOG.debug("Can't start Jadabs Shell, BundleLoader not running !");
            System.out.println("Can't start Jadabs Shell, BundleLoader not running !");
            throw new BundleException("Can't start Jadabs Shell, BundleLoader not running !");
        }
        Activator.bloader = (IBundleLoader) bc.getService(sref);

        Activator.peerName = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");

        if (LOG.isDebugEnabled())
            LOG.debug("peername is " + peerName);

        shell = Shell.getInstance();
        shell.start();
        
        b_context.registerService(IShellPluginService.class.getName(),shell,null);

    }
    
    /**
     * stops the bundle, this method is called by the OSGi implementation.
     * 
     * @param bc
     *            the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void stop(BundleContext bc) throws Exception
    {
       running = false;
       shell = null;
    }
}
