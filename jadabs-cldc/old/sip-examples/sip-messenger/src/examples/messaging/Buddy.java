/*
 * Buddy.java
 * 
 * Created on Mar 1, 2004
 *
 */
package examples.messaging;

/**
 * @author Jean Deruelle <jean.deruelle@nist.gov>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class Buddy {
	private String sipURI=null;
	private String status=null;
	/**
	 * Creates a new Buddy with the specified sipURI
	 * @param sipURI
	 */
	public Buddy(String sipURI) {
		this.sipURI=sipURI;
		status="offline";
	}

	/**
	 * Retrieve the Sip Uri of this buddy
	 * @return the Sip Uri of this buddy
	 */
	public String getSipUri(){
		return sipURI;
	}
	
	/**
	 * Set the Sip Uri of this buddy
	 * @param sipURI - the Sip Uri of this buddy
	 */	
	public void setSipUri(String sipURI){
		this.sipURI=sipURI;
	}
	
	/**
	 * Retrieve the Sip Uri of this buddy
	 * @return the Sip Uri of this buddy
	 */
	public String getPresence(){
		return status;
	}

	/**
	 * Set the Sip Uri of this buddy
	 * @param sipURI - the Sip Uri of this buddy
	 */	
	public void setPresence(String status){
		this.status=status;
	}
}
