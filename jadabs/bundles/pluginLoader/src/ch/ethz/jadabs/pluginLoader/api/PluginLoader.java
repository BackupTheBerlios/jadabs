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
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader.api;

import java.io.InputStream;
import java.util.Iterator;

import ch.ethz.jadabs.bundleLoader.api.Loader;
import ch.ethz.jadabs.pluginLoader.PluginDescriptor;

/**
 * Plugin Loader Interface 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface PluginLoader extends Loader {

   /**
    * Load a plugin
    * @param uuid Uuid of the Plugin
    * @throws Exception
    */
   public void loadPlugin(String uuid) throws Exception;
   
   /**
    * Checks first if plugin matches and then loads it.
    * 
    * @param uuid
    * @param in
    * @return
    * @throws Exception
    */
   public boolean loadPluginIfMatches(String uuid, InputStream in) throws Exception;
   
   /**
    * Unload a plugin
    * @param uuid Uuid of the Plugin
    * @throws Exception
    */
   public void unloadPlugin(String uuid) throws Exception;
   
   /**
    * Gets the graph of a given Plugin
    * @param uuid Uuid of the Plugin
    * @return XML representation of the graph
    */
   public String getExtensionGraph(String uuid);
   
   /**
    * Get an <code>Iterator</code> over all installed plugin uuids.
    * @return 
    */
   public Iterator getInstalledPlugins();
   
   /**
    * Get an <code>Iterator</code> over all matching plugins deriven from 
    * all registered <code>InformationSources</code>
    * @param filter a <code>String</code> compliant with the following EBNF:
    * filter = ExtensionExpression " � " PlatformExpression | PlatformExpression (; PlatformExpression)* " � " [R|P|RP]
    * ExtensionExpression = "Extension/" Attribute ":" Value | Attribute ":" Value (,Attribute ":" Value)* 
    * PlatformExpression = Element "/" Attribute ":" Value | Attribute ":" Value (,Attribute ":" Value)*
    * @param requestor Reference to the caller to avoid cycles in case of a
    *        caller that is a registered <code>InformationSource</code> itself. 
    * @return <code>Iterator</code> over all found plugin uuids.
    * @throws Exception
    */
   public Iterator getMatchingPlugins(String filter, Object requestor) throws Exception;
   
   /**
	* Fetch a plugin information.
	*/
   public InputStream fetchInformation(String uuid, Object requestor);

   public PluginDescriptor getPluginDescriptor(String uuid) throws Exception;
   
   public Iterator getProvidingPlugins();
   
   public String getPlatform();
}
