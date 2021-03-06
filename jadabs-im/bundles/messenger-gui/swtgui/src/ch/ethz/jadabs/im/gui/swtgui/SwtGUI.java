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
package ch.ethz.jadabs.im.gui.swtgui;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ch.ethz.jadabs.im.gui.api.SwtManager;

/**
 * @author andfrei
 *
 */
public class SwtGUI implements Runnable
{
    private static Logger LOG = Logger.getLogger("ch.ethz.jadabs.im.gui.swtgui.SwtGUI");
	
    
    /* GUI */
	static SwtManager manager;
	Display display;
	static Shell shell;
	
	static Image blue;
	static Image green;
	static Image orange;
	static Image red;
	
	private static MainComposite maincomposite;
	
	public SwtGUI()
	{
	}
	
	public void run()
	{
		manager = IMguiActivator.ui;
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
				    LOG.debug("called SwtGUI dispose");
					shell.close();
			}
		});

	}
	
	public void init()
	{	    
		shell = new Shell(display,SWT.CLOSE | SWT.RESIZE); //main-windows	    
//		shell.setText("Jadabs - " + Activator.imService.getSipAddress());
		shell.setText("Jadabs - IM");
		
		// get image from bundle
		InputStream in = getClass().getResourceAsStream("/logo_jadabs.png");
		Image jadabsimg = new Image(display, new ImageData(in));
		shell.setImage(jadabsimg);
		
		InputStream in1 = getClass().getResourceAsStream("/blue.png");
		InputStream in2 = getClass().getResourceAsStream("/green.png");
		InputStream in3 = getClass().getResourceAsStream("/orange.png");
		InputStream in4 = getClass().getResourceAsStream("/red.png");
		blue = new Image(display, new ImageData(in1));
		green = new Image(display, new ImageData(in2));
		orange = new Image(display, new ImageData(in3));
		red = new Image(display, new ImageData(in4));

		maincomposite = new MainComposite(shell, SWT.NULL);
		IMguiActivator.setMaincomposite(maincomposite);	
		
		shell.setLayout(new org.eclipse.swt.layout.FillLayout());
		Rectangle shellBounds = shell.computeTrim(0,0,240,300);
		shell.setSize(shellBounds.width, shellBounds.height);
		
		shell.addShellListener(new ShellCloseListener());
		
		manager.addShellResource(shell, "swtgui", maincomposite);
		//manager.addShell(shell);
		
		shell.open();
	}
	
    class ShellCloseListener extends ShellAdapter
    {
        public void shellClosed(ShellEvent e)
        {
            maincomposite.dispose();
            System.exit(0);
        }
    }
  
     
    /**
     * @return Returns the maincomposite.
     */
    public static MainComposite getMaincomposite() {
        return maincomposite;
    }
}
