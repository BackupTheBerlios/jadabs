/*
 * $Id: InputUI.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt.btchat;

import javax.microedition.lcdui.*;

/**
 * A screen for entering chat message.
 * <p>
 * Description: This is a screen for user to enter chat message. User should
 * press Send button to send the message to the virtual chat room. Message is
 * limited to 200 characters.
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
public class InputUI extends TextBox
{

    /** Construct the displayable */
    public InputUI()
    {
        super("Enter Message", "", 200, TextField.ANY);
        addCommand(new Command("Send", Command.SCREEN, 1));
        addCommand(new Command("Back", Command.SCREEN, 1));

        setCommandListener(ChatMain.instance);
    }

    public void showUI()
    {
        this.setString("");
    }

}