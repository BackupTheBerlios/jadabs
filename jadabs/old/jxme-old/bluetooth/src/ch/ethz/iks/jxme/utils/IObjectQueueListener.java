/**
 * JXME[dk]
 * ch.ethz.iks.utils
 * IMessageQueueListener.java
 * 
 * @author Daniel Kaeppeli, danielka@student.ethz.ch
 *
 * Jul 9, 2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.utils;


public interface IObjectQueueListener {

	public void processEvent(Object message);

}
