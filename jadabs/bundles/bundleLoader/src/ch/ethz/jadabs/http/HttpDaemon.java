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
 * Created on 06-Feb-2005
 */
package ch.ethz.jadabs.http;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler;

/**
 * HttpDaemon, a small unthreaded web server
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class HttpDaemon extends Thread {
   private Vector handlers = new Vector();
   protected final static int PORT = 9278; 
      
   /**
    * Constructor 
    */
   public HttpDaemon() {
      handlers.add(new StandardHandler());
   }

   /**
    * Add a request handler
    * @param handler
    */
   public void addRequestHandler(HttpRequestHandler handler) {
      if (!handlers.contains(handler)) {
         handlers.add(0, handler);
      }
   }
   
   /**
    * Remove a request handler
    * @param handler
    */
   public void removeRequestHandler(HttpRequestHandler handler) {
      handlers.remove(handler);
   }
   
   /**
    * The one and only thread in the server. But in contrast of 
    * real enterprise scale web servers like apache, there is no
    * separate thread for each incoming request. This is just the 
    * main working thread of the server.
    * @see java.lang.Runnable#run()
    */
   public void run() {
      HttpServerSocket serverSocket = null;
      
      try
      {
          // Create the server socket
          serverSocket = new HttpServerSocket(PORT);
      }
      catch (IOException e)
      {
         // could not open socket, so maybe there is no suitable hardware
         System.out.println("Could not start the HttpDaemon ...");
      }

      try
      {
         HttpSocket clientSocket;
         do
         {
             /* the main loop for processing incoming requests */
             clientSocket = (HttpSocket)serverSocket.accept();
             System.out.println("Connect from: " + clientSocket );
             for (Enumeration en = handlers.elements(); en.hasMoreElements(); ) {
                if (((HttpRequestHandler)en.nextElement()).delegate(clientSocket)) break;
             }
             clientSocket.close();
         } while (clientSocket != null);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
  }
}

