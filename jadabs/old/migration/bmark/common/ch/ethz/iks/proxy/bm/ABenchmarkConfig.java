package ch.ethz.iks.proxy.bm;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.jadabs.ComponentManager;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.proxy.bm.invoke.BenchmarkEmptyMethodInvocation;
import ch.ethz.iks.proxy.bm.invoke.BenchmarkExternalArgumentInvocation;
import ch.ethz.iks.proxy.bm.invoke.BenchmarkReturnInternalObject;
import ch.ethz.iks.proxy.bm.invoke.BenchmarktoStringInvocation;
import ch.ethz.iks.proxy.bm.invoke.BenchmarktoXMLStringInvocation;
import ch.ethz.iks.proxy.bm.invoke.IBenchmarkCase;

/**
 * Benchmarking the proxy implementation
 * Use this class as client component that uses the components referenced by the benchmark cases,
 * e.g. Class-Path testcop.jar for case BenchmarkEmptyMethodInvocation
 * Extend this class to implement the various configurations being compared (e.g. without proxy, transparent proxy, dynamic proxy)
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public abstract class ABenchmarkConfig implements IComponent {

	ABenchmarkConfig() {
		super();
	}

	private static int runCount = 3;
	private int curRun = 0;
	protected String invocationTarget;
	protected String bmName;
	protected static Logger bmLOG = Logger.getLogger("ProxyBenchmark");
	protected String caseName;
	long avgTotal = 0L;
	private Vector bmarks = new Vector();

	public void addBenchmark(IBenchmarkCase bm) {
		this.bmarks.add(bm); 
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#initComponent()
	 */
	public void initComponent() {
		//todo enclosing table to print benchmarks side by side instead of among each other
		String bmHeader = "<table border=3 cellpadding=5>\n<tr><th colspan=3>invocation performance (in millis)</th></tr>";
		bmHeader += "<tr><td colspan=3>" + bmName + "</td></tr>\n";
		bmLOG.info(bmHeader);
		this.addBenchmark(new BenchmarkEmptyMethodInvocation()); // stops thread of tcm
		this.addBenchmark(new BenchmarkExternalArgumentInvocation());
		this.addBenchmark(new BenchmarkReturnInternalObject());
		this.addBenchmark(new BenchmarktoXMLStringInvocation());
		//this.addBenchmark(new BenchmarkPublishInvocation()); 
		
		this.addBenchmark(new BenchmarktoStringInvocation());// NOT available with dyn proxies config 
		
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {
		if (args != null && args.length > 0) {
			try {
				int userDefRunCount = Integer.parseInt(args[0]);
				if (userDefRunCount <= 0) {
					throw new NumberFormatException("do not accept non-positive values as " + userDefRunCount);
				}
				runCount = userDefRunCount;
			} catch (NumberFormatException nan) {
				System.err.println(
					"Please provide a valid and positive integer as first argument or nothing at all (default="
						+ runCount
						+ ")"
				);
			}
		}
		Thread benchmark = new Thread() {
			public void run() {
				this.setName("benchmarker");
				System.out.println("************************ \n"
				                  +"* Benchmarking started * \n"				                  
				                  +"  now = "+System.currentTimeMillis()
				                  +"\n************************"
				);
				try {
					Thread.sleep(3000);
					ComponentManager.Instance().getComponentLoader().stopLoader();//suspend();
					String bmHeader =
						"<tr><td colspan=3>"
							+ runCount
							+ " runs per case, "		
							+ IBenchmarkCase.LOOP_COUNT
							+ " loops per run</td></tr>\n";
					bmHeader += "<tr><td>case</td><td>total</td><td>per invocation</td></tr>\n";
					bmLOG.info(bmHeader);
					
				} catch (InterruptedException e) {
					stopComponent();
				}
				
				Iterator cases = bmarks.iterator();
				while(cases.hasNext() ) {
					doBenchmark((IBenchmarkCase) cases.next());
				}
				
				ComponentManager.Instance().getComponentLoader().startLoader(5000);//resume();
				System.out.println(
								  "************************** \n"
								  +"* Benchmarking completed * \n"
								  +"  now = "+System.currentTimeMillis()
								  +"\n**************************"
								  
				);
				stopComponent();
			}
		};
		benchmark.start();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#stopComponent()
	 */
	public void stopComponent() {
		avgTotal = avgTotal / runCount;
		double avgPerRun = (double) avgTotal / (double) IBenchmarkCase.LOOP_COUNT;
		DecimalFormat format = new DecimalFormat("###0.0000000");
		FieldPosition f = new FieldPosition(NumberFormat.FRACTION_FIELD);
		StringBuffer s = new StringBuffer();
		String summary =
		/*	"\n<tr><td><b>Average (cases "
				+ 0
				+ ".."
				+ (runCount - 1)
				+ "):</b></td><td><b>"
				+ avgTotal
				+ "</b></td><td><b>"
				+ format.format(avgPerRun, s, f)
				+ */ "</b></td></tr>\n</table>";
		bmLOG.info(summary);

	}

	private void doBenchmark(IBenchmarkCase bmark) {
		prepareCase(bmark);
		bmark.measure(); // do not count this run, avoids that lazy creation effects (e.g. of hidden object) fudge the benchmark
		for (int i = 0; i < runCount; i++) {
			long duration = bmark.measure();
			avgTotal += duration;
			++curRun;
			String caseOutput = format(duration, bmark);
			bmLOG.info(caseOutput);
			System.out.println("Completed "+(i+1)+". run of benchmark case "+bmark.getCaseName()+" in configuration "+this.bmName+"\n"+caseOutput);
		}
	}
	
	public void prepareCase(IBenchmarkCase bmark) {
		ComponentManager.Instance().getComponentLoader().suspend();
		bmark.prepare();
	}
	
	private String format(long duration, IBenchmarkCase bmark) {
		double perRun = (double) duration / (double) IBenchmarkCase.LOOP_COUNT; // fp division!
		DecimalFormat format = new DecimalFormat("###0.0000000");
		FieldPosition f = new FieldPosition(NumberFormat.FRACTION_FIELD);
		StringBuffer s = new StringBuffer();
		return "<tr><td>"
			+ curRun
			+ ". "
			+ bmark.getCaseName()
			+ "</td><td>"
			+ duration
			+ "</td><td>"
			+ format.format(perRun, s, f)
			+ "</td></tr>\n";
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
		ComponentManager.Instance().getComponentLoader().resume();
	}
}
