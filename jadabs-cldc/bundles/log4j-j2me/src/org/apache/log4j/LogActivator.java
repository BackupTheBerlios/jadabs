/*
 * Created on 09.06.2004
 * $Id: LogActivator.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package org.apache.log4j;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * This Log4J Activator sets up the Log4J system for J2ME/MIDP as 
 * initial logging mechanism.
 * 
 * @author Ren&eacute; M&uuml;ller
 * @author andfrei
 */
public class LogActivator implements BundleActivator
{

    /**
     * Start the Log4J bundle
     * @param bc context the Log4J bundle runs in
     */
    public void start(BundleContext bc) throws Exception
    {
        Logger.createLogCanvas();
        String level = (String) bc.getProperty("log4j.priority");

        if (level != null)
        {
            Priority priority = Priority.INFO;
            if (level.equals("FATAL"))
            {
                priority = Priority.FATAL;
            } else if (level.equals("ERROR"))
            {
                priority = Priority.ERROR;
            } else if (level.equals("WARN"))
            {
                priority = Priority.WARN;
            } else if (level.equals("INFO"))
            {
                priority = Priority.INFO;
            } else if (level.equals("DEBUG"))
            {
                priority = Priority.DEBUG;
            }
            
            Logger.priority = priority;
        }
    }

    /**
     * Stop the Log4J bundle
     * @param bc context the Log4J bundle runs in
     */
    public void stop(BundleContext bc) throws Exception
    {
        // nothing to do
    }
}