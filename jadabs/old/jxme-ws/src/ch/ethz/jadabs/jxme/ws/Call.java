/*
 * Created on Jul 28, 2004
 *
 */
package ch.ethz.jadabs.jxme.ws;

import java.io.IOException;
import java.util.Vector;

import com.thoughtworks.xstream.XStream;

import ch.ethz.jadabs.eventsystem.EventService;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;


/**
 * @author andfrei
 *
 */
public class Call
{
    
    String operationName;
    
    Vector params = new Vector();
    
    public void setOperationName(String opName) {
        operationName = opName ;
    }
    
    public void addParameter(String paramName, String xmlType, String parameterMode)
    {
        params.add(paramName);
    }
    
    public void setReturnType(String type)
    {
        
    }
    
    public Object invoke(String method, Object obj)
    {
        XStream xstream = new XStream();
        String objstr = xstream.toXML(obj);
        
        Element[] elm = new Element[2];
        elm[0] = new Element("METHOD_CALL", method, "");
        elm[1] = new Element("METHOD_ARGS", objstr, "");
        
        try
        {
            EndpointAddress addr = new EndpointAddress("tcp://129.132.130.2:8080");

            Service.endptsvc.send(elm, new EndpointAddress[]{addr});
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        // should now wait for a response....
        Object returns = null;
        
        return returns;
        
    }
}
