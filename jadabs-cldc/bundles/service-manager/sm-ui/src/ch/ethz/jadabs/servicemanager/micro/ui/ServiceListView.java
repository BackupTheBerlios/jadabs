package ch.ethz.jadabs.servicemanager.micro.ui;


import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;


import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 *
 * @author andfrei
 */

public class ServiceListView extends List
{

//    private Display display;

    /** reference to the main MIDlet */
    private ServiceManagerMIDlet midlet;
    
    private Hashtable services = new Hashtable();
    
    /**
     */
    public ServiceListView(ServiceManagerMIDlet midlet, Command cmds[]) 
    {
        super("Available Services", List.IMPLICIT, new String[]{""}, null);
        
        this.midlet = midlet;
        this.setCommandListener(midlet);
        
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
    }

    public void addService(ServiceReference sref)
    {
        this.append(sref.getName(), null);
    }
  
    
    public void removeService(int index)
    {
        String name = this.getString(index);
        services.remove(name);
        
        this.delete(index);
    }
}
