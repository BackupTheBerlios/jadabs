/*
 * Copyright (c) 2003-2004, Jadabs project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the Jadabs project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Created on May 16, 2004
 *
 */
package ch.ethz.iks.jadabs.maingui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Michael Hiner
 *
 */
public interface SwtManager {
	
	public Display getDisplay();
	
	/**
	 * Add a shell to the SwtManager so that when the SwtManager is shutdown 
	 * the shell will be as well.
	 */
	public void addShell(Shell s);
	
	/**
	 * Add a resource that is bound to a shell.  It will be disposed of
	 * when the shell is disposed.
	 * @param s - shell to associates the resource with
	 * @param key - key of the resource for later retrieval
	 * @param res - resource
	 */
	public void addShellResource(Shell s, String key, Object res);
	
	/**
	 * Get a previously registered resource for a Shell
	 * @param s - shell the resource is associated with
	 * @param key - key used ofr retrieval
	 * @return
	 */
	public Object getShellResource(Shell s, String key);
	
	/**
	 * execute the runnable on the display thread
	 * @param r - runnable to execute
	 * @param sync - boolean to signify whether the runnable should run 
	 * synchronously or not.
	 * @return - boolean signifying whether the runnable was sucessfully started. 
	 */
	public boolean exec(Runnable r, boolean sync);
	

	
}
