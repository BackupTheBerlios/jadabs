package ch.ethz.jadabs.webservices.sbb.serializable;



/**
 * Sent from client to server, contains all information of the timetable query.
 * JavaBean version for the server side.
 * 
 * @author Stefan Vogt
 */
public class TimetableQuery_Bean
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


	
	/*
	 *  Constructor
	 */
	public TimetableQuery_Bean()
	{
	}
	
	
		
	/**
	 *  Setters for the Java Bean serializer
	 */
	public void setFrom(String string)
	{
		from = string;
	}
	
	public void setTo(String string)
	{
		to = string;
	}
	
	public void setDate(String string)
	{
		date = string;
	}
	
	public void setTime(String string)
	{
		time = string;
	}
	
	public void setTimeToggle(int integer)
	{
		timeToggle = integer;
	}
	
	public void setDetails(int integer)
	{
		details = integer;
	}
	
		
	
	/**
	 *  Getters for the Java Bean serializer
	 */
	public String getFrom()
	{
		return from;
	}
	
	public String getTo()
	{
		return to;
	}
	
	public String getDate()
	{
		return date;
	}
	
	public String getTime()
	{
		return time;
	}
	
	public int getTimeToggle()
	{
		return timeToggle;
	}
	
	public int getDetails()
	{
		return details;
	}
}