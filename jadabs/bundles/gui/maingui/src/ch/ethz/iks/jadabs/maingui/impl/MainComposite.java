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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.osgi.framework.Bundle;

import ch.ethz.jadabs.remotefw.BundleInfo;
import ch.ethz.jadabs.remotefw.BundleInfoListener;
import ch.ethz.jadabs.remotefw.Framework;
import ch.ethz.jadabs.remotefw.RemoteFrameworkListener;

/**
 * This code was generated using CloudGarden's Jigloo SWT/Swing GUI Builder,
 * which is free for non-commercial use. If Jigloo is being used commercially
 * (ie, by a for-profit company or business) then you should purchase a license -
 * please visit www.cloudgarden.com for details.
 */
public class MainComposite extends Composite implements RemoteFrameworkListener, BundleInfoListener
{

    private static Logger LOG = Logger.getLogger(MainComposite.class.getName());

    private Label cLabel1;

    private Menu menu1;

    private MenuItem aboutmenue;

    private Menu menubar;

    private Text consoleText;

    private Button startButton;

    private Button updateButton;

    private Button installButton;

    private Button stopButton;

    private Button uninstallButton;

    private Composite composite1;

    protected Tree peertree;

//    final MainComposite maincomposite = this;
    
    /* instance fields */
    //    MainGUI maingui;
    public MainComposite(Composite parent, int style)
    {
        super(parent, style);
        
        initGUI();
    }

