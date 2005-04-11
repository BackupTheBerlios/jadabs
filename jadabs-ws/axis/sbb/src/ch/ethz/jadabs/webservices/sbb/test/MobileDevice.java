package ch.ethz.jadabs.webservices.sbb.test;

import ch.ethz.jadabs.webservices.sbb.SBBWebService;
import ch.ethz.jadabs.webservices.sbb.serializable.*;
import ch.ethz.jadabs.webservices.sbb.util.ExceptionHandler;
import org.ksoap.*;
import org.kxml.io.*;
import org.kxml.parser.XmlParser;
import java.net.*;
import java.io.*;



public class MobileDevice
{
	public static void main(String args[])
	{
		try
		{
			MobileDevice mobileDevice  = new MobileDevice();
			Proxy proxy                = new Proxy();
			TimetableQuery_kSOAP query = new TimetableQuery_kSOAP();
			SoapObject soapObject      = new SoapObject("SBBWebService", "queryTimetable");
			String request, response;
			
			soapObject.addProperty("TimetableQuery", query);
			
			
			// Query 01
			
			System.out.println("Query 01: Should return an oberview\n");
			
			query.from       = URLEncoder.encode(new String("Niederweningen"), "UTF-8");
			query.to         = URLEncoder.encode(new String("Zürich")        , "UTF-8");
			query.date       = URLEncoder.encode(new String("10.11.2005")    , "UTF-8");
			query.time       = URLEncoder.encode(new String("10:00")         , "UTF-8");
			query.timeToggle = 1;
			query.details    = 0;
			
			request  = mobileDevice.soapObjectToString(soapObject, "http://sbb.webservices.jadabs.ethz.ch");
			response = proxy.forwardSoapObjectToServer(request, new URL("http://wlab.ethz.ch:8080/axis/services/SBBWebService"));
			
			Test.processResponse(mobileDevice.stringToObject(response));
			
			System.out.println("\n");
			
			
			
			// Query 02
			
			System.out.println("Query 02: Should return details\n");
			
			query.from       = URLEncoder.encode(new String("Niederweningen"), "UTF-8");
			query.to         = URLEncoder.encode(new String("Burgdorf")      , "UTF-8");
			query.date       = URLEncoder.encode(new String("10.11.2005")    , "UTF-8");
			query.time       = URLEncoder.encode(new String("10:00")         , "UTF-8");
			query.timeToggle = 1;
			query.details    = 2;
			
			request  = mobileDevice.soapObjectToString(soapObject, "http://sbb.webservices.jadabs.ethz.ch");
			response = proxy.forwardSoapObjectToServer(request, new URL("http://wlab.ethz.ch:8080/axis/services/SBBWebService"));
			
			Test.processResponse(mobileDevice.stringToObject(response));
			
			System.out.println("\n");
			
			
			
			// Query 03
			
			System.out.println("Query 03: Should return an inquiry\n");
			
			query.from       = URLEncoder.encode(new String("Niederweningen"), "UTF-8");
			query.to         = URLEncoder.encode(new String("Buchs")         , "UTF-8");
			query.date       = URLEncoder.encode(new String("10.11.2005")    , "UTF-8");
			query.time       = URLEncoder.encode(new String("10:00")         , "UTF-8");
			query.timeToggle = 1;
			query.details    = 0;
			
			request  = mobileDevice.soapObjectToString(soapObject, "http://sbb.webservices.jadabs.ethz.ch");
			response = proxy.forwardSoapObjectToServer(request, new URL("http://wlab.ethz.ch:8080/axis/services/SBBWebService"));
			
			Test.processResponse(mobileDevice.stringToObject(response));
			
			System.out.println("\n");
		}
		catch(Exception exception)
		{
			new ExceptionHandler(new SBBWebService(), exception);
		}
	}


	/**
	 * Transforms a SoapObject into a string, includes the enclosing SoapEnvelope tags
	 */
	private String soapObjectToString(SoapObject soapObject, String namespace)
	{
		String string = "";
		string += "<SOAP-ENV:Envelope xmlns:n0=\"" + namespace + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" + '\n';
		string += "<SOAP-ENV:Body SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" + '\n';
		
		try
		{
			ByteArrayOutputStream baus = new ByteArrayOutputStream();
			Writer writer = new OutputStreamWriter(baus);
			XmlWriter xmlWriter = new XmlWriter(writer);
			SoapWriter soapWriter = new SoapWriter(xmlWriter, Test.createClassMap());
			soapWriter.write(soapObject);
			xmlWriter.flush();
			writer.flush();
			string += baus.toString("UTF-8");
			
		}
		catch(Exception exception)
		{
			new ExceptionHandler(new SBBWebService(), exception);
		}
		
		string += "</SOAP-ENV:Body>" + '\n';
		string += "</SOAP-ENV:Envelope>" + '\n';
		
		return string;
	}
	
	
	
	/**
	 * 
	 *  Jetzt klappt's :-)
	 */
	private Object stringToObject(String string)
	{
		Object object = null;
		
		try
		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());
			Reader reader = new InputStreamReader(inputStream);
			XmlParser xmlParser = new XmlParser(reader);
			SoapEnvelope soapEnvelope = new SoapEnvelope();
			soapEnvelope.setClassMap(Test.createClassMap());
			soapEnvelope.parse(xmlParser);
			return ((SoapObject)soapEnvelope.getBody()).getProperty(0);
						
		}
		catch(Exception exception)
		{
			new ExceptionHandler(new SBBWebService(), exception);
		}
		
		return object;
	}
}