package ch.ethz.jadabs.servicemanager.micro.ui;


import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.apache.log4j.Logger;


import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 *
 * @author andfrei
 */

public class ServiceListView extends List
{

    private Logger LOG = Logger.getLogger("serviceListView");

    /** reference to the main MIDlet */
    private ServiceManagerMIDlet midlet;
        
    private boolean none = true;
    
    Hashtable services = new Hashtable();
    
    /**
     */
    public ServiceListView(ServiceManagerMIDlet midlet, Command cmds[]) 
    {
        super("Available Services", List.IMPLICIT, new String[]{"none"}, null);
        
        this.midlet = midlet;
        this.setCommandListener(midlet);
        
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
    }
    
    public void foundService(ServiceReference sref)
    {
        services.put(sref.getName(), sref);
        
        if (none)
        {
            this.set(0,sref.getName(), null);
            none = false;
            LOG.info("b4");
        }
        else
            this.append(sref.getName(), null);
    }
  
    ServiceReference getSelectedServiceReference()
    {
        int idx = getSelectedIndex();
        
        String name = getString(idx);
        
        return (ServiceReference)services.get(name);
    }
    
    public void removedService(ServiceReference sref)
    {
        services.remove(sref.getName());
        
        for (int i = 0; i < size(); i++)
        {
            if (getString(i).equals(sref.getName()))
            {
                delete(i);
                break;
            }
        }
        
        if (size() == 0)
        {
            this.append("none", null);  
            none = true;
        }
        
    }
}
