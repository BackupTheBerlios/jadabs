/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
 * Creator: O. Deruelle (deruelle@nist.gov)                                     *
 * Questions/Comments: nist-sip-dev@antd.nist.gov                               *
 *******************************************************************************/
package gov.nist.sip.proxy.registrar;

import gov.nist.sip.proxy.ProxyDebug;

import java.util.TimerTask;

import javax.sip.address.Address;
import javax.sip.header.ContactHeader;

/**
 * Class for removing the bindings after expires
 */
public class ExpiresTask extends TimerTask
{

    private String key;

    private ContactHeader contactHeader;

    private RegistrationsTable registrationsTable;

    /**
     * Constructor
     */
    public ExpiresTask(String key, ContactHeader contactHeader, RegistrationsTable registrationsTable)
    {
        this.registrationsTable = registrationsTable;
        this.key = key;
        this.contactHeader = contactHeader;
    }

    public void run()
    {
        Address address = contactHeader.getAddress();
        javax.sip.address.URI cleanedUri = Registrar.getCleanUri(address.getURI());
        String contactURI = cleanedUri.toString();
        registrationsTable.removeContact(key, contactHeader);
        registrationsTable.expiresTaskTable.remove(contactURI);
        ProxyDebug.println("ExpiresTask, run(), we  removed the contact: " + contactURI + " for the user: " + key);
        synchronized (registrationsTable.expiresTaskTable)
        {
            registrationsTable.expiresTaskTable.remove(key);
        }
    }

}