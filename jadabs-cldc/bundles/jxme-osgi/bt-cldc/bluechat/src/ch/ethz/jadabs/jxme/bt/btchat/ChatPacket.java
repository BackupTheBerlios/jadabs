/*
 * $Id: ChatPacket.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt.btchat;

/**
 * A holder object for BlueChat network packet data.
 * <p>
 * Description: ChatPacket can represent severl type of message, which is
 * defined by NetLayer.SIGNAL_XXX enumeration. The common type is
 * SIGNAL_MESSAGE, which hold an user entered message to sent across the virtual
 * chat room.
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

public class ChatPacket
{

    // signal, must be one of NetLayer.SIGNAL_XXX
    public int signal;

    // indicate the nick name of the sender
    public String sender;

    // the message content
    public String msg;

    public ChatPacket(int signal, String msg)
    {
        this.signal = signal;
        this.msg = msg;
    }

    public ChatPacket(int signal, String sender, String msg)
    {
        this.signal = signal;
        this.sender = sender;
        this.msg = msg;
    }

    public ChatPacket()
    {
    }

}