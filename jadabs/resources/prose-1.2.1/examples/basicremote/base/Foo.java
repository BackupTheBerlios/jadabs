// $Id: Foo.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
// =====================================================================
//
// (history at end)
//

import ch.ethz.prose.*;

// used packages
import java.util.*;
import java.io.*;
import ch.ethz.prose.tools.RemoteProseComponent;
/**
 * Class Foo will be extended by FooAspect
 *
 * @version	$Revision: 1.1 $
 * @author	Andrei Popovici & Angela Nicoara
 */
public class Foo
{
	

    public void baz(String arg1)
    {
	try{
	    System.err.println ("Called: " + this + ".baz(" + arg1 + ")");
	    throw new TestException(); 
	} catch(TestException e) { 
	    System.err.println("Catch for method baz"); 
	} 
    }

    public void bar(String arg1)
    {
	try{
	    System.err.println ("Called: " + this + ".bar(" + arg1 + ")");
	    throw new TestException2("text21 text22");
	} catch(TestException2 e) { 
	    System.err.println("Catch for method bar"); 
	} 
    }

    public void barbaz(String arg1, int count)
    {
	try{
	    System.err.println ("Called: " + this + ".barbaz(" + arg1 + ","  + count +  ")");
	    throw new TestException3(); 
	} catch(TestException3 e) { 
	    System.err.println("Catch for method barbaz"); 
	} 
    }


    public static void main(String[] args) throws Exception
    {
	// load the following classes in JVM
	Class toload;
	toload = TestException.class;   
	toload = TestException2.class;
	toload = TestException3.class;
	
	ProseSystem.startup();
	
	Foo obj = new Foo();
	while(true)
	    {
		try {Thread.currentThread().sleep(3000);}  catch(InterruptedException e){}
		obj.baz("Hello"); System.err.println("\n");
		obj.bar("Sheherazade"); System.err.println("\n");
		obj.barbaz("Nights",1001);
		System.err.println("\n*******************************\n");
	    }
    }

}


//======================================================================
//
// $Log: Foo.java,v $
// Revision 1.1  2004/11/08 07:30:35  afrei
// initial checkin to berlios.de
//
// Revision 1.1  2004/07/09 10:50:37  andfrei
// *** empty log message ***
//
// Revision 1.2  2003/07/03 16:47:35  anicoara
// Added ExceptionCatchExample to the remote examples files
//
// Revision 1.10  2003/05/26 18:32:53  popovici
// Minor corrections
//
// Revision 1.9  2003/05/25 14:44:11  popovici
// Small fixes in non-java classes after refactoring
//
// Revision 1.8  2003/05/05 14:03:14  popovici
// renaming from runes to prose
//
// Revision 1.7  2003/04/17 15:15:01  popovici
// Extension->Aspect renaming
//
// Revision 1.6  2003/03/13 14:20:26  popovici
// Tools and modifications for a demo with remote clients
//
// Revision 1.5  2002/11/28 08:01:33  popovici
// New, independent version added
//
// Revision 1.4  2002/09/23 07:45:47  popovici
// ProseSystem.teardown not needed, since VM DEATH
//
// Revision 1.3  2002/09/21 14:04:35  popovici
// Bug 0000010 fixed. Added 'teardown' procedure
// in the JVMAI, Jikes & JDK prose implementation
//
// Revision 1.2  2002/06/07 07:40:24  popovici
// Adapted to the ClasseS/DeclarationS, MethodCut, etc.. refactorization
//
// Revision 1.1  2002/03/27 13:56:39  popovici
// Legal and realease changes:
//  added LEGAL & licenses
//  added profiles/release
//  added programs/* scripts for installation
//  modified project/* files for installation
//
