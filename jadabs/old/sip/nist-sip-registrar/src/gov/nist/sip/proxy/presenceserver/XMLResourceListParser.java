/*
 * XMLResourceListParser.java
 *  
 */

package gov.nist.sip.proxy.presenceserver;

import java.io.File;
import java.util.Vector;

import org.xml.sax.helpers.DefaultHandler;

public class XMLResourceListParser extends DefaultHandler
{

    private PresentityManager presentityManager;

    public XMLResourceListParser(PresentityManager pm)
    {
        this.presentityManager = pm;
    }

    public Vector getNotifiers(File resourceListFile)
    {
        Vector notifiers = new Vector();

        //parse the file and add all uri:s to vector

        return notifiers;

    }

}

