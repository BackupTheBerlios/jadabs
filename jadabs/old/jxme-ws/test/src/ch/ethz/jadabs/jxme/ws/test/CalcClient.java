/*
 * Created on Jul 28, 2004
 *
 */
package ch.ethz.jadabs.jxme.ws.test;

/**
 * @author andfrei
 *
 */
public class CalcClient
{
    public static void main(String [] args) throws Exception 
    {
        new CalcClient().add(3,4);
    }
    
    public int add(int i1, int i2)
    {
      return new CalcClient_Stub().add(i1, i2);
    }
}