    /**
     * Initializes the GUI. Auto-generated code - any changes you make will
     * disappear.
     */
    public void initGUI()
    {
        try
        {
            preInitGUI();

            cLabel1 = new Label(this, SWT.SHADOW_IN);
            consoleText = new Text(this, SWT.MULTI | SWT.WRAP | SWT.BORDER);
            composite1 = new Composite(this, SWT.NULL);
            updateButton = new Button(composite1, SWT.PUSH | SWT.CENTER);
            installButton = new Button(composite1, SWT.PUSH | SWT.CENTER);
            startButton = new Button(composite1, SWT.PUSH | SWT.CENTER);
            stopButton = new Button(composite1, SWT.PUSH | SWT.CENTER);
            uninstallButton = new Button(composite1, SWT.PUSH | SWT.CENTER);
            peertree = new Tree(this, SWT.SINGLE | SWT.BORDER);

            this.setSize(new org.eclipse.swt.graphics.Point(507, 526));
            
            

            FormData cLabel1LData = new FormData();
            cLabel1LData.height = 22;
            cLabel1LData.width = 507;
            cLabel1LData.left = new FormAttachment(0, 1000, 0);
            cLabel1LData.right = new FormAttachment(1000, 1000, 0);
            cLabel1LData.top = new FormAttachment(1000, 1000, -22);
            cLabel1LData.bottom = new FormAttachment(1000, 1000, 0);
            cLabel1.setLayoutData(cLabel1LData);
            cLabel1.setSize(new org.eclipse.swt.graphics.Point(507, 22));

            FormData consoleTextLData = new FormData();
            consoleTextLData.height = 108;
            consoleTextLData.width = 494;
            consoleTextLData.left = new FormAttachment(0, 1000, 0);
            consoleTextLData.right = new FormAttachment(1000, 1000, 0);
            consoleTextLData.top = new FormAttachment(1000, 1000, -137);
            consoleTextLData.bottom = new FormAttachment(957, 1000, 0);
            consoleText.setLayoutData(consoleTextLData);
            consoleText.setSize(new org.eclipse.swt.graphics.Point(494, 108));

            FormData composite1LData = new FormData();
            composite1LData.height = 46;
            composite1LData.width = 334;
            composite1LData.left = new FormAttachment(22, 1000, 0);
            composite1LData.right = new FormAttachment(0, 1000, 345);
            composite1LData.top = new FormAttachment(1000, 1000, -191);
            composite1LData.bottom = new FormAttachment(1000, 1000, -145);
            composite1.setLayoutData(composite1LData);
            composite1.setSize(new org.eclipse.swt.graphics.Point(334, 46));

            RowData updateButtonLData = new RowData(57, 39);
            updateButton.setLayoutData(updateButtonLData);
            updateButton.setText("update");
            updateButton.setSize(new org.eclipse.swt.graphics.Point(57, 39));
            updateButton.addSelectionListener(new SelectionAdapter()
            {

                public void widgetSelected(SelectionEvent evt)
                {
                    updateButtonWidgetSelected(evt);
                }
            });

            RowData installButtonLData = new RowData(57, 39);
            installButton.setLayoutData(installButtonLData);
            installButton.setText("install");
            installButton.setSize(new org.eclipse.swt.graphics.Point(57, 39));
            installButton.addSelectionListener(new SelectionAdapter()
            {

                public void widgetSelected(SelectionEvent evt)
                {
                    installButtonWidgetSelected(evt);
                }
            });

            RowData startButtonLData = new RowData(57, 39);
            startButton.setLayoutData(startButtonLData);
            startButton.setText("start");
            startButton.setSize(new org.eclipse.swt.graphics.Point(57, 39));
            startButton.addSelectionListener(new SelectionAdapter()
            {

                public void widgetSelected(SelectionEvent evt)
                {
                    startButtonWidgetSelected(evt);
                }
            });

            RowData stopButtonLData = new RowData(57, 39);
            stopButton.setLayoutData(stopButtonLData);
            stopButton.setText("stop");
            stopButton.setSize(new org.eclipse.swt.graphics.Point(57, 39));
            stopButton.addSelectionListener(new SelectionAdapter()
            {

                public void widgetSelected(SelectionEvent evt)
                {
                    stopButtonWidgetSelected(evt);
                }
            });

            RowData uninstallButtonLData = new RowData(57, 39);
            uninstallButton.setLayoutData(uninstallButtonLData);
            uninstallButton.setText("uninstall");
            uninstallButton.setSize(new org.eclipse.swt.graphics.Point(57, 39));
            uninstallButton.addSelectionListener(new SelectionAdapter()
            {

                public void widgetSelected(SelectionEvent evt)
                {
                    uninstallButtonWidgetSelected(evt);
                }
            });
            RowLayout composite1Layout = new RowLayout(256);
            composite1.setLayout(composite1Layout);
            composite1Layout.type = SWT.HORIZONTAL;
//            composite1Layout.marginWidth = 0;
//            composite1Layout.marginHeight = 0;
            composite1Layout.spacing = 3;
            composite1Layout.wrap = false;
            composite1Layout.pack = true;
//            composite1Layout.fill = true;
            composite1Layout.justify = true;
            composite1Layout.marginLeft = 3;
            composite1Layout.marginTop = 3;
            composite1Layout.marginRight = 3;
            composite1Layout.marginBottom = 3;
            composite1.layout();

            FormData peertreeLData = new FormData();
            peertreeLData.height = 274;
            peertreeLData.width = 486;
            peertreeLData.left = new FormAttachment(0, 1000, 0);
            peertreeLData.right = new FormAttachment(1000, 1000, 0);
            peertreeLData.top = new FormAttachment(0, 1000, 0);
            peertreeLData.bottom = new FormAttachment(1000, 1000, -211);
            peertree.setLayoutData(peertreeLData);
            peertree.setSize(new org.eclipse.swt.graphics.Point(486, 274));
//            peertree.setEnabled(false);
            peertree.addSelectionListener(new SelectionAdapter()
            {

                public void widgetSelected(SelectionEvent evt)
                {
                    peertreeWidgetSelected(evt);
                }
            });
            FormLayout thisLayout = new FormLayout();
            this.setLayout(thisLayout);
            thisLayout.marginWidth = 0;
            thisLayout.marginHeight = 0;
//            thisLayout.spacing = 0;
            this.layout();
            menubar = new Menu(getShell(), SWT.BAR);
            aboutmenue = new MenuItem(menubar, SWT.CASCADE);
            menu1 = new Menu(aboutmenue);

            getShell().setMenuBar(menubar);

            aboutmenue.setText("About");

            aboutmenue.setMenu(menu1);
            menu1.addMenuListener(new MenuAdapter()
            {

                public void menuShown(MenuEvent evt)
                {
                    menu1MenuShown(evt);
                }
            });

            postInitGUI();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Add your pre-init code in here */
    public void preInitGUI()
    {
    }

    /** Add your post-init code in here */
    public void postInitGUI()
    {
    }

    /*
     * 
     */
    public void enterFrameworkEvent(Framework rframework)
    {
        final Framework finalrfw = rframework;
        //list1.add(rframework.getPeername());
        
        // register this gui as listener change in the framework
        rframework.addBundleInfoListener(this);
        
        MainGUI.manager.exec(new Runnable()
        {

            public void run()
            {
                TreeItem item = new TreeItem(peertree, SWT.NULL);
                item.setText(finalrfw.getPeername());
            }
        }, false);

    }

    /*
     *
     */
    public void leaveFrameworkEvent(Framework rframework)
    {
        //        list1.remove(rframework.getPeername());
        final Framework finalrfw = rframework;
        
        // register this gui as listener change in the framework
        rframework.removeBundleInfoListener(this);
        
        MainGUI.manager.exec(new Runnable()
        {

            public void run()
            {
                //                peerList.remove(finalrfw.getPeername());
                synchronized(peertree)
                {
                    synchronized(peertree)
                    {
		                TreeItem[] items = peertree.getItems();
		                for (int i = 0; i < items.length; i++)
		                {
		                    if (items[i].getText().equals(finalrfw.getPeername()))
		                        items[i].dispose();
		                }
                    }
                }
            }
        }, false);
    }

    protected void select(String peername)
    {
        TreeItem peeritem = null;

        // get peer-item
        TreeItem[] titems = peertree.getItems();
        for (int i = 0; i < titems.length; i++)
        {
            if (titems[i].getText().equals(peername))
            {
                peeritem = titems[i];
                break;
            }
        }
        
        if (peeritem != null)
            peertree.setSelection(new TreeItem[]{peeritem});
    }
    
    /**
     * PeerTree Selected change.
     * 
     * @param evt
     */
    protected void peertreeWidgetSelected(SelectionEvent evt)
    {        
        TreeItem titem;
        TreeItem[] selection;
        if ( (peertree.getSelection().length > 0) && 
             ((titem = peertree.getSelection()[0]).getParentItem() == null) &&
             (titem.getItems().length == 0))
        {
            //            TreeItem titem = treeitems[0];
            
            // get Bundles for the selection
            String peername = titem.getText();
            Framework rframework = Activator.rmanager.getFrameworkByPeername(peername);
            
            // register this gui as listener change in the framework
            rframework.addBundleInfoListener(this);
            
            long[] bids = rframework.getBundles();
            
            if (bids != null)
            {
	            Arrays.sort(bids);
	
	//            TreeItem[] items = titem.getItems();
	//            for (int k = 0; k < items.length; k++)
	//                items[k].dispose();
	
	            for (int i = 0; i < bids.length; i++)
	            {
	                addBundle(peername, titem, rframework.getBundleInfo(bids[i]));
	            }
            }
            
            //titem.setExpanded(true);
        }
    }

    protected void fillupTree()
    {
        Enumeration en = Activator.rmanager.getFrameworks();
        
        for (; en.hasMoreElements();)
        {
            Framework frw = (Framework) en.nextElement();
                        
            // refresh remoteFW
            frw.refresh();
            synchronized(peertree)
            {
                TreeItem item = new TreeItem(peertree, SWT.NULL);
                item.setText(frw.getPeername());
            }
        }
    }
    
    /**
     * Update PeerTree
     */
    protected void updateButtonWidgetSelected(SelectionEvent evt)
    {
        Enumeration en = Activator.rmanager.getFrameworks();
        
        synchronized(peertree)
        {
	        peertree.removeAll();
	
	        for (; en.hasMoreElements();)
	        {
	            Framework frw = (Framework) en.nextElement();
	
	            // refresh remoteFW
	            frw.refresh();
	
	            TreeItem item = new TreeItem(peertree, SWT.NULL);
	            item.setText(frw.getPeername());
	        }
        }
    }

    /**
     * Refresh the given item, whereas the item should be the peer item.
     * All bundle subtree items will be deleted and the peer item is filled
     * with the new bundle values from the framework.
     * 
     * @param item
     * @param fw
     */
    protected void refreshTreeItem(TreeItem item, Framework fw)
    {

        if (item != null && fw != null)
        {

            // dispose old one
            TreeItem[] items = item.getItems();
            for (int i = 0; i < items.length; i++)
            {
                items[i].dispose();
                items[i] = null;
            }

            long[] bids = fw.getBundles();
            Arrays.sort(bids);

            for (int i = 0; i < bids.length; i++)
            {
                String bname = fw.getBundleName(bids[i]);
                String state = getBundleState(fw.getBundleState(bids[i]));

                TreeItem it = new TreeItem(item, SWT.NULL);
                it.setText(bids[i] + " : " + state + " : " + bname);
            }

            item.setExpanded(true);
        }
    }

    /**
     * Start a bundle in a Framework.
     * 
     * @param evt
     */
    protected void startButtonWidgetSelected(SelectionEvent evt)
    {
        TreeItem titem;
        Framework fw = null;
        if ((titem = peertree.getSelection()[0]).getParentItem() != null)
        {
            String peername = titem.getParentItem().getText();
            String bundlestr = titem.getText();
            
            StringTokenizer st = new StringTokenizer(bundlestr, ": ");
            long bid = new Long(st.nextToken()).longValue();

            fw = Activator.rmanager.getFrameworkByPeername(peername);
            fw.startBundle(bid);

        } else
            System.out.println("do a proper selection");

    }

    /**
     * Install a Bundle in a Framework.
     * 
     * @param evt
     */
    protected void installButtonWidgetSelected(SelectionEvent evt)
    {
        TreeItem titem = null;
        Framework fw = null;
        if ( peertree.getSelection().length > 0 && 
             (titem = peertree.getSelection()[0]).getParentItem() == null)
        {
            String peername = titem.getText();

            fw = Activator.rmanager.getFrameworkByPeername(peername);

            FileChooser filechooser = new FileChooser(fw, MainGUI.shell, SWT.ICON_QUESTION);
            filechooser.open();

        } else
            System.out.println("select first a peer!");

    }

    /**
     * Stop a bundle in a Framework.
     * 
     * @param evt
     */
    protected void stopButtonWidgetSelected(SelectionEvent evt)
    {
        TreeItem titem;
        Framework fw = null;
        if ((titem = peertree.getSelection()[0]).getParentItem() != null)
        {
            String peername = titem.getParentItem().getText();
            String bundlestr = titem.getText();
            
            StringTokenizer st = new StringTokenizer(bundlestr, ": ");
            long bid = new Long(st.nextToken()).longValue();

            fw = Activator.rmanager.getFrameworkByPeername(peername);
            fw.stopBundle(bid);

        } else
            System.out.println("do a proper selection");

    }

    /**
     * Remove a bundle from a framework.
     * 
     * @param evt
     */
    protected void uninstallButtonWidgetSelected(SelectionEvent evt)
    {
        TreeItem titem;
        Framework fw = null;
        if ((titem = peertree.getSelection()[0]).getParentItem() != null)
        {
            String peername = titem.getParentItem().getText();
            String bundlestr = titem.getText();
            
            StringTokenizer st = new StringTokenizer(bundlestr, ": ");
            long bid = new Long(st.nextToken()).longValue();

            fw = Activator.rmanager.getFrameworkByPeername(peername);
            fw.uninstallBundle(bid);

        } else
            System.out.println("do a proper selection");

    }

    //
    // helper function
    //

    public static String getBundleState(int state)
    {
        switch (state) {
        case Bundle.ACTIVE:
            return "active";
        case Bundle.INSTALLED:
            return "installed";
        case Bundle.RESOLVED:
            return "resolved";
        case Bundle.STARTING:
            return "starting";
        case Bundle.STOPPING:
            return "stopping";
        case Bundle.UNINSTALLED:
            return "uninstalled";
        default:
            return "";
        }

    }

    public void allBundlesChanged(String peername, Framework framework)
    {
        final Framework fframework = framework;
        final String fpeername = peername;

        MainGUI.manager.exec(new Runnable()
        {

            public void run()
            {
                TreeItem peeritem = null;

                // get peer-item
                TreeItem[] titems = peertree.getItems();
                for (int i = 0; i < titems.length; i++)
                {
                    if (titems[i].getText().equals(fpeername))
                    {
                        peeritem = titems[i];
                        break;
                    }
                }

                // get bundle-item
                if (peeritem != null)
                {
                    refreshTreeItem(peeritem, fframework);
                } else
                {
                    LOG.warn("this is a new peer: " + fpeername);

                }

	           
            }
        }, false);

    }
    
    /**
     * Update the TreeView for changed BundleInfos
     */
    public void bundleChanged(String peername, BundleInfo bundleinfo)
    {

        final String fpeername = peername;
        final BundleInfo fbundleinfo = bundleinfo;

        MainGUI.manager.exec(new Runnable()
        {

            public void run()
            {
                TreeItem peeritem = null;

                // get peer-item
                TreeItem[] titems = peertree.getItems();
                for (int i = 0; i < titems.length; i++)
                {
                    if (titems[i].getText().equals(fpeername))
                    {
                        peeritem = titems[i];
                        break;
                    }
                }

                // get bundle-item
                if (peeritem != null)
                {
                    TreeItem bundleitem = null;
                    titems = peeritem.getItems();
                    long bid = fbundleinfo.bid;

                    for (int i = 0; i < titems.length; i++)
                    {
                        if (titems[i].getText().startsWith(new Long(bid).toString()))
                        {
                            bundleitem = titems[i];
                            break;
                        }
                    }

                    // bundleitem is new
                    if (bundleitem == null)
                    {
                        bundleitem = addBundle(fpeername, peeritem, fbundleinfo);
                    } else
                    {
                        // set text of bundle-item
                        setBundleItemText(bundleitem, fbundleinfo);
                    }
                } else
                {
                    LOG.warn("this is a new peer: " + fpeername);

                }

	           
            }
        }, false);

    }

    private TreeItem addBundle(String peername, TreeItem peeritem, BundleInfo binfo)
    {

        Framework frwk = Activator.rmanager.getFrameworkByPeername(peername);

        TreeItem bundleitem = new TreeItem(peeritem, SWT.NULL);

        setBundleItemText(bundleitem, binfo);

        return bundleitem;
    }

    private void setBundleItemText(TreeItem item, BundleInfo binfo)
    {
        item.setText(binfo.bid + " : " + getBundleState(binfo.state) + " : " + binfo.name);
    }

    /**
     * Setup the About Box.
     * 
     * @param evt
     */
    protected void menu1MenuShown(MenuEvent evt)
    {
        MessageBox messageBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
        messageBox.setMessage("Jadabs - Administrator\n\n" + "ETH - Zuerich, IKS-Group\n\n"
                + "Andreas Frei, frei@inf.ethz.ch");

        messageBox.open();
    }
    
    public void dispose()
    {
        
        // remove this as frameworkListener
        Activator.rmanager.removeListener(this);
        
        // remove all bundle listener
        TreeItem[] items = peertree.getItems();
        
         
        for(int i =0; i < items.length; i++)
        {            
            Framework fw = Activator.rmanager.getFrameworkByPeername(
                    items[i].getText());
            fw.removeBundleInfoListener(this);
            
        }
    }
}