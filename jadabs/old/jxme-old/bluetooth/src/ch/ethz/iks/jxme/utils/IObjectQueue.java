/**
 * JXME[dk]
 * ch.ethz.iks.utils
 * IObjectQueue.java
 * 
 * @author Daniel Kaeppeli, danielka@student.ethz.ch
 *
 * Jul 10, 2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.utils;


public interface IObjectQueue {
	
	public void putEvent( Object message );
	
	public void addListener( IObjectQueueListener listener);
//	
//	public void removeListener( IObjectQueueListener listener);

	public void stopQueue();

}
