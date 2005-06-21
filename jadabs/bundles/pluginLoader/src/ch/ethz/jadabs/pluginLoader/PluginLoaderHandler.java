/*
 * Created on 20-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

import ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler;
import ch.ethz.jadabs.http.HttpSocket;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginLoaderHandler implements HttpRequestHandler {

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler#delegate(ch.ethz.jadabs.http.HttpSocket)
    */
   public boolean delegate(HttpSocket request) {
      if (request.file.startsWith("/match/")) {         
         String filter = request.file.substring(7);

         StringBuffer buffer = new StringBuffer();
         try {                
            for (Iterator plugins = PluginLoaderActivator.ploader.getMatchingPlugins(filter, this); plugins.hasNext(); ) {
               String uuid = (String)plugins.next();
               BufferedReader reader = new BufferedReader(new InputStreamReader(PluginLoaderImpl.getInstance().fetchInformation(uuid, this)));
               while (reader.ready()) {
                  buffer.append(reader.readLine());
               }
               buffer.append("#####");
            }
            request.sendString(buffer.toString().substring(0, buffer.toString().length() - 5), "text");
         } catch (Exception err) {
            err.printStackTrace();
         }
         return true;
      }
      return false;
   }

}
