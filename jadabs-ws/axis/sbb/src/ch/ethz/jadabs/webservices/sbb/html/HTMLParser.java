package ch.ethz.jadabs.webservices.sbb.html;

import java.io.*;
import javax.swing.text.html.HTMLEditorKit;
import ch.ethz.jadabs.webservices.sbb.SBBWebService;
import ch.ethz.jadabs.webservices.sbb.util.ExceptionHandler;



/**
 * 
 * 
 * @author Stefan Vogt
 */
public class HTMLParser extends HTMLEditorKit
{
	private int requestNumber;
	
	public void initialize(int requestNumber)
	{
		this.requestNumber = requestNumber;
	}
	
	public void parseFile(File input, File output)
	{
		try
		{
			Parser parser = new HTMLParser().getParser();
			InputStreamReader streamReader = new InputStreamReader(new FileInputStream(input));
			FileWriter fileWriter = new FileWriter(output);
			HTMLHandler htmlHandler = HTMLHandler.load(SBBWebService.installDir + "/settings/HTMLHandler.xml");
			htmlHandler.setWriter(fileWriter);

			parser.parse(streamReader, htmlHandler, true);
			
			streamReader.close();
			fileWriter.flush();
			fileWriter.close();
			
			SBBWebService.logFile.addLine("Request " + requestNumber + ": HTMLParser successfully parsed the HTML document");
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
}