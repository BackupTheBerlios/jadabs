/*
 * Created on Aug 4, 2004
 *
 * $Id: SettingsForm.java,v 1.1 2005/02/13 12:46:43 afrei Exp $
 */
package ch.ethz.jadabs.servicemanager.micro.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;


/**
 * This class implements the setting screen when the user can
 * enter the receiver's phone number. 
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class SettingsForm extends Form 
{
    /** reference to the main MIDlet */
    private ServiceManagerMIDlet midlet;
    
    /** the phone number text field */
    private TextField phoneField;    
    
    /** object representing the choice group */
//    private ChoiceGroup sendOptions;
    
    /**
     * Create new form for message settings 
     * @param midlet parent MIDlet where this form is integrated into
     * @param cmds array with commands that can be triggered from this screen
     */
    public SettingsForm(ServiceManagerMIDlet midlet, Command cmds[]) 
    {
        super("Settings");
        
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
        
        // build-up form
        phoneField = new TextField("to phonenumber:", "", 12, 
                                             TextField.PHONENUMBER); 
        this.append(phoneField);
        
//        sendOptions = new ChoiceGroup("use service:", 
//                       ChoiceGroup.EXCLUSIVE);
//        sendOptions.append("WMA (local)", null);
//        sendOptions.append("SMTP (Bluetooth)", null);
//        this.append(sendOptions);    
    }  
    
    /** 
     * Return the entered phone number
     * @return String representing the entered phone number
     */
    public String getPhoneNumber() 
    {
        return phoneField.getString();
    }
    
    /** 
     * Return true if the use chose to use the SMTPGateway to send
     * the message with
     * 
     * @return true if the user chose to use the SMTPGateway, false
     * if the MWA sould be used, i.e. the message is sent directly by
     * the mobile phone. 
     */
//    public boolean useSMTP()
//    {
//        return (sendOptions.getSelectedIndex() == 1);
//    }
}
