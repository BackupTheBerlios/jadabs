/*
 * Copyright (c) 2003-2005, Jadabs project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the Jadabs project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  Created on 14-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import ch.ethz.jadabs.bundleLoader.api.Descriptor;
import ch.ethz.jadabs.bundleLoader.security.BundleSecurityImpl;

/**
 * Holding information about bundles taken from .obr files 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleDescriptor extends Descriptor {
   
   private static Logger LOG = Logger.getLogger(BundleDescriptor.class);
   
   private static final String[] PROPERTIES = new String[]{"bundle-name",
           "bundle-group", "bundle-version", "bundle-updatelocation",
           "digestGenerationAlgorithm", "keyGenerationAlgorithm",
           "signature", "digest", "certificate"};
   
   protected Vector dependencies = new Vector();
   private KXmlParser parser;
   private Properties properties = new Properties();
//   private String name;
//   private String version;
//   private String group;
//   private String bundleLocation;
//   private String bundleChecksum;
//   private String digestGenAlgo;
//   private String keyGenAlgo;
//   private byte[] digest;
//   private byte[] signature;
//   private String tempInfo;
//   private byte[] certificate;
   protected boolean processed = false;
   private int level = 0;
   
   
   /**
    * Hidden constructor    
    */
   private BundleDescriptor() {   
      super(null);
   }
   
   private boolean isProperty(Object name){
       for (int i = 0; i < PROPERTIES.length; i++) {
           if (PROPERTIES[i].equals(name)) return true;
       }
       return false;
   }

   /**
    * Constructor for BundleDescriptor 
    * @param uuid Uuid of the underlying .obr file, e.g. 
    *             <code>jadabs:jxme-osgi:0.7.1:obr</code>
    * @throws Exception
    */
   protected BundleDescriptor(String uuid) throws Exception {      
      super(uuid);
      
      parser = new KXmlParser();
      
      // fetch input stream from obr file
      InputStream instream = BundleLoaderActivator.bundleLoader.fetchInformation(uuid, this);      
      if (instream != null)
      {
	      parser.setInput(new InputStreamReader(instream));
	      // parse obr file
	      parseOBR();
	      String group = properties.getProperty("bundle-group");
	      String name = properties.getProperty("bundle-name");
	      String version = properties.getProperty("bundle-version");
	      if (! uuid.equals(group + ":" + name + ":" + version + ":obr"))
	         throw new Exception("OBR file corrupted. Could not create BundleDescriptor");
	      
	      if (LOG.isDebugEnabled()) 
	         LOG.debug("Created new BundleDescriptor " + uuid);
      }
      else
          LOG.warn("could not read information: "+ uuid);
   }

   /**
    * Parse obr file via XmlPullParser kxml2 
    * @throws XmlPullParserException
    * @throws IOException
    */
   private void parseOBR() throws XmlPullParserException, IOException
   {
       Stack stack = new Stack();
       Vector dependencies = new Vector();

       for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser.next())
       {
           if (type == KXmlParser.START_TAG)
           {
               stack.push(parser.getName());
           }
           if (type == KXmlParser.END_TAG)
           {
               try
               {
                   stack.pop();
               } catch (Exception e)
               {
                   System.err.println("ERROR while parsing, OBR-File not well-formed");
               }
           }
           if (type == KXmlParser.TEXT)
           {
               if (!parser.getText().trim().equals(""))
               {
                   processElement(stack);
               }
           }
       }
   }

   /**
    * Processes a text element from a obr file
    * @param stack
    * @throws XmlPullParserException
    * @throws IOException
    */
   private void processElement(Stack stack) throws XmlPullParserException, IOException
   {
       if (stack.peek().equals("dependency-uuid"))
       {
           String uuid = parser.getText().trim();
           try
           { 
              LOG.debug("Dependency:" + uuid);
              dependencies.add(uuid);
           } catch (Exception e)
           {
               LOG.error("malformed bundle uuid: " + uuid);
               e.printStackTrace();
           }
       } else if (isProperty(stack.peek())){
           properties.put(stack.peek(), parser.getText().trim());
       }
   }
   
   /**
    * Get the uuid of the jar file corresponding to the 
    * BundleDescriptor's obr uuid. 
    * @return uuid of the bundle jar file, e.g. 
    * <code>jadabs:jxme-osgi:0.7.1:jar</code>
    */
   protected String jar_uuid() {
      String uuid = toString();
      uuid = uuid.substring(0, uuid.lastIndexOf(":")) + ":jar";
      return uuid;
   }
   
   /**
    * Get the location of the jar file as provided by the obr file
    * @return <code>String</code> containing a url to the bundle
    */
   protected String jar_source() {
      return properties.getProperty("bundle-updatelocation");
   }
   
   /**
    * Check a bundle according to the obr bundle checksum 
    * @param bundle <code>InputStream</code> content of a bundle> 
    * @return <code>boolean</code> value of success
    */
   protected boolean checkBundle(byte[] jarBytes) {
      try {
          LOG.debug("Checking bundle " + jar_uuid());
          boolean retVal = BundleSecurityImpl.Instance().checkBundle(this, jarBytes);
          LOG.debug("result: " + retVal);
          return retVal;
		  } catch (Exception e) {
		      LOG.error(e.getMessage());
		      LOG.debug("something went wrong checking the bundle", e);
		  }
      return false;
   }
   
   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj) {
      if (obj instanceof String) {
         return ((String)obj).equalsIgnoreCase(this.toString());
      } else if (obj instanceof BundleDescriptor) {
         BundleDescriptor descr = (BundleDescriptor)obj;
         return (this.toString().equals(descr.toString()) && this.dependencies.equals(descr.dependencies));
      } 
      return false;
   }
       
	public String getProperty(String name){
	    return properties.getProperty(name);
	}
}
