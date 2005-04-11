package ch.ethz.jadabs.sbbmidlet.ksoap;

import org.kobjects.serialization.KvmSerializable;
import org.kobjects.serialization.PropertyInfo;


/**
 * Sent from client to server, contains all information of the timetable query.
 * kSOAP version for the client side.
 * 
 * @author Stefan Vogt
 */
public class TimetableQuery_kSOAP implements KvmSerializable
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
	 *  Fields for serialization
	 */
	private static int PROP_COUNT = 6;
	private static PropertyInfo PI_from       = new PropertyInfo("from"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_to         = new PropertyInfo("to"        , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_date       = new PropertyInfo("date"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_time       = new PropertyInfo("time"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_timeToggle = new PropertyInfo("timeToggle", PropertyInfo.INTEGER_CLASS);
	private static PropertyInfo PI_details    = new PropertyInfo("details"   , PropertyInfo.INTEGER_CLASS);
	private static PropertyInfo[] PI_PROP_ARRAY = {PI_from, PI_to, PI_date, PI_time, PI_timeToggle, PI_details};
	
	
	
	/**
	 *  Returns the property at a specified index (for serialization)
	 */
	public Object getProperty(int param)
	{
		Object object = null;
		
		switch(param)
		{
			case 0 : object = new String(from);        break;
			case 1 : object = new String(to);          break;
			case 2 : object = new String(date);        break;
			case 3 : object = new String(time);        break;
			case 4 : object = new Integer(timeToggle); break;
			case 5 : object = new Integer(details);    break;
			
			default: System.out.println("Unexpected parameter number: " + param);
		}
		
		return object;
	}	
	
	
	
	/**
	 *  Sets the property with the given index to the given value.	
	 */
	public void setProperty(int param, Object obj)
	{
		switch(param)
		{
			case 0	: from       = (String)obj;               break;
			case 1	: to         = (String)obj;               break;
			case 2	: date       = (String)obj;               break;
			case 3	: time       = (String)obj;               break;
			case 4	: timeToggle = ((Integer)obj).intValue(); break;
			case 5	: details    = ((Integer)obj).intValue(); break;
			
			default	: System.out.println("Unexpected parameter number: " + param);
		}
	}	
	
	
	
	/**
	 *  Returns the number of serializable properties
	 */
	public int getPropertyCount()
	{
		return PI_PROP_ARRAY.length;
	}	
	
	
	
	/**
	 *  Fills the given property info record
	 */
	public void getPropertyInfo(int param, PropertyInfo propertyInfo)
	{
		propertyInfo.name = PI_PROP_ARRAY[param].name;
		propertyInfo.nonpermanent = PI_PROP_ARRAY[param].nonpermanent;
		propertyInfo.copy(PI_PROP_ARRAY[param]);
	}
}