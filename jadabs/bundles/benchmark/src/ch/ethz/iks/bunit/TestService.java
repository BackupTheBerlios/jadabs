/*
 * Created on Apr 28, 2004
 *
 */
package ch.ethz.iks.bunit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;


/**
 * @author andfrei
 * 
 */
public abstract class TestService
{
    public static String PROP_SERVICE_NAME = "PROP_SERVICE_NAME";
    
    protected Vector testcases = new Vector();
    
    public abstract String getServiceName();
    
    public Enumeration getTestCases()
    {
        return testcases.elements();
    }
    
//    public abstract void setThreadTime(int time);
    
    public void invoke(String testcase, TestCaseTuple tuple)
    {
        try
        {
            if (!tuple.ranged)
            {
	            Method method = getClass().getMethod(testcase, null);
	            System.out.println("methodname: "+method.getName());
	            method.invoke(this, null);
            }
            else
            {                
                Method method = getClass().getMethod(testcase, 
                        new Class[]{TestCaseTuple.class});
  	            System.out.println("methodname: "+method.getName());
  	            method.invoke(this, new Object[]{tuple});
  	            
            }
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        } catch (SecurityException e)
        {
            e.printStackTrace();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
    }
    
    public void invokeSetupTeardown(String testcase, TestCaseTuple tuple)
    {
        try
        {
            Method method = getClass().getMethod(testcase, 
                    new Class[]{TestCaseTuple.class});
            System.out.println("methodname: "+method.getName());
            method.invoke(this, new Object[]{tuple});
            
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        } catch (SecurityException e)
        {
            e.printStackTrace();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
    }
}
