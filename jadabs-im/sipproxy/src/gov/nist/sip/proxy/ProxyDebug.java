package gov.nist.sip.proxy;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.db.UserDB;

/** Debugging println.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author Olivier Deruelle <deruelle@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ProxyDebug {
	
	private static final Logger logger = Logger.getLogger(ProxyDebug.class);
	
	
	public static  boolean debug = true;
	private static String proxyOutput;
	private static PrintStream stream=System.out;
	
	
	public static void setProxyOutputFile(String proxyOut) {}
	
	public static void writeFile(String inFile,String outFile, String text, boolean sep) {}
	
	public static void logException(Exception ex) {
		if (debug) {
			ex.printStackTrace(stream);
		}
	}
	
	public static void println(String text){
		logger.debug(text);
	}
	public static void info(String text) {
		logger.info(text);
	}
	
	public static void println(){
		logger.debug("\n");
	}
	
	public static void print(String text){
		logger.debug(text);
	}
	
}
