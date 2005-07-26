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
 * Created on 13-Feb-2005
 */

package ch.ethz.jadabs.http;

import java.io.File;

import ch.ethz.jadabs.bundleLoader.BundleLoaderActivator;
import ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler;

/**
 * A standard handler, always contacted if all other handlers fail 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class StandardHandler implements HttpRequestHandler {
   private final static String repolocation = BundleLoaderActivator.bc.getProperty("org.knopflerfish.gosg.jars");

   /**
    * Used to send the favicon to web browser clients or send a 404 otherwise
    * @see ch.ethz.jadabs.http.HttpRequestHandler#delegate(ch.ethz.jadabs.http.HttpSocket)
    */
   public boolean delegate(HttpSocket request) {      
      // we catch favicon requests here because modern web browsers want to get
      // a favicon before displaying a page and we don't want to have 404's all the time
      if (request.file.startsWith("/favicon.ico")) {
          //repolocation.substring(5) + File.separatorChar + ".." + File.separatorChar + "htdocs" + File.separatorChar + "favicon.ico"
         File favicon = new File("favicon.ico");
         request.sendFile(favicon, "image/ico", null);
      } else if (request.file.startsWith("/repository.xml")) {
         File repoxml = new File(repolocation.substring(5) + File.separatorChar + "repository.xml");
         request.sendFile(repoxml, "text/xml", null);
      } else {
         request.send404();
      }
      return true;
   }

}
