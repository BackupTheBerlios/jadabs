/************************************************************************
 *
 * $Id: Logger.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
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
 **********************************************************************/

/**
 * log4j stubs for running JXTA on smaller devices
 */

package org.apache.log4j;

import java.util.Hashtable;

public final class Logger 
{

    private static Logger logger;
    
    private static Hashtable categories = new Hashtable();
    
    private static final long start = System.currentTimeMillis();
    
    private static Priority priority =  Priority.INFO;
    
    private String className;
    
    private Logger() 
    {
    }
    
    protected static Logger createLogger(Priority prio) 
    {
        if (logger != null)
            logger = new Logger();
        
        priority = prio;
        
        return logger;
    }
    
    public static Logger getLogger(String name) 
    {               
        return logger;
    }
    
    public static Logger getLogger(Class clazz) 
    {               
        return logger;
    }
    
    private void log(Priority level, Object msg, Throwable ex) {
        if (level.isGreaterOrEqual(priority)) {
            System.out.println(
                Long.toString(System.currentTimeMillis() - start) + " " +
                level.toString() + " " + className +" - " +
                msg.toString());
            if (ex != null) {
                ex.printStackTrace();
            }
        }
    }

    public void debug(Object msg) {
        log(Priority.DEBUG, msg, null);
    }

    public void debug(Object msg, Throwable ex) {
        log(Priority.DEBUG, msg, ex);
    }

    public void info(Object msg) {
        log(Priority.INFO, msg, null);
    }

    public void info(Object msg, Throwable ex) {
        log(Priority.INFO, msg, ex);
    }

    public void warn(Object msg) {
        log(Priority.WARN, msg, null);
    }

    public void warn(Object msg, Throwable ex) {
        log(Priority.WARN, msg, ex);
    }

    public void error(Object msg) {
        log(Priority.ERROR, msg, null);
    }

    public void error(Object msg, Throwable ex) {
        log(Priority.ERROR, msg, ex);
    }

    public void fatal(Object msg) {
        log(Priority.FATAL, msg, null);
    }

    public void fatal(Object msg, Throwable ex) {
        log(Priority.FATAL, msg, ex);
    }

    public void setPriority(Priority p) {
        priority = p;
    }
    
    public boolean isInfoEnabled()
    {
        return Priority.INFO.isGreaterOrEqual(priority);
    }
    
    public boolean isDebugEnabled()
    {
        return Priority.DEBUG.isGreaterOrEqual(priority);
    }
    
}

