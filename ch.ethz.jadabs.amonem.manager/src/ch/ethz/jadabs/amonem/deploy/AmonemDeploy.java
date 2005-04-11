/*
 * Created on Dec 8, 2004
 * 
 * This class handles the deployment and adaption of "peers". Adaption means
 * install/remove/start/stop bundles (for the moment).
 */
package ch.ethz.jadabs.amonem.deploy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.amonem.AmonemManager;
import ch.ethz.jadabs.amonem.manager.DAGBundle;
import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.remotefw.Framework;


/**
 * This class handles the deployment and adaption of "peers". Adaption means
 * install/remove/start/stop bundles at the current state.
 * 
 * @author bam
 */
public class AmonemDeploy
{
    
    private static Logger LOG = Logger.getLogger(AmonemDeploy.class.getName());
    private AmonemManager amonemManager;
    
    private File workdir;
    
    /**
     * 
     * @param amonemManager The AmonemManager that created this AmonemDeploy
     */
    public AmonemDeploy(AmonemManager amonemManager) {
        this.amonemManager = amonemManager;
        LOG.debug("Deployer up and running...");
    }
    
    /**
     * 
     * Starts a new peer (in a new VM) on the local machine.
     * 
     * @param ads The Skeleton to deploy
     * @return True on success, False otherwise
     */
    public boolean deployLocal(AmonemDeploySkeleton ads) {
        boolean success = false;
        
        /*
         * First, we wanted to create a zip file containing all the *.jar files
         * needed to deploy (= start) a new peer on a REMOTE system.
         * 
         * We then dropped the remote deployment, so creation of a zip was not longer
         * necessary. The method used to create the zip file (createZip) can still be
         * found at the very end of this file.
         */
//        String zipName = createZip(ads.getNeededJars());

        
        /*
         * The variable success is used to check if (and what) went wrong while
         * deploying a new peer.
         * 
         * First, try to copy the needed *.jar files. Copying means fetch them from
         * the net (if necessary) into the temp-directory (ads.getWorkdir()) and then
         * put them in a folder in the deploy-dir. The folder is named after the peer,
         * if it exists, the contens is overwritten.
         */
        success = copyJars(ads.getNeededJars(), ads.getWorkdir());

        if (success) {
            success = createXargs(ads);

            if (success) {
                success = startNewPeer(ads);
            
                if (!success){
                    LOG.error("Failed to start peer; Could not execute shell command.");
                }
            }
            else {
                LOG.error("Failed to start peer; Could not create init.xargs file.");
            }
        }
        else {
            LOG.error("Failed to start peer; Could not copy requested jar-files.");
        }
        
        return success;
    }
    
