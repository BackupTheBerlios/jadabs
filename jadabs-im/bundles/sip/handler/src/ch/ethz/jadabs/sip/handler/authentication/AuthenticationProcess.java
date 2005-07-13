/*
 * AuthenticationProcess.java
 *
 * Created on January 7, 2003, 5:30 PM
 */

package ch.ethz.jadabs.sip.handler.authentication;


//import gov.nist.sip.instantmessaging.authentication.UserTag;

import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.ioapi.Settings;
import ch.ethz.jadabs.sip.handler.IMUserAgent;

/**
 * 
 * @author olivier deruelle
 */
public class AuthenticationProcess
{
	private static Logger logger = Logger.getLogger(AuthenticationProcess.class.getName());

    private IMUserAgent imUA;
    private Settings imAuth;
    private String username;
    private String realm;
    private String password;
    private boolean ownsme = true;

    /** Creates a new instance of AuthenticationProcess */
    public AuthenticationProcess(IMUserAgent imUA, Settings imAuth)
    {
        ownsme = true;
        this.imUA = imUA;
        this.imAuth = imAuth;
    }

    public AuthenticationProcess(IMUserAgent imUA, String username, String realm,  String password) {
        ownsme = false;
        this.imUA = imUA;
        this.realm = realm;
        this.username = username;
        this.password = password;
    }
    
//    public boolean hasLoginInformations(String realmParameter)
//    {
//        for (int i = 0; i < usersTagList.size(); i++)
//        {
//            UserTag userTag = (UserTag) usersTagList.elementAt(i);
//            String realm = userTag.getUserRealm();
//            if (realm != null && realm.trim().equals(realmParameter)) { return true; }
//        }
//        return false;
//    }
//
//    public String getUserName(String realmParameter)
//    {
//        for (int i = 0; i < usersTagList.size(); i++)
//        {
//            UserTag userTag = (UserTag) usersTagList.elementAt(i);
//            String realm = userTag.getUserRealm();
//            if (realm != null && realm.trim().equals(realmParameter)) { return userTag.getUserName(); }
//        }
//        return null;
//    }
//
//    public String getPassword(String realmParameter)
//    {
//        for (int i = 0; i < usersTagList.size(); i++)
//        {
//            UserTag userTag = (UserTag) usersTagList.elementAt(i);
//            String realm = userTag.getUserRealm();
//            if (realm != null && realm.trim().equals(realmParameter)) { return userTag.getUserPassword(); }
//        }
//        return null;
//    }
//
//    public void addUser(String userName, String password, String realm)
//    {
//        UserTag userTag = new UserTag();
//        userTag.setUserName(userName);
//        userTag.setUserRealm(realm);
//        userTag.setUserPassword(password);
//
//        usersTagList.addElement(userTag);
//    }

    public Header getHeader(Response response)
    {
        try
        {

            // Proxy-Authorization header:
            ProxyAuthenticateHeader authenticateHeader = (ProxyAuthenticateHeader) response
                    .getHeader(ProxyAuthenticateHeader.NAME);

            WWWAuthenticateHeader wwwAuthenticateHeader = null;
            CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

            String cnonce = null;
            String uri = "sip:"+imUA.getRegistrar();
            String method = cseqHeader.getMethod();
            String userName = null;
            String password = null;
            String nonce = null;
            String realm = null;
            String qop = null;

            if (authenticateHeader == null)
            {
                wwwAuthenticateHeader = (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);

                nonce = wwwAuthenticateHeader.getNonce();
                realm = wwwAuthenticateHeader.getRealm();
                if (realm == null)
                {
                    logger.debug("AuthenticationProcess, getProxyAuthorizationHeader(),"
                            + " ERROR: the realm is not part of the 401 response!");
                    return null;
                }
                cnonce = wwwAuthenticateHeader.getParameter("cnonce");
                qop = wwwAuthenticateHeader.getParameter("qop");
            } else
            {

                nonce = authenticateHeader.getNonce();
                realm = authenticateHeader.getRealm();
                if (realm == null)
                {
                    logger.debug("AuthenticationProcess, getProxyAuthorizationHeader(),"
                            + " ERROR: the realm is not part of the 407 response!");
                    return null;
                }
                cnonce = authenticateHeader.getParameter("cnonce");
                qop = authenticateHeader.getParameter("qop");
            }

            /*
             * if ( hasLoginInformations(realm) ) { // We can send the stored
             * informations: userName=getUserName(realm);
             * password=getPassword(realm); } else {
             */
            // We have to ask the user:
            
            // TODO
            // userName = imAuth.getUserName();
            // real = imAuth.getrealm
            if (ownsme) {
                userName = imAuth.getUserName();
            	realm = imAuth.getRealm();
            	password = imAuth.getPassword();
            }
            else {
                userName = this.username;
                realm = this.realm;
                password = this.password;
            }

            // Let's store those informations:
//            addUser(userName, password, realm);
            //}

            HeaderFactory headerFactory = imUA.getHeaderFactory();

            DigestClientAuthenticationMethod digest = new DigestClientAuthenticationMethod();
            digest.initialize(realm, userName, uri, nonce, password, method, cnonce, "MD5");

            if (authenticateHeader == null)
            {
                AuthorizationHeader header = headerFactory.createAuthorizationHeader("Digest");
                header.setParameter("username", userName);
                header.setParameter("realm", realm);
                header.setParameter("uri", uri);
                header.setParameter("algorithm", "MD5");
                header.setParameter("opaque", "");
                header.setParameter("nonce", nonce);
                header.setParameter("response", digest.generateResponse());
                if (qop != null)
                    header.setParameter("qop", qop);

                return header;

            } else
            {
                ProxyAuthorizationHeader header = headerFactory.createProxyAuthorizationHeader("Digest");
                header.setParameter("username", userName);
                header.setParameter("realm", realm);
                header.setParameter("uri", uri);
                header.setParameter("algorithm", "MD5");
                header.setParameter("opaque", "");
                header.setParameter("nonce", nonce);
                header.setParameter("response", digest.generateResponse());
                if (qop != null)
                    header.setParameter("qop", qop);

                return header;

            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
    
}