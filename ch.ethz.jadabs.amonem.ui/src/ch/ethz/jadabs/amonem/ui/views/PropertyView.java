
package ch.ethz.jadabs.amonem.ui.views;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import ch.ethz.jadabs.amonem.manager.DAGBundle;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.amonem.ui.perspective.Controller;
import ch.ethz.jadabs.servicemanager.ServiceReference;


public class PropertyView extends ViewPart{
	
		// SWT elements
	private static Label nameLabel;
	private static Label nameFieldLabel;
	private static Label groupLabel;
	private static Label groupFieldLabel;
	
	private static Composite parent;
	private static ScrolledComposite scrolledComposite;
	private static Composite propertyCmposite;
	
		// remember state for "update"
	private static String currentProperty;
	private static DAGPeer currentPeer;
	private static DAGBundle currentBundle;
	
		// if initialized
	private static boolean init = false;


	
//	---------------------------------------------------------------------------
//	--------------------- layout ----------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * This method is called to create the SWT elements of the view.
     * 
     * 
     * @param parent Base composite of the view
     * @return no return value
     */
	public void createPartControl(final Composite parent) {
		
		Controller.setPropertyView(this);
		
		PropertyView.parent = parent;
		
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrolledComposite.setBounds(0,0,500,300);
		
		propertyCmposite = new Composite(scrolledComposite, SWT.NONE);
		propertyCmposite.setBounds(0,0,scrolledComposite.getBounds().height,scrolledComposite.getBounds().width);
		
		scrolledComposite.setContent(propertyCmposite);
		
		init = true;
	}


//	---------------------------------------------------------------------------
//	--------------------- property layout -------------------------------------
//	---------------------------------------------------------------------------	
	
	/**
     * 
     * This method draws the property view.
     * You can choose different kinds of property types (pipe, bundles,...).
     * More informations in PeerListView
     * 
     * 
     * @param property This string contains information about the property type.
     * @param peer Show properties of this peer
     * @return no return value
     */
	public static void drawProperties(String property, DAGPeer peer){
		
		currentPeer = peer;
		currentProperty = property;
		
		
		if(property == PeerListView.getPipeString()){
			drawPipeProperties(peer);
		}
		else if(property == PeerListView.getPropertyString()){
			drawPropertyProperties(peer);
		}
		else if(property == PeerListView.getBundleString()){
			//drawBundleProperties(peer);
		}
		else { // bundleChild
			drawBundle(peer,property);
		}
	}
		
	public static void showServiceAdvertisement(String uuid, DAGPeer peer)
	{
//		propertyCmposite.dispose();
		
		
//		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
		
		propertyCmposite = new Composite(scrolledComposite, SWT.NONE);
		propertyCmposite.setBounds(0,0,scrolledComposite.getBounds().width,scrolledComposite.getBounds().height);
//		scrolledComposite.setBounds(0,0,800,300);
		
		scrolledComposite.setContent(propertyCmposite);
		

		FillLayout thisLayout = new FillLayout(org.eclipse.swt.SWT.HORIZONTAL);
		propertyCmposite.setLayout(thisLayout);

            Text consoleText = new Text(propertyCmposite, SWT.MULTI | SWT.WRAP);
//            consoleText.setText("text1");
            GridData text1LData = new GridData();
            text1LData.widthHint = 800;
            text1LData.heightHint = 800;
            consoleText.setLayoutData(text1LData);
        
        propertyCmposite.layout();
	
//		FillLayout fillLayout = new FillLayout(org.eclipse.swt.SWT.HORIZONTAL);
//		propertyCmposite.setLayout(fillLayout);
//		propertyCmposite.setSize(313, 173);
////		propertyCmposite.setBounds(0, 0, 800, 500);
//		
//		scrolledComposite.setContent(propertyCmposite);
//		
//		final Text consoleText = new Text(propertyCmposite, SWT.MULTI | SWT.WRAP | SWT.BORDER);
//		
//		GridData consoleTextLData = new GridData();
//		consoleTextLData.widthHint = 181;
//		consoleTextLData.heightHint = 108;
//        consoleText.setLayoutData(consoleTextLData);
		
	
		// fill with service ref
		ServiceReference sref = peer.getServiceReference(uuid);
		if (sref != null)
		    consoleText.setText(sref.getAdvertisement());
	}
	
