/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import ch.ethz.jadabs.bundleLoader.api.Descriptor;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginDescriptor extends Descriptor {

   private PluginDescriptor() {
      super(null);
   }
   
   /**
    * @param uuid
    */
   protected PluginDescriptor(String uuid) {
      super(uuid);
   }

}
