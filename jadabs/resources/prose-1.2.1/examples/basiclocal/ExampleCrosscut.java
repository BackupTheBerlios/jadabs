

import ch.ethz.prose.*;
import ch.ethz.prose.crosscut.*;
import ch.ethz.prose.filter.*;

public class ExampleCrosscut extends  MethodCut
{
  // trap Foo.*(..)
  public void METHOD_ARGS(Foo x, String arg1,REST y)
    {
      System.err.println("     ->advice: before "+  x + ".'bar*'("+ arg1 +",..) called");
    }

  // .. && calls(* bar(..))
  protected PointCutter pointCutter()
    { return  (Executions.before() . AND   (Within.method("bar.*")) );}
}
