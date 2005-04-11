/*
 * @(#)Interface.java	1.2 03/11/14
 *
 * Copyright (c) 2000-2003 Sun Microsystems, Inc. All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms
 */

package serverscript;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface file with functions that client can call on this service.
 */
public interface Interface extends Remote {
    /**
     * Request screen.
     * Client state is defined by function parameters.
     * Result of the function is string containing xml data describing
     * next screen to be displayed on client.
     * @param id Client or client state id. Interpreted only by server.
     * Initial value of client id is 0. Server in xml data returned can request
     * client to change his id.
     * @param info Data entered or selected on client. In List it is name of
     * selected item. In TextBox it is a value entered by user.
     * @param command Command name pressed by user.
     * @return xml string representing next screen to be displayed on client.
     */
    String request(String info, String command) throws RemoteException;
}

