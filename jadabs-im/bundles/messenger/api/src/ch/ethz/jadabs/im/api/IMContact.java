/*
 * Created on Nov 16, 2004
 *
 */
package ch.ethz.jadabs.im.api;


public class IMContact
{
    String username;
    int status;
    
    public IMContact(String username, int status)
    {
        this.username = username;
        this.status = status;
    }
	/**
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username The username to set.
	 */
	public void getUsername(String username) {
		this.username = username;
	}
	/**
	 * @return Returns the status.
	 */
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
	    this.status = status;
	}

}