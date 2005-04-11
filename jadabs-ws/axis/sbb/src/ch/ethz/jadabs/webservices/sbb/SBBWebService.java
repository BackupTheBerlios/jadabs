package ch.ethz.jadabs.webservices.sbb;

import java.io.*;
import java.net.*;
import java.util.Vector;
import ch.ethz.jadabs.webservices.sbb.html.HTMLFormSimulator;
import ch.ethz.jadabs.webservices.sbb.html.HTMLParser;
import ch.ethz.jadabs.webservices.sbb.serializable.*;
import ch.ethz.jadabs.webservices.sbb.util.*;



/**
 * The main class of the SBB Webservice.
 * 
 * @author Stefan Vogt
 */
public class SBBWebService
{
        /** Necessary because the classes will be located in the axis directory but the configuration files and logs not. Temporary solution. */
	public static String installDir;
	
	/** The number of the current request since the SBBWebService was started */
	private static int requestNumber;
	
	/** The number of the current request since the SBBWebService was started. This is per-instance copy due to concurrency reasons */
	private int localRequestNumber;
	
	private File temporaryHTMLFile;
	private File temporaryTextFile;
	public static LogFile logFile;
	
	
	/**
	 * Sets the installDir variable.
	 * Temporary solution.
	 */
	static
	{
		if(System.getProperty("os.name").equals("Windows XP"))
			installDir = "E:\\Stefan\\My Documents\\Education\\Lab\\jadabs-ws\\axis\\sbb";
		else
			installDir = "/home/jadabsws/tomcat/sbb";
		
		if(! new File(installDir).exists())
		{
			System.out.println("Installation directory of SBB Webservice not found. Please adapt \"SBBWebService.java\".");
			System.exit(0);
		}
		
		requestNumber = 1;
		
		try
		{			
			logFile = new LogFile(SBBWebService.installDir + "/logs/SBBWebService.txt");
			
			logFile.addLine("SBBWebService was started, current directory is " + new File(".").getCanonicalPath());
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(exception);
		}
	}
	
