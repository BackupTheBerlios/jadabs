package ch.ethz.jadabs.eventsystem.fwevents.impl;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.InitializationException;
import ch.ethz.jadabs.eventsystem.impl.AEvent;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Message;

/**
 * RemoteCommandEvent.
 * 
 * @author jrellermeyer, andfrei
 *
 */
public class RemoteCommandEvent extends AEvent 
{
    private static Logger LOG = Logger.getLogger(RemoteCommandEvent.class.getName());
    
    //---------------------------------------------------
    // Serialization name
    //---------------------------------------------------
    protected static final String COMMAND	= "command";
    
    //---------------------------------------------------
    // instance fields
    //---------------------------------------------------
    public int command = 0;
    private IElement elem_data;
    
    // empty constructor for event instantiation
    public RemoteCommandEvent()
    {
        
    }
    
	public RemoteCommandEvent(int command) {
		super();
		
		this.command = command;
	}
	
	public void setDataElement(IElement element)
	{
	    elem_data = element;
	}
	
	public IElement getDataElement(String name)
	{

		return message.getElement(name);
	}
	
	//---------------------------------------------------
    // De-,Serialization
    //---------------------------------------------------
	
	public IMessage toMessage(Class clas){
	
		IMessage msg = null;
		if (clas != null)
			 msg = super.toMessage(clas);
		else
			msg = super.toMessage(RemoteCommandEvent.class);

		msg.setElement(new Element(COMMAND, command));
		msg.setElement(elem_data);
		
		return (msg);

	}
	
	public void init(IMessage msg) throws InitializationException
	{	
        // set message for later use
        message = msg;
        
        super.init(msg);
        	
//		try {
			command = Message.getElementInt(msg, COMMAND);
			
//		} catch(IOException ioe){
//			throw new InitializationException("could not initialize Event", ioe);
//		}		
	}
}
