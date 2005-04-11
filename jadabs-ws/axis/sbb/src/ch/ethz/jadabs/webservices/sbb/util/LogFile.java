package ch.ethz.jadabs.webservices.sbb.util;

import java.io.FileWriter;
import java.util.Date;


/**
 * Creates a new log file or appends to an existing one.
 * 
 * @author Stefan Vogt
 */
public class LogFile
{
	private FileWriter fileWriter;
	private String lineSeperator;
	
	public LogFile(String fileName) throws Exception
	{			
		this.lineSeperator =  System.getProperty("line.separator", "\n");	// Gets the systems line separator, "\n" ist the default
		
		this.fileWriter = new FileWriter(fileName, true);
	}
	
	public void addLine(String string) throws Exception
	{
		fileWriter.write("[" + new Date() + "] " + string + lineSeperator);
		fileWriter.flush();
	}
	
	public void close() throws Exception
	{
		fileWriter.close();	
	}
}