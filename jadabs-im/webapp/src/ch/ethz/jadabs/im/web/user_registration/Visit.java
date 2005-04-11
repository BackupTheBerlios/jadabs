/*
 * Created on Dec 5, 2004
 */
package ch.ethz.jadabs.im.web.user_registration;

import ch.ethz.jadabs.im.db.*;
import java.io.*;
import org.objectstyle.cayenne.access.*;

/**
 * @author Jean-Luc Geering
 */
public class Visit implements Serializable {

	private DataContext dataContext;
	private Account account;

	public Visit() {
		dataContext = DataContext.createDataContext();
		account = null;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public boolean isAuthenticated() {
		return (account != null);
	}
	public void logout() {
		account = null;
	}
	public Account getAccount() {
		return account;
	}
	public DataContext getDataContext() {
		return dataContext;
	}
}
