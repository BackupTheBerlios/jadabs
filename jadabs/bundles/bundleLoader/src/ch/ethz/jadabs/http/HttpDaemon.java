/*
 * Created on 06-Feb-2005
 */
package ch.ethz.jadabs.http;

import java.io.IOException;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class HttpDaemon extends Thread {
   private RequestHandler handler;
   protected final static int PORT = 9278; 
      
   public HttpDaemon(RequestHandler handler) {
      this.handler = handler;
   }
   
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
             handler.delegate(clientSocket);
             clientSocket.close();
         } while (clientSocket != null);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
  }
}

