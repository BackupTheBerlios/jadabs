/*
 * Created on 30.12.2004
 */
package ch.ethz.jadabs.pluginloader;

import java.util.Enumeration;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface PluginLoader {
   
    Enumeration getOSGiPlugins();
    
}
