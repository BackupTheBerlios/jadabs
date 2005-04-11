/*
 * Created on Dec 4, 2004
 */
package ch.ethz.jadabs.im.web.user_registration;

import java.util.*;

import org.apache.tapestry.*;
import org.apache.tapestry.html.*;

import ch.ethz.jadabs.im.db.Account;
import ch.ethz.jadabs.im.db.NewAccount;

/**
 * @author Jean-Luc Geering
 */
public class Home extends BasePage {
	
	private String username;
	private String password;
	private boolean error = false;
	private String errorMsg = "";
	
	public void validate(IRequestCycle cycle) {
		super.validate(cycle);
		username = "";
		password = "";
		error = false;
	}
	public void onLogin(IRequestCycle cycle) {
		Visit v = (Visit)getVisit();
		Map parameters = new HashMap();
		parameters.put("username", username);
		parameters.put("password", password);
		List objects = v.getDataContext().performQuery("login", parameters, true);
		if (objects.size() == 1) {
			v.setAccount((Account)objects.get(0));
			cycle.activate("Configurator");
		}
		else {
			error = true;
			errorMsg = "Wrong username or password !";
		}
	}
	public void onNewAccount(IRequestCycle cycle) {
		cycle.activate("NewAccount");
	}
	public void onForgotPassword(IRequestCycle cycle) {
		cycle.activate("ForgotPassword");
	}
	public void onValidate(IRequestCycle cycle) {
		Visit v = (Visit)getVisit();
		Map parameters = new HashMap();
		parameters.put("email", cycle.getServiceParameters()[0]);
		parameters.put("password", cycle.getServiceParameters()[1]);
		List objects = v.getDataContext().performQuery("validation", parameters, true);
		if (objects.size() == 1) {
			Account a = (Account)v.getDataContext().createAndRegisterNewObject(Account.class);
			NewAccount temp = (NewAccount)objects.get(0);
			a.setSystemEmail(temp.getEmail());
			a.setPref(Configurator.EMAIL);
			a.setEmail(temp.getEmail());
			v.getDataContext().deleteObject(temp);
			//commit done when user is done with config.
			v.setAccount(a);
			cycle.activate("Configurator");
		}
		else {
			Confirmation page = (Confirmation)cycle.getPage("Confirmation");
			page.setMessage("Validation failed for email:\n"
					+cycle.getServiceParameters()[0]+"\nand password:\n"
					+cycle.getServiceParameters()[1]+"\nPlease try again !");
			page.setNext("Home");
			cycle.activate(page);	
		}
	}
	public Object [] getParams() {
		return new String [] {"param1","param2"};
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public void setUsername(String s) {
		username = s;
	}
	public void setPassword(String s) {
		password = s;
	}
	public boolean isError() {
		return error;
	}
	public String getErrorMessage() {
		return errorMsg;
	}
}
