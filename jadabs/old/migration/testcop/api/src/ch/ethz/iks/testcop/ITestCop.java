package ch.ethz.iks.testcop;

/**
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface ITestCop {
	
	public void nop();
	
	public ITestCop getInternalObj();
	
	public void subscr(ITestListener l);
}