/*
 * Created on Dec 8, 2004
 * 
 * AmonemDeploySkeleton is used to store relevant information about a peer that
 * will be deployed.
 * 
 * relevant information is:
 *  - the needed Jars (= bundles), vector of AmonemDeployFiles
 *  - name (the name of the peer)
 *  - javaPath (the path to the java runtime that should be used to start this peer)
 *  - platform (the uuid of the platform bundle)
 * 
 */
package ch.ethz.jadabs.amonem.deploy;

import java.io.File;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

//import org.apache.log4j.Logger;


/**
 * 
 * This class provides the skeleton (kind of a framework) of a peer. This sekelton ist then
 * used to depoy the peer.
 * 
 * relevant information is:
 *  - the needed Jars (= bundles), vector of AmonemDeployFiles
 *  - name (the name of the peer)
 *  - javaPath (the path to the java runtime that should be used to start this peer)
 *  - platform (the uuid of the platform bundle)
 * 
 * @author bam
 */
public class AmonemDeploySkeleton
{
//    private Logger LOG = Logger.getLogger(AmonemDeploySkeleton.class.getName());
    
    private Vector neededJars = new Vector(); // vector containing AmonemDeployFiles
    private String name;
    private String javaPath;
    private String platform; //uuid of the platform, used for redeployment
    
    private final String WORKDIR;	// where to store temp. data (e.g. downloaded jar)
    private final String LOCAL_DEPLOY_DIR;	// where to deploy the peer
    private final String XARGS_TEMPLATE_PATH;	// where to find the xargs template (has to be a local path!)
    
