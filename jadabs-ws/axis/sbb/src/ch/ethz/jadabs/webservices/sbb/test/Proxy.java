package ch.ethz.jadabs.webservices.sbb.test;

import java.io.*;
import java.net.*;



public class Proxy
{
	public String forwardSoapObjectToServer(String string, URL url)
	{
		String response = "";
		
		try
		{
			byte[] soapMessage = string.getBytes();
		        
		        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		
			connection.setUseCaches(false);
		        connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("User-Agent", "ETH Jadabs Proxy for kSOAP/Axis");
		        connection.setRequestProperty("SOAPAction", "");
			connection.setRequestProperty("Content-Type", "text/xml");
			connection.setRequestProperty("Connection", "close");
		        connection.setRequestProperty("Content-Length", "" + soapMessage.length);
		        connection.setRequestMethod("POST");
	
		        OutputStream outputStream = connection.getOutputStream();
		        outputStream.write(soapMessage, 0, soapMessage.length);
		        outputStream.close();
	
		        connection.connect ();
		        
		        InputStream inputStream = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			int linesToSkip = 1;
			for(int i=0; i<linesToSkip; i++)
				reader.readLine();
			
			while(reader.ready())
				response += reader.readLine() + '\n';
				
			inputStream.close();
			connection.disconnect();
			
		}
		catch(Exception e)
		{
			System.out.println(e);	
		}
		
		return response;
	}
	
	public static void main(String[] argv) throws Exception
	{
		String string = "";
		string += "<SOAP-ENV:Envelope xmlns:n0=\"http://sbb.webservices.jadabs.ethz.ch\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" + '\n';
		string += "<SOAP-ENV:Body SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" + '\n';
		string += "<queryTimetable xmlns=\"SBBWebService\" id=\"o0\" SOAP-ENC:root=\"1\">" + '\n';
		string += "<TimetableQuery xmlns=\"\" xsi:type=\"n0:TimetableQuery\">" + '\n';
		string += "<from xsi:type=\"xsd:string\">Z%C3%BCrich</from>" + '\n';
		string += "<to xsi:type=\"xsd:string\">Niederweningen</to>" + '\n';
		string += "<date xsi:type=\"xsd:string\">10.11.2005</date>" + '\n';
		string += "<time xsi:type=\"xsd:string\">10%3A00</time>" + '\n';
		string += "<timeToggle xsi:type=\"xsd:int\">1</timeToggle>" + '\n';
		string += "<details xsi:type=\"xsd:int\">0</details>" + '\n';
		string += "</TimetableQuery>" + '\n';
		string += "</queryTimetable>" + '\n';
		string += "</SOAP-ENV:Body>" + '\n';
		string += "</SOAP-ENV:Envelope>" + '\n';
		
		//System.out.println(new Proxy().forwardSoapObjectToServer(string, new URL("http://localhost:8081/axis/services/SBBWebService")));
		System.out.println(new Proxy().forwardSoapObjectToServer(string, new URL("http://wlab.ethz.ch:8080/axis/services/SBBWebService")));
}
}