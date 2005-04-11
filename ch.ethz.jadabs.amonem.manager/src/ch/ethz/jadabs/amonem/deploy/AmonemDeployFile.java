/*
 * Created on Dec 28, 2004
 * 
 * AmonemDeployFile is used to store relevant information for a file (bundle) that
 * is contained in a peer we want to deploy.
 * 
 * Relevant information is:
 *  - uuid of the package (from the XML-file (= repository))
 *  - url (where to fetch the bundle from)
 *  - name (the name of the package, derived from the uuid)
 *  - filePath (the relative path in the deploy-dir where the bundle will be saved)
 *  - localDeployPath (the deploy-dir)
 * 
 */
package ch.ethz.jadabs.amonem.deploy;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/**
 * @author bam
 */

public class AmonemDeployFile {
    
    private String localDeployPath, filePath, uuid, url, name;
    private int action;
    
    
    /**
     * 
     * Create a new AmonemDeployFile with corresponding uuid, url, localDeployPath and action.
     * Action can be one of COPY, START or INSTALL
     * 
     * @param uuid
     * @param url
     * @param localDeployPath
     * @param action
     */
    public AmonemDeployFile(String uuid, String url, String localDeployPath, int action) {
        
        String tmp_str;
        StringTokenizer st = new StringTokenizer(uuid, ":", false);
        
        this.url = url;
        this.uuid = uuid;
        try {
            
            /*
             * create filePath and name for bundle from uuid
             * uuid looks like string1:string2:string3:
             * 
             * name becomes "string2-string3"
             * filePath becomes "string1/jars/string2-string3.jar"
             */
            
            /** TODO "/" should be replaced with File.separator */
            this.filePath = st.nextToken() + "/jars/";
            tmp_str = st.nextToken();
            this.filePath += tmp_str + "-";
            this.name = tmp_str + "-";
            tmp_str = st.nextToken();
            this.filePath += tmp_str + ".jar";
            this.name += tmp_str;
            
            
            this.localDeployPath = localDeployPath;
            this.action = action;
        }
        catch (NoSuchElementException e) {
            // if stringTokenizer fails (e.g. incorrect format of uuid)
            this.filePath = null;
            this.name = null;
        }
    }
    
    
    /**
     * @return Returns the action.
     */
    protected int getAction() {
        return action;
    }
    
    
    /**
     * @return Returns the filePath.
     */
    protected String getPath() {
        return filePath;
    }
    
    
    /**
     * @return Returns the fileRepo.
     */
    protected String getFullLocalPath() {
        if(localDeployPath.charAt(localDeployPath.length() - 1) != File.separatorChar) {
            return localDeployPath + File.separatorChar + filePath;
        }
        else {
            return localDeployPath + filePath;
        }
    }

    
    /**
     * @return Returns the URL.
     */
    protected String getURL() {
        return url;
    }

    
    /**
     * @return Returns the UUID.
     */
    protected String getUUID() {
        return uuid;
    }

    
    /**
     * @return Returns the name.
     */
    protected String getName() {
        return name;
    }

}
