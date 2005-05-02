package ch.ethz.jadabs.sbbws.com;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Vector;

import org.ksoap.ClassMap;
import org.ksoap.SoapEnvelope;
import org.ksoap.SoapObject;
import org.ksoap.SoapWriter;
import org.kxml.io.XmlWriter;
import org.kxml.parser.XmlParser;

import ch.ethz.jadabs.sbbws.ksoap.TimetableDetails_kSOAP;
import ch.ethz.jadabs.sbbws.ksoap.TimetableInquiry_kSOAP;
import ch.ethz.jadabs.sbbws.ksoap.TimetableOverview_kSOAP;
import ch.ethz.jadabs.sbbws.ksoap.TimetableQuery_kSOAP;

/**
 * This class converts SoapObjects to Strings and vice versa. The information
 * stored in the Type SoapTransformation is used for displaying the proper
 * values on the screen as well as to decide which form should be shown to the
 * user.
 * 
 * @author Franz Maier, Stefan Vogt
 */

public class SoapTransformation
{

    private TimetableQuery_kSOAP fQuery;

    private SoapObject fQuerySoapObject;

    public String fDate;
    
    private String fRequest;

    private Vector fFrom;

    private Vector fTo;

    private Vector fTimeDep;

    private Vector fTimeArr;

    private Vector fDuration;

    private Vector fChanges;

    private Vector fPlatform;

    private Vector fTravelWith;

    private Vector fComments;

    public boolean fFromAmbiguous = false;

    public boolean fToAmbiguous = false;

    private String fDetailsDuration;

    private int fDetailsChanges;

    private boolean fDetailedResponse;

    public SoapTransformation()
    {
    }