	public SBBWebService()
	{	
		localRequestNumber = getLocalRequestNumber();
		
		try
		{
			temporaryHTMLFile = new File(installDir + "/temp/query" + localRequestNumber + ".html");
			temporaryTextFile = new File(installDir + "/temp/query" + localRequestNumber + ".txt");
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	
	
	/**
	 * Queries the SBB timetable.v
	 * 
	 * @param
	 * 	argument Must be of type {@link ch.ethz.jadabs.webservices.sbb.serializable.TimetableQuery_Bean}.
	 * 
	 * @return
	 * 	The return object is of type
	 *	{@link ch.ethz.jadabs.webservices.sbb.serializable.TimetableQuery_Bean},
	 *	{@link ch.ethz.jadabs.webservices.sbb.serializable.TimetableOverview_Bean},
	 *	{@link ch.ethz.jadabs.webservices.sbb.serializable.TimetableDetails_Bean}
	 *	or <code>null</code> if an error occurred.
	 * 
	 */
	public Object queryTimetable(Object argument)
	{
		Object response = null;
				
		if(argument instanceof TimetableQuery_Bean)
	    	{
	    		TimetableQuery_Bean query = (TimetableQuery_Bean)argument;

			try
			{
				String from = URLDecoder.decode(query.from, "UTF-8");
				String to   = URLDecoder.decode(query.to  , "UTF-8");
				String date = URLDecoder.decode(query.date, "UTF-8");
				String time = URLDecoder.decode(query.time, "UTF-8");
				int timeToggle = query.timeToggle;
				int details    = query.details;
					
				logFile.addLine("Request " + localRequestNumber + ": SBBWebService received the query " + from + ", " + to + ", " + date  + ", " + time + ", " + timeToggle + ", " + details);
	     			
				HTMLFormSimulator formSimulator;
				formSimulator = HTMLFormSimulator.load(SBBWebService.installDir + "/settings/HTMLFormSimulator.xml");			
				formSimulator.createQuery(from, to, date, time, timeToggle);
				formSimulator.initialize(localRequestNumber);
				formSimulator.submit();
				formSimulator.receive(temporaryHTMLFile);
				
				HTMLParser htmlParser;
				htmlParser = new HTMLParser();
				htmlParser.initialize(localRequestNumber);
				htmlParser.parseFile(temporaryHTMLFile, temporaryTextFile);
				
				if(details == 0)
				{
					if(stationsAreAmbiguous())
						response = getAmbiguousStations(query);	// Get the ambiguous stations
					else
						response = getOverview(query);		// Get the connection overview
				}
				else
				{
					response = getDetails(query);			// Get the details for a given connection
				}
			}
			catch(Exception exception)
			{
			    	new ExceptionHandler(this, exception);
			}
		}
	        else
	        {
	                new ExceptionHandler(this, new Exception("Unexpected object received, " + argument.getClass().getName()));
		}
		
		temporaryHTMLFile.delete();
		temporaryTextFile.delete();
		
		try
		{
			if(response != null)
				logFile.addLine("Request " + localRequestNumber + ": SBBWebService successfully processed the query");
			else
				logFile.addLine("Request " + localRequestNumber + ": SBBWebService failed to process the query");
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}

		return response;
	}
	
	
	private synchronized int getLocalRequestNumber()
	{
		return requestNumber++;
	}
	
	
	
	/**
	 * Checks whether there are ambiguous stations in the query.
	 * 
	 * @return
	 * 	<code>true</code> if at least one statin is ambiguous.
	 * 	<code>false</code> both stations are unambiguous
	 */
	private boolean stationsAreAmbiguous()
	{
		boolean ambiguous = false;
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(temporaryTextFile));
			
			while(reader.ready())
			{
				if(reader.readLine().trim().equals("Use the list below to refine your request."))
				{
					ambiguous = true;
					break;
				}					
			}
				
			reader.close();
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
		
		return ambiguous;	
	}
	
	
	/**
	 * @return
	 * 	@link ch.ethz.jadabs.webservices.sbb.serializable.TimetableInquiry_Bean
	 */
	private TimetableInquiry_Bean getAmbiguousStations(TimetableQuery_Bean query)
	{
		TimetableInquiry_Bean inquiry = new TimetableInquiry_Bean();
		
		Vector from = new Vector();
		Vector to   = new Vector();
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(temporaryTextFile));
			String line;

			while(reader.ready())
			{
				line = reader.readLine().trim();
				
				if(line.equals("From:"))
				{
					// Get all the possibilities
					while(! (line = reader.readLine().trim()).equals("Station/Stop"))
						from.add(line);
					
					// Store the possibilities in the inquiry object
					inquiry.from = new String[from.size()];
					for(int i=0; i<from.size(); i++)
						inquiry.from[i] = (String)from.get(i);
				}
				
				if(line.equals("To:"))
				{
					// Get all the possibilities
					while(! (line = reader.readLine().trim()).equals("Station/Stop"))
						to.add(line);
					
					// Store the possibilities in the inquiry object					
					inquiry.to = new String[to.size()];
					for(int i=0; i<to.size(); i++)
						inquiry.to[i] = (String)to.get(i);
				}
			}
				
			reader.close();
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
		
		// Fill in the values submitted from the user, so that the mobile device doesn't need to maintain a state
		inquiry.requestedFrom       = query.from;
		inquiry.requestedTo         = query.to;
		inquiry.requestedDate       = query.date;
		inquiry.requestedTime       = query.time;
		inquiry.requestedTimeToggle = query.timeToggle;
		
		return inquiry;
	}


