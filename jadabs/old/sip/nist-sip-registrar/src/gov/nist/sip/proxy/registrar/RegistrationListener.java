/*
 * Created on Nov 14, 2004
 *
 */
package gov.nist.sip.proxy.registrar;


/**
 * @author andfrei
 * 
 */
public interface RegistrationListener
{

    public void updateRegistration(Registration registration, boolean toRemove);
}
