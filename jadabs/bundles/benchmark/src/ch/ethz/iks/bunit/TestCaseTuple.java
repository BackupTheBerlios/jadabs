/*
 * Created on Apr 28, 2004
 *
 */
package ch.ethz.iks.bunit;

/**
 * @author andfrei
 * 
 */
public class TestCaseTuple
{
    public String testcase;
    public String deptestservice;
    public String deptestcase;
    
    public boolean ranged = true;
    public int crtrange = -1;
    
    public TestCaseTuple(String testcase, String deptestservice, String deptestcase)
    {
        this.testcase = testcase;
        this.deptestservice = deptestservice;
        this.deptestcase = deptestcase;
    }
}
