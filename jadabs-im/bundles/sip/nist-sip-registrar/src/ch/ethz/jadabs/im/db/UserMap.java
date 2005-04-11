/*
 * Created on Jan 18, 2005
 */
package ch.ethz.jadabs.im.db;

import gov.nist.sip.proxy.presenceserver.Notifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectstyle.cayenne.access.DataContext;

public class UserMap extends HashMap {

	private DataContext dataContext;
	
	public UserMap(DataContext dataContext) {
		super();
		this.dataContext = dataContext;
	}

	public boolean containsKey(Object key) {
		System.out.println("UserMap.containsKey("+key+")");
		testAndInsert((String)key);
		return super.containsKey(key);
	}
	public Object get(Object key) {
		System.out.println("UserMap.get("+key+")");
		testAndInsert((String)key);
		return super.get(key);
	}
	public Object put(Object key, Object value) {
		System.out.println("UserMap.put("+key+", "+value+")");
		//Maybe forbidden to put in a value ?
		//return super.put(key, value);
		return null;
	}
	// Called by "cleaner" thread
	public Collection values() {
		// TODO perform some cleaning
		return super.values();
	}
	private void testAndInsert(String username) {
		if (!super.containsKey(username)) {
			Map parameters = new HashMap();
			parameters.put("username", username.substring(4));
			List objects = dataContext.performQuery("account_by_username", parameters, true);
			if (objects.size() == 1) {
				Account a = (Account)(objects.get(0));
				// TODO expires and contact from not never used ???
				Notifier n = new Notifier(username, Integer.MAX_VALUE, null);
				super.put(username, n);
			}
			
		}		
	}
}
