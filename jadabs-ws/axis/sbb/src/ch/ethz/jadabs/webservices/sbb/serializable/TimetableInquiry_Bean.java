package ch.ethz.jadabs.webservices.sbb.serializable;



/**
 * Sent from server to client if at least one of the two stations are ambiguous. Contains a selection of possible matches.
 * JavaBean version for the server side.
 * 
 * @author Stefan Vogt
 */
public class TimetableInquiry_Bean
{
	/** Ambiguous stations in the from field. Length is 1 if unambiguous. */
	public String[] from;
	
	/** Ambiguous stations in the to field. Length is 1 if unambiguous. */
	public String[] to;

	/*  
	 *  The following values are the ones submitted by the user. They are returned again,
	 *  so that the mobile device doesn't need to maintain a state.
	 */
	public String requestedFrom;
	public String requestedTo;
	public String requestedDate;
	public String requestedTime;
	public int    requestedTimeToggle;
	
	
		
	/*
	 *  Setters for the Java Bean serializer
	 */
	public void setFrom               (String[] stringArray) { from                = stringArray; }
	public void setTo                 (String[] stringArray) { to                  = stringArray; }
	public void setRequestedFrom      (String   string     ) { requestedFrom       = string;      }
	public void setRequestedTo        (String   string     ) { requestedTo         = string;      }
	public void setRequestedDate      (String   string     ) { requestedDate       = string;      }
	public void setRequestedTime      (String   string     ) { requestedTime       = string;      }
	public void setRequestedTimeToggle(int      integer    ) { requestedTimeToggle = integer;     }
	
		
	
	/*
	 *  Getters for the Java Bean serializer
	 */
	public String[] getFrom               () { return from;                }
	public String[] getTo                 () { return to;                  }
	public String   getRequestedFrom      () { return requestedFrom;       }
	public String   getRequestedTo        () { return requestedTo;         }
	public String   getRequestedDate      () { return requestedDate;       }
	public String   getRequestedTime      () { return requestedTime;       }
	public int      getRequestedTimeToggle() { return requestedTimeToggle; }
}