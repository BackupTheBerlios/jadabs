/*
 * Created on Dec 13, 2004
 */
package ch.ethz.jadabs.im.web.user_registration;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import org.apache.tapestry.*;
import org.apache.tapestry.form.*;
import org.apache.tapestry.html.*;
import org.apache.tapestry.link.*;

import ch.ethz.jadabs.im.db.NewAccount;

/**
 * @author Jean-Luc Geering
 */
public class NewAccountPage extends BasePage {
	
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
				error = true;
				errorMsg = "There is already an account attached to this email address !";
			}
			else {
				objects = v.getDataContext().performQuery("new_account_by_email", parameters, true);
				NewAccount a;
				if (objects.size() == 1) {
					a = (NewAccount)objects.get(0);
				}
				else {
					a = (NewAccount)v.getDataContext().createAndRegisterNewObject(NewAccount.class);
					a.setEmail(user+"@"+domain);
					a.setPassword(PasswordGenerator.newPassword(25));
				}
				DirectLink dl = (DirectLink)cycle.getPage("Home").getComponent("validationLink");	
				String link = dl.getLink(cycle).getAbsoluteURL(null, null, 0, null, false);
				link = link.substring(0, link.indexOf(";jsessionid")) + "?service=direct/0/Home/validationLink&sp=S"+a.getEmail()+"&sp=S"+a.getPassword();
				sendMail(a.getEmail(),"To validate your account creation go to\n\n"+link+"\n\nand fill out the form.");
				v.getDataContext().commitChanges();
				Confirmation page = (Confirmation)cycle.getPage("Confirmation");
				page.setMessage("A validation request has been sent to "+a.getEmail());
				page.setNext("Home");
				cycle.activate(page);	
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
