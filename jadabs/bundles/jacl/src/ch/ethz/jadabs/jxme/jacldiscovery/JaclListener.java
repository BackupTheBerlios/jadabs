/*
 * Created on 31.05.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.jxme.jacldiscovery;


/**
 * @author rjan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface JaclListener {
   public void JaclGotPeerEvent(String newPeer);
   public void JaclLostPeerEvent(String lostPeer);
}
