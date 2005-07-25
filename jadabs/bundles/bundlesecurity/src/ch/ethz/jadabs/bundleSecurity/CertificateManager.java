/*
 * Created on Jul 14, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

/**
 * @author otmar
 */
public class CertificateManager {
    
    private static Logger LOG = Logger.getLogger(CertificateManager.class);
    
    FileFilter certFilter = new FileFilter(){
    	public boolean accept(File pathname){
    		return pathname.getName().endsWith(certSuffix);
    	}
    };
    
    private static final String certDir = "cert";
    private static final String certSuffix = ".cer";
    private static final String repoFileName = ".trusted";
    
    private CertificateRepository checkedCerts;
    private CertificateFactory certFact = CertificateFactory.getInstance("X.509");
    private String caCertLoc;
    
    private static CertificateManager instance;
    
    private CertificateManager() throws Exception{
    	LOG.debug("Loading local certificates...");
        String repoDir = BundleSecurityActivator.bc.getProperty("org.knopflerfish.gosg.jars").substring(5);
        caCertLoc = repoDir + File.separator + certDir;
        String repoFile = caCertLoc + File.separator + repoFileName;
        checkedCerts = CertificateRepository.Instance(repoFile);
        loadLocalCerts();
    }
    
    private void loadLocalCerts(){
        File caCertDir = new File(caCertLoc);
        File[] certFiles = caCertDir.listFiles(certFilter);
        X509Certificate cert;
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
    			checkedCerts.putCert(cert);
        	} catch (Exception e){
        	    if (e instanceof CertificateNotYetValidException)
					LOG.info("Certificate in " + certFiles[i] + " is not yet valid.");
				else if (e instanceof CertificateExpiredException)
				    LOG.info("Certificate in " + certFiles[i] + " has expired.");
				else
				    LOG.debug("Error loading certificate " + certFiles[i], e);
        	}	
		}
    }
    
    protected X509Certificate getTrustedCertificate(byte[] certData) throws Exception{
    	X509Certificate cert;
    	X509Certificate parentCert;
		ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
		try {
			cert = (X509Certificate)certFact.generateCertificate(certStream);
		} catch (Exception e){
			LOG.info("An error occured parsing the certificate data.");
			return null;
		}
		try {
			cert.checkValidity();
		} catch (Exception e){
			if (e instanceof CertificateNotYetValidException)
				LOG.info("The supplied certificate is not yet valid.");
			else if (e instanceof CertificateExpiredException)
				LOG.info("The supplied certificate has expired.");
			checkedCerts.removeCert(cert);
			return null;
		}
		//already checked this certificate?
		if (checkedCerts.contains(cert)) return cert;
		parentCert = (X509Certificate)checkedCerts.getCert(cert.getIssuerDN());
		if (parentCert != null){
			try {
				parentCert.checkValidity();
			} catch (Exception e){
				if (e instanceof CertificateNotYetValidException)
					LOG.info("The supplied parent certificate is not yet valid.");
				else if (e instanceof CertificateExpiredException)
					LOG.info("The supplied parent certificate has expired.");
				checkedCerts.removeCert(parentCert);
			}
			try {
				cert.verify(parentCert.getPublicKey());
			} catch (Exception e){
				LOG.info("Could not verify the supplied certificate.");
				return null;
			}
			checkedCerts.putCert(cert);
			return cert;
		} else {
		    LOG.info("Could not verify certificate. Parent certificate not available");
			return null;
		}
    }
    
    public static CertificateManager Instance() throws Exception{
        if (instance == null) instance = new CertificateManager();
        return instance;
    }
    
}
