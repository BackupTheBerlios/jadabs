/*
 * Created on 27.07.2005
 *
 */
package ch.ethz.jadabs.pluginLoader;

import java.util.Iterator;

import ch.ethz.jadabs.http.PageHandler;


public class PluginLoaderPageHandler implements PageHandler
{

    public String getContent(String url)
    {
        
        return getPluginsHTMLFormat();
    }
    
    private String getPluginsHTMLFormat()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("<p>&nbsp;</p>");
        sb.append(        
                "<table border=1 cellpadding=0 cellspacing=0 style=\"border-collapse: collapse\" align=center bordercolor=#111111 width=46% id=\"AutoNumber1\">"
                + "<tr>"
                + "<td width=44% bgcolor=#808080 align=left><font color=#FFFFFF>Plugin</td>"
                + "</tr>\n"
        );

        // loop over installed plugins
        Iterator itopds = PluginLoaderActivator.ploader.getInstalledPlugins();
        for (;itopds.hasNext();)
        {
            String opduuid = (String)itopds.next();
            
            sb.append(
                    "<tr>" + 
                    "<td>"+ opduuid +"</td>" + 
                    "</tr>\n"
                    );
        }                        
        sb.append("</table>\n\n");
        sb.append("<p>&nbsp;</p>");
            
        return sb.toString();
    }
    
}
