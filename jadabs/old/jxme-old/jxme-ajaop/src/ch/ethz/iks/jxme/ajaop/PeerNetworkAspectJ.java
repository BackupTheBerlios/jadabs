/*
 * Created on Nov 26, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.ethz.iks.jxme.ajaop;

import ch.ethz.iks.jxme.*;
import org.apache.log4j.*;

/**
 * @author daniel
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public aspect PeerNetworkAspectJ {
	
	private static Logger LOG = Logger.getLogger(PeerNetworkAspectJ.class);
	
	pointcut createMethod() : execution( * PeerNetwork.create(..) );
	
	before(IPeerNetwork thisPeerNetwork) : createMethod() && target(thisPeerNetwork){
		
		if(LOG.isDebugEnabled()){
			LOG.debug("\t\t-> Aspect on create method (" + thisPeerNetwork + ")" );
		}
		
		if (! (thisPeerNetwork instanceof JxmeService) ){
		
			JxmeServiceAopAspectJ.pnets.add(thisPeerNetwork);
		
			if(LOG.isInfoEnabled()){
				LOG.info("Adding interface " + thisPeerNetwork);
			}
		}	
	}
	
	pointcut closeMethod() : execution ( * PeerNetwork.close(..) );
	
	before(IPeerNetwork thisPeerNetwork) : closeMethod() && target(thisPeerNetwork){
	
		if(LOG.isDebugEnabled()){
			LOG.debug("\t\t-> Aspect on close method (" + thisPeerNetwork + ")" );
		}
	
		if(! (thisPeerNetwork instanceof JxmeService) ){
		
			JxmeServiceAopAspectJ.pnets.remove(thisPeerNetwork);
		
			if( LOG.isInfoEnabled() ){
				LOG.info("Removing interface " + thisPeerNetwork);
			}
			
		}
	}
	
	pointcut processMessage(IMessage data) : execution( * PeerNetwork.processMessage(IMessage) ) && args(data) ;
	
	before(IMessage data, IPeerNetwork thisPeerNetwork) : processMessage(data) && target(thisPeerNetwork){
		
		if(LOG.isDebugEnabled()){
			LOG.debug("\t\t-> Aspect on processMessage method (" + thisPeerNetwork + ")" );
		}
		
		// avoid recursive call of the processMessage methode of the class JxmeService.
		if( (! (thisPeerNetwork instanceof JxmeService) ) ){
			if( LOG.isInfoEnabled() ){
				LOG.info("==>\t\tprocessing message:\n" + data.toXMLString());
			}
			JxmeService.Instance().processMessage(data);
		}
	} 
	
	

}
