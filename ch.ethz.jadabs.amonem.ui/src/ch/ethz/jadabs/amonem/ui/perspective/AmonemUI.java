package ch.ethz.jadabs.amonem.ui.perspective;

import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.amonem.AmonemManager;
import ch.ethz.jadabs.amonem.ui.starter.AmonemPlugin;


public class AmonemUI {

	public static AmonemManager amonemManager;
	
	public void start()
	{
		// get AmonemManager 
		ServiceReference sref = AmonemPlugin.bc.getServiceReference(AmonemManager.class.getName());
		amonemManager = (AmonemManager)AmonemPlugin.bc.getService(sref);
		
		
		// register all listeners
		amonemManager.addGroupListener(new GroupListenerImpl());
		amonemManager.addPeerListener(new PeerListenerImpl());
		amonemManager.addBundleListener(new BundleListenerImpl());

		
		// start the amonem Manager
		amonemManager.start();
	
		
		// get and set roots
		Controller.setDiscoveryRoot(amonemManager.getDiscoveryROOT());
		Controller.setDeployRoot(amonemManager.getDeployROOT());
		
	}
	
}
