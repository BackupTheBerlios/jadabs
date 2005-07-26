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
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;


import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.bundleLoader.api.BundleLoader;
import ch.ethz.jadabs.bundleLoader.security.BundleSecurityImpl;
import ch.ethz.jadabs.http.HttpDaemon;

/**
 * Activator for bundleLoader 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleLoaderActivator implements BundleActivator {
	protected static Logger LOG = Logger.getLogger(BundleLoaderImpl.class.getName());	
	public static BundleContext bc;
	protected static BundleLoaderImpl bundleLoader;
//	protected static HttpDaemon httpDaemon;
	

	
	/**
	 * Called by the OSGi framework to start the bundle
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception 
	{
		LOG.info("BundleLoader starting ...");			
		BundleLoaderActivator.bc = bc;
				
		// start a http daemon to answer bundle loader requests
//		httpDaemon = new HttpDaemon();
//		httpDaemon.addRequestHandler(new BundleLoaderHandler());
//		httpDaemon.start();
		
		// instanciate BundleLoader, register and start
		BundleLoaderActivator.bundleLoader = BundleLoaderImpl.getInstance();
		bc.registerService(BundleLoader.class.getName(), bundleLoader, null);
		bc.addBundleListener(BundleLoaderActivator.bundleLoader);
		
		// init BundleSecurity
		BundleSecurityImpl.init(BundleLoaderActivator.bundleLoader);
	}

	/**
	 * Called by the OSGi framework to stop the bundle
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		BundleLoaderActivator.bc = null;
		BundleLoaderActivator.bundleLoader = null;		
	}

}
