// $Id: ExampleAspect.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
// =====================================================================
//
// (history at end)
//



// used packages
import ch.ethz.prose.DefaultAspect;
import ch.ethz.prose.crosscut.Crosscut;
import ch.ethz.prose.crosscut.MethodCut;
import ch.ethz.prose.crosscut.REST;
import ch.ethz.prose.crosscut.ANY;
import ch.ethz.prose.filter.Executions;
import ch.ethz.prose.filter.Within;
import ch.ethz.prose.filter.PointCutter;
import ch.ethz.prose.crosscut.MissingInformationException;

/**
 * Class ExampleAspect
 *
 * @version	$Revision: 1.1 $
 * @author	Andrei Popovici
 */
public
class ExampleAspect extends DefaultAspect
{

    public Crosscut c1 = new MethodCut()
	{
	    // execute advice on executions of the form *.*(String..)
	    public void METHOD_ARGS(ANY x, String arg1,REST y)
	    {
	      System.err.println("     ->advice: before "+  x.getObject() + ".'bar*'("+ arg1 +",..) called");
	    }

	  // .. && calls(* bar(..))
	  protected PointCutter pointCutter()
	    {return (  (Executions.before()) . AND
		       (Within.method("bar.*")) . AND
	               (Within.type("Foo")) );}
	};

}


//======================================================================
//
// $Log: ExampleAspect.java,v $
// Revision 1.1  2004/11/08 07:30:34  afrei
// initial checkin to berlios.de
//
// Revision 1.1  2004/07/09 10:50:37  andfrei
// *** empty log message ***
//
// Revision 1.1.1.1  2003/07/02 15:30:42  apopovic
// Imported from ETH Zurich
//
// Revision 1.1  2003/06/10 16:43:33  popovici
// New basic local and remote examples; old GUI von pschoch obsolete
//
// Revision 1.13  2003/05/05 14:03:14  popovici
// renaming from runes to prose
//
// Revision 1.12  2003/04/27 13:09:03  popovici
// Specializers renamed to PointCutter
//
// Revision 1.11  2003/04/17 15:15:00  popovici
// Extension->Aspect renaming
//
// Revision 1.10  2003/04/17 14:51:16  popovici
// ExceptionS renamed to Exception; method renamings
//
// Revision 1.9  2003/04/17 13:54:33  popovici
// Refactorization of 'ExecutionS' into 'Within' and 'Executions'.
// Method names refer now to 'types'
//
// Revision 1.8  2003/04/17 12:49:31  popovici
// Refactoring of the crosscut package
//  ExceptionCut renamed to ThrowCut
//  McutSignature is now SignaturePattern
//
// Revision 1.7  2003/04/17 12:31:00  popovici
// Fixed problems after refactoring; examples/gui
// still used the old style advices/pointcuts
//
// Revision 1.6  2003/04/17 08:46:40  popovici
// Important functionality additions
//  - Cflow specializers
//  - Restructuring of the MethodCut, SetCut, ThrowCut, and GetCut (they are much smaller)
//  - Transactional capabilities
//  - Total refactoring of Specializer evaluation, which permits fine-grained distinction
//    between static and dynamic specializers.
//  - Functionality pulled up in abstract classes
//  - Uniformization of advice methods patterns and names
//
// Revision 1.5  2003/03/13 14:20:25  popovici
// Tools and modifications for a demo with remote clients
//
// Revision 1.4  2003/03/05 12:08:40  popovici
// Deobfuscation of exception filters. They are now
// top level classes. This is just an intermediate refactoring
// step. Many Exceptions. predicate will be eliminated
// by more powerful Specializers like 'Within'
//
// Revision 1.3  2002/06/07 07:40:23  popovici
// Adapted to the ClasseS/DeclarationS, MethodCut, etc.. refactorization
//
// Revision 1.1  2002/03/27 13:56:39  popovici
// Legal and realease changes:
//  added LEGAL & licenses
//  added profiles/release
//  added programs/* scripts for installation
//  modified project/* files for installation
//
