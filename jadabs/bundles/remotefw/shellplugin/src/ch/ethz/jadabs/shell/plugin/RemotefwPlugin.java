package ch.ethz.jadabs.shell.plugin;

import ch.ethz.jadabs.remotefw.Framework;
import ch.ethz.jadabs.shell.IShellPlugin;

/**
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class RemotefwPlugin implements IShellPlugin {

	/**
	 * @see ch.ethz.jadabs.shell.IShellPlugin#getQualifier()
	 */
	public String getQualifier() {
		return new String("remote");
	}

	/**
	 * @see ch.ethz.jadabs.shell.IShellPlugin#delegateCommand(java.lang.String[])
	 */
	public void delegateCommand(String[] cmd) {
        Framework context = null;		
        String cmdString = cmd[0];
        
        int seperator = cmdString.indexOf(".");
        if (seperator != -1) { 
           cmdString = cmdString.substring(++seperator);
        } else {
           throw new IllegalArgumentException();           
        }
        
        seperator = cmdString.indexOf(".");
        if (seperator != -1) {

           String qualifier = cmdString.substring(0, seperator);
           String pluginQ;
        
           cmdString = cmdString.substring(seperator + 1, cmdString.length());
           context = PluginActivator.remotefw.getFrameworkByPeername(qualifier);
           if (context == null) {
             System.out.println("Error: peer " + qualifier
                   + " could not be resolved.");
             return;
           }
           
        }
     

     try {

        if (cmdString.equalsIgnoreCase("startBundle")) {

           if (context.startBundle(Long.parseLong(cmd[1]))) {
              System.out.println("[Bundle " + cmd[1] + " started]");
           } else {
              System.out.println("[ERROR while starting " + cmd[1] + "]");
           }

        } else if (cmdString.equalsIgnoreCase("stopBundle")) {

           if (context.stopBundle(Long.parseLong(cmd[1]))) {
              System.out.println("[Bundle " + cmd[1] + " stopped]");
           } else {
              System.out.println("[ERROR while stopping " + cmd[1] + "]");
           }

        } else if (cmdString.equalsIgnoreCase("installBundle")) {

           if (context.installBundle(cmd[1]) != 0) {
              System.out.println("[Bundle at " + cmd[1] + " installed]");
           } else {
              System.out.println("[ERROR while installing " + cmd[1] + "]");
           }

        } else if (cmdString.equalsIgnoreCase("uninstallBundle")) {

           if (context.uninstallBundle(Long.parseLong(cmd[1]))) {
              System.out.println("[Bundle " + cmd[1] + " uninstalled]");
           } else {
              System.out.println("[ERROR while uninstalling " + cmd[1] + "]");
           }
           
        } else if (cmdString.equalsIgnoreCase("loadBundle")) {
        	if (cmd.length > 3) {
        		// TODO: implement load
        		// context.load(cmd[1], cmd[2], cmd[3]);
        	} else {
        		System.out.println("[USAGE: loadBundle <bundlename> <bundleid> <bundleversion>");
        	}

        } else if (cmdString.equalsIgnoreCase("listBundles")) {

           long[] bundles = context.getBundles();
           System.out.println("[Available bundles:]");
           for (int i = 0; i < bundles.length; i++) {
              System.out.println("[BID " + bundles[i] + " - "
                    + context.getBundleName(bundles[i]) + "]");
           }

        } else if (cmdString.equalsIgnoreCase("bundleState")) {

           System.out.println("[Bundle " + cmd[1] + " has state "
                 + context.getBundleState(Long.parseLong(cmd[1])) + "]");

        } else if (cmdString.equalsIgnoreCase("bundleLocation")) {

           System.out.println("[Bundle " + cmd[1] + " has location "
                 + context.getBundleState(Long.parseLong(cmd[1])) + "]");

        } else {

           System.out.println("[Unknown command " + cmdString + "]");

        }

     } catch (Exception e) {
        System.out.println("[ERROR: bad argument]");
        e.printStackTrace();
     }
	}

	/**
	 * @see ch.ethz.jadabs.shell.IShellPlugin#printHelp()
	 */
	public void printHelp() {
		System.out.println("RemoteFW Shell Plugin:");
		System.out.println("remote.<peername>.<command>");
	}

}
