/*
 */
package ch.ethz.iks.jadabs.shell.svc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;

import ch.ethz.iks.remotefw.Framework;


/**
 * A simple command-line shell to controll jadabs.
 *  
 */
public class Shell extends Thread implements IShellPluginService {

   //---------------------------------------
   // fields
   //---------------------------------------
   private static Shell me;

   private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

   private Hashtable plugIns = new Hashtable();

   private Shell() {

   }

   public static Shell getInstance() {
      if (me == null) {
         me = new Shell();
      }
      return me;
   }

   //---------------------------------------
   // MainLoop
   //---------------------------------------

   public void run() {
      System.out.println("Jadabs-Shell:");
      while (Activator.running) {
         try {
            final String[] cmd = nextCommand();
            if (cmd == null) {
               return;
            }
            if (cmd.length == 0) {
               continue;
            }
            final String cmdName = cmd[0];
            if (cmdName.equalsIgnoreCase("exit")) {
               Activator.running = false;
               break;
            } else if (cmdName.equalsIgnoreCase("quit")) {
               System.exit(0);
            }
            try {
               handleCommand(cmd);
            } catch (final Exception e) {
               System.out.println("Command failed:");
               e.printStackTrace(System.err);
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      System.out.println("... stopped Jadabs-Shell.");
   }

   /** Handles a command. */
   private void handleCommand(final String[] cmd) throws Exception {
      String cmdString = cmd[0];
      Framework context;

      if (cmdString.equalsIgnoreCase("help")) {
         help();
         return;
      } else {
         int seperator = cmdString.indexOf(".");
         if (seperator != -1) {

            String qualifier = cmdString.substring(0, seperator);
            String pluginQ;
            for (Enumeration pluginQualifiers = plugIns.keys(); pluginQualifiers
                  .hasMoreElements();) {

               pluginQ = (String) pluginQualifiers.nextElement();

               if (qualifier.equalsIgnoreCase(pluginQ)) {
                  ((IShellPlugin) plugIns.get(pluginQ)).delegateCommand(cmd);
                  return;
               }

            }

            cmdString = cmdString.substring(seperator + 1, cmdString.length());
            context = Activator.remotefw.getFrameworkByPeername(qualifier);
            if (context == null) {
               System.out.println("Error: peer " + qualifier
                     + " could not be resolved.");
               return;
            }

         } else {

            context = Activator.remotefw.getLocalFramework();

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
      }
   }

   /** Returns the next command, split into tokens. */
   private String[] nextCommand() throws IOException {
      
      System.out.print("jadabs> ");
      final String line = reader.readLine();
      
      if (line == null) {
         return null;
      }
      
      final ArrayList cmd = new ArrayList();
      
      final StringTokenizer tokens = new StringTokenizer(line);
      
      while (tokens.hasMoreTokens()) {
         cmd.add(tokens.nextToken());
      }
      
      return (String[]) cmd.toArray(new String[cmd.size()]);      
   }

   /** Does a 'help' command. */
   private void help() {
      System.out.println("Commands:");

      System.out.println("startBundle      <bid>             Starts a bundle");
      System.out.println("stopBundle       <bid>             Stops a bundle");
      System.out.println("installBundle    <location>        Installs a bundle");
      System.out.println("uninstallBundle  <bid>             Uninstalls a bundle");
      System.out.println("listBundles                        Lists all available bundles");
      System.out.println("bundleState      <bid>             Retrieves the bundle state");
      System.out.println("bundleLocation   <bid>             Retrieves the bundle location");
      
      System.out.println("help                               Shows this message.");
      System.out.println("exit                               Exits the Shell Program.");
      System.out.println("quit                               Exits Jadabs.");

      for (Enumeration pi = plugIns.elements(); pi.hasMoreElements();) {
         ((IShellPlugin) pi.nextElement()).printHelp();
      }

   }

   /**
    * @see ch.ethz.iks.jadabs.shell.svc.IShellPluginService#registerPlugin(ch.ethz.iks.jadabs.shell.svc.IShellPlugin)
    */
   public void registerPlugin(IShellPlugin plugin) {
      plugIns.remove(plugin.getQualifier());
      plugIns.put(plugin.getQualifier(), plugin);
   }

   /**
    * @see ch.ethz.iks.jadabs.shell.svc.IShellPluginService#unregisterPlugin(ch.ethz.iks.jadabs.shell.svc.IShellPlugin)
    */
   public void unregisterPlugin(IShellPlugin plugin) {
      plugIns.remove(plugin.getQualifier());
   }

}