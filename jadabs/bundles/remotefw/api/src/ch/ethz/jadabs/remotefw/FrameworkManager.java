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
 * Created on May 13, 2004
 *
 */
package ch.ethz.jadabs.remotefw;

import java.util.Enumeration;


/**
 * The RemoteManager allows to find other OSGi containers.
 * The RemoteFramework can than be used to configure a remote OSGi container.
 * 
 * @author andfrei
 * 
 */
public interface FrameworkManager
{    
    /**
     * Get a List of Remote running OSGi containers.
     * 
     * @return Enumeration of RemoteFramework instances.
     */    
    Enumeration getFrameworks();
    
    /**
     * Return the Local Framework, this actually wraps the Framework
     * implementation.
     * 
     * @return Framework
     */
    Framework getLocalFramework();
    
     /**
     * Subscribe to RemoteFWListener whereas every node supporting
     * remote framework management has to register himself.
     * 
     * @param listener
     */
    void addListener(RemoteFrameworkListener listener);
    
    /**
     * Remove the RemoteFWListner.
     * 
     * @param listener
     */
    void removeListener(RemoteFrameworkListener listener);
    
    /**
     * Return a Framework matching the name, value pair.
     * 
     * @param property
     * @param value
     * @return Enumeration of frameworks
     */
    Enumeration getFrameworkByProperty(String property, String value);
    
    /**
     * Return a framework by peername.
     * 
     * @param name
     * @return Framework with matching name
     */
    Framework getFrameworkByPeername(String name);
    
}
