/*
 * Created on Dec 6, 2004
 */
package ch.ethz.jadabs.im.web.user_registration;

import ch.ethz.jadabs.im.db.*;
import java.util.*;
import org.apache.tapestry.*;
import org.apache.tapestry.html.*;
import org.objectstyle.cayenne.exp.Expression;
import org.objectstyle.cayenne.query.SelectQuery;

/**
 * @author Jean-Luc Geering
 */
public class Configurator extends BasePage {
	
	public final static Integer EMAIL = new Integer(1);
	public final static Integer SMS = new Integer(2);
	public final static Integer BOTH = new Integer(3);
	
	private boolean error = false;
	private String errorMsg;
	
	private String password2;
	
	public void validate(IRequestCycle cycle) {
		super.validate(cycle);
		error = false;
		Visit v = (Visit)getVisit();
		if (!v.isAuthenticated()) {
			cycle.activate("Home");
		}
	}
	public void onSave(IRequestCycle cycle) {
		Visit v = (Visit)getVisit();
		Account a = v.getAccount();
		try {
			if (a.getUsername() == null || a.getUsername().length() < 3) {
				throw new Exception("The username is too short !");
			} 
			Expression e = Expression.fromString("systemEmail!=$systemEmail");
			//e = e.notExp();
			e = e.andExp(Expression.fromString("username=$username"));
			
			Map parameters = new HashMap();
			parameters.put("username", a.getUsername());
			parameters.put("systemEmail", a.getSystemEmail());
			//List objects = v.getDataContext().performQuery("unique_user", parameters, true);
			
			List objects = v.getDataContext().performQuery(new SelectQuery(Account.class, e.expWithParameters(parameters)));
			if (objects.size() > 0) {
				throw new Exception("This username is alredy taken !");
			}
			if (a.getPassword() == null || a.getPassword().length() < 3) {
				a.setPassword("");
				throw new Exception("The password is too short !");
			}
			else if (!a.getPassword().equals(password2)) {
				a.setPassword("");
				throw new Exception("The two passwords do not match !");
			}
			//TODO test email & phone
			a.setMobilePhone(a.getMobilePhone().replaceAll(" ", ""));
			
			v.getDataContext().commitChanges();
			v.logout();
			Confirmation page = (Confirmation)cycle.getPage("Confirmation");
			page.setMessage("Your account has been saved !");
			page.setNext("Home");
			cycle.activate(page);	
		}
		catch (Exception e) {
			error = true;
			errorMsg = e.getMessage();
		}
	}
	public void onCancel(IRequestCycle cycle) {
		Visit v = (Visit)getVisit();
		v.getDataContext().rollbackChanges();
		v.logout();
		cycle.activate("Home");
	}
	public boolean isError() {
		return error;
	}
	public String getErrorMessage() {
		return errorMsg;
	}
	public void setPassword2(String s) {
		password2 = s;
	}
	public String getPassword2() {
		return ((Visit)getVisit()).getAccount().getPassword();
	}
}
