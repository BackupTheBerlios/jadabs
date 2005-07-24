/*
 * Created on Jul 14, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleLoader.BundleLoaderActivator;

/**
 * @author otmar
 */
public class CertificateManager {
    
    private static Logger LOG = Logger.getLogger(CertificateRepository.class);
    
    FileFilter certFilter = new FileFilter(){
    	public boolean accept(File pathname){
    		return pathname.getName().endsWith(certSuffix);
    	}
    };
    
    private static final String certDir = "cert";
    private static final String repoDir = "repository";
    private static final String certSuffix = ".cer";
    
    private Hashtable checkedCerts;
    private CertificateFactory certFact = CertificateFactory.getInstance("X.509");
    private String httpRepo;
    private String caCertLoc;
    private X509Certificate rootCertificate;
    
    private static CertificateManager instance;
    
    private CertificateManager() throws Exception{
    	LOG.debug("Loading local certificates...");
        String repoDir = BundleSecurityActivator.bc.getProperty("org.knopflerfish.gosg.jars").substring(5);
        caCertLoc = repoDir + File.separator + certDir;
        File caCertDir = new File(caCertLoc);
        File[] certFiles = caCertDir.listFiles(certFilter);
        X509Certificate cert;
        for (int i = 0; i < certFiles.length; i++) {
        	try {
        		cert = (X509Certificate)certFact.generateCertificate(new FileInputStream(certFiles[i]));
    			cert.checkValidity();
        		checkedCerts.put(cert.getSubjectDN(), cert);
        	} catch (Exception e){
        		LOG.debug("Error loading certificate " + certFiles[i], e);
        	}	
		}
    }
    
    protected boolean verifyCertificate(byte[] certData){
    	X509Certificate cert;
    	X509Certificate parentCert;
		ByteArrayInputStream certStream = new ByteArrayInputStream(certData);
		try {
			cert = (X509Certificate)certFact.generateCertificate(certStream);
		} catch (Exception e){
			LOG.info("An error occured parsing the certificate data.");
			return false;
		}
		try {
			cert.checkValidity();
		} catch (Exception e){
			if (e instanceof CertificateNotYetValidException)
				LOG.info("The supplied certificate is not yet valid.");
			else if (e instanceof CertificateExpiredException)
				LOG.info("The supplied certificate has expired.");
			checkedCerts.remove(cert.getSubjectDN());
			return false;
		}
		//already checked this certificate?
		if (checkedCerts.contains(cert)) return true;
		parentCert = (X509Certificate)checkedCerts.get(cert.getIssuerDN());
		if (parentCert != null){
			try {
				parentCert.checkValidity();
			} catch (Exception e){
				if (e instanceof CertificateNotYetValidException)
					LOG.info("The supplied parent certificate is not yet valid.");
				else if (e instanceof CertificateExpiredException)
					LOG.info("The supplied parent certificate has expired.");
				checkedCerts.remove(parentCert.getSubjectDN());
			}
			try {
				cert.verify(parentCert.getPublicKey());
			} catch (Exception e){
				LOG.info("Could not verify the supplied certificate.");
				return false;
			}
			checkedCerts.put(cert.getSubjectDN(), cert);
			return true;
		}
		LOG.info("Could not verify certificate. Parent certificate not available");
		return false;
    }
    
    public static CertificateManager Instance() throws Exception{
        if (instance == null) instance = new CertificateManager();
        return instance;
    }

    // only trusted certificates are stored local
    private X509Certificate getLocalCertificate(String identifier) throws Exception{
        LOG.debug("getting local certificate with ID " + identifier);
        FileInputStream certFile = new FileInputStream(getCertPath(identifier));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate)cf.generateCertificate(certFile);
    }
    
    private boolean isLocal(String identifier){
        File certFile = new File(getCertPath(identifier));
        return certFile.exists();
    }
    
    protected X509Certificate getTrustedCertificate(String identifier) throws Exception{
        if (isLocal(identifier)){
        	X509Certificate certificate = getLocalCertificate(identifier);
        	try{
        		certificate.checkValidity();
        		return certificate;
        	}catch (Exception e){
        		// fall through and try it with a remote version...
        	}
        }
        return getRemoteCertificate(identifier);
    }
    
    private X509Certificate getRemoteCertificate(String identifier) throws Exception{
        LOG.debug("getting remote certificate with ID " + identifier);
        String urlString = "http://" + httpRepo + "/" + repoDir + "/" + certDir + "/" + identifier + ".cer";
        URL certURL = new URL(urlString);
        InputStream certStream = certURL.openStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)cf.generateCertificate(certStream);
        certificate.verify(rootCertificate.getPublicKey());
        certificate.checkValidity();
        FileOutputStream certFile = new FileOutputStream(getCertPath(identifier));
        certFile.write(certificate.getEncoded());
        certFile.flush();
        certFile.close();
        return certificate;
    }
    
    private String getCertPath(String identifier){
        String retVal = localCertDir + File.separator + identifier;
        if (!identifier.endsWith(certSuffix)) retVal += certSuffix;
        return retVal;
    }
}
