/*
 * Created on Jul 28, 2004
 *
 */
package ch.ethz.jadabs.jxme.ws.test;

import ch.ethz.jadabs.jxme.ws.Call;
import ch.ethz.jadabs.jxme.ws.Service;


/**
 * @author andfrei
 *
 */
public class CalcClient_Stub
{
    public int add(int i1, int i2)
    {
        String method = "add";
        
        InternalCalculator interncalc = new InternalCalculator();
        interncalc.int1 = i1;
        interncalc.int2 = i2;
        
        Service  service = new Service();
        Call     call    = (Call) service.createCall();

//        call.setTargetEndpointAddress( new java.net.URL(endpoint) );
        call.setOperationName( method );
        call.addParameter( "op1", "XMLType.XSD_INT", "ParameterMode.IN" );
        call.addParameter( "op2", "XMLType.XSD_INT", "ParameterMode.IN" );
        call.setReturnType( "XMLType.XSD_INT" );

        Integer ret = (Integer) call.invoke( method, interncalc);
        
        return ret.intValue();
    }

    class InternalCalculator
    {
        int int1, int2;
    }

}
