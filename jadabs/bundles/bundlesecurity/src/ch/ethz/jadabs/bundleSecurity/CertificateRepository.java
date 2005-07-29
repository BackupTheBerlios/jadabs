/*
 * Created on Jul 14, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.Principal;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * @author otmar
 */
public class CertificateRepository{
	
	private static Logger LOG = Logger.getLogger(CertificateRepository.class);
    
    private static final String certDir = "cert";
    private static final String certSuffix = ".cer";
    private static final String repoFileName = ".trusted";
    
    private static String repoDir = BundleSecurityActivator.bc.getProperty("org.knopflerfish.gosg.jars").substring(5);
    private static String caCertLoc = repoDir + File.separator + certDir;
    private static String repoFilePath = caCertLoc + File.separator + repoFileName;
    
    private Hashtable repo = new Hashtable();
    
    FileFilter certFilter = new FileFilter(){
    	public boolean accept(File pathname){
    		return pathname.getName().endsWith(certSuffix);
    	}
    };
    
    private static CertificateRepository instance;
    
    private CertificateRepository() throws Exception{
    	loadLocalCerts();
    }
    
    protected static CertificateRepository Instance() throws Exception{
        if (instance == null)
        	instance = readFromFile();
        if (instance == null)
        	instance = new CertificateRepository();
        return instance;
    }
    
    protected void putCert(X509Certificate cert) throws Exception{
        repo.put(cert.getSubjectDN().toString(), cert);
        updateFile();
    }
    
    protected X509Certificate getCert(Principal subjectDN){
    	return (X509Certificate)repo.get(subjectDN.toString());
    }
    
    protected void removeCert(X509Certificate cert) throws Exception{
        repo.remove(cert.getSubjectDN());
        updateFile();
    }
    
    protected boolean contains(X509Certificate cert){
        return repo.containsValue(cert);
    }
    
    private void updateFile() throws Exception{
        FileOutputStream fout = new FileOutputStream(repoFileName);
        ObjectOutputStream obout = new ObjectOutputStream(fout);
        obout.writeInt(repo.size());
        for (Enumeration e = repo.elements(); e.hasMoreElements();) {
			X509Certificate element = (X509Certificate) e.nextElement();
			obout.writeObject(element.getEncoded());
		}
        obout.flush();
        obout.close();
        fout.flush();
        fout.close();
        LOG.debug("wrote " + repo.size() + " certificates to " + repoFileName);
    }
    
    private static CertificateRepository readFromFile() throws Exception {
    	File repoFile = new File(repoFilePath);
        CertificateRepository repoFromFile = null;
        CertificateFactory certFac = CertificateFactory.getInstance("X509");
        if (repoFile.exists()){
        	LOG.debug("reading certificates from " + repoFileName);
        	repoFromFile = new CertificateRepository();
            FileInputStream fin = new FileInputStream(repoFile);
            ObjectInputStream obin = new ObjectInputStream(fin);
            int nOfCerts = obin.readInt();
            for (int i = 0; i < nOfCerts; i++){
            	Object inObj = obin.readObject();
            	ByteArrayInputStream certBytes = new ByteArrayInputStream((byte[]) inObj);
            	X509Certificate cert = (X509Certificate)certFac.generateCertificate(certBytes);
            	repoFromFile.repo.put(cert.getSubjectDN().toString(), cert);
            }
            obin.close();
            fin.close();
            LOG.debug("read " + nOfCerts + " certificates from " + repoFileName);
        }
        return repoFromFile;
    }
    
    private void loadLocalCerts() throws Exception{
        LOG.debug("Loading local certificates...");
        File caCertDir = new File(caCertLoc);
        File[] certFiles = caCertDir.listFiles(certFilter);
        CertificateFactory certFact = CertificateFactory.getInstance("X.509");
        X509Certificate cert;
        boolean changed = false;
        for (int i = 0; i < certFiles.length; i++) {
        	try {
        		cert = (X509Certificate)certFact.generateCertificate(new FileInputStream(certFiles[i]));
    			cert.checkValidity();
    			if (LOG.isDebugEnabled()){
    			    MessageDigest md = MessageDigest.getInstance("MD5");
    			    md.update(cert.getEncoded());
    			    byte[] digest = md.digest();
    			    String digestEnc = "";
    			    for (int j = 0; j < digest.length; j++) {
                        digestEnc += ":" + Integer.toHexString(((int)digest[j]) & 0xff);
                    }
    			    digestEnc = (digestEnc.substring(1, digestEnc.length())).toUpperCase();
    			    LOG.debug("MD5 (Hex) Fingerprint of " + certFiles[i] + ": " + digestEnc);
    			}
    			repo.put(cert.getSubjectDN().toString(), cert);
    			changed = true;
        	} catch (Exception e){
        	    if (e instanceof CertificateNotYetValidException)
					LOG.info("Certificate in " + certFiles[i] + " is not yet valid.");
				else if (e instanceof CertificateExpiredException)
				    LOG.info("Certificate in " + certFiles[i] + " has expired.");
				else
				    LOG.debug("Error loading certificate " + certFiles[i], e);
        	}	
		}
        if (changed) updateFile();
    }
    
    
}