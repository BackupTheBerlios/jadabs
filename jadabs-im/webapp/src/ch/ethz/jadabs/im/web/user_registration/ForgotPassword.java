/*
 * Created on Dec 3, 2004
 */
package ch.ethz.jadabs.im.web.user_registration;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.apache.tapestry.*;
import org.apache.tapestry.form.*;
import org.apache.tapestry.html.*;

import ch.ethz.jadabs.im.db.Account;

/**
 * @author Jean-Luc Geering
 */
public class ForgotPassword extends BasePage {

	public static final IPropertySelectionModel DOMAINS = 
		new StringPropertySelectionModel(new String[] {
				"student.ethz.ch", "inf.ethz.ch", "ethz.ch"});
	private String user;
	private String domain;
	private boolean error = false;
	private String errorMsg = "";
	
	
	public void validate(IRequestCycle cycle) {
		super.validate(cycle);
		error = false;
	}
	public void onFormSubmit(IRequestCycle cycle) {
		Visit v = (Visit)getVisit();
		try {
			Map parameters = new HashMap();
			parameters.put("email", user+"@"+domain);
			List objects = v.getDataContext().performQuery("account_by_email", parameters, true);
			if (objects.size() == 1) {
				Account a = (Account)objects.get(0);
				sendMail(a.getSystemEmail(),"Username:\n"+a.getUsername()+"\nPassword:\n"+a.getPassword());
				Confirmation page = (Confirmation)cycle.getPage("Confirmation");
				page.setMessage("Your password has been sent to "+a.getSystemEmail());
				page.setNext("Home");
				cycle.activate(page);	
			}
			else {
				error = true;
				errorMsg = "There is no account for "+user+"@"+domain+" !";
				
			}
		}
		catch (MessagingException e) {
			v.getDataContext().rollbackChanges();
			error = true;
			errorMsg = e.getMessage();
		}
	}
	public void onCancel(IRequestCycle cycle) {
		cycle.activate("Home");
	}
	private void sendMail(String emailAddress, String messageText) throws AddressException, MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.ethz.ch");
		Session session = Session.getDefaultInstance(props);
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress("ETHZ_SIP_Manager"));
		msg.setRecipient(Message.RecipientType.TO,new InternetAddress(emailAddress));
		msg.setSubject("ETHZ SIP Account");
		msg.setText(messageText);
		msg.setSentDate(new Date());
		Transport.send(msg);
	}
	public void setUser(String s) {
		user = s;
	}
	public String getUser() {
		return user;
	}
	public void setDomain(String s) {
		domain = s;
	}
	public String getDomain() {
		return domain;
	}
	public IPropertySelectionModel getDomains() {
		return DOMAINS;
	}
	public boolean isError() {
		return error;
	}
	public String getErrorMessage() {
		return errorMsg;
	}
}
