import ch.ethz.prose.DefaultAspect;
import ch.ethz.prose.crosscut.Crosscut;
import ch.ethz.prose.crosscut.CatchCut;
import ch.ethz.prose.crosscut.REST;
import ch.ethz.prose.filter.Exceptions;
import ch.ethz.prose.filter.Within;
import ch.ethz.prose.filter.PointCutter;
import ch.ethz.prose.crosscut.MissingInformationException;

/**
 * Class ExampleCatchAspect
 *
 * @version	$Revision: 1.1 $
 * @author	Angela Nicoara
 */

public class ExampleCatchAspect extends DefaultAspect
{
	public Crosscut c = new CatchCut()
	{
		public void CATCH_ARGS()
		{
			System.err.println("     ->advice: the exception was caught");
		}
		
		protected PointCutter pointCutter()
		{
			return ( Exceptions.type("TestException3$") );
			//return ( Exceptions.type("TestException.*") .AND (Exceptions.subtypeOf(java.lang.RuntimeException.class) ) );
			//return ( Exceptions.type("TestException.*") .AND (Within.method("baz")) .OR (Within.method("bar")) );
			//return ( Exceptions.type("TestException.*") .AND (Within.method("barbaz")) .AND (Exceptions.subtypeOf(java.lang.RuntimeException.class)) );
			//return ( Exceptions.type("TestException.*") .AND (Exceptions.withMessage("21")) );
			//return ( Exceptions.type("TestException.*") .OR (Within.method("bar|baz")) ); 
			//return ( Exceptions.type("TestException.*") .AND (Within.method("bar|baz")) ); 
			//return ( Exceptions.type("TestException.*") .AND (Within.method("bar")) );			
			//return ( Exceptions.type("TestException.*") );
			//return ( Exceptions.withMessage("21") );
		}
	};
}