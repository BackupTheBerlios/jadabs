/*
 * Created on Jul 28, 2004
 *
 */
package ch.ethz.jadabs.jxme.test;

import junit.framework.TestCase;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.MalformedURIException;


/**
 * @author andfrei
 * 
 */
public class EndpointAddressTest extends TestCase
{

    public void testCreateEndpointURI()
    {
        String uribtstr = "btspp://006057ba684c";
        String uribtsvcstr = "btspp://006057ba684c/service/";
        String uritcpstr = "tcp://192.168.55.1:3000";
        String uritcpsvcstr = "tcp://192.168.55.1:3000/service/";
        String urinullsvcstr = "null://null/ResolverService/urn:jxta:uuid-0:0:02";
        
        
        try
        {
            EndpointAddress uribt = new EndpointAddress(uribtstr);
            assertEquals(uribtstr, uribt.toString());
            
            EndpointAddress uribtsvc = new EndpointAddress(uribtsvcstr);
            assertEquals(uribtsvcstr, uribtsvc.toString());
            
            EndpointAddress uritcp = new EndpointAddress(uritcpstr);
            assertEquals(uritcpstr, uritcp.toString());
            
            EndpointAddress uritcpsvc = new EndpointAddress(uritcpsvcstr);
            assertEquals(uritcpsvcstr, uritcpsvc.toString());
            
            EndpointAddress endpturinullsvcstr = new EndpointAddress(urinullsvcstr);
            assertEquals(urinullsvcstr, endpturinullsvcstr.toString());
            
        } catch(MalformedURIException me)
        {
            
        }
        
    }
}
