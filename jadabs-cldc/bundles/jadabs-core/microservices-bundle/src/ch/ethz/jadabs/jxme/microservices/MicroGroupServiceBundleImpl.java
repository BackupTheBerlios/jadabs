/*
 * Created on Jan 16, 2005
 *
 * $Id: MicroGroupServiceBundleImpl.java,v 1.1 2005/01/16 14:06:37 printcap Exp $
 */
package ch.ethz.jadabs.jxme.microservices;

import java.io.IOException;

import org.apache.log4j.Logger;


/**
 * This class is the implementation of the MicroGroupService on the bundle
 * side, i.e. the bundle stub.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class MicroGroupServiceBundleImpl implements MicroGroupService
{
    /** Apache Log4J logger to be used in the MicroGroupServiceBundleImpl */
    private static Logger LOG = Logger.getLogger("MicroGroupServiceBundleImpl");
    
    /**
     * Constructore creates new MicroGroupService BundleImpl
     *
     */
    public MicroGroupServiceBundleImpl()
    {
        
    }
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#publish(java.lang.String, java.lang.String, java.lang.String)
     */
    public void publish(String resourceType, String resourceName, String stringID)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#remotePublish(java.lang.String, java.lang.String, java.lang.String)
     */
    public void remotePublish(String resourceType, String resourceName, String stringID)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#localSearch(java.lang.String, java.lang.String, java.lang.String, int)
     */
    public String[] localSearch(String type, String attribute, String value, int threshold) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#remoteSearch(java.lang.String, java.lang.String, java.lang.String, int, ch.ethz.jadabs.jxme.microservices.MicroDiscoveryListener)
     */
    public void remoteSearch(String type, String attribute, String value, int threshold, MicroDiscoveryListener listener)
            throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#cancelSearch(ch.ethz.jadabs.jxme.microservices.MicroDiscoveryListener)
     */
    public void cancelSearch(MicroDiscoveryListener listener)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#create(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public String create(String resourceType, String resourceName, String precookedID, String arg)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#join(java.lang.String, java.lang.String)
     */
    public MicroGroupService join(String groupID, String password)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#send(java.lang.String, ch.ethz.jadabs.jxme.microservices.MicroMessage)
     */
    public void send(String pipeID, MicroMessage data) throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#listen(java.lang.String, ch.ethz.jadabs.jxme.microservices.MicroListener)
     */
    public void listen(String pipeID, MicroListener listener) throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#resolve(java.lang.String, int)
     */
    public boolean resolve(String pipeID, int timeout) throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#close(java.lang.String)
     */
    public void close(String stringID) throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroListener#handleMessage(ch.ethz.jadabs.jxme.microservices.MicroMessage, java.lang.String)
     */
    public void handleMessage(MicroMessage message, String listenerId)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroListener#handleSearchResponse(java.lang.String)
     */
    public void handleSearchResponse(String namedResourceName)
    {
        // TODO Auto-generated method stub

    }
    
    /** 
     * Stop MicroGroupService bundle.
     */
    public void stop()
    {
        // nothing to be done yet.
    }
}