	/**
     * 
     * This method is called to draw the properties of a pipe
     * 
     * 
     * @param peer Draw properties of this peer
     * @return no return value
     */
	public static void drawPipeProperties(DAGPeer peer){
		
		propertyCmposite.dispose();
		
		propertyCmposite = new Composite(scrolledComposite,SWT.NONE);
		propertyCmposite.setBounds(0, 0, 500, 200);
	
		scrolledComposite.setContent(propertyCmposite);
		
		final Label label = new Label(propertyCmposite, SWT.NONE);
		label.setBounds(10, 5, 100, 25);
		label.setText("Pipe");
		
		final Label nameLabel = new Label(propertyCmposite, SWT.NONE);
		nameLabel.setBounds(10, 35, 100, 25);
		nameLabel.setText("!!! Not available !!!");
		
	}
	
	/**
     * 
     * This method is called to draw properties for a peer
     * 
     * 
     * @param peer Draw properties of this peer
     * @return no return value
     */
	public static void drawPropertyProperties(DAGPeer peer){
		
		propertyCmposite.dispose();
		
		propertyCmposite = new Composite(scrolledComposite,SWT.NONE);
		propertyCmposite.setBounds(0, 0, 500, 200);
	
		scrolledComposite.setContent(propertyCmposite);
		
		final Label label = new Label(propertyCmposite, SWT.NONE);
		label.setBounds(10, 5, 50, 25);
		label.setText("Properties");
		
		final Label nameLabel = new Label(propertyCmposite, SWT.NONE);
		nameLabel.setBounds(10, 35, 50, 25);
		nameLabel.setText("Name:");

		final Label nameValue = new Label(propertyCmposite, SWT.NONE);
		nameValue.setText(peer.getName());
		nameValue.setBounds(70, 35, 100, 25);

	}
	
	// elements of bundle
//	public static void drawBundleProperties(DAGPeer peer){
//		
//		final Vector bundles;
//		
//		propertyCmposite.dispose();
//		
//		propertyCmposite = new Composite(scrolledComposite,SWT.NONE);
//		propertyCmposite.setBounds(0, 0, 500, 250);
//	
//		scrolledComposite.setContent(propertyCmposite);
//		
//		final Label label = new Label(propertyCmposite, SWT.NONE);
//		label.setBounds(15, 15, 100, 25);
//		label.setText("label");
//		
//		final Label labelBundles = new Label(propertyCmposite, SWT.NONE);
//		labelBundles.setBounds(30, 60, 100, 25);
//		labelBundles.setText("bundles:");
//
//		final Label labelName = new Label(propertyCmposite, SWT.NONE);
//		labelName.setBounds(215, 95, 55, 25);
//		labelName.setText("Name:");
//
//		final Label labelState = new Label(propertyCmposite, SWT.NONE);
//		labelState.setBounds(215, 125, 55, 25);
//		labelState.setText("State:");
//
//		final Label labelProperty = new Label(propertyCmposite, SWT.NONE);
//		labelProperty.setBounds(215, 155, 55, 25);
//		labelProperty.setText("Property:");
//
//		final Label labelrName = new Label(propertyCmposite, SWT.NONE);
//		labelrName.setBounds(290, 95, 100, 25);
//
//		final Label labelrState = new Label(propertyCmposite, SWT.NONE);
//		labelrState.setBounds(290, 125, 100, 25);
//
//		final Label labelrProperty = new Label(propertyCmposite, SWT.NONE);
//		labelrProperty.setBounds(290, 155, 100, 25);
//
//		final List list = new List(propertyCmposite, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
//		bundles = peer.getBundles();
//		for(int i=0;i<bundles.size();i++){
//			list.add(((DAGBundle) bundles.get(i)).getName());
//		}
//		list.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				String bundleName = ((List)e.getSource()).getSelection()[0];
//				for(int i=0;i<bundles.size();i++){
//					if(((DAGBundle)bundles.get(i)).getName().equalsIgnoreCase(bundleName)){
//						DAGBundle bundle =  ((DAGBundle)bundles.get(i));
//						try {
//							labelrName.setText(bundle.getName());
//							labelrState.setText(String.valueOf(bundle.getState()));
//							labelrProperty.setText(bundle.getProperty());
//						} catch (RuntimeException e1) {
//
//						}
//					}
//				}
//			}
//		});
//		list.setBounds(35, 90, 135, 120);	
//
//		
//	}
		
