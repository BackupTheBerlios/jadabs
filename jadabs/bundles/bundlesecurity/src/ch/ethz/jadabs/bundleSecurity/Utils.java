/*
 * Created on Jul 5, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author otmar
 */
public class Utils {

    // This method returns all available services types
    public static String[] getServiceTypes() {
        Set result = new HashSet();
    
        // All all providers
        Provider[] providers = Security.getProviders();
        for (int i=0; i<providers.length; i++) {
            // Get services provided by each provider
            Set keys = providers[i].keySet();
            for (Iterator it=keys.iterator(); it.hasNext(); ) {
                String key = (String)it.next();
                //key = key.split(" ")[0];
    
                if (key.startsWith("Alg.Alias.")) {
                    // Strip the alias
                    key = key.substring(10);
                }
                int ix = key.indexOf('.');
                result.add(key.substring(0, ix));
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }
    
    // This method returns the available implementations for a service type
    public static String[] getCryptoImpls(String serviceType) {
        Set result = new HashSet();
    
        // All all providers
        Provider[] providers = Security.getProviders();
        for (int i=0; i<providers.length; i++) {
            // Get services provided by each provider
            Set keys = providers[i].keySet();
            for (Iterator it=keys.iterator(); it.hasNext(); ) {
                String key = (String)it.next();
                //key = key.split(" ")[0];
    
                if (key.startsWith(serviceType+".")) {
                    result.add(key.substring(serviceType.length()+1));
                } else if (key.startsWith("Alg.Alias."+serviceType+".")) {
                    // This is an alias
                    result.add(key.substring(serviceType.length()+11));
                }
            }
        }
        return (String[])result.toArray(new String[result.size()]);
    }
    
    public static void print(String[] args) {
//        String provider = Security.getProviders("MessageDigest." + "SHA1")[0].getName();
//        System.out.println("Provider used for digest computation: " + provider);
//        provider = Security.getProviders("Signature." + "DSA")[0].getName();
//        System.out.println("Provider used for signature checking: " + provider);
        System.out.println("getServiceTypes:");
        String[] serv = getServiceTypes();
        for (int i = 0; i < serv.length; i++) {
            System.out.println("\t" + serv[i]);
        }
        System.out.println("getCryptoImpls MessageDigest:");
        String[] crImpls = getCryptoImpls("MessageDigest");
        for (int i = 0; i < crImpls.length; i++) {
            System.out.println("\t" + crImpls[i]);
        }
        crImpls = getCryptoImpls("Signature");
        System.out.println("getCryptoImpls Signature:");
        for (int i = 0; i < crImpls.length; i++) {
            System.out.println("\t" + crImpls[i]);
        }        
    }
    public static void main(String[] args){
        
    }
}
