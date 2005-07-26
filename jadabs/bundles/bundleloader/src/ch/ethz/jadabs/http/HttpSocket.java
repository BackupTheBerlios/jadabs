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
 * Created on 2-Feb-2005
 */
package ch.ethz.jadabs.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import ch.ethz.jadabs.bundleLoader.api.Utilities;

/**
 * Http based implementation of a <code>Socket</code> 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class HttpSocket extends Socket {
   protected BufferedReader fromClient = null;

   public String version = null;
   public String method = null;
   public String file = null;
   public Hashtable headerValues = new Hashtable();

   public String data;

   /**
    * Constructor 
    */
   public HttpSocket() {
      super();
   }
   
   public HttpSocket(String host, int port) throws UnknownHostException, IOException {
      super(host, port);
   }

   /**
    * Returns the http request
    * @throws Exception
    */
   public void request() throws Exception {
      try {

         fromClient = new BufferedReader(
               new InputStreamReader(getInputStream()));

         String reqhdr = readHeader(fromClient);

         parseHeader(reqhdr);
         
         StringBuffer buffer = new StringBuffer();
         while (fromClient.ready()) {
            buffer.append(fromClient.readLine());
         }
         
         data = buffer.toString();
         
      } catch (IOException ioe) {
         if (fromClient != null)
            fromClient.close();
         throw ioe;
      }
   }
   
   public InputStream getFileInputStream(String urlfile) throws IOException
   {
       System.out.println("get urlfile: "+urlfile);
       
       URL url = new URL(urlfile);
       
       return url.openStream();
   }
   
   /**
    * Send a http GET request
    * @param request
    * @throws IOException
    */
   public void get(String request) throws IOException {
      DataOutputStream outbound = new DataOutputStream(
            getOutputStream() );

      String httpreq = "GET " + request +  " HTTP/1.0 \r\n\r\n";
      
      System.out.println("request:" + httpreq);
      
      outbound.writeBytes(httpreq);
      outbound.flush();
   }
   
   /**
    * Send a http POST request
    * @param request
    * @throws IOException
    */
   public void post(String request) throws IOException {
      DataOutputStream outbound = new DataOutputStream(
            getOutputStream() );

      String httpreq = "POST " + request +  " HTTP/1.0 \r\n\r\n";
          
      System.out.println("request:" + httpreq);
      
      outbound.writeBytes(httpreq);
      outbound.flush();
   }

   
   /**
    * Read the http header
    * @param input 
    * @return
    * @throws IOException
    */
   private String readHeader(BufferedReader input) throws IOException {
      String command;
      String line;

      if ((command = input.readLine()) == null)
         command = "";
      command += "\n";

      if (command.indexOf("HTTP/") != -1) {
         while ((line = input.readLine()) != null && !line.equals(""))
            command += line + "\n";
      } else {
         throw new IOException();
      }
      return command;
   }

   
   /**
    * Parse the http header
    * @param reqhdr
    * @throws IOException
    * @throws ProtocolException
    */
   private void parseHeader(String reqhdr) throws IOException,
         ProtocolException {
      StringTokenizer lines = new StringTokenizer(reqhdr, "\r\n");
      String currentLine = lines.nextToken();

      StringTokenizer members = new StringTokenizer(currentLine, " \t");
      method = members.nextToken();
      file = Utilities.unescape(members.nextToken());

      version = members.nextToken();

      while (lines.hasMoreTokens()) {
         String line = lines.nextToken();

         int slice = line.indexOf(':');

         if (slice == -1) {
            throw new ProtocolException("Invalid HTTP header: " + line);
         } else {
            String name = line.substring(0, slice).trim();
            String value = line.substring(slice + 1).trim();
            addNameValue(name, value);
         }
      }
   }

   /**
    * Add a name / value pair 
    * @param name
    * @param value
    */
   private void addNameValue(String name, String value) {
      headerValues.put(name, value);
   }

   /**
    * Send a 404 (not found)    
    */
   public void send404() {
      OutputStream toClient = null;

      try {

         toClient = getOutputStream();

         String hdr = "HTTP/1.0 404 NOT FOUND\r\nNONE\r\n\r\n Not Found";
         toClient.write(hdr.getBytes());

         toClient.close();
         fromClient.close();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         toClient = null;
         fromClient = null;
      }
   }

   
   /**
    * Send a file via http
    * @param file the file to be send
    * @param MimeType the mime type as <code>String</code>
    * @param extraHdr additional header fields, can be null
    */
   public void sendFile(File file, String MimeType, String extraHdr) {
      OutputStream toClient = null;
      
      try {
         toClient = getOutputStream();

         FileInputStream fis = new FileInputStream(file);
         long fileSize = file.length();

         String hdr = "HTTP/1.0 200 OK\r\n";
         hdr += "Content-type: " + MimeType + "\r\n";
         hdr += "Content-Length: " + fileSize + "\r\n";         
         if (extraHdr != null)                             
            hdr += extraHdr;
         
         hdr += "\r\n";

         toClient.write(hdr.getBytes());

         if (!method.equals("HEAD")) {
            byte dataBody[] = new byte[1024];
            int cnt;
            while ((cnt = fis.read(dataBody)) != -1) {
               toClient.write(dataBody, 0, cnt);
            }
         }
         toClient.close();
         fromClient.close();
      } catch (IOException e) {
         send404();
         e.printStackTrace();         
      } finally {
         toClient = null;
         fromClient = null;
      }
   }

   
   /**
    * Send a string 
    * @param data the <code>String</code> to be send
    * @param MimeType the mime type
    */
   public void sendString(String data, String MimeType) {
      OutputStream toClient = null;

      try {
         toClient = getOutputStream();

         String hdr = "HTTP/1.0 200 OK\r\n";
         hdr += "Content-type: " + MimeType + "\r\n";
         hdr += "Content-Length: " + data.length() + "\r\n";
         hdr += "\r\n";

         toClient.write(hdr.getBytes());

         if (!method.equals("HEAD")) {
            byte dataBody[] = new byte[1024];
            int cnt;
            toClient.write(data.getBytes(), 0, data.length());
         }
         toClient.close();
         fromClient.close();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         toClient = null;
         fromClient = null;
      }
   }

}