	/**
	 * @return
	 * 	@link ch.ethz.jadabs.webservices.sbb.serializable.TimetableOverview_Bean
	 */
	private TimetableOverview_Bean getOverview(TimetableQuery_Bean query)
	{
		TimetableOverview_Bean overview = new TimetableOverview_Bean();
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(temporaryTextFile));
			String line;
			int lineNumber = 0;
			
			while(reader.ready())
			{
				line = reader.readLine();	
				lineNumber++;
				
				for(int i=0; i<4; i++)
				{
					if(lineNumber == (10 + i*12)) overview.from    [i] = line; else
					if(lineNumber == (17 + i*12)) overview.to      [i] = line; else 
					if(lineNumber == (13 + i*12)) overview.timeDep [i] = line.replaceAll("[^0-9:]", ""); else
					if(lineNumber == (20 + i*12)) overview.timeArr [i] = line.replaceAll("[^0-9:]", ""); else
					if(lineNumber == (14 + i*12)) overview.duration[i] = line.replaceAll("[^0-9:]", ""); else
					if(lineNumber == (15 + i*12)) overview.changes [i] = line.replaceAll("[^0-9]" , "");
				}			
			}
				
			reader.close();
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
		
		// Fill in the values submitted from the user, so that the mobile device doesn't need to maintain a state
		overview.requestedFrom       = query.from;
		overview.requestedTo         = query.to;
		overview.requestedDate       = query.date;
		overview.requestedTime       = query.time;
		overview.requestedTimeToggle = query.timeToggle;
		
