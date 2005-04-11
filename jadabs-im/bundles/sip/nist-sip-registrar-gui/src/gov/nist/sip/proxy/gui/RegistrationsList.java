/*
 * BuddyList.java
 *
 * Created on September 25, 2002, 2:46 PM
 */

package gov.nist.sip.proxy.gui;

import gov.nist.sip.proxy.registrar.Registrar;
import gov.nist.sip.proxy.registrar.Registration;
import gov.nist.sip.proxy.registrar.RegistrationListener;
import gov.nist.sip.proxy.registrar.RegistrationsTable;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * 
 * @author deruelle
 * @version 1.0
 */
public class RegistrationsList extends JList implements RegistrationListener
{

    protected DefaultListModel list;

    protected ProxyLauncher proxyLauncher;

    /** Creates new BuddyList */
    public RegistrationsList(ProxyLauncher proxyLauncher)
    {
        this.proxyLauncher = proxyLauncher;
        list = new DefaultListModel();
        this.setModel(list);

        list.addElement("(empty)");
        
        //add registrationlistener to registrationstable
        ProxyGUIActivator.proxyadmin.proxy.getRegistrar().
        	getRegistrationsTable().addRegistrationListener(this);
        
        uploadRegistrations(ProxyGUIActivator.proxyadmin.proxy.getRegistrar());

    }

    public void uploadRegistrations(Registrar registrar)
    {
        try
        {
            RegistrationsTable registrationsTable = registrar.getRegistrationsTable();
            if (registrationsTable != null)
            {
                Hashtable r = registrationsTable.getRegistrations();
                if (r == null || r.size() == 0)
                {
                    list.addElement("(empty)");
                    return;
                }
                Enumeration e = r.keys();
                while (e.hasMoreElements())
                {
                    String key = (String) e.nextElement();
                    list.addElement(key);
                }
            } else
                list.addElement("(empty)");
        } catch (Exception e)
        {
        }
    }

    public void updateRegistration(Registration registration, boolean toRemove)
    {

        System.out.println("called update Registration");
        
        if (registration != null)
        {
            String key = registration.getKey(); // key=="sip:user@domain"
            boolean inList = list.contains(key);

            System.out.println("updReg: list=" + list.toString() + "        inList=" + inList + "        key=" + key);

            //remove (empty)-string if adding
            if ((list.size() == 1) && (!toRemove))
            {
                String s = (String) list.firstElement();
                if (s.equals("(empty)"))
                    list.removeElementAt(0);
            }

            //add non-existing element
            //or remove existing element
            if ((!toRemove) && (!inList))
                list.addElement(key);
            else if (toRemove && inList)
                list.removeElement(key);

            //Add (empty) if necessary
            if (list.isEmpty())
                list.addElement("(empty)");

            System.out.println("updReg: updated list=" + list.toString());
        }

        /*
         * Old code. Causes arrayOutOfBounds when deregistering users Changed by
         * Henrik
         * 
         * if (registration!=null ) { String key=registration.getKey();
         * 
         * Vector contacts = registration.getContactsList();
         * 
         * key = contacts.elementAt(0).toString();
         * 
         * key = key.substring(key.indexOf(" <sip:"));
         * 
         * boolean b=list.contains(key); if (b) { if (toRemove) {
         * list.removeElement(key); if (list.isEmpty())
         * list.addElement("(empty)"); } } else { if (!toRemove) { if
         * (list.size()==1) { String s=(String)list.firstElement(); if
         * (s.equals("(empty)") ) list.removeElement(s); } list.addElement(key); } } }
         */
    }

    public void clean()
    {
        list.removeAllElements();
        list.addElement("(empty)");
        
        ProxyGUIActivator.proxyadmin.proxy.getRegistrar().
    		getRegistrationsTable().removeRegistrationListener(this);
    }

}