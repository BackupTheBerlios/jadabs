package ch.ethz.jadabs.ws.proxy.test;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;



/**
 * Date: 21.01.2005 21:35:17 To change this template use Options | File
 * Templates.
 * 
 * @author Franz Maier
 */

public class Proxy 
{
    private static Logger LOG = Logger.getLogger(Proxy.class.getName());

    protected static final String SBB_WEBSERVICE_URL = "http://wlab.ethz.ch:8080/axis/services/SBBWebService";
        
    private String response;

    public String getSoapResponse(String message, String args) {
        LOG.debug(message);
        try {
            response = forwardSoapObjectToServer(message, new URL(SBB_WEBSERVICE_URL));
        } catch (MalformedURLException mue) {
            LOG.debug(mue.getMessage());
        }
        return response;
    }

    /**
     * proxy-method is called when a new SoapMessage arrives through the JXME
     * EndpointService. The Message will be forwarded to the SBB Server.
     * 
     * @param string
     *            The JXME message received from the client
     * @param url
     *            The Url where the SBBWebService runs
     */
    public String forwardSoapObjectToServer(String string, URL url) {
        String response = "";
        try {
            byte[] soapMessage = string.getBytes();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("User-Agent", "ETH Jadabs Proxy for kSOAP/Axis");
            connection.setRequestProperty("SOAPAction", "");
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Content-Length", "" + soapMessage.length);
            connection.setRequestMethod("POST");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(soapMessage, 0, soapMessage.length);
            outputStream.close();
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while (reader.ready())
                response += reader.readLine() + '\n';

            inputStream.close();
            connection.disconnect();

        } catch (Exception e) {
            System.out.println(e);
        }
        return response;
    }
}
