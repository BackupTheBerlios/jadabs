/*
 * HelpBox.java
 *
 * Created on April 15, 2002, 10:55 AM
 */

package gov.nist.sip.instantmessaging;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 
 * @author deruelle
 * @version 1.0
 */
public class HelpBox extends Dialog
{

    TextArea helpTextArea;

    Button ok;

    //    File helpFile;

    /** Creates new HelpBox */
    public HelpBox()
    {
        super(new Frame(), " IM Help ", false);
        try
        {

            //helpFile = new
            // File("./configuration/gov/nist/sip/instantmessaging/configuration/help.txt")
            // ;

            //            String helpfilestr =
            //                "bundle://" + InstantMessagingGUI.bc.getBundle().getBundleId() +
            // ":0" +
            //                "/gov/nist/sip/instantmessaging/configuration/help.txt";

            InputStream inhelpfile = getClass().getResourceAsStream(
                    "/gov/nist/sip/instantmessaging/configuration/help.txt");

            //            helpFile = new File(new java.net.URI(helpfilestr));

            this.setLayout(new BorderLayout());
            this.setBackground(Color.lightGray);
            helpTextArea = new TextArea();
            helpTextArea.setBackground(Color.white);
            helpTextArea.setEditable(false);

            Color color = helpTextArea.getBackground();
            //DebugIM.println("color:"+color.equals(Color.white));

            ok = new Button(" Ok ");
            ok.setBackground(Color.lightGray);
            ok.setForeground(Color.black);
            this.add(helpTextArea, BorderLayout.CENTER);
            this.add(ok, BorderLayout.SOUTH);
            ok.addMouseListener(new MouseAdapter()
            {

                public void mouseClicked(MouseEvent e)
                {
                    setVisible(false);
                    dispose();
                }
            });
            this.addWindowListener(new WindowAdapter()
            {

                public void windowClosing(WindowEvent e)
                {
                    setVisible(false);
                    dispose();
                }
            });
            // width, height
            this.setSize(400, 400);

            // fill the help box.

            //            BufferedReader buffReader =
            //            new BufferedReader(
            //            new InputStreamReader(
            //            new FileInputStream(helpFile))) ;
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(inhelpfile));

            String line = null;

            while ((line = buffReader.readLine()) != null)
                helpTextArea.append(line + "\n");

        } catch (Exception e)
        {
            DebugIM.println("DebugIM, Problem while opening the file ./configuration/help.txt");
            e.printStackTrace();
        }
    }

}