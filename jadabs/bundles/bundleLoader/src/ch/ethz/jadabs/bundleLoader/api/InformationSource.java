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
package ch.ethz.jadabs.bundleLoader.api;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Interface of all Information Sources 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface InformationSource {

   /**
    * Retrieve information undirected from all possible known information sources.
    * @param uuid uuid of the requested information, e.g. jadabs:jxme-osgi:0.7.1-SNAPSHOT:obr for the obr file for jxme-osgi. 
    * @return Reader over the retrieved information or null if no information could be retrieved.
    */
   public InputStream retrieveInformation(String uuid);
   
   /**
    * Retrieve information directed from a source.
    * @param uuid uuid uuid of the requested information, e.g. jadabs:jxme-osgi:0.7.1-SNAPSHOT:obr for the obr file for jxme-osgi.
    * @param source Source of the requested information, e.g. a hostname for a http source or a peer name for a ServiceManager source.
    * @return Reader over the retrieved information or null if no information could be retrieved.
    */
   public InputStream retrieveInformation(String uuid, String source);
   
   /**
    * Get an iterator over all uuids matching a pluginFilter. 
    * @param filter a <code>String</code> compliant with the following EBNF:
    * filter = ExtensionExpression " ¦ " PlatformExpression | PlatformExpression (; PlatformExpression)* " ¦ " [R|P|RP]
    * ExtensionExpression = "Extension/" Attribute ":" Value | Attribute ":" Value (,Attribute ":" Value)* 
    * PlatformExpression = Element "/" Attribute ":" Value | Attribute ":" Value (,Attribute ":" Value)*
    * @return Iterator over uuids of matching plugins
    * @throws Exception
    */
   public Iterator getMatchingPlugins(String filter) throws Exception;
   
}