	/**
     * 
     * This method is called to draw properties of bundles
     * 
     * 
     * @param peer Draw properties of this peer
     * @param propertyName Name of the bundle
     * @return no return value
     */
	public static void drawBundle(DAGPeer peer, String propertyName){
		
		propertyCmposite.dispose();
		
		propertyCmposite = new Composite(scrolledComposite,SWT.NONE);
		propertyCmposite.setBounds(0, 0, 500, 200);
	
		scrolledComposite.setContent(propertyCmposite);
		
		DAGBundle bundle = null;
		Vector bundles = peer.getBundles();
		
		for(int i=0;i<bundles.size();i++){
			if(((DAGBundle)bundles.get(i)).getName().equals(propertyName)){
				bundle = (DAGBundle) bundles.get(i);
			}
		}
		if(bundle == null){
			return;
		}
					
		final Label name = new Label(propertyCmposite, SWT.NONE);
		name.setBounds(40, 50, 90, 25);
		name.setText("Name:");

		final Label labelName = new Label(propertyCmposite, SWT.NONE);
		labelName.setBounds(145, 50, 400, 25);
		try {
			labelName.setText(bundle.getName());
		} catch (RuntimeException e) {
			labelName.setText("");
		}

		final Label state = new Label(propertyCmposite, SWT.NONE);
		state.setBounds(40, 90, 100, 25);
		state.setText("State:");

		final Label labelState = new Label(propertyCmposite, SWT.NONE);
		labelState.setBounds(145, 90, 400, 25);
		try {		
			labelState.setText(getState(bundle.getState()));
		} catch (RuntimeException e1) {
			labelState.setText("");
		}

		final Label property = new Label(propertyCmposite, SWT.NONE);
		property.setBounds(40, 130, 100, 25);
		property.setText("Property:");

		final Label labelProperty = new Label(propertyCmposite, SWT.NONE);
		labelProperty.setBounds(145, 130, 400, 25);
		try {
			labelProperty.setText(bundle.getProperty());
		} catch (RuntimeException e2) {
			labelProperty.setText("");
		}
		
	}
	
	/**
     * 
     * This method is called to convert states (from integer to strings)
     * 
     * 
     * @param state State as integer
     * @return returns state as string
     */
	private static String getState(int state){
		switch(state)
	       {
	       case Bundle.UNINSTALLED:
	           return "UNINSTALLED";
	       case Bundle.INSTALLED:
	           return "INSTALLED";
	       case Bundle.RESOLVED:
	           return "RESOLVED";
	       case Bundle.STARTING:
	           return "STARTING";
	       case Bundle.STOPPING:
	           return "STOPPING";
	       case Bundle.ACTIVE:
	           return "ACTIVE";
	       } 
		return "";
	}

	
//	---------------------------------------------------------------------------
//	--------------------- update ----------------------------------------------
//	---------------------------------------------------------------------------
	
	/**
     * 
     * This method is called to update the property view
     * 
     * 
     * @param none No parameters needed
     * @return no return value
     */
	public static synchronized void update(){
		
		if(!init)return;
				
		if(currentPeer==null)return;
		
		Thread guit = new Thread (){	
			public void run(){	
				
				if(parent.isDisposed())return;
							
				if(Controller.getDeployRoot().getElement(currentPeer.getName())!=null
						|| Controller.getDiscoveryRoot().getElement(currentPeer.getName())!=null){
					if(currentProperty == PeerListView.getPipeString()){
						drawPipeProperties(currentPeer);
					}
					else if(currentProperty == PeerListView.getPropertyString()){
						drawPropertyProperties(currentPeer);
					}
					else if(currentProperty == PeerListView.getBundleString()){
						//drawBundleProperties(currentPeer);
					}
					else { 
						drawBundle(currentPeer,currentProperty);
					}
				}else{
					propertyCmposite.dispose();
				}
				
			};
		};
		parent.getDisplay().asyncExec(guit);
		
	}
	
	/**
     * 
     * This method is called after a peer deletion (the property composite will 
     * be disposed)
     * 
     * 
     * @param peer Deleted peer
     * @return no return value
     */
	public static synchronized void deletePeerUpdate(DAGPeer peer){
		if(currentPeer.getName().equals(peer.getName())){
			propertyCmposite.dispose();
		}
	}
	
	public void setFocus() {

		
	}
	
}
