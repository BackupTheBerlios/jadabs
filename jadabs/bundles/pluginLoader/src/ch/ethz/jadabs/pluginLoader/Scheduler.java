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
 * A scheduler for plugin schedules. 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class Scheduler {
   
   private Vector schedules = new Vector();
   private static Logger LOG = Logger.getLogger(Scheduler.class);
   protected ArrayList stillToProcess = new ArrayList();
   
   /**
    * Constructor
    */
   protected Scheduler() {
      clear();      
   }

   /**
    * Clear the scheduler, delete all existing schedules and the queue    
    */
   protected void clear() {
      schedules.clear();
      ArrayList schedule = new ArrayList();
      schedules.add(schedule);
      stillToProcess.clear();
   }
   
   /** 
    * Adds a plugin to all existing schedules. If a plugin is already contained in 
    * a specific schedule, it will not be added to this specific schedule but still
    * to all other. 
    * @param uuid Uuid of the plugin.
    */
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
   
   /**
    * Adds alternative Plugins to all existing schedules. In case of two alternative 
    * plugins, a copy of every existing schedule will be made, the originals get 
    * alternative plugin one enqueued while the copies get alternative two. Works 
    * for any number of alternatives.  
    * @param plugins <code>ArrayList</code> of the alternatives.
    */
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
   
   /**
    * Get an iterator of all existing schedules, result will be ordered according
    * to the length of the schedules, shortest schedule first. 
    * @return
    */
   protected Iterator getIterator() {
       
       // not JVM 1.3 compatible
//     ArrayList sortedSchedules = Collections.list(schedules.elements()); 
      
       
       // JVM 1.3 compatible
       ArrayList sortedSchedules = new ArrayList();
       
       for(Enumeration en = schedules.elements(); en.hasMoreElements();)
       {
           sortedSchedules.add(en.nextElement());
       }
       

      return sortedSchedules.iterator();
   }
   
   /**
    * @see java.lang.Object#toString()
    */
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Schedules: \n");
      
      // not JVM 1.3 compatible
//    ArrayList sortedSchedules = Collections.list(schedules.elements()); 
     
      
      // JVM 1.3 compatible
      ArrayList sortedSchedules = new ArrayList();
      
      for(Enumeration en = schedules.elements(); en.hasMoreElements();)
      {
          sortedSchedules.add(en.nextElement());
      }
      
      Collections.sort(sortedSchedules, new ScheduleComparator());
      for (Iterator existingSchedules = sortedSchedules.iterator(); existingSchedules.hasNext();) {
         ArrayList schedule = (ArrayList)existingSchedules.next();
         buffer.append(schedule.toString() + "\n");
      }
      return buffer.toString();
   }
}
