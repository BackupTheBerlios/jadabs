/*
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader.api;

import java.io.InputStream;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface InformationSource {

   /**
    * Retrieve information undirected from all possible known information sources.
    * @param uuid uuid of the requested information, e.g. jadabs:jxme-osgi:0.7.1-SNAPSHOT:obr for the obr file for jxme-osgi. 
    * @return Reader over the retrieved information or null if no information could be retrieved.
    */
   public InputStream retrieveInformation(String uuid);
   
   /**
    * Retrieve information directed from a source.
    * @param uuid uuid uuid of the requested information, e.g. jadabs:jxme-osgi:0.7.1-SNAPSHOT:obr for the obr file for jxme-osgi.
    * @param source Source of the requested information, e.g. a hostname for a http source or a peer name for a ServiceManager source.
    * @return Reader over the retrieved information or null if no information could be retrieved.
    */
   public InputStream retrieveInformation(String uuid, String source);
   
}
