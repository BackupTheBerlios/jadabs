/*
 * Created on Dec 1, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.ethz.iks.jxme.ajaop;

import java.util.Iterator;
import java.io.IOException;
import org.apache.log4j.Logger;
import ch.ethz.iks.jxme.*;

/**
 * send crosscut
 * @author daniel
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public aspect JxmeServiceAspectJ {
	
	private static Logger LOG = Logger.getLogger(JxmeServiceAspectJ.class);
	
	pointcut sendMethod(String destination, IMessage data) : execution( * JxmeService.send(String, IMessage) ) && args(destination, data);
	
	after(IPeerNetwork thisPeerNetwork, String destination, IMessage data) : sendMethod(destination, data) && target(thisPeerNetwork){
		
		if(LOG.isInfoEnabled()){
			LOG.info("Sending out message: " + data.toXMLString() );
		}
		
		Iterator iter =JxmeServiceAopAspectJ.pnets.iterator();
		
		while( iter.hasNext() ){
			IPeerNetwork currentPeerNetwork = (IPeerNetwork)iter.next();
			try{
				currentPeerNetwork.send(destination, data);
			} catch (IOException e){
				LOG.error("Can't send out message over interface " + currentPeerNetwork, e);
			} catch (NoPeerAvailableException e){
				LOG.error("No peer connected to interface " + currentPeerNetwork, e);
			}
		}
		
	}
}
