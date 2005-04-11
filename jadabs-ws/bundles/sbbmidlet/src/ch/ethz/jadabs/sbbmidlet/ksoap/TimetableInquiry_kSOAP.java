package ch.ethz.jadabs.sbbmidlet.ksoap;

import java.util.Vector;
import org.kobjects.serialization.KvmSerializable;
import org.kobjects.serialization.PropertyInfo;



/**
 * Sent from server to client if at least one of the two stations are ambiguous. Contains a selection of possible matches.
 * kSOAP version for the client side.
 * 
 * @author Stefan Vogt
 */
public class TimetableInquiry_kSOAP implements KvmSerializable
{
	/** Ambiguous stations in the from field */
	public Vector from;
	
	/** Ambiguous stations in the to field */
	public Vector to;

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
	private static int PROP_COUNT = 7;
	
	private static PropertyInfo PI_from                = new PropertyInfo("from"               , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_to                  = new PropertyInfo("to"                 , PropertyInfo.VECTOR_CLASS);
	private static PropertyInfo PI_requestedFrom       = new PropertyInfo("requestedFrom"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedTo         = new PropertyInfo("requestedTo"        , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedDate       = new PropertyInfo("requestedDate"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedTime       = new PropertyInfo("requestedTime"      , PropertyInfo.STRING_CLASS);
	private static PropertyInfo PI_requestedTimeToggle = new PropertyInfo("requestedTimeToggle", PropertyInfo.INTEGER_CLASS);
	
	private static PropertyInfo[] PI_PROP_ARRAY =
	{
			PI_from,
			PI_to,
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
			case 0 : object = from;                             break;
			case 1 : object = to;                               break;
			case 2 : object = requestedFrom;                    break;
			case 3 : object = requestedTo;                      break;
			case 4 : object = requestedDate;                    break;
			case 5 : object = requestedTime;                    break;
			case 6 : object = new Integer(requestedTimeToggle); break;
		
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
			case 0 : from                = (Vector)obj;               break;
			case 1 : to                  = (Vector)obj;               break;
			case 2 : requestedFrom       = (String)obj;               break;
			case 3 : requestedTo         = (String)obj;               break;
			case 4 : requestedDate       = (String)obj;               break;
			case 5 : requestedTime       = (String)obj;               break;
			case 6 : requestedTimeToggle = ((Integer)obj).intValue(); break;
			
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