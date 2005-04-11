package ch.ethz.jadabs.sbbmidlet.ksoap;

import java.util.Vector;

import org.kobjects.serialization.KvmSerializable;
import org.kobjects.serialization.PropertyInfo;



/**
 * Sent from server to client, contains the detail information of a connection.
 * kSOAP version for the client side.
 * 
 * @author Stefan Vogt
 */
public class TimetableDetails_kSOAP implements KvmSerializable
{
        /**  Departure stations */
	public Vector from;
	
	/**  Arrival stations */
	public Vector to;
	
	/**  Times of departure */
	public Vector timeDep;
	
	/**  Times of arrival */
	public Vector timeArr;
	
	/**  Platforms */
	public Vector platform;
	
	/**  Travel with train, bus, ... */
	public Vector travelWith;
	
	/**  Comments */
	public Vector comments;
	
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
	
	
	
	/*
	 *  Fields for serialization
	 */
	private static int PROP_COUNT = 14;
	
	private static PropertyInfo PI_from                = new PropertyInfo("from"               , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_to                  = new PropertyInfo("to"                 , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_timeDep             = new PropertyInfo("timeDep"            , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_timeArr             = new PropertyInfo("timeArr"            , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_platform            = new PropertyInfo("platform"           , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_travelWith          = new PropertyInfo("travelWith"         , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_comments            = new PropertyInfo("comments"           , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_duration            = new PropertyInfo("duration"           , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_changes             = new PropertyInfo("changes"            , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedFrom       = new PropertyInfo("requestedFrom"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedTo         = new PropertyInfo("requestedTo"        , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedDate       = new PropertyInfo("requestedDate"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedTime       = new PropertyInfo("requestedTime"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedTimeToggle = new PropertyInfo("requestedTimeToggle", PropertyInfo.INTEGER_CLASS);
	
	private static PropertyInfo[] PI_PROP_ARRAY =
	{
		PI_from,
		PI_to,
		PI_timeDep,
		PI_timeArr,
		PI_platform,
		PI_travelWith,
		PI_comments,
		PI_duration,
		PI_changes,
		PI_requestedFrom,
		PI_requestedTo,
		PI_requestedDate,
		PI_requestedTime,
		PI_requestedTimeToggle
	};
	
	
	
	/**
	 *  Returns the property at a specified index (for serialization)
	 */
	public Object getProperty(int param)
	{
		Object object = null;
		
		switch(param)
		{
			case  0 : object = from;                             break;
			case  1	: object = to;                               break;
			case  2	: object = timeDep;                          break;
			case  3	: object = timeArr;                          break;
			case  4	: object = platform;                         break;
			case  5	: object = travelWith;                       break;
			case  6	: object = comments;                         break;
			case  7	: object = duration;                         break;
			case  8	: object = new Integer(changes);             break;
			case  9	: object = requestedFrom;                    break;
			case 10	: object = requestedTo;                      break;
			case 11	: object = requestedDate;                    break;
			case 12	: object = requestedTime;                    break;
			case 13	: object = new Integer(requestedTimeToggle); break;
			
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
			case  0	: from                = (Vector)obj;               break;
			case  1	: to                  = (Vector)obj;               break;
			case  2	: timeDep             = (Vector)obj;               break;
			case  3	: timeArr             = (Vector)obj;               break;
			case  4	: platform            = (Vector)obj;               break;
			case  5	: travelWith          = (Vector)obj;               break;
			case  6	: comments            = (Vector)obj;               break;
			case  7	: duration            = (String)obj;               break;
			case  8	: changes             = ((Integer)obj).intValue(); break;
			case  9	: requestedFrom       = (String)obj;               break;
			case 10	: requestedTo         = (String)obj;               break;
			case 11	: requestedDate       = (String)obj;               break;
			case 12	: requestedTime       = (String)obj;               break;
			case 13	: requestedTimeToggle = ((Integer)obj).intValue(); break;
			
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