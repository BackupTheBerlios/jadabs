/* $Id: JxmeChat.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */

package ch.ethz.jadabs.jxme.chat.j2se;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.EndpointService;

/**
 * Jxme-Chat application for J2SE. 
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class JxmeChat extends JFrame implements ChatListener {

   /** Log4j logger get logger for debug output */
   static Logger LOG = Logger.getLogger(JxmeChat.class.getName());
   
   /** window size */
   static final Dimension WINDOW_SIZE = new Dimension(600, 400);
   
   /** local name */
   private String localName = null;
   
   /** network layer for the chat application */
   public ChatCommunication chat;

   /** text field where the user enters its chat message */  
   public JTextField messageField;

   /** text area that contains the activities within the chat */
   public JTextArea chatLog;
   
   /** EndpointService used in the chat application */
   private EndpointService endptsvc;   
   
   /** 
    * Constructor of the JxmeChat
    * @param endptsvc EndpointService that is used for communication
    */
   public JxmeChat(EndpointService endptsvc) {
      super("J2SE -- Blue Chat");
      
      this.endptsvc = endptsvc;
      this.setSize(WINDOW_SIZE);
      this.getContentPane().setLayout(new BorderLayout(5, 5));
      messageField = new JTextField(40);
      chatLog = new JTextArea("", 20, 40);
      messageField.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            /* send message */
            chat.sendMessage(messageField.getText());
            appendChatMessage(localName+": "+messageField.getText());
         }
      });
      chatLog.setEditable(false);
      chatLog.setAutoscrolls(true);

      this.getContentPane().add(messageField, BorderLayout.NORTH);
      this.getContentPane().add(new JScrollPane(chatLog),
            BorderLayout.CENTER);

      JPanel bttnPanel = new JPanel();
      JPanel listPanel = new JPanel();      
      JButton bttnSend = new JButton("Send");
      JButton bttnExit = new JButton("Exit");
      bttnSend.setMnemonic(KeyEvent.VK_S);
      bttnExit.setMnemonic(KeyEvent.VK_E);
      bttnExit.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            shutdown();
         }
      });
      bttnSend.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            /* send message */
            chat.sendMessage(messageField.getText());
            appendChatMessage(localName+": "+messageField.getText());
         }
      });
      bttnPanel.setLayout(new FlowLayout());
      bttnPanel.add(bttnSend);
      bttnPanel.add(bttnExit);
      this.getContentPane().add(bttnPanel, BorderLayout.SOUTH);
      
      // add window lister that shuts down application on closing
      this.addWindowListener(new WindowAdapter() {

         public void windowClosing(WindowEvent e) {
            shutdown();
         }
      });
      
      while (localName == null) {
          localName = JOptionPane.showInputDialog("Your Name");
      }
      start();
      this.setVisible(true);
   }
      
   /**
    * Starts the Chat 
    */
   public void start() {                          
       if (LOG.isDebugEnabled()) {
           LOG.debug("start(): starting chat");
       }
       // setup chat communication and register this service */
       chat = new ChatCommunication(this, endptsvc);
       endptsvc.addListener("jxmechat", chat);         
       chat.enterChat(localName);
   }
   
   /**
    * BlueChat chat action handler gets called when some thing 
    * happens in chat. 
    * 
    * @param event
    *            must be <code>EVENT_JOIN</code>, <code>EVENT_LEAVE</code>,
    *            <code>EVENT_RECEIVED</code> or <code>EVENT_SENT</code>
    * @param param String parameter of the action: 
    *            if <code>EVENT_JOIN</code> or </code>EVENT_LEAVE</code>
    *            then param is equal to the nickname, if <code>EVENT_SENT</code>
    *            or <code>EVENT_RECEIVED</code> then param is equal to 
    *            "nickname: message"
    */
   public void handleAction(String event, String param)
   {
       if (LOG.isDebugEnabled()) {
           LOG.debug("invoke handleAction. action=" + event);
       }

       if (event.equals(ChatListener.EVENT_JOIN))
       {
           // a new user has join the chat room
           appendChatMessage(param+" joins the chat room");
       } else if (event.equals(ChatListener.EVENT_SENT))
       {
           // nothing to do
       } else if (event.equals(ChatListener.EVENT_RECEIVED))
       {
           // a new message has received from a remote user
           // render this message on screen
           appendChatMessage(param);

       } else if (event.equals(ChatListener.EVENT_LEAVE))
       {
           // a user has leave the chat room
           appendChatMessage(param+" leaves the chat room");
       }
   }

   /**
    * Add the specified message to the chat log. 
    * @param msg Message to add.
    */
   public void appendChatMessage(String msg) {
      chatLog.append(msg+"\n");
      chatLog.setCaretPosition(chatLog.getText().length());
   }
   
   /**
    * Shuts down the chat application 
    */
   public void shutdown() {
      chat.leaveChat();       
      endptsvc.removeListener("jxmechat");
      this.dispose();
   }
}