    /*
     * These are the tings you can do with a neededJar:
     * 
     * 1. (only) install it in the osgi-framework of the peer
     * 2. install and start it in the osgi-framework of the peer
     * 3. just copy it to the deploy-folder
     */
    public final int INSTALL = 1; 
    public final int START = 2; 
    public final int COPY = 3;
    
    
    /**
     * 
     * The parameters mentioned have to be passed to the constructor since e.g. the name is vital because it is used as identifier.
     * 
     * @param name The name of the peer to create
     * @param workdir The directory where temporary files can be downloaded to
     * @param local_deploy_dir The directory where the peer will be installed to. A subdir with
     * 			the name of the peer will be created in this directory.
     * @param xargs_template_path The full path to the xargs template file
     */
    public AmonemDeploySkeleton(String name, String workdir,
            String local_deploy_dir, String xargs_template_path) {
        this.name = name;
        
        
        // make sure, we have a File.separator at the end of directory-strings.
        if(workdir.length() > 0 && workdir.charAt(workdir.length() - 1) != File.separatorChar) {
            this.WORKDIR = workdir + File.separatorChar;
        }
        else {
            this.WORKDIR = workdir;
        }
        
        if(local_deploy_dir.length() > 0 && local_deploy_dir.charAt(local_deploy_dir.length() - 1) != File.separatorChar) {
            this.LOCAL_DEPLOY_DIR = local_deploy_dir + File.separatorChar;
        }
        else {
            this.LOCAL_DEPLOY_DIR = local_deploy_dir;
        }
        
        // this is a path to a file, so do not append a File.separator!
        this.XARGS_TEMPLATE_PATH = xargs_template_path;
    }
    
    
    /**
     * @return Returns the name of the skeleton.
     */
    protected String getName() {
        return name;
    }

    
    /**
     * @return Returns the neededJars (vector of AmonemDeployFiles).
     */
    protected Vector getNeededJars() {
        return neededJars;
    }
    
    
    /**
     * @return Returns the javaPath.
     */
    public String getJavaPath() {
        return javaPath;
    }
    
    
    /**
     * 
     * Set the javaPath through this method. The javaPath is the (full) local path to the
     * jre that should be used to run this peer with.
     * 
     * @param javaPath The javaPath to set.
     */
    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    
    /**
     * 
     * Set the platform through this method. The platform is handled like a ordinary bundle
     * with the difference that it is only copied to the framework of the peer but not
     * installed and started.
     * 
     * @param uuid The UUID of the platform file
     * @param url The URL to the platform file
     */
    public void setPlatform(String uuid, String url) {
        // the platform is only copied to the deploy folder,
        // otherwise it is handled like all other bundles
        addJar(uuid, url, COPY);
        this.platform = uuid;
    }

    
    /**
     * 
     * This method adds a jar-file to the skeleton if it has a uuid different to all already
     * contained jars.
     * 
     * @param uuid UUID of the jar file
     * @param url URL to the jar file (can be a local file too, use file:///...)
     */
    public void addJar(String uuid, String url) {
        // we set START for all bundles because it does not hurt, it would be
        // possible to set COPY or INSTALL here too...
        // (see addJar below)
        addJar(uuid, url, START);
    }
    
    
    /**
     * 
     * @param uuid The UUID of the jar
     * @param url The URL of the jar
     * @param action Either COPY, INSALL or START
     */
    private void addJar(String uuid, String url, int action) {

        AmonemDeployFile adf = new AmonemDeployFile(uuid, url, LOCAL_DEPLOY_DIR, action);
        
        if (contained(uuid) == null) {
            // only add jar if it has a unique uuid
            neededJars.add(adf);
        }
        
    }
    
    
    /**
     * 
     * @param uuid The UUID of the jar to remove
     */
    public void removeJar(String uuid) {
        
        // contained returns the AmonemDeployFile if it is found, null otherwise
        neededJars.remove(contained(uuid));

    }

    
    /**
     * 
     * @param adf The AmonemDeployFile to remove
     */
    public void removeJar(AmonemDeployFile adf) {
        neededJars.remove(adf);
    }
    
    
    /**
     * 
     * Checks if a AmonemDeployFile with the given uuid has already been added to this skeleton.
     * 
     * @param uuid The UUID of the AmonemDeployFile to check for
     * @return The AmonemDeployFile with the matching UUID, null if no match
     */
    private AmonemDeployFile contained(String uuid) {
        
        AmonemDeployFile tmpADF;
        Enumeration iterator = neededJars.elements();
        
        /*
         * iterate through all AmonemDeployFiles in neededJars and
         * return the one with a matching uuid if one exists or null otherwise.
         */
        while (iterator.hasMoreElements()) {
            tmpADF = (AmonemDeployFile)iterator.nextElement();
            if (tmpADF.getUUID().equals(uuid)) {
                return tmpADF;
            }
        }
        
        return null;
        
    }
    
    
    /**
     * 
     * @return The UUID of the platform of this skeleton
     */
    protected String getPlatformUUID() {
        return this.platform;
    }
    
    
    /**
     * 
     * @return The filename of the platform of this skeleton (no path but with .jar appended),
     * null if there is an error.
     */
    protected String getPlatformFilename() {
        String filename = "";
        StringTokenizer st = new StringTokenizer(this.platform, ":");
        
        /*
         * generate the filename of the platform "on the fly" using the uuid
         * string1:string2:string3: 
         * becomes
         * string2-string3.jar
         */
        try {        
            st.nextToken(); // dump the first token (string1)
            filename = st.nextToken() + "-" + st.nextToken() + ".jar";
        }
        catch (NoSuchElementException e) {
            filename = null;
        }

        return filename;
    }
    
    
    /**
     * 
     * @return The path to the workdir (as passed to the constructor)
     */
    protected String getWorkdir() {
        return this.WORKDIR;
    }
    
    
    /**
     * 
     * @return The path to the deploydir (as passed to the constructor)
     */
    protected String getDeploydir() {
        return this.LOCAL_DEPLOY_DIR;
    }

    
    /**
     * 
     * @return The path to the xargs template (as passed to the constructor)
     */
    protected String getXArgsTemplatePath() {
        return this.XARGS_TEMPLATE_PATH;
    }

}