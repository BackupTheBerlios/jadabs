/*
 * $Id: NameUI.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt.btchat;

import javax.microedition.lcdui.*;

/**
 * A screen to enter local user name.
 * 
 * <p>
 * Description: This is a screen for user to enter a nick name for the virtual
 * chat room. User must enter a nick name, in order for BlueChat to operate
 * correctly however it is not enforced.
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
public class NameUI extends Form
{

    TextField text;

    public NameUI()
    {
        super("Enter Your Name");
        setCommandListener(ChatMain.instance);

        addCommand(new Command("Chat", Command.SCREEN, 1));
        addCommand(new Command("Chat (Debug)", Command.SCREEN, 1));

        append(new StringItem("", "You must enter a name before hitting Chat button."));
        append(text = new TextField("Your Name", "", 10, TextField.ANY));
    }
}