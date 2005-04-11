package ch.ethz.jadabs.amonem.manager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author barbara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PrintDomTree {
	private static String DocName;
	private static FileWriter fileWriter;
	
	/**
	 * this method creates a new file and a corresponding FileWriter. 
	 * 
	 * @param Rootnode of the DOM-tree
	 * @param name of the document where the DOM has to be saved
	 * @return success
	 */
	public static boolean print(Node node, String docName){
		boolean success= true;
		File myFile= new File(docName);
		DocName= docName;
		try {
			myFile.createNewFile();
		} catch (IOException e3) {
			success= false;
		}
		try {
			fileWriter= new FileWriter(DocName);
		} catch (IOException e) {
			e.printStackTrace();
			success= false;
		}
		if (node != null){
			try {
				execute(node);
			} catch (IOException e1) {
				e1.printStackTrace();
				success= false;
			}
		}
		try {
			fileWriter.close();
		} catch (IOException e2) {
			e2.printStackTrace();
			success= false;
		}
		return success;
	}
	
	/**
	 * writes the DOM-tree to the File.
	 * 
	 * @param Rootnode of the DOM-tree
	 * @throws IOException
	 */
	public static void execute(Node node) throws IOException{
		int type= node.getNodeType();
		switch (type)
		{
		case Node.DOCUMENT_NODE:
		{
			execute(((Document)node).getDocumentElement());
			break;
		}
		case Node.ELEMENT_NODE:
		{
			fileWriter.write("<");
			fileWriter.write(node.getNodeName());
			NamedNodeMap attrs = node.getAttributes();
			for( int i =0; i< attrs.getLength(); i++){
				Node attr= attrs.item(i);
				fileWriter.write(" "+attr.getNodeName() +
					"=\"" + attr.getNodeValue() + "\"");
			}
			fileWriter.write(">");
			fileWriter.flush();
			NodeList children= node.getChildNodes();
			if (children != null) {
				int len= children.getLength();
				for (int i=0; i<len; i++){
					execute(children.item(i));
				}
			}
			break;
		}
		
		case Node.ENTITY_REFERENCE_NODE:
		{
			if (node.getNodeName()!=null){
				fileWriter.write("&");
				fileWriter.write(node.getNodeName());
				fileWriter.write(";");
				fileWriter.flush();
			}
			break;
		}
		
		case Node.CDATA_SECTION_NODE:
		{
			if (node.getNodeValue()!=null){
				fileWriter.write("<![CDDATA[");
				fileWriter.write(node.getNodeValue());
				fileWriter.write("]]>");
				fileWriter.flush();
			}
			break;
		}
		
		case Node.TEXT_NODE:
		{
			if (node.getNodeValue()!=null){
				fileWriter.write(node.getNodeValue());
				fileWriter.flush();
			}
			break;
		}
		
		case Node.PROCESSING_INSTRUCTION_NODE:
		{
			if (node.getNodeName()!= null){
				fileWriter.write("<?");
				fileWriter.write(node.getNodeName());
				String data = node.getNodeValue();
				fileWriter.write(" ");
				fileWriter.write(data);
				fileWriter.write("?>");
				fileWriter.flush();
			}
			break;
		}
		}
		if (type == Node.ELEMENT_NODE){
			if (node.getNodeName()!=null){
				fileWriter.write("</");
				fileWriter.write(node.getNodeName());
				fileWriter.write(">");
				fileWriter.write("\n");
				fileWriter.flush();
			}
		}
	}
}
