package ch.ethz.jadabs.sbbws.ksoap;



/**
 * Sent from server to client, contains the detail information of a connection.
 * This class is never used, it's here only for the sake of completeness.
 * 
 * @author Stefan Vogt
 */
public abstract class TimetableDetails
{
        /**  Departure stations */
	public String[] from;
	
	/**  Arrival stations */
	public String[] to;
	
	/**  Times of departure */
	public String[] timeDep;
	
	/**  Times of arrival */
	public String[] timeArr;
	
	/**  Platforms */
	public String[] platform;
	
	/**  Travel with train, bus, ... */
	public String[] travelWith;
	
	/**  Comments */
	public String[] comments;
	
	/**  Duration */
	public String duration;
	
	/**  Number of necessary changes */
	public int changes;
	
	/*  
	 *  The following values are the ones submitted by the user. They are returned again,
	 *  so that the mobile device doesn't need to maintain a state.
	 */
	public String requestedFrom;
	public String requestedTo;
	public String requestedDate;
	public String requestedTime;
	public int    requestedTimeToggle;
}