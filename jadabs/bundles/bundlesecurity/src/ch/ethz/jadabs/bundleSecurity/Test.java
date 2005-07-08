/*
 * Created on Jul 6, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.Vector;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author otmar
 */
public class Test {
    
    private String digestGenAlgo;
    private String keyGenAlgo;
    private String digest;
    private String signature;
    private String tempInfo;
    private String name;
    private String version;
    private String group;
    private String bundleLocation;
    private String bundleChecksum;
    private KXmlParser parser;
    
    public Test(String obrFile) throws Exception{
        parser = new KXmlParser();
        
        // fetch input stream from obr file
        System.out.println("parsing " + obrFile);
        FileInputStream instream = new FileInputStream(obrFile);
        if (instream != null)
        {
  	      parser.setInput(new InputStreamReader(instream));
  	      // parse obr file
  	      parseOBR();
        }
    }
    
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

    private void processElement(Stack stack) throws XmlPullParserException, IOException
    {
        if (stack.peek().equals("bundle-name"))
        {
            name = parser.getText().trim();
        } else if (stack.peek().equals("bundle-group"))
        {
            group = parser.getText().trim();
        } else if (stack.peek().equals("bundle-version"))
        {
            version = parser.getText().trim();
        } else if (stack.peek().equals("bundle-updatelocation"))
        {
            bundleLocation = parser.getText().trim();
        } else if (stack.peek().equals("bundle-checksum"))
        {
            bundleChecksum = parser.getText().trim();
        } else if (stack.contains("bundle-security")) {
            if (stack.peek().equals("digestGenerationAlgorithm")){
                digestGenAlgo = parser.getText().trim();
            } else if (stack.peek().equals("keyGenerationAlgorithm")){
                keyGenAlgo = parser.getText().trim();
            } else if (stack.peek().equals("digest")){
                digest = parser.getText().trim();
            } else if (stack.peek().equals("signature")){
                signature = parser.getText().trim();
            } else if (stack.peek().equals("temp-info")){
                tempInfo = Utilities.removeAll(parser.getText().trim(), "\t");
            }
        }
    }
    
    protected boolean checkBundle(String jarFile) {
        try {
            // get the content of the jar
            InputStream instream = new FileInputStream(jarFile);
            return BundleSecurityImpl.getInstance().checkBundle(instream,
                    digest, digestGenAlgo, signature, keyGenAlgo, tempInfo);
  		  } catch (Exception e) {
  		      // TODO Auto-generated catch block
  		      e.printStackTrace();
  		  }
        return false;
     }
    
    public static void main(String[] args) throws Exception{
        Test test = new Test(args[0]);
        System.out.println(test.checkBundle(args[1]));
    }

}
