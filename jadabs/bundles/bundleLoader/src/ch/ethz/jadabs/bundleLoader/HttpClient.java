/*
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.io.*;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import ch.ethz.jadabs.bundleLoader.api.InformationSource;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class HttpClient implements InformationSource {
   private Vector knownHosts;
   
   public HttpClient() {
      //TODO: read property or file and build up list of known hosts
      
      knownHosts = new Vector();
      knownHosts.add("jadabsrepo.ethz.ch");
   }
   
   /**
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String)
    */
   public InputStream retrieveInformation(String uuid) {
      String[] args = uuid.split(":");
      String group = args[0];
      String name = args[1];
      String version = args[2];
      String type = args[3];

      for (Enumeration hosts = knownHosts.elements(); hosts.hasMoreElements(); ) {
         try {
            String host = (String)hosts.nextElement();
            Socket clientSocket = new Socket(host, 9278);

            DataOutputStream outbound = new DataOutputStream(
                  clientSocket.getOutputStream() );
            BufferedReader inbound = new BufferedReader(
                  new InputStreamReader(clientSocket.getInputStream()) );

            outbound.writeBytes("GET " + "/get" + type + "/" + uuid +  " HTTP/1.0\r\n\r\n");

            //TODO: Finish here ...
         } catch (Exception e) {
            // best efford strategy
         }
      }
            
      return null;
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String, java.lang.String)
    */
   public InputStream retrieveInformation(String uuid, String source) {
      // TODO Auto-generated method stub
      return null;
   }
   
}
