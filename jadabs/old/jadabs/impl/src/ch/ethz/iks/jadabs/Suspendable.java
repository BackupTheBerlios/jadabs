/*
 * Created on Dec 10, 2003
 *
 */
package ch.ethz.iks.jadabs;

/**
 * @author andfrei
 *
 */
public interface Suspendable
{
	/**
	 * Suspends the component.
	 */
	void suspend();

	/**
	 * Resumes the component.
	 */
	void resume();
}
