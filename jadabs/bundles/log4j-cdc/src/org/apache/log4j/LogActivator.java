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
 * Created on 09.06.2004
 * 
 * $Id: LogActivator.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package org.apache.log4j;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This Log4J Activator sets up the Log4J system for J2ME/MIDP as 
 * initial logging mechanism.
 * 
 * @author andfrei
 */
public class LogActivator implements BundleActivator
{

    /**
     * Start the Log4J bundle
     * @param bc context the Log4J bundle runs in
     */
    public void start(BundleContext bc) throws Exception
    {
        
        String level = (String) bc.getProperty("log4j.priority");

        Priority priority = Priority.INFO;
        
        if (level != null)
        {
            if (level.equals("FATAL"))
            {
                priority = Priority.FATAL;
            } else if (level.equals("ERROR"))
            {
                priority = Priority.ERROR;
            } else if (level.equals("WARN"))
            {
                priority = Priority.WARN;
            } else if (level.equals("INFO"))
            {
                priority = Priority.INFO;
            } else if (level.equals("DEBUG"))
            {
                priority = Priority.DEBUG;
            }
        }
        
        Logger.createLogger(priority);
    }

    /**
     * Stop the Log4J bundle
     * @param bc context the Log4J bundle runs in
     */
    public void stop(BundleContext bc) throws Exception
    {
        // nothing to do
    }
}