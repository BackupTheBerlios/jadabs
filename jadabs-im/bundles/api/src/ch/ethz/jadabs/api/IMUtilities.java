package ch.ethz.jadabs.api;
/** Utilities for the IM client.
*
*@version  JAIN-SIP-1.1
*
*@author Olvier Deruelle <deruelle@nist.gov>  <br/>
*@author M. Ranganathan <mranga@nist.gov> <br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class IMUtilities {
 
    public static String getUsernameFromURI(String URI) {
        return URI.substring(4);
    }
    
	public static int getIntStatus(String status) {
	    if (status.equalsIgnoreCase("offline")) {
	        return MessageCons.IM_STATUS_OFFLINE;
	    }
	    else if (status.equalsIgnoreCase("online")) {
	        return MessageCons.IM_STATUS_ONLINE;
	    }
	    else if (status.equalsIgnoreCase("busy")) {
	        return MessageCons.IM_STATUS_BUSY;
	    }
	    else return 0;
	}
	
	public static String getStringStatus(int status) {
		if (status == MessageCons.IM_STATUS_BUSY) {
			return "busy";
		}
		else if (status == MessageCons.IM_STATUS_OFFLINE) {
			return "offline";
		}
		else if (status == MessageCons.IM_STATUS_ONLINE) {
			return "online";
		}
		else return "";
	}
}
