/*
 * Created on Jan 18, 2005
 */
package ch.ethz.jadabs.im.db;

import gov.nist.sip.proxy.presenceserver.*;

import java.util.*;

import org.objectstyle.cayenne.access.*;

public class UserMap extends HashMap {

	private DataContext dataContext;
	
	public UserMap(DataContext dataContext) {
		super();
		this.dataContext = dataContext;
	}

	public boolean containsKey(Object key) {
		testAndInsert((String)key);
		return super.containsKey(key);
	}
	public Object get(Object key) {
		testAndInsert((String)key);
		return super.get(key);
	}
	public Object put(Object key, Object value) {
		//Maybe forbidden to put in a value ?
		//return super.put(key, value);
		return null;
	}
	// Called by "cleaner" thread
	public Collection values() {
		// TODO perform some cleaning
//		return super.values();
		return new ArrayList(0);
	}
	private void testAndInsert(String key) {
		// key is sip:bob@wherever.com
		if (!super.containsKey(key)) {
			String [] arr = key.split("@", 2); // split uname and domain
			if (arr[1].equals("wlab.ethz.ch")) {
				Map parameters = new HashMap();
				parameters.put("username", arr[0].substring(4)); // remove sip:
				List objects = dataContext.performQuery("account_by_username", parameters, true);
				if (objects.size() == 1) {
					Account a = (Account)(objects.get(0));
					// TODO expires and contact from not never used ???
					Notifier n = new Notifier(key, Integer.MAX_VALUE, null);
					super.put(key, n);
				}
			}			
		}		
	}
}
