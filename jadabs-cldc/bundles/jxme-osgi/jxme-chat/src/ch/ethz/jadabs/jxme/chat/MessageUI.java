/*
 * $Id :$
 */
package ch.ethz.jadabs.jxme.chat;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A screen to display current messages in the BlueChat virtual chat room.
 * 
 * <p>
 * Description: This is a canvas screen to display the current messages in
 * virtual chat room. Only the latest messages are displayed. If there are more
 * messages than those can fit into one screen, old messages are roll off from
 * the upper edge. User is not able to scroll back to see old messages, however,
 * the old messages is still available in msgs Vector until a clear command is
 * invoked. When a clear command is invoked, all message will be removed from
 * msgs vector.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <pre>
 * BlueChat example application. Originally published in Java Developer's
 * Journal (volume 9 issue 2). Updated by Ben Hui on www.benhui.net. Copyright:
 * (c) 2003-2004 Author: Ben Hui
 * 
 * YOU ARE ALLOWED TO USE THIS CODE FOR EDUCATIONAL, PERSONAL TRAINNING,
 * REFERENCE PURPOSE. YOU MAY DISTRIBUTE THIS CODE AS-IS OR MODIFIED FORM.
 * HOWEVER, YOU CANNOT USE THIS CODE FOR COMMERCIAL PURPOSE. THIS INCLUDE, BUT
 * NOT LIMITED TO, PRODUCING COMMERCIAL SOFTWARE, CONSULTANT SERVICE,
 * PROFESSIONAL TRAINNING MATERIAL.
 * </pre>
 * 
 * @author Ben Hui
 * @version 1.0
 */
public class MessageUI extends Canvas
{

    /** list of available message to display (contains Strings) */ 
    private Vector msgs = new Vector();

    /** current message idx */
    private int midx = 0;

    /** graphic width and height */
    private int w, h;

    /** font height */
    private int fh;

    /** font to write message on screen */
    private Font f;

    /** current x coordinate of top left corner */
    private int x0 = 0;
    
    /** current y coordinate of top left corner */
    private int y0 = 0;

    /**
     * Create new message GUI 
     */
    public MessageUI()
    {
        addCommand(new Command("Write", Command.SCREEN, 1));
        addCommand(new Command("Clear", Command.SCREEN, 2));
        addCommand(new Command("About BlueChat", Command.SCREEN, 3));
        addCommand(new Command("Log", Command.SCREEN, 4));
        addCommand(new Command("Heap Size", Command.SCREEN, 5));
        addCommand(new Command("Exit", Command.SCREEN, 6));
        setCommandListener(ChatMIDlet.instance);
    }

    protected void paint(Graphics g)
    {

        if (f == null)
        {
            // cache the font and width,height value
            // when it is used the first time
            f = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            w = this.getWidth();
            h = this.getHeight();
            fh = f.getHeight();
        }
        //
        // detemine midx based on the screen height and number of message
        /*
         * midx = msgs.size() - (h/fh) ; if ( midx < 0 ) midx = 0;
         */
        int y = fh; // 1st line y value

        // message will be rendered in black color, on top of white backgound
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, w, h);
        g.setColor(0, 0, 0);
        g.setFont(f);

        g.translate(-x0, -y0);

        // render the messages on screen
        for (int i = midx; i < msgs.size(); i++)
        {
            String s = (String)msgs.elementAt(i);
            g.drawString(s, 0, y, Graphics.BASELINE | Graphics.LEFT);
            y += fh;
        }

    }

    /** 
     * Called when a game key is pressed (overwrites Canvas#keyPressed)
     * @param key code of that kee
     */
    public void keyPressed(int key)
    {
        if (getGameAction(key) == Canvas.RIGHT)
        {
            x0 += 50;
        } else if (getGameAction(key) == Canvas.LEFT)
        {
            x0 -= 50;
        } else if (getGameAction(key) == Canvas.UP)
        {
            // note: change this from 50 to 100 if you want to scroll faster
            y0 -= 50;
        } else if (getGameAction(key) == Canvas.DOWN)
        {
            // note: change this from 50 to 100 if you want to scroll faster
            y0 += 50;
        }
        repaint();
    }

    /**
     * Add new entry to the chat log
     * @param entry string to enter
     */
    public void addEntry(String entry) 
    {
        msgs.addElement(entry);
        repaint();        
    }
    
    /** 
     * Clear chat log. Remove entries from chat log.  
     */
    public void removeAllEntries() 
    {
        msgs.removeAllElements();
    }
        
}