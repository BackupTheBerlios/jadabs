/*
 * Created on Nov 26, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.ethz.iks.jxme.ajaop;

import ch.ethz.iks.cop.IComponent;

import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author daniel
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JxmeServiceAopAspectJ
	implements IComponent {

	private static Logger LOG = Logger.getLogger(JxmeServiceAopAspectJ.class);
	
	protected static Vector pnets = new Vector();
	
	private static JxmeServiceAopAspectJ  jxmesvcaop = null;

	// make consturctor private (singleton)
	private JxmeServiceAopAspectJ(){
		if( LOG.isDebugEnabled() ){
			LOG.debug("Create instance of JxmeServiceAopAspectJ");
		}
		return;
	}
	
	
	public static JxmeServiceAopAspectJ Instance(){
		if(jxmesvcaop == null){
			jxmesvcaop = new JxmeServiceAopAspectJ();
			if(LOG.isDebugEnabled()){
				LOG.debug("Create instance of JxmeServiceAopAspectJ");
			}
		}
		return jxmesvcaop;
	}

	public void initComponent() {
		if (jxmesvcaop == null){
			jxmesvcaop = new JxmeServiceAopAspectJ();
		}
	}

	public void startComponent(String[] args) {
		return;

	}


	public void stopComponent() {
		return;
	}

}
