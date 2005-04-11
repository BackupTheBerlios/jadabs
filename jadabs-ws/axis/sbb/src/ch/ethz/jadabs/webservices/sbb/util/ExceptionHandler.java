package ch.ethz.jadabs.webservices.sbb.util;

import ch.ethz.jadabs.webservices.sbb.SBBWebService;


/**
 * Prints the thrown exception to the screen and writes it to the error log.
 * 
 * @author Stefan Vogt
 */
public class ExceptionHandler
{        
	public ExceptionHandler(Exception exception)
	{
	        String error = "Exception: " + exception;
	        
		System.err.print(error + '\n');
		writeErrorLog(error);
	}
	
	public ExceptionHandler(Object source, Exception exception)
	{
	    	String error = "Exception in " + source.getClass().getName() + ": " + exception;
	    	
		System.err.print(error + '\n');
		writeErrorLog(error);
	}
	
	
	
	private void writeErrorLog(String error)
	{
	        try
	        {
	                LogFile logFile = new LogFile(SBBWebService.installDir + "/logs/ErrorLog.txt");
	                logFile.addLine(error);
	                logFile.close();
	        }
	        catch(Exception exception)
	        {
	                System.err.print("Exception in " + this.getClass().getName() + ": ");
		    	System.err.print(exception);
		    	System.err.print("\n");   
	        }
	}
}
