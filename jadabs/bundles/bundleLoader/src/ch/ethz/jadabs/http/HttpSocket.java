package ch.ethz.jadabs.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class HttpSocket extends Socket
{
    protected BufferedReader inbound = null;

    public String version = null;
    public String method = null;
    public String file = null;
    public Hashtable headerValues = new Hashtable();
    public String extraHdr = null;

    HttpSocket()
    {
        super();
    }

    public void getRequest()
        throws IOException, ProtocolException
    {
        try
        {

            inbound = new BufferedReader(
                new InputStreamReader(getInputStream()) );

            String reqhdr = readHeader(inbound);

            parseReqHdr(reqhdr);
        }
        catch (IOException ioe)
        {
            if ( inbound != null )
                inbound.close();
            throw ioe;
        }
    }

    private String readHeader(BufferedReader is)
        throws IOException
    {
        String command;
        String line;

        // Get the first request line
        if ( (command = is.readLine()) == null )
            command = "";
        command += "\n";

        // Check for HTTP/1.0 signature
        if (command.indexOf("HTTP/") != -1)
        {
            // Retreive any additional lines
            while ((line = is.readLine()) != null  &&  !line.equals(""))
                command += line + "\n";
        }
        else
        {
            throw new IOException();
        }
        return command;
    }

    private void parseReqHdr(String reqhdr)
        throws IOException, ProtocolException
    {
        // Break the request into lines
        StringTokenizer lines = new StringTokenizer(reqhdr, "\r\n");
        String currentLine = lines.nextToken();

        // Process the initial request line
        // into method, file, version Strings
        StringTokenizer members = new StringTokenizer(currentLine, " \t");
        method = members.nextToken();
        file = members.nextToken();
        // if (file.equals("/")) file += "../index.html";
        version = members.nextToken();

        // Process additional lines into name/value pairs
        while ( lines.hasMoreTokens() )
        {
            String line = lines.nextToken();

            // Search for separating character
            int slice = line.indexOf(':');

            // Error if no separating character
            if ( slice == -1 )
            {
                throw new ProtocolException(
                    "Invalid HTTP header: " + line);
            }
            else
            {
                // Separate at the slice character into name, value
                String name = line.substring(0,slice).trim();
                String value = line.substring(slice + 1).trim();
                addNameValue(name, value);
            }
        }
    }

    /**
     * Add a name/value pair to the internal array
     */
    private void addNameValue(String name, String value)
    {
    	headerValues.put(name, value);
    }

}