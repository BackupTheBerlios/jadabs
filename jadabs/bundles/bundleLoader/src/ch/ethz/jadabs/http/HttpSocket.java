package ch.ethz.jadabs.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class HttpSocket extends Socket {
   protected BufferedReader fromClient = null;

   public String version = null;
   public String method = null;
   public String file = null;
   public Hashtable headerValues = new Hashtable();

   HttpSocket() {
      super();
   }

   public void getRequest() throws IOException, ProtocolException {
      try {

         fromClient = new BufferedReader(
               new InputStreamReader(getInputStream()));

         String reqhdr = readHeader(fromClient);

         parseReqHdr(reqhdr);
      } catch (IOException ioe) {
         if (fromClient != null)
            fromClient.close();
         throw ioe;
      }
   }

   private String readHeader(BufferedReader is) throws IOException {
      String command;
      String line;

      if ((command = is.readLine()) == null)
         command = "";
      command += "\n";

      if (command.indexOf("HTTP/") != -1) {
         while ((line = is.readLine()) != null && !line.equals(""))
            command += line + "\n";
      } else {
         throw new IOException();
      }
      return command;
   }

   private void parseReqHdr(String reqhdr) throws IOException,
         ProtocolException {
      StringTokenizer lines = new StringTokenizer(reqhdr, "\r\n");
      String currentLine = lines.nextToken();

      StringTokenizer members = new StringTokenizer(currentLine, " \t");
      method = members.nextToken();
      file = members.nextToken();

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

   private void addNameValue(String name, String value) {
      headerValues.put(name, value);
   }

   public void send404() {
      OutputStream toClient = null;

      try {

         toClient = getOutputStream();

         String hdr = "HTTP/1.0 404 NOT FOUND\r\nNONE\r\n\r\n\n Not Found";
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