/*
 * Created on 30.12.2004
 */
package ch.ethz.jadabs.pluginloader.fileFilters;

import java.io.File;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginFilter implements java.io.FileFilter {

   /**
    * @see java.io.FileFilter#accept(java.io.File)
    */
   public boolean accept(File pathname) {
      return pathname.getName().endsWith(".opd");
   } 

}
