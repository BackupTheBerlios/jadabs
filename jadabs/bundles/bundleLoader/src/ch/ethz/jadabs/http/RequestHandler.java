/*
 * Created on 06-Feb-2005
 */
package ch.ethz.jadabs.http;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface RequestHandler {

   public void delegate(HttpSocket request);
}
