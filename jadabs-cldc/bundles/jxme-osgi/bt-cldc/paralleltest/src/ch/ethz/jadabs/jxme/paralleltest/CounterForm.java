/*
 * Created on Jul 30, 2004
 * $Id: CounterForm.java,v 1.1 2004/11/10 10:28:12 afrei Exp $
 */
package ch.ethz.jadabs.jxme.paralleltest;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;


/**
 * This is form contains a text field . 
 * @author muellerr
 */
public class CounterForm extends Form
{
    private TextField counterValue;
    
    
    public CounterForm(CommandListener listener, String title) 
    {
        super("Counter: "+title);
        
        this.append(new StringItem(null, "Counter: "+title));
        counterValue = new TextField("Value", "", 10, TextField.ANY);
        this.append(counterValue);
        setCommandListener(listener);
    }
    
    public void setCounter(int value) {
        counterValue.setString(String.valueOf(value));
    }       

}