    public String createSoapMessageFromQuery(String from, String to, String date, String time)
    {
        fQuery = new TimetableQuery_kSOAP();
        fQuerySoapObject = new SoapObject("SBBWebService", "queryTimetable");
        fQuerySoapObject.addProperty("TimetableQuery", fQuery);

        try
        {
            StringBuffer fromBuff = new StringBuffer();
            StringBuffer toBuff = new StringBuffer();
            StringBuffer dateBuff = new StringBuffer();
            StringBuffer timeBuff = new StringBuffer();

            URLEncode(from, fromBuff);
            URLEncode(to, toBuff);
            URLEncode(date, dateBuff);
            URLEncode(time, timeBuff);

            fQuery.from = fromBuff.toString();
            fQuery.to = toBuff.toString();
            fQuery.date = dateBuff.toString();
            fQuery.time = timeBuff.toString();
            fQuery.timeToggle = 1;
            if (fDetailedResponse)
            {
                fQuery.details = 2;
            } else
            {
                fQuery.details = 0;
            }

        } catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
        }
        fRequest = soapObjectToString(fQuerySoapObject, "http://sbb.webservices.jadabs.ethz.ch");
        return fRequest;
    }

    public void handleSoapObject(String response)
    {
        Object responseObject = stringToObject(response);
      
        handleDifferentSoapObjects(responseObject);
    }

    public void handleSoapObject(InputStream is)
    {
        Object responseObject = streamToObject(is);
        
        handleDifferentSoapObjects(responseObject);
    }
    
    private void handleDifferentSoapObjects(Object responseObject)
    {
        if (responseObject instanceof TimetableInquiry_kSOAP)
        {
            TimetableInquiry_kSOAP inquiry = ((TimetableInquiry_kSOAP) responseObject);
            handleTimetableInquiryObject(inquiry);
        } else if (responseObject instanceof TimetableOverview_kSOAP)
        {
            TimetableOverview_kSOAP overview = ((TimetableOverview_kSOAP) responseObject);
            handleTimetableOverviewObject(overview);
        } else if (responseObject instanceof TimetableDetails_kSOAP)
        {
            TimetableDetails_kSOAP details = ((TimetableDetails_kSOAP) responseObject);
            handleTimetableDetailsObject(details);
        }
    }
    
    public void handleTimetableInquiryObject(TimetableInquiry_kSOAP inquiry)
    {
        if (inquiry.from.size() == 1)
        {
            fFromAmbiguous = false;
            fFrom = inquiry.from;
        } else
        {
            fFromAmbiguous = true;
            fFrom = inquiry.from;
        }

        if (inquiry.to.size() == 1)
        {
            fToAmbiguous = false;
            fTo = inquiry.to;
        } else
        {
            fToAmbiguous = true;
            fTo = inquiry.to;
        }
    }

    public void handleTimetableOverviewObject(TimetableOverview_kSOAP overview)
    {
        fDate = overview.requestedDate;
        fToAmbiguous = false;
        fFromAmbiguous = false;
        fFrom = overview.from;
        fTo = overview.to;
        fTimeDep = overview.timeDep;
        fTimeArr = overview.timeArr;
        fDuration = overview.duration;
        fChanges = overview.changes;
    }

    public void handleTimetableDetailsObject(TimetableDetails_kSOAP details)
    {
        fDate = details.requestedDate;
        fFrom = details.from;
        fTo = details.to;
        fTimeDep = details.timeDep;
        fTimeArr = details.timeArr;
        fDetailsDuration = details.duration;
        fDetailsChanges = details.changes;
        fPlatform = details.platform;
        fTravelWith = details.travelWith;
        fComments = details.comments;
    }

    /**
     * See the API documentation for URLEncoder, which lists the rules for
     * encoding: http://java.sun.com/j2se/1.4/docs/api/java/net/URLEncoder.html
     */

    public void URLEncode(String arg0, StringBuffer out) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(arg0);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        bais.read();
        bais.read();
        int c = bais.read();
        while (c >= 0)
        {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '.' || c == '-'
                    || c == '*' || c == '_')
                out.append((char) c);
            else if (c == ' ')
                out.append('+');
            else
            {
                if (c < 128)
                {
                    appendHex(c, out);
                } else if (c < 224)
                {
                    appendHex(c, out);
                    appendHex(bais.read(), out);
                } else if (c < 240)
                {
                    appendHex(c, out);
                    appendHex(bais.read(), out);
                    appendHex(bais.read(), out);
                }
            }
            c = bais.read();
        }
    }

    private void appendHex(int arg0, StringBuffer buff)
    {
        buff.append('%');
        if (arg0 < 16)
            buff.append('0');
        buff.append(Integer.toHexString(arg0));
    }

    public static ClassMap createClassMap()
    {
        ClassMap classMap = new ClassMap();
        classMap.addMapping("http://sbb.webservices.jadabs.ethz.ch", "TimetableQuery", new TimetableQuery_kSOAP()
                .getClass());
        classMap.addMapping("http://sbb.webservices.jadabs.ethz.ch", "TimetableInquiry", new TimetableInquiry_kSOAP()
                .getClass());
        classMap.addMapping("http://sbb.webservices.jadabs.ethz.ch", "TimetableOverview", new TimetableOverview_kSOAP()
                .getClass());
        classMap.addMapping("http://sbb.webservices.jadabs.ethz.ch", "TimetableDetails", new TimetableDetails_kSOAP()
                .getClass());

        return classMap;
    }

    /**
     * Transforms a SoapObject into a string, includes the enclosing
     * SoapEnvelope tags
     */
    private String soapObjectToString(SoapObject soapObject, String namespace)
    {
        String string = "";
        string += "<SOAP-ENV:Envelope xmlns:n0=\""
                + namespace
                + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + '\n';
        string += "<SOAP-ENV:Body SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" + '\n';

        try
        {
            ByteArrayOutputStream baus = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(baus);
            XmlWriter xmlWriter = new XmlWriter(writer);
            SoapWriter soapWriter = new SoapWriter(xmlWriter, createClassMap());
            soapWriter.write(soapObject);
            xmlWriter.flush();
            writer.flush();
            string += baus.toString();

        } catch (Exception exception)
        {
            //new ExceptionHandler(new SBBWebService(), exception);
        }

        string += "</SOAP-ENV:Body>" + '\n';
        string += "</SOAP-ENV:Envelope>" + '\n';

        return string;
    }

    /**
     * Jetzt klappt's :-)
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
            soapEnvelope.setClassMap(createClassMap());
            soapEnvelope.parse(xmlParser);
            return ((SoapObject) soapEnvelope.getBody()).getProperty(0);

        } catch (Exception exception)
        {
            //new ExceptionHandler(new SBBWebService(), exception);
        }

        return object;
    }

    private Object streamToObject(InputStream is)
    {
        Object object = null;

        try
        {
            Reader reader = new InputStreamReader(is);
            XmlParser xmlParser = new XmlParser(reader);
            SoapEnvelope soapEnvelope = new SoapEnvelope();
            soapEnvelope.setClassMap(createClassMap());
            soapEnvelope.parse(xmlParser);
            return ((SoapObject) soapEnvelope.getBody()).getProperty(0);

        } catch (Exception exception)
        {
            //new ExceptionHandler(new SBBWebService(), exception);
        }

        return object;
    }
    
    public void setDetailsQuery(boolean detailedResponse)
    {
        fDetailedResponse = detailedResponse;
    }

    public boolean isAmbiguous()
    {
        if (fFromAmbiguous || fToAmbiguous)
        {
            return true;
        } else
            return false;
    }

    public Vector getFrom()
    {
        return fFrom;
    }

    public Vector getTo()
    {
        return fTo;
    }

    public Vector getTimeDep()
    {
        return fTimeDep;
    }

    public Vector getTimeArr()
    {
        return fTimeArr;
    }

    public Vector getDuration()
    {
        return fDuration;
    }

    public Vector getChanges()
    {
        return fChanges;
    }

    public Vector getPlatform()
    {
        return fPlatform;
    }

    public Vector getTravelWith()
    {
        return fTravelWith;
    }

    public Vector getComments()
    {
        return fComments;
    }

    public boolean isFromAmbiguous()
    {
        return fFromAmbiguous;
    }

    public boolean isToAmbiguous()
    {
        return fToAmbiguous;
    }

    public String getDetailsDuration()
    {
        return fDetailsDuration;
    }

    public int getDetailsChanges()
    {
        return fDetailsChanges;
    }
}