    //  public boolean deployRemote(AmonemDeploySkeleton ads) {
    //  return false;
    //}

    
    /**
     * 
     * Handles the redeployment of a peer (used when restoring a saved state).
     * 
     * @param ads The AmonemDeploySkeleton that should be deployed
     * @return True if redeployment successful, False otherwise
     */
    public boolean redeployLocal(AmonemDeploySkeleton ads) {
        
        /*
         * redeployLocal is called when redeploying peers (e.g. from a saved
         * config).
         * the big difference to deployLocal is that no files are downloaded,
         * if they do not exist, the peer will not be started.
         * 
         * we agreed with andreas frei on this to make things a little easier
         * 
         * ! CHANGE !
         * redeployLocal is not used, andreas frei changed his mind -> files are
         * downloaded when redeploying, so the "normal" deployLocal is used.
         */
        
        boolean success = false;
        
        success = createXargs(ads);
        
        if (success) {
            success = startNewPeer(ads);
            
            if (!success){
                LOG.error("Failed to start peer; Could not execute shell command.");
            }
        }
        else {
            LOG.error("Failed to start peer; Could not create init.xargs file.");
        }
        
        return success;
    }
    
    
    /**
     * 
     * installBundle takes the (local!) path to bundle and sends this to a (remote) peer.
     * As far a I know the bundle must not be bigger than 8 KBytes.
     * 
     * @param fw The jadabs framework to intstall the bundle on
     * @param pathToBundle The path on the local disk to the *.jar file to send
     * @return False (always, because jadabs remoteframework.installBundle always returns -1)
     */
    public boolean installBundle(Framework fw, String pathToBundle) {
        long bid;
        
        bid = fw.installBundle(pathToBundle);
        
        if (bid == -1) {
            LOG.debug("Versuche " + pathToBundle + " auf " + fw.getPeername() + " zu installieren... failed!");
            return false;
        }
        else {
            LOG.debug("Versuche " + pathToBundle + " auf " + fw.getPeername() + " zu installieren...  SIEG!");
            return true;
        }
    }
    
    
    /**
     * 
     * @param fw The jadabs framework to start the bundle on
     * @param bid The ID of the bundle to start
     * @return True on success, False otherwise
     */
    public boolean startBundle(Framework fw, long bid) {
        LOG.debug("Versuche Bundle " + bid + " auf " + fw.getPeername() + " zu starten.");
        return fw.startBundle(bid);
    }
    
    
    /**
     * 
     * @param fw The jadabs framework to remove the bundle from
     * @param bid
     * @return True on success, False otherwise
     */
    public boolean removeBundle(Framework fw, long bid) {
        LOG.debug("Versuche Bundle " + bid + " auf " + fw.getPeername() + " zu loeschen.");
        return fw.uninstallBundle(bid);
    }
    
    
    /**
     * 
     * @param fw The jadabs framework to start the bundle on
     * @param bid
     * @return True on success, False otherwise
     */
    public boolean stopBundle(Framework fw, long bid) {
        LOG.debug("Versuche Bundle " + bid + " auf " + fw.getPeername() + " zu stoppen.");
        return fw.stopBundle(bid);
    }
    
    
    /**
     * 
     * Stop the VM of a deployed peer.
     * 
     * @param peer The DAGPeer (from the deployDAG) to kill
     */
    public void killPeer(DAGPeer peer) {
        Process proc = peer.getProcess();
        proc.destroy();
        
        /*
         * the setDeployed property of a peer is used in the GUI. this could of course
         * be handled much more elegant but there was (apparently) no time.
         */
        peer.setDeployed(false);
        
        // peer is not removed in case the user wants to save the configuration
        // after killing a peer.
//        peer.removeMyself();
    }
    
    
    /**
     * 
     * getSkeleton returns an AmonemDeploySkeleton which can then be used to deploy
     * a new peer.
     * 
     * @param name The name of the Peer to be created
     * @param workdir The (local) directory to save temporary data in
     * @param local_deploy_path The (local) directory to deploy to
     * @param xargs_template_path The (local) path to the xargs template file
     * @return An AmonemDeploySkeleton filled with the supplied information
     */
    public AmonemDeploySkeleton getSkeleton(String name, String workdir,
            String local_deploy_path, String xargs_template_path) {
        return new AmonemDeploySkeleton(name, workdir, local_deploy_path, xargs_template_path);
    }
    
    
    /**
     * 
     * @param aDeployFiles Vector containing AmonemDeployFiles, one for each bundle to install
     * @param workdir The (local) directory to save temporary data in
     * @return True on success, False otherwise
     */
    private boolean copyJars(Vector aDeployFiles, String workdir) {
        
        /*
         * This method can only be used to place *.jar files on the
         * LOCAL disc!
         * It can not put them on a remote system.
         * 
         * It can get them from a remote destination, though.
         */
        
        boolean success = true, tmpSuccess = false;
        Enumeration iterator = aDeployFiles.elements();
        AmonemDeployFile adf;	// info where to fetch the file from, where to etc.
        String completeDestinationPath;	// the full path (including filename) where the file will be saved
        String destinationPath; // the directory where the file will be saved (= completeDestinationPath without the filename)
        URL completeSourceURL;	// URL where the file has to be fetched from (can be file:///... for local files)
        
        // prepareDirPath schould take care of wrong file separators "/" under Windows
        workdir = amonemManager.prepareDirPath(workdir);
        
        // iterate over all files that have to be copied
        while (iterator.hasMoreElements()) {
            adf = (AmonemDeployFile)iterator.nextElement();
            try {
                completeSourceURL = new URL(adf.getURL());
                completeDestinationPath = amonemManager.prepareFilePath(adf.getFullLocalPath());
                
                // get the destinationPath from the completeDestinationPath
                // i.e. get rid of the filename
                int start = completeDestinationPath.lastIndexOf(File.separator);
                destinationPath = completeDestinationPath.substring(0, start+1);
                
                createDir(destinationPath);
                if (!checkIfFileExists(completeDestinationPath)) {
                    
                    // ! copyFile is overloaded ! This calls copyFile(URL, String, String)
                    tmpSuccess = copyFile(completeSourceURL, completeDestinationPath, workdir);
                    
                    if (!tmpSuccess) {
                        success = false;
                    }
                }
            }
            
            catch (MalformedURLException e) {
                LOG.warn("Malformed URL: " + e.getMessage() + " unable to retrieve file.");
                success = false;
            }
            
        }
        return success;
    }
    
    
    /**
     * @param path The (local) path to the file whos existence should be checked
     * @return True if the File exists, False otherwise
     */
    private boolean checkIfFileExists(String path) {
        File f = new File(path);
        return f.exists(); 
    }
    
    
    /**
     * 
     * Copy a file. If destination file exists, it will be overwritten.
     * 
     * @param in The (local) path to the file to copy from
     * @param out The (local) path to the file to copy into
     * @return True on success, False otherwise
     */
    private boolean copyFile(String in, String out) {
        boolean success = true;
        try {
            FileInputStream fis  = new FileInputStream(in);
            try{
            	FileOutputStream fos = new FileOutputStream(out);

            	byte[] buf = new byte[1024];
                int i = 0;
                while((i=fis.read(buf))!=-1) {
                    fos.write(buf, 0, i);
                }
                fis.close();
                fos.close();
            }
            catch (FileNotFoundException e) {
                LOG.error("Deploy: can not write output jar file (" + out + "). " + e.getMessage());
            }
        }
        catch (FileNotFoundException e) {
            LOG.error("Deploy: can not find input jar file (" + in + "). " + e.getMessage());
            success = false;
        }
        catch (IOException e) {
            LOG.error("Deploy: unable to copy jar-files. " + e.getMessage());
            success = false;
        }
        return success;
    }

    
    /**
     * 
     * @param url The URL to fetch the file from (can be file:/// too)
     * @param out The (local) path to copy the file to
     * @param workdir The (local) directory to save temporary data in
     * @return True on success, False otherwise
     */
    private boolean copyFile(URL url, String out, String workdir) {
        
        boolean success = false;
        String tmpFile;
        
        /*
         * Depending on remote or local source file, download or copy
         */
        if (url.getProtocol().equals("file")) {
            // copy
            String in = url.getHost() + url.getPath();
            
            // copyFile is overloaded, call copyFile(String, String)
            success = copyFile(in, out);
            
            LOG.debug("Copied file to: " + out);
        }
        else {
            // download
            try {
                tmpFile = downloadFile(url, workdir, false, false);
                if (!tmpFile.equals("")) {
                    // if download worked, copy the file to its destination
                    success = copyFile(tmpFile, out);
                }
                else {
                    LOG.debug("Bad filedownload, peer will not be started.");
                    success = false;
                }
            }
            catch (MalformedURLException e) {
                LOG.error("Unable to download jar: " + e.getMessage());
                success = false;
            }
        }
        return success;
    }
    
    
    /**
     * 
     * Downloads the file denoted by the param srcURL from the web to destinationDir.
     * Force (True/False) toggles, if an existing file in the destinationDir with the
     * same name should be overwritten or not.
     * Extract could be used to extract downloaded *.jar archives, but is not implemented.
     * 
     * It returns the full path to the downloaded file on success or the empty string
     * otherwise.
     * 
     * @param srcURL The URL to fetch the file from
     * @param destinationDir The (local) path to save the file to (without a filename)
     * @param force If this is True the file will be downloaded even if a file
     * 		  with the same name already exists in destinationDir 
     * @param extract If this is True a downloaded jar would be unpacked. NOT IMPLEMENTED
     * @return A string containing the path to the downloaded file on success, the empty string
     * 		   otherwise.
     * @throws MalformedURLException
     */
    public String downloadFile(URL srcURL, String destinationDir, boolean force, boolean extract)
    throws MalformedURLException {
        
        int index, total, count, len;
        String fileName;
        File dir, file;
        
        // Get the file name from the URL.
        index = srcURL.getPath().lastIndexOf('/');
        if (index == -1) {
            // if there is no "/" at all, the URL is invalid
            throw new MalformedURLException("Bad URL: " + srcURL.toString());
        }
        else {
            fileName = srcURL.getPath().substring(index + 1);
        }
        
        if (checkIfFileExists(destinationDir + fileName) && !force) {
            // if file exists and force is False, do not download the file
            LOG.info("Found local copy of " + fileName + " in " + destinationDir +
                    ". I will start the peer using this file. If this is not what you want, delete " + 
                    destinationDir + fileName);
        }
        else {
            // download the file
            try {
                LOG.debug("Connecting...");
                
                createDir(destinationDir);
                dir = new File(destinationDir);
                file = new File(dir, fileName);
                
                OutputStream os = new FileOutputStream(file);
                URLConnection conn = srcURL.openConnection();
                InputStream is = conn.getInputStream();
                
                total = conn.getContentLength();
                
                if (total > 0) {
                    LOG.info("Downloading " + fileName + " (" + total + " Bytes).");
                }
                else {
                    LOG.info("Downloading " + fileName + ".");
                }
                
                byte[] buffer = new byte[4096];
                count = 0;
                for (len = is.read(buffer); len > 0; len = is.read(buffer))
                {
                    count += len;
                    os.write(buffer, 0, len);
                }
                
                os.close();
                is.close();
                
                LOG.info("Download finished (" + fileName + ").");
                
                
                //            if (extract)
                //            {
                //                is = new FileInputStream(file);
                //                JarInputStream jis = new JarInputStream(is);
                //                out.println("Extracting...");
                //                unjar(jis, dir);
                //                jis.close();
                //                file.delete();
                //            }
                
            }
            catch (FileNotFoundException e) {
                LOG.error("Trouble opening file: " + e.getMessage());
                return "";
            }
            catch (IOException e)
            {
                LOG.error("Trouble getting file from remote: " + e.getMessage());
                return "";
            }
        }
        
        return destinationDir + File.separator + fileName;
    }
    
    
    /**
     * 
     * createXargs creates a xargs file for a peer by adapting a template.
     * It inserts -install <path> directions in the section marked by <CH.EHTZ.JADABS.AMONEM.JARSPACE>
     * (including the angel brackets) in the template and -start <num> directions
     * in the section marked by <CH.EHTZ.JADABS.AMONEM.STARTSPACE>
     * 
     * 
     * @param ads The AmonemDeploySkeleton to create the xargs for
     * @return True on success, False otherwise
     */
    private boolean createXargs(AmonemDeploySkeleton ads) {
        BufferedReader reader;
        BufferedWriter writer;

        // create the folder where the peer will live in (if necessary)
        createDir(ads.getDeploydir() + ads.getName());

        AmonemDeployFile adf;
        Enumeration jarspaceIterator = ads.getNeededJars().elements();
        Enumeration startspaceIterator = ads.getNeededJars().elements();
        String line = "";
        
        // the path to the new xargs file (including filename), filename is <name_of_peer>.xargs 
        String XARGS_WRITE_PATH = ads.getDeploydir() + ads.getName() + File.separator + ads.getName() + ".xargs";
        
        boolean success = false, done = false;
        int i;
        
//        // only go ahead if .xargs does not exist
//        if (!checkIfFileExists(XARGS_WRITE_PATH)) {
            
            // read the dummy xargs file, adapt it appropriately and write it to disk
            try {
                reader = new BufferedReader(new FileReader(ads.getXArgsTemplatePath()));
                writer = new BufferedWriter(new FileWriter(XARGS_WRITE_PATH));
                
                line = reader.readLine();
                while (!done && line != null) {
                    try {
                        if (line.indexOf("<CH.EHTZ.JADABS.AMONEM.JARSPACE>") != -1) {
                            // we hit the line where we start writing -install directions
                            line = "";
                            while (jarspaceIterator.hasMoreElements()) {
                                adf = (AmonemDeployFile)jarspaceIterator.nextElement();
                                if (adf.getAction() == ads.START || adf.getAction() == ads.INSTALL) {
                                    
                                    /*
                                     * getAction is an AmonemDeployFile feature, it helps dealing with
                                     * the fact, that some files (e.g. the framework (= platform))
                                     * must not be started.
                                     * 
                                     * getAction is either COPY, START or INSTALL. In the first case,
                                     * no -install directive will be written for this file, in the
                                     * latter two, the -install will be written (if it is START, the bundle
                                     * will be startet later)
                                     */
                                    
                                    writer.write("-install " + adf.getPath());
                                    writer.newLine();
                                }
                            }
                        }
                        else if (line.indexOf("<CH.EHTZ.JADABS.AMONEM.STARTSPACE>") != -1) {
                            // we hit the line where we start writing -start directions
                            i = 1;
                            while(startspaceIterator.hasMoreElements()) {
                                adf = (AmonemDeployFile)startspaceIterator.nextElement();
                                if (adf.getAction() == ads.START) {
                                    
                                    /*
                                     * for the bundles that have to be startet a -start directive is
                                     * written to the xargs file
                                     * 
                                     * until now, everything but the framework (= platform) is startet
                                     * (even APIs). this is not necessary, but does not hurt too much
                                     * either.
                                     */
                                    
                                    writer.write("-start " + (i));
                                    writer.newLine();
                                    i++;
                                }
                                else if (adf.getAction() == ads.INSTALL) {
                                    // if the bundle should be installed only, just increment the counter
                                    i++;
                                }
                            }
                            done = true;
                        }
                        
                        
                        /**
                         * TODO make this better...
                         */
                        
                        else {
                            writer.write(line);
                            writer.newLine();
                        }
                        line = reader.readLine();
                    }
                    catch (IOException e) {
                        LOG.error("Unable to read init.xargs template although file is open: " + e.getMessage());
                    }
                }
                writer.flush();
                reader.close();
                writer.close();
                success = true;
            }
            catch (FileNotFoundException e) {
                LOG.error(ads.getName() + ": can not find init.xargs template: " + e.getMessage());
            }
            catch (IOException e) {
                LOG.error(ads.getName() + ": can not open init.xargs for writing: " + e.getMessage());
            }
            
            return success;
        }
//        
//        // if xargs exists, assume it is good...
//        else {
//            LOG.debug("Existing xargs file found for " + ads.getName() + ", not creating a new one.");
//            LOG.debug("   --> See " + XARGS_WRITE_PATH);
//            return true;
//        }
//    }

