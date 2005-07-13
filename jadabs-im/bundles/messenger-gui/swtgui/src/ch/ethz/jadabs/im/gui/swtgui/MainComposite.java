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
package ch.ethz.jadabs.im.gui.swtgui;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import ch.ethz.jadabs.im.ioapi.MessageCons;
import ch.ethz.jadabs.im.api.IMContact;
import ch.ethz.jadabs.im.api.IMService;
import ch.ethz.jadabs.im.api.IMSettings;

public class MainComposite extends Composite 
{

    private static Logger LOG = Logger.getLogger(MainComposite.class.getName());
    
	private final static int NEIGHBOURS	= 0;
	private final static int BUDDIES	= 1;
	private final static int MSG		= 2;
	private final static int CONFIG		= 3;
	
	private IMService imService;
	private IMSettings imSettings;
	
	private TabFolder tabFolder;
	
	// Neighbour Tab
	private TabItem tabNeighbours;
	private Composite neighbours;
	private Table neighbourList;
	
	// Buddy Tab
	private TabItem tabBuddies;
	private Composite buddies;  
	private Table buddyList;
	private Composite mainboxBuddyLabels;
	private Text newBuddyField;
	private Button addBuddyButton;
	private Button removeBuddyButton;
	
	// Message Tab
	private TabItem tabMsg;
	private Composite msgComposite;
	private Text msgHistoryField;
	private Combo dest;
	private Text newMsgField;
	private Button sendMsgButton;	
	
	// Config Tab
	private TabItem tabConfig;
	private Composite config;
	private Text setUsernameField;
	private Text setPasswordField;
	private Text setIPPortField;
	private Text setRegistrarField;
	private Button saveConfigButton;				
	
	// Bottom
	private Button connectButton;
	private Button disconnectButton;

	public MainComposite(Composite parent, int style)
	{
		super(parent, style);        
		
		imService = IMguiActivator.imService;
		imSettings = IMguiActivator.imSettings;
		
		imService.setListener(new Listener(this));
		
		buildGUI();
		
		// Initialize GUI
		
		tabFolder.setSelection(MSG);
		tabFolder.setSelection(BUDDIES);
		
		disconnectButton.setEnabled(false);
		addBuddyButton.setEnabled(false);
		sendMsgButton.setEnabled(false);
		
		setUsernameField.setText(imSettings.getUserName()+"@"+imSettings.getRealm());
		setPasswordField.setText(imSettings.getPassword());
		setRegistrarField.setText(imSettings.getRegistrar());
		setIPPortField.setText(imSettings.getIpPort());
		
		initBuddyList();
		initNeighbourList();
	}
	
