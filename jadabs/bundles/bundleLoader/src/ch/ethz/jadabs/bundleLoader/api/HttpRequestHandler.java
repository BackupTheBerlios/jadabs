/*
 * Created on 06-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader.api;

import ch.ethz.jadabs.http.HttpSocket;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface HttpRequestHandler {

   public boolean delegate(HttpSocket request);
}