    /**
     * 
     * startNewPeer does the actual work when deploying a new peer. This is where a new
     * JavaVM is started.
     * 
     * @param ads The AmonemDeploySkeleton to start the peer with
     * @return True on success, False otherwise
     */
    private boolean startNewPeer(AmonemDeploySkeleton ads) {
        boolean success = false;
        int i;
        Process proc;
        Vector jars;
        DAGBundle tmpBundle;  
        DAGGroup root;
        DAGPeer tmpDAGPeer;
        
        String command = amonemManager.prepareFilePath(ads.getJavaPath()) + " -Dch.ethz.jadabs.jxme.peeralias=" + ads.getName() + " -Dorg.knopflerfish.gosg.jars=file:" + ads.getDeploydir() + " -jar " + ads.getDeploydir() + "osgi" + File.separator + "jars" + File.separator + ads.getPlatformFilename() + " -xargs " + ads.getDeploydir() + ads.getName() + File.separator + ads.getName() + ".xargs";
        LOG.debug("Versuche folgenden Befehl auszufuehren: " + command);
        
        /**
         * TODO should not work with "Worldgroup" only...
         */        
        try {
            workdir = new File(ads.getDeploydir() + ads.getName());
//            LOG.debug("Workdir: " + workdir);
            proc = Runtime.getRuntime().exec(command, null, workdir);

            
            /*
             * (Especially under Windows) the output streams (output and error) of a
             * process started through Runtime.exec() have to be read, the process will
             * not start correctly otherwise.
             */
            
            // comes from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
            // any error message?
            StreamGobbler errorGobbler = new
                StreamGobbler(proc.getErrorStream(), ads.getName() + ": ERROR");            
            
            // any output?
            StreamGobbler outputGobbler = new
                StreamGobbler(proc.getInputStream(), ads.getName() + ": OUTPUT");
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            
            // proc.waitFor must not be used, would wait until JVM stops (e.g. when peer killed)
            
//            try {
//                proc.waitFor();
//            }
//            catch (InterruptedException ex) {
//                success = false;
//            }
            
            root = amonemManager.getDeployROOT();
            tmpDAGPeer = (DAGPeer)root.getElement(ads.getName());
            if(tmpDAGPeer != null) {
                /*
                 * if there is a peer with the same name in the deployDAG, remove it
                 * this is not the best way to handle this, but we agreed on it
                 */
                root.removeChild(tmpDAGPeer);
            }

            /*
             * set the relevant info in the peer
             * 
             * in my opinion, it would be more secure (or less error prone) to move
             * some things (e.g. setJavaPath) to the constructor of AmonemDAGPeer
             * but this is off-limits. 
             */
            tmpDAGPeer = new DAGPeer(ads.getName());
            tmpDAGPeer.setProcess(proc);
            tmpDAGPeer.setJavaPath(ads.getJavaPath());
            tmpDAGPeer.setPlatform(ads.getPlatformUUID());
            tmpDAGPeer.setDEPLOY_PATH(ads.getDeploydir());
            tmpDAGPeer.setDeployed(true);
            
            jars = ads.getNeededJars();
            for(i = 0; i < jars.size(); i++) {
                
                /*
                 * same thing (as with AmonemDAGPeer above) with the constructor here...
                 */
                
                tmpBundle = new DAGBundle();
                tmpBundle.setName(((AmonemDeployFile)jars.get(i)).getName());
                tmpBundle.setUUID(((AmonemDeployFile)jars.get(i)).getUUID());
                tmpBundle.setUpdateLocation(((AmonemDeployFile)jars.get(i)).getURL());
                tmpDAGPeer.setBundle(tmpBundle);
            }

            // add the child to the deployDAG and...
            root.addChild(tmpDAGPeer);
            // ...trigger the event in the manager
            amonemManager.childAddedInDeploy(tmpDAGPeer);
            
            success = true;
        }
        catch (IOException e) {
            LOG.error("Failed to start peer: " + e.getMessage());
            success = false;
        }

        return success;
    }
    


