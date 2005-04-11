/*
 * Created on Jan 18, 2005
 */
package ch.ethz.jadabs.im.db;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectstyle.cayenne.access.DataContext;
import org.objectstyle.cayenne.conf.Configuration;
import org.objectstyle.cayenne.conf.FileConfiguration;

public class UserDB {

	private DataContext dataContext;
	private UserMap userMap;

	public UserDB(String cayenneConfigFile) {
		//TODO Check if conf file exists !
		FileConfiguration conf = new FileConfiguration(new File(cayenneConfigFile));
		Configuration.initializeSharedConfiguration(conf);
		dataContext = DataContext.createDataContext();
		userMap = new UserMap(dataContext);
	}
	public String getPassword(String username) {
		Map parameters = new HashMap();
		parameters.put("username", username);
		List objects = dataContext.performQuery("account_by_username", parameters, true);
		String password = null;
		if (objects.size() == 1) {
			password = ((Account)objects.get(0)).getPassword();
		}
		return password;
	}
	public HashMap getUserMap() {
		return userMap;
	}
}
