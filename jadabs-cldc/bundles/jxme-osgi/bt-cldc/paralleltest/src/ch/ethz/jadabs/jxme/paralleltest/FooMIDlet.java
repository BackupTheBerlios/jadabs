/*
 * Created on Jul 22, 2004
 * $Id: FooMIDlet.java,v 1.1 2004/11/10 10:28:12 afrei Exp $
 */
package ch.ethz.jadabs.jxme.paralleltest;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

/**
 * This is the MIDlet foo parallel test MIDlet.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class FooMIDlet extends MIDlet implements CommandListener
{
    private Display display;
    
    private Command logCmd;
    private Command exitCmd;
    private CounterForm form;
    private Counter counter;
    private LogCanvas logCanvas;
    
    /**
     * constructor sets up OSGi container and starts bundles
     */
    public FooMIDlet()
    {        
        counter = new Counter();        
        Thread counterThread = new Thread(counter);        
        counterThread.start();
    }
    
    /**
     * Start Application
     */
    protected void startApp()
    {
        display = Display.getDisplay(this);
        logCmd = new Command("Log", Command.SCREEN, 1);
        exitCmd = new Command("Exit", Command.SCREEN, 2);
        form = new CounterForm(this, "Foo");
        form.addCommand(logCmd);
        form.addCommand(exitCmd);
        logCanvas = new LogCanvas();
        logCanvas.setDisplay(display);
        logCanvas.setPreviousScreen(form);
        logCanvas.addEntry("startApp() invoked.");
        display.setCurrent(form);
    }

    /**
     * Called when the MIDlet is temprarily paused
     */
    protected void pauseApp()
    {
        if (logCanvas != null) {
            logCanvas.addEntry("pauseApp() invoked");        
        }
    }

    /**
     * Called when application is about to be terminated
     * @param unconditional if set to true the MIDlet must terminate,
     *        if false the MIDlet may throw an MIDletStateChangeException
     *        it it does not want to be terminated 
     */
    protected void destroyApp(boolean unconditional)
    {
        counter.abort();
        if (logCanvas != null) {
            logCanvas.addEntry("destroyApp("+unconditional+") invoked");
        }
    }

    /**
     * Listener for GUI commands
     * @param c command that was issued
     * @param d displayable where the command was issued
     */
    public void commandAction(Command c, Displayable d)
    {
        if (c == exitCmd) {            
            destroyApp(true);
            notifyDestroyed();
        } else if (c == logCmd) {
            logCanvas.setPreviousScreen(form);
            display.setCurrent(logCanvas);
        }
    }
    
    
    class Counter implements Runnable {       
        boolean abort = false;
        int count = 0;
        
        public void run() {
            while (!abort) {
                count++;
                if (form != null) {
                    form.setCounter(count);
                }
                try {
                    Thread.sleep(1000);                    
                } catch (InterruptedException e) {
                    
                }
            }
        }
        
        public void abort() {
            abort = true;
        }
    }
}