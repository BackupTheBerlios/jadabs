/*
 * Created on Jun 2, 2003
 * 
 * $Id: Queue.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.concurrent.LinkedQueue;

/**
 * Queue is a general Queue which allows to put objects into it and call the
 * listener asynchronously.
 * 
 * @author andfrei
 */
public abstract class Queue extends Thread
{

    private static Logger LOG = Logger.getLogger(Queue.class.getName());

    private static int count = 0;

    private LinkedQueue queue = new LinkedQueue();

    // start/stop flag of the thread
    private boolean threadRunning = true;

    public void put(Object obj)
    {

        try
        {
            queue.put(obj);

        } catch (InterruptedException ie)
        {
            LOG.error("queue has been interrupted", ie);
        }

    }

    private synchronized int getCount()
    {
        return count++;
    }

    public void run()
    {

        this.setName(Queue.this.getClass().getName() + " no." + getCount());
        while (threadRunning)
        {

            Object obj = null;

            try
            {
                obj = queue.take();
            } catch (InterruptedException ie)
            {
                LOG.error("queue has been interrupted", ie);
            }

            processEntry(obj);

        }

    }

    public abstract void processEntry(Object obj);

    public void stopThread()
    {
        threadRunning = false;
    }

}