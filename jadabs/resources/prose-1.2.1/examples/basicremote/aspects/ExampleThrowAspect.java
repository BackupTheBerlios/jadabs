import ch.ethz.prose.DefaultAspect;
import ch.ethz.prose.crosscut.Crosscut;
import ch.ethz.prose.crosscut.ThrowCut;
import ch.ethz.prose.crosscut.REST;
import ch.ethz.prose.filter.Exceptions;
import ch.ethz.prose.filter.Within;
import ch.ethz.prose.filter.PointCutter;
import ch.ethz.prose.crosscut.MissingInformationException;

/**
 * Class ExampleThrowAspect
 *
 * @version	$Revision: 1.1 $
 * @author	Angela Nicoara
 */

public class ExampleThrowAspect extends DefaultAspect
{
	public Crosscut c = new ThrowCut()
	{
		public void THROW_ARGS()
		{
			System.err.println("     ->advice: throw an exception");
		}
		
		protected PointCutter pointCutter()
		{
			return ( Exceptions.type("TestException2$") );
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