/*
 * Created on 17-Feb-2005
 */

package ch.ethz.jadabs.pluginLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.pluginLoader.api.ScheduleComparator;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class Scheduler {
   
   private Vector schedules = new Vector();
   private static Logger LOG = Logger.getLogger(Scheduler.class);
   protected ArrayList stillToProcess = new ArrayList();
   
   protected Scheduler() {
      clear();      
   }
   
   protected void clear() {
      schedules.clear();
      ArrayList schedule = new ArrayList();
      schedules.add(schedule);
      stillToProcess.clear();
   }
   
   protected void addPlugin(String uuid) {
      for (Enumeration existingSchedules = schedules.elements(); existingSchedules.hasMoreElements();) {
         ArrayList schedule = (ArrayList)existingSchedules.nextElement();
         if (!schedule.contains(uuid)) 
            schedule.add(0, uuid);
         if (LOG.isDebugEnabled()) { 
            LOG.debug("Enqueueing Plugin " + uuid);
            LOG.debug("Schedule is now: " + schedule);
         }
      }
         
   }  
   
   protected void addAlternativePlugins(ArrayList plugins) {
      ArrayList newSchedules = new ArrayList();
      for (Enumeration existingSchedules = schedules.elements(); existingSchedules.hasMoreElements();) {
         ArrayList schedule = (ArrayList)existingSchedules.nextElement();
         String first = (String)plugins.remove(0);
         for (Iterator uuids = plugins.iterator(); uuids.hasNext(); ) {
            ArrayList copy = (ArrayList)schedule.clone();
            String uuid = (String)uuids.next(); 
            if (! copy.contains(uuid))
               copy.add(0, uuid);
            newSchedules.add(copy);
         }
         if (! schedule.contains(first))
            schedule.add(0, first);
         plugins.add(0, first);
      }      
      schedules.addAll(newSchedules);
   }
   
   protected Iterator getIterator() {
      ArrayList sortedSchedules = Collections.list(schedules.elements());      
      return sortedSchedules.iterator();
   }
   
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Schedules: \n");
      ArrayList sortedSchedules = Collections.list(schedules.elements());
      Collections.sort(sortedSchedules, new ScheduleComparator());
      for (Iterator existingSchedules = sortedSchedules.iterator(); existingSchedules.hasNext();) {
         ArrayList schedule = (ArrayList)existingSchedules.next();
         buffer.append(schedule.toString() + "\n");
      }
      return buffer.toString();
   }
}
