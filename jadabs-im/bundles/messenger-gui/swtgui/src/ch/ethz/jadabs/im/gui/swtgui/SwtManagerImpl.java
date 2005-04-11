/*
 * Copyright (c) 2003-2004, Jadabs project
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
 * Created on May 16, 2004
 *
 */
package ch.ethz.jadabs.im.gui.swtgui;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import ch.ethz.jadabs.im.gui.api.SwtManager;

/**
 * This is a basic implementation of SwtManager. It is not optimal in design
 * though functional. No attempt is made to share resources such as Fonts or
 * Colors.
 * 
 * @author Michael Hiner, andfrei
 *  
 */
public class SwtManagerImpl extends Thread implements SwtManager
{

    protected static boolean isRunning = true;

    protected static Display d;

    protected static Hashtable shells = new Hashtable();

    public final static boolean bDebug = true;

    /*
     * Return the shared Display. If it has not been initialized return null
     */
    public Display getDisplay()
    {
        return d;
    }

    /*
     * set up the UI thread
     */
    public void run()
    {
        //if d is already initialized simply return
        // we only want a single display on a single thread and
        // if the display is already initialized then there is
        // a thread running already.
        if ((d != null) && (!d.isDisposed()))
            return;

        //initialize display
        Display display = new Display();
        d = display;

        while (isRunning)
        {
            if (!display.readAndDispatch())
                display.sleep();
        }

        if (bDebug)
            System.out.println("finalizing....");
        //if we still have any registered shells then call dispose to
        //clean them out and dispose of any resourses that are registered
        
        if (!shells.isEmpty())
            dispose();
        
        //finally dispose of the display itself
        display.dispose();
        
    }

    /*
     * Add a shell to the Manager.
     */
    public void addShell(Shell s)
    {
        if (shells.containsKey(s))
            return;

        //add the shell and an empty Vector for the resources
        shells.put(s, new Vector());

        //set up a listener to the shell so that when the shell itself
        //is disposed all rsources associated with it are also disposed
        s.addDisposeListener(new DisposeListener()
        {

            public void widgetDisposed(DisposeEvent e)
            {
                Vector v = (Vector) shells.get(e.getSource());
                Object resource;
                //TODO: need to shift this since the Vector now contains key,
                // resource pairs
                //FIXME: I don't like the way I am doing this
                for (int i = 0; i < v.size(); i++)
                {
                    resource = v.elementAt(++i);
                    disposeResource(resource);
                }

                v.removeAllElements();
                shells.remove(e.getSource());

                if (bDebug)
                {
                    if (shells.isEmpty())
                        System.out.println("no more shells....");
                }
            }

        });
    }

    /*
     * Add a resource to be disposed when the corresponding shell is disposed
     */
    public void addShellResource(Shell s, String key, Object res)
    {
        if (null == key)
            key = "";

        if (!shells.containsKey(s))
            addShell(s);

        Vector v = (Vector) shells.get(s);
        int i = -1;

        if (!key.equals(""))
            i = v.indexOf(key);

        if (i < 0)
        {
            v.add(key);
            v.add(res);
        } else
        {
            disposeResource(v.elementAt(i + 1));
            v.remove(i + 1);
            v.add(i + 1, res);

        }

    }

    public Object getShellResource(Shell s, String key)
    {
        if (!shells.containsKey(s))
            return null;

        Vector v = (Vector) shells.get(s);
        int index = v.indexOf(key);
        if (index < 0)
            return null;

        return v.elementAt(index + 1);
    }

    /*
     * Execute the runnable on the UI-thread. The boolean sync should be true if
     * it is to be executed synchorously.
     */
    public boolean exec(Runnable r, boolean sync)
    {
        int time = 0;
        while ((d == null) && (time < 500))
        {
            time++;
            try
            {
                Thread.sleep(10);
            } catch (Exception e)
            {
            }
        }

        if (null == d)
            return false;

        if (sync)
            d.syncExec(r);
        else
            d.asyncExec(r);

        return true;
    }

    /*
     * Dispose of all registered resources and the display. Terminating the
     * thread.
     */
    public void dispose()
    {
        if (!shells.isEmpty())
        {
            try
            {
                d.syncExec(new Runnable()
                {

                    public void run()
                    {
                        Enumeration enume = shells.keys();
                        //TODO: This is a little weak. If any of the shells
                        // throws an exception none of the shells after it will
                        // be closed
                        while (enume.hasMoreElements())
                            ((Shell) enume.nextElement()).close();

                    }
                });
            } catch (Exception e)
            {
                System.out.println("failed on close of swtManager registered shells.");
                e.printStackTrace();
            }
        }
        
        //we have disposed of all the shells, now stop the loop for the display
        // thread.
        isRunning = false;
        if (!d.isDisposed())
            try
            {
                
                d.syncExec(new Runnable()
                {

                    public void run()
                    {
                        
                    }
                });
            } catch (Exception e)
            {
                System.out.println("failed on close of display.");
            }
    }

    /**
     * Dispose of specified resource, if the type has a known dispose method.
     * For a registered bundle I am stopping the bundle.
     * 
     * @param resource
     */
    private void disposeResource(Object resource)
    {
        
        if ((resource instanceof Widget) && !((Widget) resource).isDisposed())
        {
            ((Widget) resource).dispose();
        } else if ((resource instanceof Color) && !((Color) resource).isDisposed())
        {
            ((Color) resource).dispose();
        } else if ((resource instanceof Image) && !((Image) resource).isDisposed())
        {
            ((Image) resource).dispose();
        } else if ((resource instanceof Font) && !((Font) resource).isDisposed())
        {
            ((Font) resource).dispose();
        } else if (resource instanceof Bundle)
        {
            if (((Bundle) resource).getState() == Bundle.ACTIVE)
            {
                try
                {
                    ((Bundle) resource).stop();
                } catch (BundleException be)
                {
                }
            }
        }

        resource = null;
    }
}