    /**
     * Creates the Directory <path> if it does not exist already
     * 
     * @param path The (local) path to the directory that should be created (if it does
     * 		  exist already
     */
    private void createDir(String path) {
        File f;
        f = new File(path);
        
        // only create the directory if it does not exist already!
        if (!f.exists()) {
            f.mkdirs();
            LOG.debug("Verzeichnis " + path + " erstellt.");
        }
        
    }
}


/*
 * comes from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * 
 * used in/with startNewPeer to read output streams
 * 
 */

class StreamGobbler extends Thread {
    
    private static Logger LOG = Logger.getLogger(StreamGobbler.class.getName());
    
    InputStream is;
    String type;
    
    StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }
    
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                LOG.debug(type + ">" + line);    
        }
        catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }
}

//private String createZip(Vector jarNames) {
//FileOutputStream fout;
//try {
//  fout = new FileOutputStream(ZIPPATH);
//}
//catch (FileNotFoundException e) {
//  LOG.info("ERROR: ZIPPATH in AmonemDeploy is a directory instead of a file. Using default location: " + DEFAULTZIPPATH);
//  try {
//  fout = new FileOutputStream(DEFAULTZIPPATH);
//  }
//  catch (FileNotFoundException e2) {
//      // this line will NEVER be reached...
//      fout = null;                
//  }
//}
//
//String currJar = "";
//byte b[] = new byte[1024];
//ZipOutputStream zout = new ZipOutputStream(fout);
//
//try {
//  for (int i = 0; i < jarNames.size(); i ++) {
//      currJar = REPOPATH + (String)jarNames.get(i);
//      InputStream in = new FileInputStream(currJar);
//      ZipEntry e = new ZipEntry(currJar.replace(File.separatorChar,'/'));
//      zout.putNextEntry(e);
//      int len=0;
//      while((len = in.read(b)) != -1) {
//          zout.write(b,0,len);
//      }
//      zout.closeEntry();
//      print(e);
//  }
//  zout.close();
//}
//catch (IOException e) {
//  LOG.info("Zip-File macht Probleme: " + e.getMessage());
//}
//return "";
//}
//
//private void print(ZipEntry e){
//PrintStream err = System.err;
//LOG.debug("Added " + e.getName());
//if (e.getMethod() == ZipEntry.DEFLATED) {
//long size = e.getSize();
//if (size > 0) {
//long csize = e.getCompressedSize();
//long ratio = ((size-csize)*100) / size;
//err.println(" (deflated " + ratio + "%)");
//}
//else {
//err.println(" (deflated 0%)");
//}
//}
//else {
//err.println(" (stored 0%)");
//}
//}
