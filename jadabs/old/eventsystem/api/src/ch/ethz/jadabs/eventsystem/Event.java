/*
 * Created on Jun 2, 2003
 *
 * $Id: Event.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem;

import ch.ethz.jadabs.jxme.ID;


/**
 * AEvent is an abstract class which basically can be used to
 * ch.ethz.iks.jadabs.evolution a basic type.
 * 
 * 
 * @author andfrei
 *  
 */
public interface Event
{

    public ID getID();

//    /**
//     * Add an attribute Name = Value Pair to the MidasEvent.
//     * 
//     * @param name
//     * @param value
//     */
//    public void addAttribute(Object name, Object value);
//
//    /**
//     * Return Value for the given Attribute Name.
//     * 
//     * @param name
//     * @return
//     */
//    public Object getAttributeValue(Object name);
//
//    /**
//     * Remove Attribute with given name.
//     * 
//     * @param name
//     */
//    public void removeAttributeValue(Object name);

}