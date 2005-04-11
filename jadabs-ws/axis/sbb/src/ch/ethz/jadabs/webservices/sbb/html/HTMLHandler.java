package ch.ethz.jadabs.webservices.sbb.html;

import java.beans.XMLDecoder;
import java.io.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.HTML.*;
import javax.swing.text.html.HTMLEditorKit.*;
import java.util.Enumeration;
import java.util.HashSet;
import ch.ethz.jadabs.webservices.sbb.util.ExceptionHandler;



/**
 * 
 * 
 * @author Stefan Vogt
 */
public class HTMLHandler extends ParserCallback
{
	private HashSet tagsToParse;
	private HashSet tagsToIgnore;
	private HashSet attributesToParse;
	private HashSet attributesToIgnore;
	
	private boolean ignoreText;
	private boolean prettyPrint;
	private boolean textOnly;
	
	private Writer writer;
	
	private int lineLengthMin;
	private int tabStop;
	private int tabIndent;
	
	private String lineSeperator;

	
	
	public HTMLHandler()
	{
		tagsToParse        = new HashSet();
		tagsToIgnore       = new HashSet();
		attributesToParse  = new HashSet();
		attributesToIgnore = new HashSet();

		ignoreText = false;
		
		lineSeperator = System.getProperty("line.separator","\n");
	}
	
	
	
	public void handleStartTag(Tag tag, MutableAttributeSet attributes, int pos)
	{
		try
		{
			if(toBeParsed(tag))
			{
				if(! textOnly) writer.write(tab() + "<" + tag + attributesToString(attributes) + ">" + lineSeperator);
				tabIndent += tabStop;
			}
			else
			{
				ignoreText = true;	
			}
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	
	
	
	public void handleEndTag(Tag tag, int pos)
	{
		try
		{
			if(toBeParsed(tag))
			{
				if(! textOnly) writer.write(tab()+"</"+tag+">" + lineSeperator);
				tabIndent -= tabStop;
			}
			else
			{
				ignoreText = false;	
			}
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	
	
	
	public void handleSimpleTag(Tag tag, MutableAttributeSet attributes, int pos)
	{
		try
		{
			if(toBeParsed(tag))
			{
				if(! textOnly) writer.write(tab() + "<" + tag + attributesToString(attributes) + ">" + lineSeperator);
			}
			else
			{
				ignoreText = true;
			}
			
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	
	
	
	public void handleText(char[] text, int pos)
	{
		try
		{
			if( (! ignoreText) && (text.length >= lineLengthMin) )
			{	
				writer.write(tab());
				writer.write(text);
				writer.write(lineSeperator);
			}
		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(this, exception);
		}
	}
	
	
	
	public void handleError(String tag, int pos)
	{
		
	}
	
	
	
	public void handleEndOfLineString(String eol)
	{
		
	}
	
	
	
	public void setWriter(Writer writer)
	{
		this.writer = writer;	
	}
	
	
	
	public String tab()
	{
		String s="";
		
		if(prettyPrint)
			for(int i=0; i<tabIndent; i++)
				s += " ";

		return s;
	}
	
	
	
	private boolean toBeParsed(Object object)
	{
		if(object instanceof HTML.Tag)
		{
			HTML.Tag tag = (HTML.Tag)object;
			return (tagsToParse.contains(new String("*")) || tagsToParse.contains(tag.toString())) && !(tagsToIgnore.contains(new String("*")) || tagsToIgnore.contains(tag.toString()));
		}
		else if(object instanceof HTML.Attribute)
		{
			HTML.Attribute attribute = (HTML.Attribute)object;
			return (attributesToParse.contains(new String("*")) || attributesToParse.contains(attribute.toString())) && !(attributesToIgnore.contains(new String("*")) || attributesToIgnore.contains(attribute.toString()));
		}
		else
			return false;
	}
	
	
	
	/*
	private boolean hasRequiredAttributes(MutableAttributeSet attributes)
	{
		SimpleAttributeSet set = new SimpleAttributeSet();
		
		set.addAttribute(HTML.Attribute.CLASS, new String("stop-station"));
		
		if(attributes.containsAttributes(set))
			return true;
		else
    			return false;
	}
	*/
	
	
	public String attributesToString(MutableAttributeSet attributes)
	{

		String string = new String();

		for(Enumeration e = attributes.getAttributeNames() ; e.hasMoreElements();)
		{
			Object a = e.nextElement();
        		if(toBeParsed(a))
        			string += " " + a + "=\"" + attributes.getAttribute(a) + "\"";
    		}
    		
		return string;
	}
	
	
	
	/**
	 * Returns a new instance of HTMLHandler, fields are initialized with information from the XML file 'fileName'
	 */
	public static HTMLHandler load(String fileName)
	{
		HTMLHandler htmlHandler = null;
		
		try
		{
			XMLDecoder xmlDecoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(fileName))); 
	     		htmlHandler = (HTMLHandler)(xmlDecoder.readObject());
	     		xmlDecoder.close();
     		}
		catch(Exception exception)
		{
		    	new ExceptionHandler(exception);
		}
     		
     		return htmlHandler;	
	}
	
	
	
	/**
	 *  JavaBean conform setters
	 */
	public void setTagsToParse       (HashSet hashSet) { tagsToParse        = hashSet; }
	public void setTagsToIgnore      (HashSet hashSet) { tagsToIgnore       = hashSet; }
	public void setAttributesToParse (HashSet hashSet) { attributesToParse  = hashSet; }
	public void setAttributesToIgnore(HashSet hashSet) { attributesToIgnore = hashSet; }
	
	public void setLineLengthMin(int integer) { lineLengthMin  = integer; }
	public void setTabIndent    (int integer) { tabIndent      = integer; }
	public void setTabStop      (int integer) { tabStop        = integer; }
	
	public void setPrettyPrint(boolean bool) { prettyPrint = bool; }
	public void setTextOnly   (boolean bool) { textOnly    = bool; }
	 
	 
	 
	/**
	 *  JavaBean conform getters
	 */		
	public HashSet getTagsToParse       () { return tagsToParse;        }
	public HashSet getTagsToIgnore      () { return tagsToIgnore;       }
	public HashSet getAttributesToParse () { return attributesToParse;  }
	public HashSet getAttributesToIgnore() { return attributesToIgnore; }
	
	public int getLineLengthMin() { return lineLengthMin; }
	public int getTabIndent    () { return tabIndent;     }
	public int getTabStop      () { return tabStop;       }
	
	public boolean getPrettyPrint() { return prettyPrint; }
	public boolean getTextOnly   () { return textOnly; }
}