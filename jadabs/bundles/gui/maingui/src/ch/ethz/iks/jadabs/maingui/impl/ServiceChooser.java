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
 */
package ch.ethz.iks.jadabs.maingui.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.ethz.jadabs.remotefw.Framework;

/**
* This code was generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a
* for-profit company or business) then you should purchase
* a license - please visit www.cloudgarden.com for details.
*/
public class ServiceChooser extends Dialog
{

    // GUI fields
	private Button cancelbutton;
	private Button okbutton;
	private Text fctext;
	private Label fclabel;
	private Shell dialogShell;

	// own fields
	ch.ethz.jadabs.remotefw.Framework fw;
	
	public ServiceChooser(Framework fw, Shell parent, int style)
	{
		super(parent, style);
		
		this.fw = fw;
	}

	/**
	* Opens the Dialog Shell.
	* Auto-generated code - any changes you make will disappear.
	*/
	public void open(){
		try {
			preInitGUI();
	
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			dialogShell.setText(getText());
			fclabel = new Label(dialogShell,SWT.NULL);
			fctext = new Text(dialogShell,SWT.BORDER);
			okbutton = new Button(dialogShell,SWT.PUSH| SWT.CENTER);
			cancelbutton = new Button(dialogShell,SWT.PUSH| SWT.CENTER);
	
			dialogShell.setSize(new org.eclipse.swt.graphics.Point(399,107));
	
			FormData fclabelLData = new FormData();
			fclabelLData.height = 20;
			fclabelLData.width = 161;
			fclabelLData.left =  new FormAttachment(324, 1000, 0);
			fclabelLData.right =  new FormAttachment(728, 1000, 0);
			fclabelLData.top =  new FormAttachment(126, 1000, 0);
			fclabelLData.bottom =  new FormAttachment(313, 1000, 0);
			fclabel.setLayoutData(fclabelLData);
			fclabel.setText("Choose File to upload");
			fclabel.setSize(new org.eclipse.swt.graphics.Point(161,20));
			final Font fclabelfont = new Font(Display.getDefault(),"Tahoma",10,1);
			fclabel.setFont(fclabelfont);
	
			FormData fctextLData = new FormData();
			fctextLData.height = 18;
			fctextLData.width = 332;
			fctextLData.left =  new FormAttachment(91, 1000, 0);
			fctextLData.right =  new FormAttachment(956, 1000, 0);
			fctextLData.top =  new FormAttachment(341, 1000, 0);
			fctextLData.bottom =  new FormAttachment(574, 1000, 0);
			fctext.setLayoutData(fctextLData);
			fctext.setSize(new org.eclipse.swt.graphics.Point(332,18));
	
			FormData okbuttonLData = new FormData();
			okbuttonLData.height = 23;
			okbuttonLData.width = 40;
			okbuttonLData.left =  new FormAttachment(347, 1000, 0);
			okbuttonLData.right =  new FormAttachment(447, 1000, 0);
			okbuttonLData.top =  new FormAttachment(677, 1000, 0);
			okbuttonLData.bottom =  new FormAttachment(892, 1000, 0);
			okbutton.setLayoutData(okbuttonLData);
			okbutton.setText("Ok");
			okbutton.addSelectionListener( new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					okbuttonWidgetSelected(evt);
				}
			});
	
			FormData cancelbuttonLData = new FormData();
			cancelbuttonLData.height = 23;
			cancelbuttonLData.width = 42;
			cancelbuttonLData.left =  new FormAttachment(459, 1000, 0);
			cancelbuttonLData.right =  new FormAttachment(565, 1000, 0);
			cancelbuttonLData.top =  new FormAttachment(677, 1000, 0);
			cancelbuttonLData.bottom =  new FormAttachment(892, 1000, 0);
			cancelbutton.setLayoutData(cancelbuttonLData);
			cancelbutton.setText("cancel");
			cancelbutton.setSize(new org.eclipse.swt.graphics.Point(42,23));
			cancelbutton.addSelectionListener( new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					cancelbuttonWidgetSelected(evt);
				}
			});
			FormLayout dialogShellLayout = new FormLayout();
			dialogShell.setLayout(dialogShellLayout);
			dialogShellLayout.marginWidth = 0;
			dialogShellLayout.marginHeight = 0;
//			dialogShellLayout.spacing = 0;
			dialogShell.layout();
			dialogShell.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					fclabelfont.dispose();
				}
			});
			Rectangle bounds = dialogShell.computeTrim(0, 0, 399,107);
			dialogShell.setSize(bounds.width, bounds.height);
			postInitGUI();
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** Add your pre-init code in here 	*/
	public void preInitGUI(){
	}

	/** Add your post-init code in here 	*/
	public void postInitGUI(){
	}


	/** Auto-generated event handler method */
	protected void okbuttonWidgetSelected(SelectionEvent evt)
	{
		String service = fctext.getText();
		System.out.println("add providing service:" + service);
		
		// send service advertisement
        Activator.serviceManager.addProvidingService(service);
        
		dialogShell.dispose();
		
	}

	/** Auto-generated event handler method */
	protected void cancelbuttonWidgetSelected(SelectionEvent evt)
	{
		dialogShell.dispose();
	}
}
