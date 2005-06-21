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
 * Created on Nov 28, 2004
 *
 */

package ch.ethz.jadabs.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

// import ch.ethz.jadabs.remotefw.BundleInfo;
// import ch.ethz.jadabs.remotefw.Framework;

/**
 * A simple command-line shell to controll jadabs.
 *  
 */
public class Shell extends Thread implements IShellPluginService {

   //---------------------------------------
   // fields
   //---------------------------------------
   private static Shell me;

   private BufferedReader reader = new BufferedReader(new InputStreamReader(
         System.in));

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
      while (ShellActivator.running) {
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
               ShellActivator.running = false;
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
      //      Framework context;

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

            // cmdString = cmdString.substring(seperator + 1,
            // cmdString.length());
            // context =
            // Activator.remotefw.getFrameworkByPeername(qualifier);
            // if (context == null) {
            //   System.out.println("Error: peer " + qualifier
            //         + " could not be resolved.");
            //   return;
            // }

            System.out.println("Error: plugin " + qualifier
                  + " could not be resolved");
            return;

            //         } else {

            //         	context = Activator.remotefw.getLocalFramework();

         }
      }

      try {

         if (cmdString.equalsIgnoreCase("startBundle")) {

            if (startBundle(Long.parseLong(cmd[1]))) {
               System.out.println("[Bundle " + cmd[1] + " started]");
            } else {
               System.out.println("[ERROR while starting " + cmd[1] + "]");
            }

         } else if (cmdString.equalsIgnoreCase("stopBundle")) {

            if (stopBundle(Long.parseLong(cmd[1]))) {
               System.out.println("[Bundle " + cmd[1] + " stopped]");
            } else {
               System.out.println("[ERROR while stopping " + cmd[1] + "]");
            }

         } else if (cmdString.equalsIgnoreCase("installBundle")) {

            if (installBundle(cmd[1]) != 0) {
               System.out.println("[Bundle at " + cmd[1] + " installed]");
            } else {
               System.out.println("[ERROR while installing " + cmd[1] + "]");
            }

         } else if (cmdString.equalsIgnoreCase("uninstallBundle")) {

            if (uninstallBundle(Long.parseLong(cmd[1]))) {
               System.out.println("[Bundle " + cmd[1] + " uninstalled]");
            } else {
               System.out.println("[ERROR while uninstalling " + cmd[1] + "]");
            }

         } else if (cmdString.equalsIgnoreCase("loadBundle")) {
            if (cmd.length == 2) {
               if (ShellActivator.bloader != null) {
                  ShellActivator.bloader.loadBundle(cmd[1] + ":obr");
               } else {
                  System.out
                        .println("Sorry, there is no BundleLoader on this machine.");
               }
            } else {
               System.out
                     .println("[USAGE: loadBundle <group:name:version>");
            }

         } else if (cmdString.equalsIgnoreCase("ss")) {

            long[] bundles = getBundles();
            System.out.println("[Available bundles:]");

            Arrays.sort(bundles);
            
            System.out.println("id	state		bundle");
            
            for (int i = 0; i < bundles.length; i++) {
            
                StringBuffer sb = new StringBuffer();
                sb.append(bundles[i]);
                sb.append("	   ");
                sb.append(getBundleStateString(bundles[i]));
                sb.append("	   ");
                sb.append(getBundleName(bundles[i]));
                System.out.println(sb.toString());
           }
            
            
         } else if (cmdString.equalsIgnoreCase("bundleState")) {

            System.out.println("[Bundle " + cmd[1] + " has state "
                  + getBundleState(Long.parseLong(cmd[1])) + "]");

         } else if (cmdString.equalsIgnoreCase("bundleLocation")) {

            System.out.println("[Bundle " + cmd[1] + " has location "
                  + getBundleState(Long.parseLong(cmd[1])) + "]");

         } else {

            System.out.println("[Unknown command " + cmdString + "]");

         }

      } catch (Exception e) {
         System.out.println("[ERROR: bad argument]");
         e.printStackTrace();
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

      System.out
            .println("startBundle      <bid>                       Starts a bundle");
      System.out
            .println("stopBundle       <bid>                       Stops a bundle");
      System.out
            .println("loadBundle       <group:name:version>        Loads a bundle using the Jadabs BundleLoader");
      System.out
            .println("installBundle    <location>                  Installs a bundle");
      System.out
            .println("uninstallBundle  <bid>                       Uninstalls a bundle");
      System.out
            .println("ss                                           display installed bundles (short status)");
      System.out
            .println("bundleState      <bid>                       Retrieves the bundle state");
      System.out
            .println("bundleLocation   <bid>                       Retrieves the bundle location");

      System.out
            .println("help                                         Shows this message.");
      System.out
            .println("exit                                         Exits the Shell Program.");
      System.out
            .println("quit                                         Exits Jadabs.");

      for (Enumeration pi = plugIns.elements(); pi.hasMoreElements();) {
         ((IShellPlugin) pi.nextElement()).printHelp();
      }

   }

   /**
    * @see ch.ethz.jadabs.shell.IShellPluginService#registerPlugin(ch.ethz.jadabs.shell.IShellPlugin)
    */
   public void registerPlugin(IShellPlugin plugin) {
      plugIns.remove(plugin.getQualifier());
      plugIns.put(plugin.getQualifier(), plugin);
   }

   /**
    * @see ch.ethz.jadabs.shell.IShellPluginService#unregisterPlugin(ch.ethz.jadabs.shell.IShellPlugin)
    */
   public void unregisterPlugin(IShellPlugin plugin) {
      plugIns.remove(plugin.getQualifier());
   }

   /*
    * (non-Javadoc)
    * 
    * @see ch.ethz.iks.remotefw.RemoteFramework#startBundle(long)
    */
   public boolean startBundle(long bid) {
      try {
         ShellActivator.b_context.getBundle(bid).start();
         return true;
      } catch (BundleException e) {
         ShellActivator.LOG.error("couldn't start bundle: " + bid);
         return false;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ch.ethz.iks.remotefw.RemoteFramework#stopBundle(long)
    */
   public boolean stopBundle(long bid) {
      try {
         ShellActivator.b_context.getBundle(bid).stop();

         return true;
      } catch (BundleException e) {
         ShellActivator.LOG.error("couldn't start bundle: " + bid);
         return false;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see ch.ethz.iks.remotefw.RemoteFramework#uninstallBundle(long)
    */
   public boolean uninstallBundle(long bid) {
      try {
         ShellActivator.b_context.getBundle(bid).uninstall();

         return true;
      } catch (BundleException e) {
         ShellActivator.LOG.error("couldn't start bundle: " + bid);
         return false;
      }
   }

   /**
    * Install Bundle locally
    */
   public long installBundle(String location) {
      try {
         ShellActivator.LOG.debug("install local bundle: " + location);
         File file = new File(location);
         FileInputStream fin = new FileInputStream(file);
         Bundle bundle = ShellActivator.b_context.installBundle(file.getName(),
               fin);
         return bundle.getBundleId();
      } catch (BundleException e) {
         ShellActivator.LOG.error("couldn't install bundle: " + location, e);
      } catch (FileNotFoundException e) {
         ShellActivator.LOG.error("file not found: " + location, e);
      }
      return -1;
   }

   /*
    * (non-Javadoc)
    * 
    * @see ch.ethz.iks.remotefw.RemoteFramework#getBundles()
    */
   public long[] getBundles() {

      Bundle[] bundles = ShellActivator.b_context.getBundles();
      long[] bids = new long[bundles.length];

      for (int i = 0; i < bundles.length; i++) {
         bids[i] = ((Bundle) bundles[i]).getBundleId();

      }

      return bids;
   }

   /*
    * (non-Javadoc)
    * 
    * @see ch.ethz.iks.remotefw.RemoteFramework#getBundleName(long)
    */
   public String getBundleName(long bid) {

      Bundle bundle = ShellActivator.b_context.getBundle(bid);

      String bname = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);

      return bname;
   }

   public String getBundleStateString(long bid)
   {
       Bundle bundle = ShellActivator.b_context.getBundle(bid);
       
       int state = bundle.getState();
       
       switch(state)
       {
       case Bundle.UNINSTALLED:
           return "UNINSTALLED";
       case Bundle.INSTALLED:
           return "INSTALLED  ";
       case Bundle.RESOLVED:
           return "RESOLVED   ";
       case Bundle.STARTING:
           return "STARTING   ";
       case Bundle.STOPPING:
           return "STOPPING   ";
       case Bundle.ACTIVE:
           return "ACTIVE     ";
       }
       
       return "ERROR";
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see ch.ethz.iks.remotefw.RemoteFramework#getBundleLocation(long)
    */
   public String getBundleLocation(long bid) {
      return "not implemented";
   }

   /*
    * (non-Javadoc)
    * 
    * @see ch.ethz.iks.remotefw.RemoteFramework#getBundleState(long)
    */
   public int getBundleState(long bid) {
      Bundle bundle = ShellActivator.b_context.getBundle(bid);

      return bundle.getState();
   }

}