/*
 * Created on Jul 14, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleLoader.BundleLoaderActivator;

/**
 * @author otmar
 */
public class CertificateRepository {
    
    private static Logger LOG = Logger.getLogger(CertificateRepository.class);
    
    private static final String certDir = "cert";
    private static final String repoDir = "repository";
    
    private String httpRepo;
    private String localCertDir;
    private X509Certificate rootCertificate;
    
    private static CertificateRepository instance;
    
    private CertificateRepository() throws Exception{
        httpRepo = BundleLoaderActivator.bc.getProperty("ch.ethz.jadabs.bundleloader.httprepo");
        String repoDir = BundleSecurityActivator.bc.getProperty("org.knopflerfish.gosg.jars").substring(5);
        localCertDir = repoDir + File.separator + certDir;
        String rootCertID = BundleSecurityActivator.bc.getProperty("ch.ethz.jadabs.bundlesecurity.rootcertificate");;
        rootCertificate = getLocalCertificate(rootCertID);
        LOG.debug("CertificateRepository initialized.");
    }
    
    public static CertificateRepository Instance() throws Exception{
        if (instance == null) instance = new CertificateRepository();
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
        if (isLocal(identifier)) return getLocalCertificate(identifier);
        else return getRemoteCertificate(identifier);
    }
    
    private X509Certificate getRemoteCertificate(String identifier) throws Exception{
        LOG.debug("getting remote certificate with ID " + identifier);
        String urlString = "http://" + httpRepo + "/" + repoDir + "/" + certDir + "/" + identifier + ".cer";
        URL certURL = new URL(urlString);
        InputStream certStream = certURL.openStream();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)cf.generateCertificate(certStream);
        certificate.verify(rootCertificate.getPublicKey());
        FileOutputStream certFile = new FileOutputStream(getCertPath(identifier));
        certFile.write(certificate.getEncoded());
        certFile.flush();
        certFile.close();
        return certificate;
    }
    
    private String getCertPath(String identifier){
        String retVal = localCertDir + File.separator + identifier;
        if (!identifier.endsWith(".cer")) retVal += ".cer";
        return retVal;
    }
}
