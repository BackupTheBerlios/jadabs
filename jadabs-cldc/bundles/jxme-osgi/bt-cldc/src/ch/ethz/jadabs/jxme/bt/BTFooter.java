/*
 * $Id: BTFooter.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */

/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTFooter.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 * @author Ren&eacute; M&uuml;ller, muellren[at]student.ethz.ch
 *
 * Jul 9, 2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * Adaptations by Ren&eacute; M&uul;ller 
 * for semester work summer 2004
 * "JXME-Bluetooth for a Mobile Phone (J2ME/CLDC)"   
 */
package ch.ethz.jadabs.jxme.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** 
 * This class is representing the footer of packages. A footer does 
 * not contain any data (up to now, but it might be extended in the 
 * future) it only consists of an EPILOG <code>'----'</code>.
 *  
 * It is simply used to read a control sequence (epilog) checking if the given 
 * length of the message is correct.
 * 
 * <p>Message format <code>BT_FOOTER</code>:</b>
 * <table border="1">
 * 	<tr><th>Offset</th> <th>Field</th> <th>Type</th> <th>Length</th> 
 *        <th>Description</th></tr>
 *    <tr><td>0</td> <td><code>EPILOG</code></td> <td><code>char</code></td> 
 *        <td>4 bytes</td> <td>synchronisation sequence <code>'----'</code></td></tr>
 * </table>  
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 * @author Ren&eacute; M&uuml;ller, muellren[at]student.ethz.ch 
 */
public class BTFooter {

    /** sequence marking the footer of a message. */
    public static final String EPILOG = "----";
    
    /** 
     * so far the footer is always the same, thus we can make use
     *  of a single instance (singleton pattern)
     */
    private static BTFooter singleton;
	
    /** 
	  * writes the <code>EPILOG</code> to the given 
	  * <code>java.io.OutputStream</code>.
	  * 
	  * @param out data output stream to write to
	  * @throws IOException in case that the underlying OutputStream
	  *         throws a corresponding exception 
	  */
    public void  writeFooter(OutputStream out) throws IOException{
        out.write(EPILOG.getBytes());
    }
	
	 /** 
	  * Reads the <code>BT_FOOTER</code>. Precondition: If next byte
	  * that is read must be the first byte of the epilog <code>'-'</code>.
	  * If the data does not match the specified format of the footer
	  * an null reference is returned.
	  *    
     * @param in <code>InputStream</code> to read footer data from
     * @return footer instance of <code>null</code> if the footer does not 
     *         match the format from the specification. 
	  * @throws IOException if the underlying <code>InputStream</code>
	  *         throws such an exception 
	  */
    public static BTFooter readFooter(InputStream in) throws IOException{
        
        // read the epilog of the footer
        for( int i = 0; i < EPILOG.length(); i++ ){
            if (in.read() != EPILOG.charAt(i)) {
                // invalid epilog
                return null;
            }
        }
        
        // so far there is no more data in the footer thus just return 
        // en empty instance.        
        return new BTFooter();
    }
    
    /**
     * Get singleton footer. 
     * 
     * So far there is only only one footer (i.e. it is not data
     * depenend) thus we can use a singleton here.
     * 
     * @return the singleton instance of the BTFooter
     */
    public static BTFooter getFooter() 
    {
        if (singleton == null) {
            singleton = new BTFooter();
        }
        return singleton;
    }
}