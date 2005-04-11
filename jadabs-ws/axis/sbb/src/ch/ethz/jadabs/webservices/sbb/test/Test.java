package ch.ethz.jadabs.webservices.sbb.test;

import ch.ethz.jadabs.webservices.sbb.SBBWebService;
import ch.ethz.jadabs.webservices.sbb.serializable.*;
import ch.ethz.jadabs.webservices.sbb.util.ExceptionHandler;

import org.ksoap.SoapObject;
import org.ksoap.transport.HttpTransportSE;
import org.ksoap.ClassMap;
import java.net.*;



public class Test
{
	public static void main(String args[])
	{
		try
		{
			TimetableQuery_kSOAP query = new TimetableQuery_kSOAP();
			
			SoapObject soapObject = new SoapObject("SBBWebService","queryTimetable");
			soapObject.addProperty("TimetableQuery", query);
			
			HttpTransportSE transport = new HttpTransportSE();
			transport.setClassMap(createClassMap());
			transport.setUrl("http://wlab.ethz.ch:8080/axis/services/SBBWebService");
		//	transport.setUrl("http://localhost:8081/axis/services/SBBWebService");
		//	transport.setUrl("http://localhost:8888/axis/services/SBBWebService");
			
			// Query 01
			System.out.println("Query 01: Should return an oberview\n");
			query.from       = URLEncoder.encode(new String("Niederweningen"), "UTF-8");
			query.to         = URLEncoder.encode(new String("Zürich")        , "UTF-8");
			query.date       = URLEncoder.encode(new String("10.11.2005")    , "UTF-8");
			query.time       = URLEncoder.encode(new String("10:00")         , "UTF-8");
			query.timeToggle = 1;
			query.details    = 0;
			processResponse(transport.call(soapObject));
			System.out.println("\n");
			
			// Query 02
			System.out.println("Query 02: Should return details\n");
			query.from       = URLEncoder.encode(new String("Niederweningen"), "UTF-8");
			query.to         = URLEncoder.encode(new String("Burgdorf")      , "UTF-8");
			query.date       = URLEncoder.encode(new String("10.11.2005")    , "UTF-8");
			query.time       = URLEncoder.encode(new String("10:00")         , "UTF-8");
			query.timeToggle = 1;
			query.details    = 2;
			processResponse(transport.call(soapObject));
			System.out.println("\n");
			
			// Query 03
			System.out.println("Query 03: Should return an inquiry\n");
			query.from       = URLEncoder.encode(new String("Niederweningen"), "UTF-8");
			query.to         = URLEncoder.encode(new String("Buchs")         , "UTF-8");
			query.date       = URLEncoder.encode(new String("10.11.2005")    , "UTF-8");
			query.time       = URLEncoder.encode(new String("10:00")         , "UTF-8");
			query.timeToggle = 1;
			query.details    = 0;
			processResponse(transport.call(soapObject));
			System.out.println("\n");
			
		}
		catch(Exception exception)
		{
			new ExceptionHandler(new SBBWebService(), exception);
		}
	}
	
	
	
	public static ClassMap createClassMap()
	{
		ClassMap classMap = new ClassMap();
		
		classMap.addMapping("http://sbb.webservices.jadabs.ethz.ch", "TimetableQuery"   , new TimetableQuery_kSOAP().getClass());
		classMap.addMapping("http://sbb.webservices.jadabs.ethz.ch", "TimetableInquiry" , new TimetableInquiry_kSOAP().getClass());
		classMap.addMapping("http://sbb.webservices.jadabs.ethz.ch", "TimetableOverview", new TimetableOverview_kSOAP().getClass());
		classMap.addMapping("http://sbb.webservices.jadabs.ethz.ch", "TimetableDetails" , new TimetableDetails_kSOAP().getClass());
		
		return classMap;
	}
	
	
	
	public static void processResponse(Object response)
	{
		try
		{
			if(response instanceof TimetableInquiry_kSOAP)
			{
				TimetableInquiry_kSOAP inquiry = ((TimetableInquiry_kSOAP)response);
				
				if(inquiry.from.size() == 1)
				{
					System.out.println("From station is unambiguous.");
				}
				else
				{
					System.out.println("From station is ambiguous:");
					System.out.println(inquiry.from);
				}
				
				if(inquiry.to.size() == 1)
				{
					System.out.println("To station is unambiguous.");
				}
				else
				{
					System.out.println("To station is ambiguous:");
					System.out.println(inquiry.to);
				}
			}
			else
			if(response instanceof TimetableOverview_kSOAP)
			{
				TimetableOverview_kSOAP overview = ((TimetableOverview_kSOAP)response);
				
				System.out.println(overview.from);
				System.out.println(overview.to);
				System.out.println(overview.timeDep);
				System.out.println(overview.timeArr);
				System.out.println(overview.duration);
				System.out.println(overview.changes);
			}
			else
			if(response instanceof TimetableDetails_kSOAP)
			{
				TimetableDetails_kSOAP details = ((TimetableDetails_kSOAP)response);
				
				System.out.println("Number of changes: " + details.changes);
				
				System.out.println("From       : " + details.from);
				System.out.println("To         : " + details.to);
				System.out.println("Departure  : " + details.timeDep);
				System.out.println("Arrival    : " + details.timeArr);
				System.out.println("Platform   : " + details.platform);
				System.out.println("Travel with: " + details.travelWith);
				System.out.println("Comments   : " + details.comments);
				System.out.println();
				
				System.out.println("Duration: " + details.duration);
			}
		}
		catch(Exception exception)
		{
			new ExceptionHandler(new SBBWebService(), exception);
		}
	}
}