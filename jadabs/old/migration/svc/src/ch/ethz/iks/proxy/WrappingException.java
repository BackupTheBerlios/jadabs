/*
 * Created on Sep 23, 2003
 *
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
package ch.ethz.iks.proxy;

/**
 * Marks a ciritical failure during proxy creation or usage.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class WrappingException extends IllegalArgumentException {

	/**
	 * 
	 */
	public WrappingException() {
		super();
	}

	/**
	 * @param s
	 */
	public WrappingException(String s) {
		super(s);
	}
	
	/**
	 * @param e - the exception being caught 
	 */
	public WrappingException(Throwable e) {
		super();
		this.caughtException = e;
	}
	protected Throwable caughtException = null;
	
	public void printStackTrace() {
		if (this.caughtException != null) {
			System.out.println("Caught Exception: "+this.caughtException.getClass()+" "+this.caughtException.toString());
			caughtException.printStackTrace();
			System.out.println("*************************************");
		}
		super.printStackTrace();
	}
	

}
