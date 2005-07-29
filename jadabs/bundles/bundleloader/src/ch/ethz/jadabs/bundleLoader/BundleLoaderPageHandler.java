/*
 * Created on 27.07.2005
 *
 */
package ch.ethz.jadabs.bundleLoader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import ch.ethz.jadabs.http.PageHandler;


public class BundleLoaderPageHandler implements PageHandler
{

    public BundleLoaderPageHandler()
    {
        BundleLoaderActivator.nanoHttpD.registerPageHandler("System", this);
        BundleLoaderActivator.nanoHttpD.registerPageHandler("Bundles", this);
    }
    
    public String getContent(String url)
    {        
        StringTokenizer st = new StringTokenizer(url,"/");
        
        String token = st.nextToken();
        if (token.equals("System"))
            return getSystemContent();
        else if (token.equals("Bundles"))
            return getBundlesHTMLFormat();
        else return "";
    }

    private String getSystemContent()
    {
        StringBuffer sb = new StringBuffer();
        
        // Ttable-Header
        sb.append("<p>&nbsp;</p>");
        sb.append( "<table border=1 cellpadding=0 cellspacing=0 style=\"border-collapse: collapse\" bordercolor=#111111 width=80% align=center id=\"AutoNumber1\" >\n\n"
                    + "<tr>"  + 
                       "<td width=50% bgcolor=#808080 align=left colspan=2><font color=#FFFFFF>System Properties</td>"+
                       "</tr>\n"
                    );
        
        long uptime = System.currentTimeMillis() - BundleLoaderActivator.startTime;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        Calendar cal = Calendar.getInstance();
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        // System-Properties
        Runtime rt = Runtime.getRuntime();
        // Runtime
        sb.append("<tr><td colspan=2>"+ "&nbsp;" +"</td></tr>\n");
        sb.append("<tr><td colspan=2>"+ "Runtime" +"</td></tr>\n");
        sb.append("<tr><td>"+ "Time" +"</td><td>"+ new Date() +"</td></tr>\n");
        sb.append("<tr><td>"+ "Up-Time" +"</td><td>"+ dateFormat.format(new Date(uptime)) +"</td></tr>\n");
        sb.append("<tr><td>"+ "Total Memory [bytes]" +"</td><td>"+ rt.totalMemory() +"</td></tr>\n");
        sb.append("<tr><td>"+ "Free Memory [bytes]" +"</td><td>"+ rt.freeMemory() +"</td></tr>\n");
//        sb.append("<tr><td>"+ "Max Memory [bytes]" +"</td><td>"+ rt.maxMemory() +"</td></tr>\n");
        sb.append("<tr><td>"+ "Threads" +"</td><td>"+ Thread.activeCount() +"</td></tr>\n");
        
        // Platform
        sb.append("<tr><td colspan=2>"+ "&nbsp;" +"</td></tr>\n");
        sb.append("<tr><td colspan=2>"+ "Platform" +"</td></tr>\n");
        sb.append("<tr><td>"+ "OS-Architecture" +"</td><td>"+ System.getProperty("os.arch") +"</td></tr>\n");
        sb.append("<tr><td>"+ "OS-Name" +"</td><td>"+ System.getProperty("os.name") +"</td></tr>\n");
        sb.append("<tr><td>"+ "Java-Vendor" +"</td><td>"+ System.getProperty("java.vendor") +"</td></tr>\n");
        sb.append("<tr><td>"+ "Java-Version" +"</td><td>"+ System.getProperty("java.version") +"</td></tr>\n");
        
        // Architecture
        
        
        // Table-Footer
        sb.append("</table>\n\n");
        sb.append("<p>&nbsp;</p>");
        
        return sb.toString();        
    }
    
    private String getBundlesHTMLFormat()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("<p>&nbsp;</p>");
        sb.append( "<table border=1 cellpadding=0 cellspacing=0 style=\"border-collapse: collapse\" align=center bordercolor=#111111 width=46% id=\"AutoNumber1\">\n\n"
                    + "<tr>"  + 
                       "<td width=44% bgcolor=#808080 align=left><font color=#FFFFFF>Bundle</td>"+
                       "</tr>\n"
                    );
// loop over installed bundles
        Iterator itbds = BundleLoaderActivator.bundleLoader.getInstalledBundles();
        for (;itbds.hasNext();)
        {
            String bdluuid = (String)itbds.next();
            
            sb.append(
              "<tr>" + 
              "<td>"+ bdluuid +"</td>" + 
              "</tr>\n"
              );
        }                        
        sb.append("</table>\n\n");
        sb.append("<p>&nbsp;</p>");
        
        return sb.toString();
    }
    
    public String getBundlesXMLFormat()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<Bundles>\n");
        
        Bundle[] bundles = BundleLoaderActivator.bc.getBundles();
                
        for (int i = 0; i < bundles.length; i++)
        {
            sb.append(emitTagOnly("Bundle", 1));
            Bundle bundle = bundles[i];
            long id = bundle.getBundleId();
            String bname = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
            sb.append(emitTag("Bundle-Name",bname,2));
            
            sb.append(emitTagOnly("/Bundle", 1));
        }
        
        sb.append("</Bundles>\n");
        return sb.toString();
    }
    
    
    private static String emitTag(String tagName, String content, int level) {
        String leveller = new String();
        for (int i = 0; i < level; i++) {
            leveller = leveller + "\t";         
        }
        
        return leveller + "<" + tagName + ">" + content + "</" + tagName  + ">\n";
    }
    
    private static String emitTagOnly(String tagName, int level) {
        String leveller = new String();
        for (int i = 0; i < level; i++) {
            leveller = leveller + "\t";         
        }
        
        return leveller + "<" + tagName + ">\n";
    }
}
