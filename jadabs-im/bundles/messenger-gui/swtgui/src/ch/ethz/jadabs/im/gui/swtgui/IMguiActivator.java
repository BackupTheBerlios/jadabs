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
 * Created on Apr 2, 2004
 *
 */
package ch.ethz.jadabs.im.gui.swtgui;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.im.api.IMService;
import ch.ethz.jadabs.im.api.IMSettings;
import ch.ethz.jadabs.im.gui.api.SwtManager;

/**
 * @author andfrei
 *  
 */
public class IMguiActivator implements BundleActivator
{

    static BundleContext bc;

    static SwtManagerImpl ui;
    
    static SwtGUI swtgui = null;
    static MainComposite maincomposite = null;

    static IMService imService = null;
    static IMSettings imSettings = null;

    public void start(BundleContext bc) throws Exception
    {
        // add context
        IMguiActivator.bc = bc;
    
        // instantiate the service
        ui = new SwtManagerImpl();
        ui.start();
     
        //register service
        bc.registerService(SwtManager.class.getName(), ui, new Hashtable());
        //register service as a singleton... need a PID?
    
        ServiceReference imref = bc.getServiceReference(IMService.class.getName());
        imService = (IMService)bc.getService(imref);       	        
        imSettings = (IMSettings)imService;

		// TODO ??? without sleep : crash !!
        Thread.sleep(1000);
        swtgui = new SwtGUI();
        ui.exec(swtgui, false);        
        
        //register service
        bc.registerService(SwtGUI.class.getName(), swtgui, new Hashtable());
        
    }


    public void stop(BundleContext context) throws Exception
    {
        bc = null;
        ui.dispose();
        ui = null;
    } 
    
    public static void setMaincomposite(MainComposite maincomposite) {
        IMguiActivator.maincomposite = maincomposite;
    }

}


