package aspects;

import program.ProseSample;
import ch.ethz.prose.DefaultAspect;
import ch.ethz.prose.crosscut.Crosscut;
import ch.ethz.prose.crosscut.MethodCut;
import ch.ethz.prose.filter.*;

public class SampleAspect extends DefaultAspect {

	public Crosscut c1 = new MethodCut() {
		public void METHOD_ARGS(ProseSample target) {
			System.out.println("-> Advice code executed.");
		}
		protected PointCutter pointCutter() {
			return (Executions.before())
					.AND(Within.method("test*"));
		}
	};

}
