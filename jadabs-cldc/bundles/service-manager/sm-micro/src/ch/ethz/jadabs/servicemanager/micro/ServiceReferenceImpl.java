/*
 * Created on Feb 12, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.micro;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceReferenceImpl implements ServiceReference
{
    private static Logger LOG = Logger.getLogger("ServiceRefImpl");
    
    private String group;
    private String name;
    private String version;
    private String peer;
    
    private Hashtable properties = new Hashtable();
    
    private KXmlParser parser;
    
    public ServiceReferenceImpl(String uuid)
    {
        this(uuid, null, null);
    }
    
    public ServiceReferenceImpl(String id, String peer, String adv)
    {
        this.peer = peer;
        
        group = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        name = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        version = id.substring(0,id.indexOf(":"));
        
        LOG.debug("parsed uuid: "+group+":"+name+":"+version);
        
        if (adv != null)
            parseOPD(adv);
    
    }
    
    /**
     * parse the opd
     * @throws Exception
     */
    private void parseOPD(String adv) 
    {
       parser = new KXmlParser();

       try {
	       ByteArrayInputStream inputStream = new ByteArrayInputStream(adv.getBytes());
	       parser.setInput(new InputStreamReader(inputStream));
	         
	       Stack stack = new Stack();
	       for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
	             .next()) 
	       {
	          if (type == KXmlParser.START_TAG) {
	             stack.push(parser.getName());
	
	             if (stack.peek().equals("Property"))
	             {	                 
	                 int i = parser.getAttributeCount();
	                 for (int k =0; k< i;k++)
	                     properties.put(parser.getAttributeName(k),
	                             parser.getAttributeValue(k));
	                     
	             }
	             else if (stack.peek().equals("Extension")) {
	                // TODO: add to Extension vector
	                
	                /*
	                String id = parser.getAttributeValue(null, "id");
	                LOG.debug("FOUND EXTENSION " + id);
	                String extension = new String();
	                if (!id.startsWith("Extension")) {
	                   extension = extension.concat(parser.getName() + "/");
	                   for (int index = 0; index < parser.getAttributeCount(); index++) {
	                      if (index != 0)
	                         extension = extension.concat(", ");
	                      extension = extension.concat(parser.getAttributeName(index) + ":"
	                            + parser.getAttributeValue(index));
	                   }
	                }
	                System.out.println("EXTENSION: " + extension);                                                                                                                                                                     
	                */
	             } else if (stack.peek().equals("Extension-Point")) {
	                String id = parser.getAttributeValue(null, "id");
	                LOG.debug("FOUND EXTENSION-POINT " + id);
	                
	//                if (id.startsWith("Extension")) {
	//                Vector matchingPlugins = new Vector();
	//                if (LOG.isDebugEnabled()) {
	//                   LOG.debug("\n");
	//                   LOG.debug("REQUESTING " + id + " ¦ " + PluginLoaderImpl.platform + " ¦ " + "R");
	//                   LOG.debug("\n");
	//                }
	//                for (Iterator matches = PluginLoaderImpl.getMatchingPlugins(id + " ¦ " + PluginLoaderImpl.platform + " ¦ " + "R"); matches.hasNext(); ) {
	//                   matchingPlugins.add(matches.next());
	//                }
	//                
	//                if (matchingPlugins.isEmpty()) throw new Exception("Unsatisfied extension point " + id + " in Plugin " + this.toString());
	//                if (matchingPlugins.size() == 1) {
	//                   PluginLoaderImpl.scheduler.addPlugin((String)matchingPlugins.get(0));
	//                } else {
	//                   PluginLoaderImpl.scheduler.addAlternativePlugins(matchingPlugins);
	//                }
	//               
	//                PluginLoaderImpl.scheduler.stillToProcess.addAll(0, matchingPlugins);
	//                }
	                
	             } else if (stack.peek().equals("ServiceActivatorBundle")) {
	//                this.activator = parser.getAttributeValue(null, "activator-uuid");
	             }
	          } else if (type == KXmlParser.END_TAG) {
	             try {
	                stack.pop();
	             } catch (Exception e) {
	                LOG.error("ERROR while parsing, Platform-File not well-formed");
	             }
	          }
	       }
       } catch(XmlPullParserException e)
       {
           
       } catch (IOException e)
	    {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
    }
    
    /*
     */
    public String getName()
    {
        return name;
    }

    /*
     */
    public String getVersion()
    {
        return version;
    }

    /*
     */
    public String getGroup()
    {
        return group;
    }

    /*
     */
    public String getID()
    {
        return group+":"+name+":"+version+":";
    }

    /*
     */
    public String getAdvertisement()
    {
        return null;
    }

    
    public String getPeer()
    {
        return peer;
    }
    
    public String getDownloadURL()
    {
        String durl = (String)properties.get("downloadurl");
        
        return durl+"/"+group+"/jads/"+version+"/"+name+".jad";
    }
    
    public String getProperty(String name)
    {
        String prop = (String)properties.get(name);
        
        return prop;
    }
}
