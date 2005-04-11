package ch.ethz.jadabs.webservices.sbb.html;

import java.beans.XMLDecoder;
import java.io.*;
import java.net.*;
import ch.ethz.jadabs.webservices.sbb.SBBWebService;
import ch.ethz.jadabs.webservices.sbb.util.ExceptionHandler;



/**
 * 
 * 
 * @author Stefan Vogt
 */
public class HTMLFormSimulator
{
	private String query;
	private String queryURL;
	private String fieldNameFROM;
	private String fieldNameTO;
	private String fieldNameDATE;
	private String fieldNameTIME;
	private String fieldNameTIMETOGGLE;
	private String otherParameters;
	private URLConnection urlConnection;
	private int requestNumber;

	public HTMLFormSimulator()
	{
		this.query = "";
	}
	
	
	private void addParameter(String value)
	{
		try
		{
			if(! query.equals("")) query = query + URLEncoder.encode("&", "UTF-8");
			query = query + URLEncoder.encode(value, "UTF-8");
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	

	public void createQuery(String from, String to, String date, String time, int timeToggle)
	{
		addParameter(fieldNameFROM       + "=" + from);
		addParameter(fieldNameTO         + "=" + to  );
		addParameter(fieldNameDATE       + "=" + date);
		addParameter(fieldNameTIME       + "=" + time);
		addParameter(fieldNameTIMETOGGLE + "=" + timeToggle);
		addParameter(otherParameters);
	}
	
	
	public void initialize(int localRequestNumber)
	{
		requestNumber = localRequestNumber;
	
		try
		{
			urlConnection = new URL(queryURL).openConnection();
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			SBBWebService.logFile.addLine("Request " + requestNumber + ": HTMLFormSimulator successfully initialized");
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	
	
	public void submit()
	{
		try
		{   	
			
			DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
			dataOutputStream.writeBytes(query);
			dataOutputStream.flush();
			dataOutputStream.close();
			
			SBBWebService.logFile.addLine("Request " + requestNumber + ": HTMLFormSimulator successfully submitted query \"" + urlConnection.getURL() + query + "\"");
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	
	public void receive(File outputFile)
	{
		try
		{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			FileWriter fileWriter = new FileWriter(outputFile);
			String line, previousLine = "";
			
			while( (line = bufferedReader.readLine()) != null)
			{	
				if(System.getProperty("os.name").equals("Linux"))
				{
					line = line.replaceAll("&#196;", "Ae");
					line = line.replaceAll("&#214;", "Oe");
					line = line.replaceAll("&#220;", "Ue");
					line = line.replaceAll("&#228;", "ae");
					line = line.replaceAll("&#246;", "oe");
					line = line.replaceAll("&#252;", "ue");
				}

				line = line.replaceAll("&nbsp;", " ");
				line = line.replaceAll(" />"   , ">");
				line = line.trim();
				
				if(previousLine.startsWith("<td") && (line.equals("</td>")))
					line = "-\n<td>";
							
				if(! line.equals(""))
				{
					fileWriter.write(line.trim() + System.getProperty("line.separator", "\n"));
					previousLine = line;
				}
			}
					
			fileWriter.flush();
			fileWriter.close();
			bufferedReader.close();

			SBBWebService.logFile.addLine("Request " + requestNumber + ": HTMLFormSimulator successfully received HTML document from SBB server");
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	
	
	
	/**
	 * Returns a new instance of HTMLFormSimulator, fields are initialized with information from the XML file 'fileName'
	 */
	public static HTMLFormSimulator load(String fileName)
	{        
		HTMLFormSimulator formSimulator = null;
		
		try
		{
		        formSimulator = new HTMLFormSimulator();

			XMLDecoder xmlDecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(fileName))); 
     			formSimulator = (HTMLFormSimulator)(xmlDecoder.readObject());
     			xmlDecoder.close();
     		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(exception);
		}
		
     		return formSimulator;	
	}
	
	
	
	/*
	 *  JavaBean conform setters
	 */
	public void setQueryURL           (String url       ) {	queryURL            = url;	  }
	public void setFieldNameTO        (String to        ) {	fieldNameTO         = to;	  }
	public void setFieldNameFROM      (String from      ) {	fieldNameFROM       = from;       }
	public void setFieldNameDATE      (String date      ) {	fieldNameDATE       = date;       }
	public void setFieldNameTIME      (String time      ) {	fieldNameTIME       = time;	  }
	public void setFieldNameTIMETOGGLE(String timeToggle) {	fieldNameTIMETOGGLE = timeToggle; }
	public void setOtherParameters    (String other     ) { otherParameters     = other;	  }
	 
	 
	 
	/*
	 *  JavaBean conform getters
	 */		
	public String getQueryURL           () { return queryURL;            }		
	public String getFieldNameTO        () { return fieldNameTO;         }	
	public String getFieldNameFROM      () { return fieldNameFROM;       }
	public String getFieldNameDATE      () { return fieldNameDATE;       }
	public String getFieldNameTIME      () { return fieldNameTIME;       }
	public String getFieldNameTIMETOGGLE() { return fieldNameTIMETOGGLE; }
	public String getOtherParameters    () { return otherParameters;     }
}