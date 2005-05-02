package ch.ethz.jadabs.sbbws.ksoap;


/**
 * Sent from client to server, contains all information of the timetable query.
 * This class is never used, it's here only for the sake of completeness.
 * 
 * @author Stefan Vogt
 */
public abstract class TimetableQuery
{
	/**  Departure station */
	public String from;
	
	/**  Destination station */
	public String to;
	
	/** Date */
	public String date;
	
	/** Time */
	public String time;
	
	/** 0 = given time is arrival time. 1 = given time is departure time */
	public int timeToggle;
	
	/** 0  = no details, return overview. x = return details for connection x (where x = {1..4}). */
	public int details;
}