	/*********************************************
	 * BUILD GUI
	 *********************************************/
	private void buildGUI() {
		
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		this.setLayout(gl);
		
		// buildMenu();
		
		tabFolder = new TabFolder(this, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		tabFolder.setLayoutData(gd);
		
		buildNeighboursTab();
		buildBuddiesTab();
		buildMsgTab();
		buildConfigTab();
		buildBottom();
		
		this.setSize(new org.eclipse.swt.graphics.Point(240,320));
		this.layout();
	}
	private void buildMenu() {
		Menu menubar = new Menu(getShell(), SWT.BAR);
		getShell().setMenuBar(menubar);
		menubar.setVisible(true);
		MenuItem about = new MenuItem(menubar, SWT.CASCADE);	            	
		about.setText("About");
		{
			Menu aboutMenu = new Menu(getShell(),SWT.DROP_DOWN);
			about.setMenu(aboutMenu);
			
			aboutMenu.addMenuListener(new MenuAdapter() {	 		
				public void menuShown(MenuEvent evt)
				{
					MessageBox messageBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
					messageBox.setMessage("Jadabs - Instant Messenger Group\n\n" + "http://wlab.ethz.ch/jadabs-im\n\n"
							+ "Francois Terrier\n" + "Jean-Luc Geering\n" + "Janneth Malibago\n\n"
							+ "Supervisor: Andreas Frei\n");
					
					messageBox.open();
				}
			});
		}
	}
	private void buildBuddiesTab() {
		GridData gd;
		
		tabBuddies = new TabItem(tabFolder, SWT.NONE);
		tabBuddies.setText("Buddies");
		buddies = new Composite(tabFolder, SWT.NONE);
		tabBuddies.setControl(buddies);
		GridLayout gl = new GridLayout();
		gl.numColumns = 3;
		buddies.setLayout(gl);
		
		buddyList = new Table(buddies, SWT.SINGLE);
		gd = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		buddyList.setLayoutData(gd);
		buddyList.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent evt) {
				buddySelected(evt);
			}
			public void widgetDefaultSelected(SelectionEvent evt) {
				buddySelected(evt);
			}
		});
		
		newBuddyField = new Text(buddies, SWT.SINGLE | SWT.BORDER);
		newBuddyField.setTextLimit(64);
		newBuddyField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newBuddyField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				newBuddyFieldModified(evt);
			}
		});
		addBuddyButton = new Button(buddies, SWT.PUSH);
		addBuddyButton.setText("Add");
		addBuddyButton.setLayoutData(new GridData());
		addBuddyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				addBuddyButtonSelected(evt);
			}
		});
		removeBuddyButton = new Button(buddies, SWT.PUSH);
		removeBuddyButton.setText("Remove");
		removeBuddyButton.setLayoutData(new GridData());
		removeBuddyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				removeBuddyButtonSelected(evt);
			}
		});
	}
	private void buildNeighboursTab() {
		tabNeighbours = new TabItem(tabFolder, SWT.NONE);
		tabNeighbours.setText("Neighbours");
		neighbours = new Composite(tabFolder, SWT.NONE);
		tabNeighbours.setControl(neighbours);
		neighbours.setLayout(new GridLayout());
		
		neighbourList = new Table(neighbours, SWT.SINGLE);
		neighbourList.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableItem ligne1 = new TableItem(neighbourList, SWT.NONE);
		ligne1.setText("alice@jxme");
		ligne1.setImage(SwtGUI.green);
		TableItem ligne2 = new TableItem(neighbourList, SWT.NONE);
		ligne2.setText("bob@jxme");
		ligne2.setImage(SwtGUI.red);
	}
	private void buildMsgTab() {
		GridData gd;
		GridLayout gl;
		Composite temp;
		Label label;
		
		tabMsg = new TabItem(tabFolder, SWT.NONE);
		tabMsg.setText("Messages");
		msgComposite = new Composite(tabFolder, SWT.NONE);
		tabMsg.setControl(msgComposite);
		msgComposite.setLayout(new GridLayout());
		
		msgHistoryField = new Text(msgComposite, SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER);
		msgHistoryField.setEditable(false);
		msgHistoryField.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		temp = new Composite(msgComposite, SWT.NONE);
		temp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl = new GridLayout();
		gl.numColumns = 2;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		temp.setLayout(gl);
		
		label = new Label(temp, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("To");
		
		dest = new Combo(temp, SWT.READ_ONLY);
		dest.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dest.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				destModified(evt);
			}
		});
		
		temp = new Composite(msgComposite, SWT.NONE);
		temp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gl = new GridLayout();
		gl.numColumns = 2;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		temp.setLayout(gl);
		
		newMsgField = new Text(temp, SWT.SINGLE | SWT.BORDER);
		newMsgField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newMsgField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				newMsgFieldModified(evt);
			}
		});
		sendMsgButton = new Button(temp, SWT.PUSH);
		sendMsgButton.setLayoutData(new GridData());
		sendMsgButton.setText("Send");
		sendMsgButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				sendMsgButtonSelected(evt);
			}
		});
		
	}
	private void buildConfigTab() {
		Label label;
		GridData gd;
		
		tabConfig = new TabItem(tabFolder, SWT.NONE);
		tabConfig.setText("Settings");
		config = new Composite(tabFolder, SWT.NONE);
		tabConfig.setControl(config);
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		config.setLayout(gl);
		
		label = new Label(config, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Username");
		
		setUsernameField = new Text(config, SWT.SINGLE | SWT.BORDER);
		setUsernameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// TODO Check USN and PSW max length
		setUsernameField.setTextLimit(64);
		setUsernameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				setUsernameFieldModified(evt);
			}
		});
		
		label = new Label(config, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Password");
		
		setPasswordField = new Text(config, SWT.SINGLE | SWT.BORDER);
		setPasswordField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setPasswordField.setTextLimit(64);
		setPasswordField.setEchoChar('*');
		setPasswordField.addModifyListener(new ModifyListener() {
			public void modifyText(
					ModifyEvent evt) {
				setPasswordFieldModified(evt);
			}
		});
		
		label = new Label(config, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("IP:Port");
		
		setIPPortField = new Text(config, SWT.SINGLE | SWT.BORDER);
		setIPPortField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setIPPortField.addModifyListener(new ModifyListener() {
			public void modifyText(
					ModifyEvent evt) {
				setIPPortFieldModified(evt);
			}
		});
		
		label = new Label(config, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Registrar");
		
		setRegistrarField = new Text(config, SWT.SINGLE | SWT.BORDER);
		setRegistrarField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setRegistrarField.addModifyListener(new ModifyListener() {
			public void modifyText(
					ModifyEvent evt) {
				setRegistrarFieldModified(evt);
			}
		});
		
		saveConfigButton = new Button(config, SWT.PUSH);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 2;
		saveConfigButton.setLayoutData(gd);
		saveConfigButton.setText("Save");
		saveConfigButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(
					SelectionEvent evt) {
				saveConfigButtonSelected(evt);
			}
		});
		
	}	
	private void buildBottom() {
		GridData gd;
		
		connectButton = new Button(this, SWT.PUSH);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
		connectButton.setLayoutData(gd);
		connectButton.setText("Connect");
		connectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				connectButtonSelected(evt);
			}
		});
		disconnectButton = new Button(this, SWT.PUSH);
		gd = new GridData();
		disconnectButton.setLayoutData(gd);
		disconnectButton.setText("Disconnect");
		disconnectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				disconnectButtonSelected(evt);
			}
		});
	}
	/*********************************************
	 * BUILD GUI
	 * -------------------------------------------
	 * INITIALIZE GUI
	 *********************************************/
	private void initBuddyList() {
		buddyList.removeAll();
		IMContact [] ct = imService.getBuddies();
		TableItem ligne;
		for (int i=0; i<ct.length; i++) {
			ligne = new TableItem(buddyList, SWT.NONE);
			ligne.setText(ct[i].getUsername());
			if (ct[i].getStatus() == MessageCons.IM_STATUS_ONLINE) {
			    ligne.setImage(SwtGUI.green);
			}
			else if (ct[i].getStatus() == MessageCons.IM_STATUS_BUSY) {
				ligne.setImage(SwtGUI.orange);
			}
			else if (ct[i].getStatus() == MessageCons.IM_STATUS_OFFLINE) {
			    ligne.setImage(SwtGUI.red);
			}
			else if (ct[i].getStatus() == MessageCons.IM_STATUS_UNKNOWN) {
				// No Image !
			}
			else {
				ligne.setImage(SwtGUI.blue);
			}
		}
	}
	private void initNeighbourList() {
		neighbourList.removeAll();
		IMContact [] ct = imService.getNeighbours();
		TableItem ligne;
		for (int i=0; i<ct.length; i++) {
			ligne = new TableItem(neighbourList, SWT.NONE);
			ligne.setText(ct[i].getUsername());
			ligne.setImage(SwtGUI.blue);
		}
	}
	private void initDest() {
	    String old = dest.getText();
	    boolean keep = false;
		dest.removeAll();
		String[] contacts = imService.getReceivers();
		for (int i=0; i<contacts.length; i++) {
			dest.add(contacts[i]);
			keep = keep || old.equals(contacts[i]);
		}
		if (keep) {
		    dest.setText(old);
		}
	}
	/*********************************************
	 * INITIALIZE GUI
	 * -------------------------------------------
	 * USER MODIFICATION
	 *********************************************/
	protected void newBuddyFieldModified(ModifyEvent evt) {
	    addBuddyButton.setEnabled(newBuddyField.getText().indexOf("@")!=-1 && newBuddyField.getText().indexOf(".")!=-1);
//		addBuddyButton.setEnabled(newBuddyField.getText().matches(".+@.+"));
	}
	protected void setUsernameFieldModified(ModifyEvent evt) {}
	protected void setPasswordFieldModified(ModifyEvent evt) {}
	protected void setIPPortFieldModified(ModifyEvent evt) {}
	protected void setRegistrarFieldModified(ModifyEvent evt) {}
	protected void destModified(ModifyEvent evt) {
		checkRTS();
	}
	protected void newMsgFieldModified(ModifyEvent evt) {
		checkRTS();
	}
	private void checkRTS() {
		sendMsgButton.setEnabled((dest.getText().length() > 0)
				&& (newMsgField.getText().length() > 0));
	}
	/*********************************************
	 * USER MODIFICATION
	 * -------------------------------------------
	 * USER SELECTION
	 *********************************************/
	protected void connectButtonSelected(SelectionEvent evt) {
		imService.connect();
	}
	protected void disconnectButtonSelected(SelectionEvent evt) {
		imService.disconnect();
	}
	protected void buddySelected(SelectionEvent evt) {
		removeBuddyButton.setEnabled(true);
	}
	protected void addBuddyButtonSelected(SelectionEvent evt) {
		imService.addSipBuddy(newBuddyField.getText());
		initBuddyList();
		initDest();
	}
	protected void removeBuddyButtonSelected(SelectionEvent evt) {
		removeBuddyButton.setEnabled(false);
		String buddyName = buddyList.getItem(buddyList.getSelectionIndex()).getText();
		imService.removeSipBuddy(buddyName);
		initBuddyList();
		initDest();
	} 
	protected void sendMsgButtonSelected(SelectionEvent evt) {
	    imService.sendMessage(dest.getText(), newMsgField.getText());
		newMsgField.setText("");
	}
	protected void saveConfigButtonSelected(SelectionEvent evt) {
		imSettings.newSettings(setUsernameField.getText(), setPasswordField.getText(), setRegistrarField.getText(), setIPPortField.getText());
	}
	/*********************************************
	 * USER MODIFICATION
	 * -------------------------------------------
	 * IM EVENTS
	 *********************************************/
	public void connectOk() {
		connectButton.setEnabled(false);
		disconnectButton.setEnabled(true);
		setUsernameField.setEnabled(false);
		setPasswordField.setEnabled(false);
		setIPPortField.setEnabled(false);
		setRegistrarField.setEnabled(false);
		saveConfigButton.setEnabled(false);
		initDest();
	}
	public void disconnectOk() {
		connectButton.setEnabled(true); 
		disconnectButton.setEnabled(false);
		setUsernameField.setEnabled(true);
		setPasswordField.setEnabled(true);
		setIPPortField.setEnabled(true);
		setRegistrarField.setEnabled(true);
		saveConfigButton.setEnabled(true);
		initBuddyList();
		initNeighbourList();
		dest.removeAll();
	}
	public void incomingMessage(String sipaddress, String msg) {
		msgHistoryField.append(sipaddress + "\n" + msg + "\n");
		tabFolder.setSelection(MSG);
	}
	public void buddyStatusChanged() {
		initBuddyList();
	}
	public void neighbourListChanged() {
	    LOG.debug("update neighbourlist");
		initNeighbourList();
		initDest();
	}
	public void gatewayEvent(boolean presence) {
//		gatewayFound = true;
//		if (imService.getIMType().equals(MessageCons.IM_JXME))
//		{
//			setUsernameField.setEditable(true);
//			saveConfigButton.setEnabled(true);
//			setPasswordField.setEditable(true);
//		}
	}
}