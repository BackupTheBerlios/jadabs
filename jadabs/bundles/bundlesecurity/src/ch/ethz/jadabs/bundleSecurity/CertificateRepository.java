/*
 * Created on Jul 14, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

/**
 * @author otmar
 */
public class CertificateRepository implements Serializable{
    
    private Hashtable repo = new Hashtable();
    private String repoFileName;
    
    private static CertificateRepository instance;
    
    private CertificateRepository(String repoFileName){
        this.repoFileName = repoFileName;
    }
    
    protected static CertificateRepository Instance(String repoFileName) throws Exception{
        if (instance == null)
            instance = readFromFile(repoFileName);
        if (instance == null)
            instance = new CertificateRepository(repoFileName);
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
        obout.writeObject(this);
        obout.flush();
        obout.close();
        fout.flush();
        fout.close();
    }
    
    private static CertificateRepository readFromFile(String repoFileName) throws Exception {
        File repoFile = new File(repoFileName);
        CertificateRepository repoFromFile = null;
        if (repoFile.exists()){
            FileInputStream fin = new FileInputStream(repoFile);
            ObjectInputStream obin = new ObjectInputStream(fin);
            repoFromFile = (CertificateRepository)obin.readObject();
            obin.close();
            fin.close();
        }
        return repoFromFile;
    }
}