package ch.ethz.jadabs.amonem.ui.starter;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.amonem.manager.AmonemManagerActivator;
import ch.ethz.jadabs.amonem.ui.perspective.AmonemUI;
import ch.ethz.jadabs.bundleLoader.BundleLoaderActivator;
import ch.ethz.jadabs.jxme.JxmeActivator;
import ch.ethz.jadabs.jxme.services.impl.ServiceActivator;
import ch.ethz.jadabs.jxme.udp.UDPActivator;
import ch.ethz.jadabs.pluginLoader.PluginLoaderActivator;
import ch.ethz.jadabs.remotefw.impl.FrameworkManagerActivator;
import ch.ethz.jadabs.servicemanager.impl.ServiceManagerActivator;

/**
 * The main plugin class to be used in the desktop.
 */
public class AmonemPlugin extends AbstractUIPlugin {

	
	//The shared instance.
	private static AmonemPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	public static BundleContext bc;

	
	/**
	 * The constructor.
	 */
	public AmonemPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("amonem.AmonemPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		
		
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception 
	{
		System.out.println("Start Amonem 1.0.0");
		
		super.start(context);
		
		bc = context;
				
//		 required plugin starter
		JxmeActivator.class.getClass();
		UDPActivator.class.getClass();
		ServiceActivator.class.getClass();
		FrameworkManagerActivator.class.getClass();
		AmonemManagerActivator.class.getClass();
		BundleLoaderActivator.class.getClass();
		PluginLoaderActivator.class.getClass();
		ServiceManagerActivator.class.getClass();
			
		AmonemUI amonemUI = new AmonemUI();
		amonemUI.start();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static AmonemPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = AmonemPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
