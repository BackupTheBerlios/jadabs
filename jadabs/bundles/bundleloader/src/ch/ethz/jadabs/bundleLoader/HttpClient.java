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
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher;
import ch.ethz.jadabs.bundleLoader.api.Utilities;
import ch.ethz.jadabs.http.HttpSocket;

/**
 * Information Source modelling a http connection to either a webserver
 * providing repository.xml or a httpDaemon on a remote jadabs peer.
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class HttpClient extends PluginFilterMatcher implements InformationSource
{

    private Vector knownHosts;

    private static Logger LOG = Logger.getLogger(HttpClient.class);

    private boolean canWS = false;

    private boolean canHTTP = false;

    private static String host = "localhost";
    private static int port = 80;

    KXmlParser parser;
    
    /** Local repository cache */
    private String repoCacheDirDefault = "./repository/";
    private File repoCacheDir;
    
    public HttpClient() throws Exception
    {
        repoCacheDir = new File(repoCacheDirDefault);
        if (!repoCacheDir.exists())
            repoCacheDir.mkdir();
        
        //TODO: read property or file and build up list of known hosts
        // String host = "jadabsrepo.ethz.ch";
        //      String host = "localhost";
        HttpSocket clientSocket = null;

        String httprepo = BundleLoaderActivator.bc.getProperty("ch.ethz.jadabs.bundleloader.httprepo");
        if (httprepo != null)
            host = httprepo;
        
        String httpport = BundleLoaderActivator.bc.getProperty("ch.ethz.jadabs.bundleloader.httprepo.port");
        if (httpport != null)
            port = Integer.parseInt(httpport);
        
        // used for client http servers
//        try
//        {
//            clientSocket = new HttpSocket(host, 9278);
//            canWS = true;
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
        
        try
        {
            clientSocket = new HttpSocket(host, port);
            canHTTP = true;
        } catch (Exception e)
        {
            LOG.debug("Could not open http repository connection");
        }

//        if (clientSocket == null) { throw new Exception("Could not open socket ...: "+host+":"+port); }

        knownHosts = new Vector();
        knownHosts.add(host);
    }

    /**
     * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String)
     */
    public InputStream retrieveInformation(String uuid)
    {

        LOG.debug("retrieve request over http: " + uuid);

        String[] args = Utilities.split(uuid, ":");
        String group = args[0];
        String name = args[1];
        String version = args[2];
        String type = args[3];

        
        
        for (Enumeration hosts = knownHosts.elements(); hosts.hasMoreElements();)
        {
            String host = (String) hosts.nextElement();
            
            if (canWS)
            {
                try
                {
                    HttpSocket clientSocket = new HttpSocket(host, 9278);

                    clientSocket.get("/get" + type + "/" + uuid);
                    clientSocket.request();

                    return new ByteArrayInputStream(clientSocket.data.getBytes());

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } else if (canHTTP)
            {
                try
                {
                    HttpSocket clientSocket = new HttpSocket(host, port);
                    
                    if (type.equals("jar"))
                    {
                        String downloadurl = "http://" + host + "/repository/" + group + "/jars/" + name + "-"
                                + version + ".jar";
                                                
                        InputStream is = clientSocket.getFileInputStream(downloadurl);
                        
                        // write file into local repository
                        saveInCache(is, group, name, version, type);
                        
                        
                        return clientSocket.getFileInputStream(downloadurl);

                    } else if (type.equals("obr"))
                    {
//                        clientSocket.get("/repository.xml");
//                        clientSocket.request();
                        
                        String obrurl = "http://" + host + "/repository/" + 
                        	group + "/obrs/" + name + "-" + version + ".obr";
                                                
                        URL url = new URL(obrurl);
                                              
                        saveInCache(url.openStream(), group, name, version, type);
                        
                        return url.openStream();
                                                
//                        StringTokenizer tokenizer = new StringTokenizer(clientSocket.data);
//                        StringBuffer result = new StringBuffer();
//                        boolean found = false;

//                        while (tokenizer.hasMoreTokens())
//                        {
//                            String token = tokenizer.nextToken();
//
//                            if (token.indexOf("<bundle>") != -1)
//                            {
//                                result = new StringBuffer();
//                            } else if (token.indexOf("</bundle>") != -1)
//                            {
//                                result.append(token);
//                                if (found)
//                                {
//                                    System.out.println("found bundle:" + result.toString());
//
//                                    return new ByteArrayInputStream(result.toString().getBytes());
//                                }
//                            } else if (token.startsWith("<bundle-uuid>"))
//                            {
//                                //	                     result.append(token);
//
//                                System.out.println("parsed bundle: " + token);
//
//                                //	                     token = tokenizer.nextToken();
//                                if (token.indexOf(uuid) != -1)
//                                {
//                                    found = true;
//
//                                    System.out.println("found uuid bundle: " + token);
//
//                                }
//                            }
//                            result.append(token);
//                        }
                    }
                    return null;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void saveInCache(InputStream is, String group, String name, 
            String version, String type)
    {        
        String filename = name + "-" + version + "."+type;
        
        
        // init folder
        StringBuffer sb = new StringBuffer();
        sb.append(repoCacheDir.getAbsolutePath() + 
                File.separatorChar +group);
        
        File groupdir = new File(sb.toString());
        groupdir.mkdir();
        
        sb.append(File.separatorChar + type+"s");
        File opddir = new File(sb.toString());
        opddir.mkdir();
        
        // save file
        sb.append(File.separatorChar + filename);
        String absfilepath = sb.toString();
        
        // set filepath in BundleInformation
//        binfo.setBundleCacheLocation(absfilepath);
                
	    File file = new File(absfilepath);
	    FileOutputStream fo;
        try
        {
            fo = new FileOutputStream(file);
            
            byte[] buff = new byte[1024];
            int k;                
            while ( (k=is.read(buff) ) != -1) 
                fo.write(buff,0,k);
                        
    	    fo.close();
    	    is.close();  	    
    	    
        } catch (FileNotFoundException e)
        {
            LOG.error("could not create file outputstream: ",e);
        } catch (IOException e)
        {
            LOG.error("could not write file:",e);
        }
	
//	    RandomAccessFile raf = new RandomAccessFile(filename, "rw");
//	    raf.write(data);

    }
    
    public static void cacheRepositoryOPDs()
    {
        String httprepo = BundleLoaderActivator.bc.getProperty("ch.ethz.jadabs.bundleloader.httprepo");
        if (httprepo != null)
            host = httprepo;
        
        String httpport = BundleLoaderActivator.bc.getProperty("ch.ethz.jadabs.bundleloader.httprepo.port");
        if (httpport != null)
            port = Integer.parseInt(httpport);
        
        String downloadurl = "http://"+host+":"+port+"/repository.xml";
        
        try {
	        URL url = new URL(downloadurl);
	 	        
	        InputStream ins = url.openStream();        
	        InputStreamReader insr = new InputStreamReader(ins);
	        BufferedReader br = new BufferedReader(insr);
	        
	        String line = br.readLine();
	        	        
	        while(!line.equals("<plugins>") )
	            line = br.readLine();
	        
	        // start parsing plugins and store them locally
	        
	        StringBuffer sb;
	        while (!line.equals("</plugins>"))
	        {
	            while(line.indexOf("<OSGiServicePlugin") == -1)
	                line = br.readLine();
	            
	            sb = new StringBuffer();
	            String uuid = null;
	            
	            while (line.indexOf(">") == -1)
	            {
	                
	            	sb.append(line+"\n");
	            	
	            	
	            	if(line.indexOf("uuid") > -1)
	        	    {
	        	        String uuidline = line.trim();
	        	    
	        	        uuid = uuidline.substring(6,uuidline.lastIndexOf("\""));
	        	        	        	        
	        	    }
	            	
	            	line = br.readLine();
	            }
	                       
	            
	        	while (line.indexOf("</OSGiServicePlugin>") == -1)
	        	{
	        	    sb.append(line+"\n");
	        	    
	        	    line = br.readLine();
	        	    
	        	}
	        	
	        	sb.append(line+"\n");
	        	
	            
	            saveOPDInCache(sb.toString(), uuid);
	            
	            line = br.readLine();
	        }      
	        
	        ins.close();
	        insr.close();
	        br.close();
	        
        }catch(IOException ioe)
        {
            LOG.debug("could not open connection to server: "+downloadurl);
        }
    }
    
    private static void saveOPDInCache(String data, String uuid)
    {
        String[] args = Utilities.split(uuid, ":");
        String group = args[0];
        String name = args[1];
        String version = args[2];
        String type = args[3];
        
        String filename = name + "-" + version + ".opd";
        
        
        // init folder
        StringBuffer sb = new StringBuffer();
        sb.append("repository" + 
                File.separatorChar +group);
        
        File groupdir = new File(sb.toString());
        groupdir.mkdir();
        
        sb.append(File.separatorChar + "opds");
        File opddir = new File(sb.toString());
        opddir.mkdir();
        
        // save file
        sb.append(File.separatorChar + filename);
        String absfilepath = sb.toString();
        
        // set filepath in BundleInformation
//        binfo.setBundleCacheLocation(absfilepath);
                
 	    File file = new File(absfilepath);
 	    FileOutputStream fo;
        try
        {
            fo = new FileOutputStream(file);
            
            fo.write(data.getBytes());
    	    fo.close();
        } catch (FileNotFoundException e)
        {
            System.out.println("could not create file outputstream: "+e);
        } catch (IOException e)
        {
            System.out.println("could not write file:"+e);
        }
 	
// 	    RandomAccessFile raf = new RandomAccessFile(filename, "rw");
// 	    raf.write(data);

    }

    
    private String getOBR(String repository, String uuid)
    {

        parser = new KXmlParser();

        try
        {
            parser.setInput(new StringReader(repository));

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
                        processElement(stack, uuid);
                    }
                }
            }
        } catch (XmlPullParserException e1)
        {
            return null;
        } catch (IOException e)
        {
            return null;
        }

        return null;
    }

    /**
     * Processes a text element from a obr file
     * 
     * @param stack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processElement(Stack stack, String uuid) throws XmlPullParserException, IOException
    {
        if (stack.peek().equals("bundle"))
        {
            String name = parser.getText().trim();
        } else if (stack.peek().equals("bundle-uuid"))
        {
            String id = parser.getText().trim();
            
            if (id.equals(uuid))
                System.out.println("found bundle");
        }
    }

    /**
     * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String,
     *      java.lang.String)
     */
    public InputStream retrieveInformation(String uuid, String source)
    {
        String[] args = Utilities.split(uuid, ":");
        String group = args[0];
        String name = args[1];
        String version = args[2];
        String type = args[3];

        LOG.debug("retrieve request over http2: " + uuid + ", " + source);

        try
        {
            HttpSocket clientSocket = new HttpSocket(source, 9278);

            clientSocket.get("/get" + type + "/" + uuid);
            clientSocket.request();

            return new ByteArrayInputStream(clientSocket.data.getBytes());

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#getMatchingPlugins(java.lang.String)
     */
    public Iterator getMatchingPlugins(String filter)
    {

        for (Enumeration hosts = knownHosts.elements(); hosts.hasMoreElements();)
        {
            try
            {
                String host = (String) hosts.nextElement();
                HttpSocket clientSocket = new HttpSocket(host, 9278);

                clientSocket.get("/match" + "/" + filter);
                clientSocket.request();

                return new PluginIterator(clientSocket.data);

            } catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        return null;
    }

    /**
     * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#debug(java.lang.String)
     */
    protected void debug(String str)
    {
        if (LOG.isDebugEnabled())
            LOG.debug(str);
    }

    /**
     * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#error(java.lang.String)
     */
    protected void error(String str)
    {
        LOG.error(str);
    }

    public class PluginIterator implements Iterator
    {

        private String[] plugins;

        private int index;

        public PluginIterator(String data)
        {
            plugins = Utilities.split(data, "#####");
            index = 0;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            // It is optional, we don't need it so we leave it unimplemented
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return (index < plugins.length);
        }

        /**
         * @see java.util.Iterator#next()
         */
        public Object next()
        {
            return new ByteArrayInputStream(plugins[index].getBytes());
        }
    }

}