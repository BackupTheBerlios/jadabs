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
 * Created on Jun 10, 2004
 *
 */
package ch.ethz.iks.jadabs.maingui.impl;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ch.ethz.iks.jadabs.maingui.SwtManager;

/**
 * @author andfrei
 *
 */
public class MainGUI implements Runnable
{
    /* GUI */
	static SwtManager manager;
	Display display;
	static Shell shell;
	
	MainComposite maincomposite;
	
	public MainGUI()
	{
	}
	
	public void run()
	{
		manager = Activator.ui;
		display = manager.getDisplay();
		
		init();
		
	}
	
	public void dispose()
	{

	    
		if (null == shell)
			return;
		
		//dispose of the shell on the display thread if it is not already disposed.
		manager.getDisplay().syncExec(new Runnable(){
			public void run() {
				if (!shell.isDisposed())
				    System.out.println("called MainGUI dispose");
					shell.close();
			}
		});

	}
	
	public void init()
	{	    
		shell = new Shell(display); //main-windows
		shell.setText("Jadabs - " + Activator.peername);
		
		// get image from bundle
		InputStream in = getClass().getResourceAsStream("/logo_jadabs.png");
		Image jadabsimg = new Image(display, new ImageData(in));
		
		shell.setImage(jadabsimg);

		maincomposite = new MainComposite(shell, SWT.NULL);

		shell.setLayout(new org.eclipse.swt.layout.FillLayout());
		Rectangle shellBounds = shell.computeTrim(0,0,554,468);
		shell.setSize(shellBounds.width, shellBounds.height);
		
		shell.addShellListener(new ShellCloseListener());
		
		//shell.pack();
		
		manager.addShellResource(shell, "maingui", maincomposite);
		//manager.addShell(shell);
		
		shell.open();
		
        // show current peer list
		maincomposite.fillupTree();
        Activator.rmanager.addListener(maincomposite);
		
        
//		maincomposite.select(Activator.peername);
//		maincomposite.peertree.setEnabled(true);
	}
	
    class ShellCloseListener extends ShellAdapter
    {
        public void shellClosed(ShellEvent e)
        {
            maincomposite.dispose();
            
        }
    }

}
