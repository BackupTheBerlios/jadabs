// $id: Foo.java,v 1.6 2003/03/13 14:20:26 popovici Exp $
// =====================================================================
//
// (history at end)
//


import ch.ethz.prose.*;



// used packages
import java.util.*;
import java.io.*;
/**
 * Class Foo will be extended by FooAspect
 *
 * @version	$Revision: 1.1 $
 * @author	Andrei Popovici
 */
public
class Foo
{
    public void baz(String arg1)
    {
	System.err.println ("Called: " + this + ".baz(" + arg1 + ")");
    }

    public void bar(String arg1)
    {
	System.err.println ("Called: " + this + ".bar(" + arg1 + ")");
    }

    public void barbaz(String arg1, int count)
    {
	System.err.println ("Called: " + this + ".barbaz(" + arg1 + ","  + count +  ")");
    }

    public static void main(String[] args) throws Exception
    {
	ProseSystem.startup();

	ExampleAspect asp = new ExampleAspect();
	ProseSystem.getAspectManager().insert(asp);

	Foo obj = new Foo();
	while(true)
	  {
	    try {Thread.currentThread().sleep(3000);}  catch(InterruptedException e){}
	    obj.baz("Hello"); System.err.println("\n\n");
	    obj.bar("Sheherazade"); System.err.println("\n\n");
	    obj.barbaz("Nights",1001);
	  }
    }

}


//======================================================================
//
// $Log: Foo.java,v $
// Revision 1.1  2004/11/08 07:30:34  afrei
// initial checkin to berlios.de
//
// Revision 1.1  2004/07/09 10:50:37  andfrei
// *** empty log message ***
//
// Revision 1.1.1.1  2003/07/02 15:30:42  apopovic
// Imported from ETH Zurich
//
// Revision 1.1  2003/06/10 16:43:32  popovici
// New basic local and remote examples; old GUI von pschoch obsolete
//
// Revision 1.4  2003/05/14 08:43:18  andfrei
// changed local example to package local
//
// Revision 1.3  2003/05/05 14:02:47  popovici
// renaming from runes to prose
//
// Revision 1.2  2003/04/27 13:08:56  popovici
// Specializers renamed to PointCutter
//
// Revision 1.1  2003/04/22 16:26:50  andfrei
// added a local test aspect
//
// Revision 1.6  2003/03/13 14:20:26  popovici
// Tools and modifications for a demo with remote clients
//
// Revision 1.5  2002/11/28 08:01:33  popovici
// New, independent version added
//
// Revision 1.4  2002/09/23 07:45:47  popovici
// ExtensionSystem.teardown not needed, since VM DEATH
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
