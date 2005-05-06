/**
 * $Id: Service.java,v 1.2 2005/05/06 15:53:27 afrei Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 *********************************************************
 */
package ch.ethz.jadabs.jxme;

import java.util.Hashtable;


//jxme related


/**
 * This class provides functionality common to all JXME services Any class
 * wanting to be a service must extend this abstract class.
 * 
 *  
 */
public abstract class Service
{

    protected final String serviceName;

    protected Peer peer;

    private final Hashtable listeners = new Hashtable();

    /**
     * Constructor to be called by every sub class
     * 
     * @param myPeer
     * @param serviceName
     */
    public Service(Peer myPeer, String serviceName)
    {

        this.peer = myPeer;
        this.serviceName = serviceName;
    }
    
    /**
     * Called to register a listener for this service
     * 
     * @param serviceId
     *            of service
     * @param listener
     */
    synchronized public void addListener(String serviceId, Listener listener)
    {

        if (serviceId != null && listener != null)
        {
            Listener[] lists;
            if (listeners.containsKey(serviceId))
            {
                Listener[] ls = (Listener[])listeners.get(serviceId);
                lists = new Listener[ls.length + 1];
                
                for (int i = 0; i < ls.length; i++)
                    lists[i] = ls[i];
                
                lists[ls.length] = listener;
                
                
            }
            else
            {
                lists = new Listener[1];
                lists[0] = listener;
            }
            
            listeners.put(serviceId, lists);
        }
    }

    /**
     * Unregister the listener from the service
     * 
     * @param serviceId
     *            of a service
     */
    synchronized public void removeListener(String serviceId)
    {

        if (serviceId != null)
        {
            listeners.remove(serviceId);
        }
    }

    /**
     * Return a registered listener with the given serviceId
     * 
     * @param serviceId
     *            of a service
     * @return Listener object if registered else return null
     */
    synchronized public Listener[] getListeners(String serviceId)
    {        
        return (Listener[]) listeners.get(serviceId);
    }

    /**
     * @param serviceId
     *            of service
     * @return true if a listener is registered with the given serviceId
     */
    // TBD kuldeep - Do we need this method?
//    synchronized public boolean listenerHasKey(String serviceId)
//    {
//
//        if (listeners.containsKey(serviceId)) { return true; }
//        return false;
//    }

    /**
     * Returns the data contained in a message element in String format
     * 
     * @param name :
     *            Name of the element in the JXTA namespace whose data is
     *            required
     * @param message
     * @return data in string format or null if no element with the specified
     *         TAG
     */
    static public String popString(String name, Message message)
    {

        Element el = message.getElement(name);
        
        return new String(el.getData());
    }

    static public String popString(String name, Element[] elm)
    {

        for (int i = 0; i < elm.length; i++)
        {
            if (elm[i].getName().equals(name))
            {
                return new String(elm[i].getData());
            }
        }
        return null;
    }
    
    /**
     * Returns the data contained in a message element in String format
     * 
     * @param name :
     *            Name of the element in the JXTA namespace whose data is
     *            required
     * @param message
     * @param defaultValue
     *            value taken if the tag name is not present or have a wrong
     *            value
     * @return data in int format or defaultValue if no element or a wrong one
     *         with the specified TAG
     */
    static public int popInt(String name, Message message, int defaultValue)
    {
        int result = 0;
        String str = popString(name, message);
        if (str == null)
        {
            result = defaultValue;
        } else
        {
            try
            {
                result = Integer.parseInt(str);
            } catch (NumberFormatException e)
            {
                result = defaultValue;
            }
        }
        return result;
    }

    /**
     * 
     * @return returns an enumeration of the all the listeners in the hashtable
     */
    // TBD kuldeep - may be we do not need this method
//    public Enumeration ListofListeners()
//    {
//        Enumeration enum = listeners.elements();
//        return enum;
//    }
}

