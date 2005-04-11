package ch.ethz.jadabs.sbbproxy;


import java.net.URL;

/**
 * This class has to be implemented by clients using the SBB Service.
 *
 * @author Franz Maier
 */                                                             
public interface IForwardSoapMessage {
    String forwardSoapObjectToServer(String str, URL url);
}