		return overview;
	}
	
	
	/**
	 * @return
	 * 	@link ch.ethz.jadabs.webservices.sbb.serializable.TimetableDetails_Bean
	 */
	private TimetableDetails_Bean getDetails(TimetableQuery_Bean query)
	{
		TimetableDetails_Bean details = new TimetableDetails_Bean();
		
		Vector from       = new Vector();
		Vector to         = new Vector();
		Vector timeDep    = new Vector();
		Vector timeArr    = new Vector();
		Vector platform   = new Vector();
		Vector travelWith = new Vector();
		Vector comments   = new Vector();

		try
		{
			BufferedReader reader;
			String line;
			String comment = "";
			int lineNumber = 0;
			
			// Get number of changes and the duration
			reader = new BufferedReader(new FileReader(temporaryTextFile));
			while(reader.ready())
			{
				line = reader.readLine();	
				lineNumber++;
				
				if(lineNumber == (14 + (query.details-1)*12)) details.duration = line.replaceAll("[^0-9:]", "");
				if(lineNumber == (15 + (query.details-1)*12)) details.changes  = new Integer(line.replaceAll("[^0-9]" , "")).intValue();			
			}
			reader.close();
			
			// Get the details
			reader = new BufferedReader(new FileReader(temporaryTextFile));
			while(reader.ready())
			{
				line = reader.readLine();
				
				if(line.equals("Details - Connection " + query.details))
				{
					while(! (line = reader.readLine().trim()).equals("Comments"))
						;
					reader.readLine();
				
					while(true)
					{
						from.add(reader.readLine());
						reader.readLine();
						reader.readLine();
						timeArr.add(reader.readLine());
						platform.add(reader.readLine());
						travelWith.add(reader.readLine());
						while(! (line = reader.readLine()).equals("-"))
							comment = comment + line;
						comments.add(comment);
						comment = "";
						to.add(reader.readLine());
						reader.readLine();
						reader.readLine();
						timeDep.add(reader.readLine());
						platform.add(reader.readLine());
						
						if(reader.readLine().startsWith("Duration"))
							break;
					}
				}
			}
			
			details.from       = new String[from.size()];
			details.to         = new String[to.size()];
			details.timeDep    = new String[timeDep.size()];
			details.timeArr    = new String[timeArr.size()];
			details.platform   = new String[platform.size()];
			details.travelWith = new String[travelWith.size()];
			details.comments   = new String[comments.size()];

			for(int i=0; i<from.size()      ; i++) details.from      [i] = (String)from.get(i);
			for(int i=0; i<to.size()        ; i++) details.to        [i] = (String)to.get(i);
			for(int i=0; i<timeDep.size()   ; i++) details.timeDep   [i] = (String)timeArr.get(i);
			for(int i=0; i<timeArr.size()   ; i++) details.timeArr   [i] = (String)timeDep.get(i);
			for(int i=0; i<platform.size()  ; i++) details.platform  [i] = (String)platform.get(i);
			for(int i=0; i<travelWith.size(); i++) details.travelWith[i] = (String)travelWith.get(i);
			for(int i=0; i<comments.size()  ; i++) details.comments  [i] = (String)comments.get(i);
				
			reader.close();
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
		
		// Fill in the values submitted from the user, so that the mobile device doesn't need to maintain a state
		details.requestedFrom       = query.from;
		details.requestedTo         = query.to;
		details.requestedDate       = query.date;
		details.requestedTime       = query.time;
		details.requestedTimeToggle = query.timeToggle;
		
		return details;
	}



	/**
	 *  Only for local debugging.
	 */
	public static void main(String[] args)
	{	
		try
		{			
			TimetableQuery_Bean query = new TimetableQuery_Bean();
		//	query.from       = URLEncoder.encode(new String("Buchs"), "UTF-8");
			query.from       = URLEncoder.encode(new String("Zürich"), "UTF-8");
			query.to         = URLEncoder.encode(new String("Burgdorf"), "UTF-8");
			query.date       = URLEncoder.encode(new String("10.11.2005")  , "UTF-8");
			query.time       = URLEncoder.encode(new String("10"), "UTF-8");
			query.timeToggle = 1;
			query.details    = 2;
			
			SBBWebService sbbWS = new SBBWebService();
			Object response = sbbWS.queryTimetable(query);
			
			if(response instanceof TimetableInquiry_Bean)
			{
				TimetableInquiry_Bean inquiry = ((TimetableInquiry_Bean)response);
				
				if(inquiry.from.length == 1)
					System.out.println("From station is unambiguous.");
				else
				{
					System.out.println("From station is ambiguous:");
					for(int i=0; i<inquiry.from.length; i++)
						System.out.println(inquiry.from[i]);
				}
				
				if(inquiry.to.length == 1)
					System.out.println("To station is unambiguous.");
				else
				{
					System.out.println("To station is ambiguous:");
					for(int i=0; i<inquiry.to.length; i++)
						System.out.println(inquiry.to[i]);
				}
			}
			else
			if(response instanceof TimetableOverview_Bean)
			{
				TimetableOverview_Bean overview = ((TimetableOverview_Bean)response);
				
				for(int i=0; i<4; i++)
				{
					System.out.println(overview.from[i]);
					System.out.println(overview.to[i]);
					System.out.println(overview.timeDep[i]);
					System.out.println(overview.timeArr[i]);
					System.out.println(overview.duration[i]);
					System.out.println(overview.changes[i]);
				}
			}
			else
			if(response instanceof TimetableDetails_Bean)
			{
				TimetableDetails_Bean details = ((TimetableDetails_Bean)response);
				
				System.out.println("Number of changes: " + details.changes);
								
				//for(int i=0; i<details.changes+2; i++)
				for(int i=0; i<details.from.length; i++)
				{
					System.out.println("From       : " + details.from[i]);
					System.out.println("To         : " + details.to[i]);
					System.out.println("Departure  : " + details.timeDep[i]);
					System.out.println("Arrival    : " + details.timeArr[i]);
					System.out.println("Platform   : " + details.platform[i]);
					System.out.println("Travel with: " + details.travelWith[i]);
					System.out.println("Comments   : " + details.comments[i]);
					System.out.println();
				}
				
				System.out.println("Duration: " + details.duration);
			}
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(new SBBWebService(), exception);
		}
	}
}