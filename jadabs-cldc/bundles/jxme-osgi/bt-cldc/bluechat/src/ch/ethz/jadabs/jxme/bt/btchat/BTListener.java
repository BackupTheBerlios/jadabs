/*
 * $Id: BTListener.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt.btchat;

/**
 * Interface for BlueChat NetLayer callback.
 * <p>
 * Description: Implementation of this interface will handle BlueChat network
 * event.
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
public interface BTListener
{

    public final static String EVENT_JOIN = "join";

    public final static String EVENT_LEAVE = "leave";

    public final static String EVENT_RECEIVED = "received";

    public final static String EVENT_SENT = "sent";

    /**
     * BlueChat network activity action handler.
     * 
     * @param action
     *            must be one of NetLayer.ACT_XXX field
     * @param param1
     *            usually the EntPoint object that correspond to the action
     * @param param2
     *            varies by action value
     */
    public void handleAction(String action, Object param1, Object param2);
}