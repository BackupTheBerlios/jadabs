/*
 * Created on Jul 22, 2004
 * $Id: LogCanvas.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package org.apache.log4j;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;


/**
 * This is the canvas that displays the log messages and allows scrolling
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class LogCanvas extends Canvas implements CommandListener 
{
    /** list of available log entries */
    private Vector logEntries = new Vector();
    
    /** current log index */
    private int logIndex = 0;
    
    /** width of graphic canvas */
    private int width;
    
    /** height of graphic canvas */
    private int height;
    
    /** Font to be used */
    private Font font;
    
    /** Height of font in pixels */
    private int fontHeight;
    
    /** top left corner of log canvas */
    private int x0;
    
    /** top left corner of log canvas */
    private int y0;
    
    /** back button */
    private Command backCmd;
    
    /** previous screen */
    private Displayable previousDisplay;
    
    /** main display of MIDlet */
    private Display mainDisplay;
    
    /**
     * Constructor creates LogCanvas which does not have any commands yet
     */
    public LogCanvas()
    {        
        backCmd = new Command("Back", Command.BACK, 1);
        this.addCommand(backCmd);
        this.setCommandListener(this);
    }
    
    /**
     * Add GUI commands and corresponding listener to this log canvas.
     * (usually the main application)  
     * @param cmds Commands to be added to the Log Canvas
     * @param listener CommandListener associated with the Log Canvas
     */
    public void setCommandAndListener(Command[] cmds, CommandListener listener) 
    {
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
        this.setCommandListener(listener);
    }
    
    /**
     * Set Main Display of MIDlet application. This display will be
     * shown when back button is pressed. 
     * @param d reference to Main Display
     */
    public void setDisplay(Display d) {
        mainDisplay = d;
    }
    
    /** 
     * Now paint the canvas
     * @param g graphics reference to the canvas
     */
    protected void paint(Graphics g)
    {
        if (font == null) {
            // Font is not yet set 
            // cache the font and width, height value
            // when it is used the first time
            font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            width = this.getWidth();
            height = this.getHeight(); 
            fontHeight = font.getHeight();
        }
         
        int y = fontHeight;	// 1st line y value
        
        // message will be rendered in black color, on top of white background
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, width, height);
        g.setColor(0, 0, 0);
        g.setFont(font);
        
        g.translate(-x0, -y0);
        
        for (int i=logIndex; i<logEntries.size(); i++) {
            String s = (String)logEntries.elementAt(i);
            g.drawString(s, 0, y, Graphics.BASELINE | Graphics.LEFT);
            y += fontHeight;
        }        
    }
    
    /**
     * Called when a key is pressed, this method handles the scrolling
     * @param key number of the pressed key
     */
    public void keyPressed(int key) 
    {
        if (getGameAction(key) == Canvas.RIGHT) {
            x0 += 50;
            repaint();
        } else if (getGameAction(key) == Canvas.LEFT) {
            x0 -= 50;
            repaint();
        } else if (getGameAction(key) == Canvas.DOWN) {            
            y0 += 50;
            repaint();
        } else if (getGameAction(key) == Canvas.UP) {
            y0 -= 50;
            repaint();
        }        
    }

    /**
     * Add new log entry and redraw canvas
     * @param entry string to be added to the log
     */
    public void addEntry(String entry) {
        logEntries.addElement(entry);
        repaint();
    }

    /**
     * Called when a command is issued in this canvas
     * @param cmd command that was issued
     * @param d displayable there the command was issued
     * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
     */
    public void commandAction(Command cmd, Displayable d)
    {
        if (cmd == backCmd) {
            if (previousDisplay == null) {
                logEntries.addElement("fatal error: no display defined, use setPreviousScreen()");
                repaint();
            }
            mainDisplay.setCurrent(previousDisplay);
        }        
    }
 
    /**
     * Set previous screen to which we return if back is pressed. 
     * @param d Displayable to display when Log window is moved to the background
     */
    public void setPreviousScreen(Displayable d) 
    {
        previousDisplay = d;
    }
}
