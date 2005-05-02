package ch.ethz.jadabs.sbbws.ksoap;



/**
 * Sent from server to client, contains all information of the timetable overview for four connections.
 * This class is never used, it's here only for the sake of completeness.
 * 
 * @author Stefan Vogt
 */
public abstract class TimetableOverview
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
	public TimetableOverview()
	{
		from     = new String[4];
		to       = new String[4];
		timeDep  = new String[4];
		timeArr  = new String[4];
		duration = new String[4];
		changes  = new String[4];
	}
}