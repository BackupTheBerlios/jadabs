package ch.ethz.jadabs.bundleLoader;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

import org.kxml2.io.*;
import org.xmlpull.v1.XmlPullParserException;

public class BundleInformation {

	private String bundleName;
	private String bundleGroup;	
	private String bundleVersion;
	private String bundleId;
	private String bundleDescription;
	private String bundleUpdateLocation;
	private String bundleSourceURL;
	private String bundleDocURL;	
	private String bundleChecksum;	
	protected Vector bundleDependencies = new Vector();
	protected String filename;
	private KXmlParser parser;

	/**
	 * 
	 * @param bundle
	 * @param group
	 */
	public BundleInformation(String bundle, String group, String version) {

		parser = new KXmlParser();
		FileReader reader;
		
		try {
			if (BundleLoader.fetchPolicy == BundleLoader.Eager) {
				BundleLoader.loadBundle(bundle, group, version);
			}
			
			filename = BundleLoader.repository + File.separator + group + File.separator + "jars" + File.separator + bundle + "-" + version + ".jar";
			reader = new FileReader(BundleLoader.repository + File.separator + group + File.separator + "obr" + File.separator + bundle + "-" + version + ".obr");
			parser.setInput(reader);
			parseOBR();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			parser=null;
			reader=null;			
		}
		
	}

	/**
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void parseOBR() throws XmlPullParserException, IOException {
		Stack stack = new Stack();
		Vector dependencies = new Vector();

		for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
				.next()) {
			if (type == KXmlParser.START_TAG) {
				if (parser.getName().equals("dependencies")) {
					buildSchedule();
				}
				stack.push(parser.getName());
			}
			if (type == KXmlParser.END_TAG) {
				try {
					stack.pop();
				} catch (Exception e) {
					System.err
							.println("ERROR while parsing, OBR-File not well-formed");
				}
			}
			if (type == KXmlParser.TEXT) {
				if (!parser.getText().trim().equals("")) {
					// DEBUG one line
					//System.out.println("Scope:" + stack + " <" + stack.peek()
					//		+ ">" + parser.getText().trim() + "</"
					//		+ stack.peek() + ">");
						processElement(stack);
				}
			}
		}
		
	}

	/**
	 * 
	 * @param stack
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void processElement(Stack stack) throws XmlPullParserException,
			IOException {		
		if (stack.peek().equals("bundle-name")) {
			this.bundleName = parser.getText().trim();
		} else if (stack.peek().equals("bundle-group")) {
			this.bundleGroup = parser.getText().trim();
		} else if (stack.peek().equals("bundle-description")) {
			this.bundleDescription = parser.getText().trim();
		} else if (stack.peek().equals("bundle-version")) {
			this.bundleVersion = parser.getText().trim();
		} else if (stack.peek().equals("bundle-updatelocation")) {
			this.bundleUpdateLocation = parser.getText().trim();
		} else if (stack.peek().equals("bundle-sourceurl")) {
			this.bundleUpdateLocation = parser.getText().trim();
		} else if (stack.peek().equals("bundle-docurl")) {
			this.bundleDocURL = parser.getText().trim();
		} else if (stack.peek().equals("bundle-checksum")) {
			this.bundleChecksum = parser.getText().trim();
		}
	}
	
	/**
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void buildSchedule() throws XmlPullParserException, IOException {
		
		parser.require(KXmlParser.START_TAG, "", "dependencies");
		parser.next();
		
		while (parser.next() == KXmlParser.START_TAG) {
			parser.require(KXmlParser.START_TAG, "", "bundle");
			parser.next();

			String bundlename = null;
			String bundleversion = null;
			String bundleid = null;
			String bundlegroup = null;

			while (parser.next() == KXmlParser.START_TAG) {
				String tagname = parser.getName();
				parser.next();

				if (tagname.equals("bundle-name")) {
					bundlename = parser.getText();
					// DEBUG one line
					// System.out.println("bundle-name: " + bundlename);
				} else if (tagname.equals("bundle-version")) {
					bundleversion = parser.getText();
					// DEBUG one line
					// System.out.println("bundle-version: " + bundleversion);
				} else if (tagname.equals("bundle-group")) {					
					bundlegroup = parser.getText();
					// DEBUG one line
					// System.out.println("bundle-group: " + bundlegroup);
				}
				if (bundlename != null && bundleversion != null && bundlegroup != null) {
					BundleInformation dependency = new BundleInformation(bundlename, bundlegroup, bundleversion);
					bundleDependencies.add(dependency);
				}
				parser.next();			
				parser.require(KXmlParser.END_TAG, "", tagname);
				parser.next();				
			}
			
			parser.require(KXmlParser.END_TAG, "", "bundle");
			parser.next();
		} 
		
		parser.require(KXmlParser.END_TAG, "", "dependencies");
		
		return;
	}
	
	/**
	 * 
	 */
	public String toString() {
		return this.bundleName + "-" + this.bundleVersion;
	}

	/**
	 * 
	 * @param obj
	 */
	public boolean equals(Object obj) {
		if (obj instanceof BundleInformation) {
			return ((BundleInformation)obj).bundleName.equals(bundleName) && ((BundleInformation)obj).bundleVersion.equals(bundleVersion);
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return bundleName;
	}

	/**
	 * 
	 * @return
	 */
	public String getGroup() {
		return bundleGroup;
	}

	/**
	 * 
	 * @return
	 */
	public String getVersion() {
		return bundleVersion;
	}

}