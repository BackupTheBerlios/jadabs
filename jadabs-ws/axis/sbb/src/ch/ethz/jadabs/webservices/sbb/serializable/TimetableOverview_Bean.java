package ch.ethz.jadabs.webservices.sbb.serializable;



/**
 * Sent from server to client, contains all information of the timetable overview for four connections.
 * JavaBean version for the server side.
 * 
 * @author Stefan Vogt
 */
public class TimetableOverview_Bean
{	
        /**  Departure stations */
	public String[] from;
	
	/**  Arrival stations */
	public String[] to;
	
	/**  Times of departure */
	public String[] timeDep;
	
	/**  Times of arrival */
	public String[] timeArr;
	
	/**  Durations */
	public String[] duration;
	
	/**  Numbers of necessary changes */
	public String[] changes;

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
	 *  Constructor
	 */
	public TimetableOverview_Bean()
	{
		from     = new String[4];
		to       = new String[4];
		timeDep  = new String[4];
		timeArr  = new String[4];
		duration = new String[4];
		changes  = new String[4];
	}
	
	
		
	/*
	 *  Setters for the Java Bean serializer
	 */
	public void setFrom               (String[] stringArray) { from                = stringArray; }
	public void setTo                 (String[] stringArray) { to                  = stringArray; }
	public void setTimeDep            (String[] stringArray) { timeDep             = stringArray; }
	public void setTimeArr            (String[] stringArray) { timeArr             = stringArray; }
	public void setDuration           (String[] stringArray) { duration            = stringArray; }
	public void setChanges            (String[] stringArray) { changes             = stringArray; }
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
	public String[] getTimeDep            () { return timeDep;             }
	public String[] getTimeArr            () { return timeArr;             }
	public String[] getDuration           () { return duration;            }
	public String[] getChanges            () { return changes;             }
	public String   getRequestedFrom      () { return requestedFrom;       }
	public String   getRequestedTo        () { return requestedTo;         }
	public String   getRequestedDate      () { return requestedDate;       }
	public String   getRequestedTime      () { return requestedTime;       }
	public int      getRequestedTimeToggle() { return requestedTimeToggle; }
}