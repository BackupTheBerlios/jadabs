/*
 * Created on 17-Feb-2005
 */

package ch.ethz.jadabs.pluginLoader.api;
import java.util.ArrayList;
import java.util.Comparator;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class ScheduleComparator implements Comparator {

   /**
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   public int compare(Object o1, Object o2) {
      ArrayList schedule1 = (ArrayList)o1;
      ArrayList schedule2 = (ArrayList)o2;
      if (schedule1.size() < schedule2.size()) {
         return -1;
      }
      else if (schedule1.size() > schedule2.size()) {
         return 1;
      } 
      return 0;
   }

}
