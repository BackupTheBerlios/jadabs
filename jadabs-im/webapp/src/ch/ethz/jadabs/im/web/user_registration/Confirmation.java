/*
 * Created on Dec 4, 2004
 */
package ch.ethz.jadabs.im.web.user_registration;

import org.apache.tapestry.*;
import org.apache.tapestry.html.*;

/**
 * @author Jean-Luc Geering
 */
public class Confirmation extends BasePage {
	
	private String message;
	private String next;
	
	public void setMessage(String s) {
		message = s;
	}
	public void setNext(String page) {
		next = page;
	}
	public String getMessage() {
		return message;
	}
	public void onNextPage(IRequestCycle cycle) {
		cycle.activate(next);
	}	
}
