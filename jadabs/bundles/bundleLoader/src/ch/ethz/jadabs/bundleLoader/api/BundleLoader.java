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
package ch.ethz.jadabs.bundleLoader.api;

import java.io.InputStream;
import java.util.Iterator;


/**
 * BundleLoader Interface 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface BundleLoader extends Loader {
   
   /**
    * Loads a bundle and all dependencies.
    * @param uuid Uuid of the bundle obr. 
    * @throws Exception 
    */
   public void loadBundle(String uuid) throws Exception;
   
   /**
    * Gets the dependency graph in XML representation.
    * @param uuid Uuid of the bundle obr. 
    * @return <code>String</code> containing the XML representation of the dependency graph. 
    */
   public String getDependencyGraph(String uuid);
   
   /**
    * Get the uuids of all installed bundles.
    * @return Iterator over uuids.
    */
   public Iterator getInstalledBundles();   

   /**
    * Get information from all registered <code>InformationSources</code>
    * @param uuid Uuid of the requested information, e.g. bundle jar, obr or opd.
    * @param requestor Reference to the caller, used to avoid cycles in case of 
    *                  a caller that is itself a registered <code>InformationSource</code>
    * @return InputStream of the found information or null.
    */
   public InputStream fetchInformation(String uuid, Object requestor);
   
   // registration functions
   /**
    * Registers a <code>HttpRequestHandler</code> at the <code>HttpDaemon</code>
    * @param handler 
    */
   public void registerRequestHandler(HttpRequestHandler handler);

   /**
    * Unregisters a <code>HttpRequestHandler</code> at the <code>HttpDaemon</code> 
    * @param handler
    */
   public void unregisterRequestHandler(HttpRequestHandler handler);
   
}
