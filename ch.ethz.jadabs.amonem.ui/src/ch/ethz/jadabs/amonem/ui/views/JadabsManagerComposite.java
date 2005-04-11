package ch.ethz.jadabs.amonem.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.amonem.ui.starter.AmonemPlugin;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.remotefw.Framework;
import ch.ethz.jadabs.remotefw.FrameworkManager;
import ch.ethz.jadabs.remotefw.RemoteFrameworkListener;
/**
* This code was generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* *************************************
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED
* for this machine, so Jigloo or this code cannot be used legally
* for any corporate or commercial purpose.
* *************************************
*/
public class JadabsManagerComposite extends org.eclipse.swt.widgets.Composite {

	private Label jadabsmanager;
	private Composite composite1;
	private Button startButton;
	private Composite top;
	private Button buttonRoot;
	public JadabsManagerComposite(Composite parent, int style) {
		super(parent, style);
		initGUI();
	}

	/**
	* Initializes the GUI.
	* Auto-generated code - any changes you make will disappear.
	*/
	public void initGUI(){
		try {
			preInitGUI();

			composite1 = new Composite(this,SWT.NULL);

			this.setSize(new org.eclipse.swt.graphics.Point(329,294));
			final Color JadabsManagerCompositebackground = new Color(Display.getDefault(),173,206,243);
			this.setBackground(JadabsManagerCompositebackground);
			final Color JadabsManagerCompositeforeground = new Color(Display.getDefault(),138,192,218);
			this.setForeground(JadabsManagerCompositeforeground);

			final Color topbackground = new Color(Display.getDefault(),173,207,247);

			GridData composite1LData = new GridData();
			composite1LData.verticalAlignment = GridData.FILL;
			composite1LData.horizontalAlignment = GridData.FILL;
			composite1LData.horizontalIndent = 1;
			composite1LData.horizontalSpan = 3;
			composite1.setLayoutData(composite1LData);
			composite1.setBackground(topbackground);

			final Font jadabsmanagerfont = new Font(Display.getDefault(),"Serif",22,0);
			FillLayout composite1Layout = new FillLayout(org.eclipse.swt.SWT.HORIZONTAL);
			composite1.setLayout(composite1Layout);
            {
                jadabsmanager = new Label(composite1, SWT.NONE);
                FormData jadabsmanagerLData = new FormData();
                jadabsmanagerLData.height = 40;
                jadabsmanagerLData.width = 212;
                jadabsmanager.setLayoutData(jadabsmanagerLData);
                jadabsmanager.setText("Jadabs-Manager");
                jadabsmanager.setSize(new org.eclipse.swt.graphics.Point(212, 40));
                jadabsmanager.setBackground(topbackground);
                jadabsmanager.setFont(jadabsmanagerfont);
            }
            {
                top = new Composite(this, SWT.NONE);
                GridData topLData = new GridData();
                topLData.widthHint = 199;
                topLData.heightHint = 102;
                topLData.horizontalIndent = 1;
                topLData.horizontalSpan = 0;
                topLData.verticalSpan = 2;
                top.setLayoutData(topLData);
                top.setBackground(topbackground);
                top.setLayout(null);
                {
                    startButton = new Button(top, SWT.PUSH | SWT.CENTER);
                    startButton.setText("start");
                    startButton.setSize(new org.eclipse.swt.graphics.Point(75, 30));
                    startButton.setBounds(new org.eclipse.swt.graphics.Rectangle(40, 22, 75, 30));
                    startButton.addSelectionListener(new SelectionAdapter() {
                        public void widgetSelected(SelectionEvent evt) {
                            System.out.println("startButton.widgetSelected, event=" + evt);
                            
                    		ServiceReference sref = AmonemPlugin.bc.getServiceReference(PeerNetwork.class.getName());
                    		PeerNetwork pnet = (PeerNetwork)AmonemPlugin.bc.getService(sref);
                            System.out.println("peername: "+ pnet.getPeer().getName());
//                            System.out.println("peername: "+
//                                    AmonemPlugin.peernetwork.getPeer().
//                                    getName());
                            
                            sref = AmonemPlugin.bc.getServiceReference(FrameworkManager.class.getName());
                            FrameworkManager fm = (FrameworkManager)AmonemPlugin.bc.getService(sref);
                            
                            fm.addListener(new PeerListener());
                        }
                    });
                }
                {
                    buttonRoot = new Button(top, SWT.PUSH | SWT.CENTER);
                    buttonRoot.setText("getRoot");
                    buttonRoot.setBounds(49, 63, 60, 30);
                    buttonRoot.addSelectionListener(new SelectionAdapter() {
                        public void widgetSelected(SelectionEvent evt) {
                            System.out.println("buttonRoot pressed");
                            
//                            System.out.println("root is: "+AmonemPlugin.amonemManager.getROOT().getName());
                        }
                    });
                }
            }
			composite1.layout();
			GridLayout thisLayout = new GridLayout(3, true);
			this.setLayout(thisLayout);
			thisLayout.marginWidth = 5;
			thisLayout.marginHeight = 5;
			thisLayout.numColumns = 3;
			thisLayout.makeColumnsEqualWidth = false;
			thisLayout.horizontalSpacing = 5;
			thisLayout.verticalSpacing = 5;
			this.layout();
			addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					JadabsManagerCompositebackground.dispose();
					JadabsManagerCompositeforeground.dispose();
					topbackground.dispose();
					jadabsmanagerfont.dispose();
				}
			});
	
			postInitGUI();
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

	/** Auto-generated main method */
	public static void main(String[] args){
		showGUI();
	}

	/**
	* This static method creates a new instance of this class and shows
	* it inside a new Shell.
	*
	* It is a convenience method for showing the GUI, but it can be
	* copied and used as a basis for your own code.	*
	* It is auto-generated code - the body of this method will be
	* re-generated after any changes are made to the GUI.
	* However, if you delete this method it will not be re-created.	*/
	public static void showGUI(){
		try {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			JadabsManagerComposite inst = new JadabsManagerComposite(shell, SWT.NULL);
			shell.setLayout(new org.eclipse.swt.layout.FillLayout());
			Rectangle shellBounds = shell.computeTrim(0,0,329,294);
			shell.setSize(shellBounds.width, shellBounds.height);
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class PeerListener implements RemoteFrameworkListener
	{

        /*
         */
        public void enterFrameworkEvent(Framework fw)
        {
            System.out.println("peer entered: "+fw.getPeername());
            
        }

        /*
         */
        public void leaveFrameworkEvent(Framework fw)
        {
            System.out.println("peer left: "+ fw.getPeername());
            
        }
	    
	}
}
