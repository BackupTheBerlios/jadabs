/*
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader.api;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface LoaderListener {
   
   public void itemChanged(String itemUuid, int type);
   
}
