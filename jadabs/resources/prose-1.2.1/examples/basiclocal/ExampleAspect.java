// $Id: ExampleAspect.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
// =====================================================================
//
// (history at end)
//




// used packages
import ch.ethz.prose.Aspect;
import ch.ethz.prose.crosscut.Crosscut;
import ch.ethz.prose.crosscut.MethodCut;
import ch.ethz.prose.crosscut.REST;
import ch.ethz.prose.filter.PointCutter;
import ch.ethz.prose.filter.Executions;
import ch.ethz.prose.filter.Within;
import ch.ethz.prose.crosscut.MissingInformationException;

/**
 * Class ExampleAspect
 *
 * @version	$Revision: 1.1 $
 * @author	Andrei Popovici
 */
public
class ExampleAspect extends Aspect {

  ExampleCrosscut myCrosscut = new ExampleCrosscut();

  public Crosscut[] crosscuts()
    {

      return new Crosscut[]{myCrosscut};
    }
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
// Revision 1.1  2003/06/10 16:43:31  popovici
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
// Revision 1.1  2003/04/22 16:26:49  andfrei
// added a local test aspect
//
// Revision 1.5  2003/03/13 14:20:25  popovici
// Tools and modifications for a demo with remote clients
//
// Revision 1.4  2003/03/05 12:08:40  popovici
// Deobfuscation of exception filters. They are now
// top level classes. This is just an intermediate refactoring
// step. Many ExceptionS. predicate will be eliminated
// by more powerful Specializers like 'ExecutionS'
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
