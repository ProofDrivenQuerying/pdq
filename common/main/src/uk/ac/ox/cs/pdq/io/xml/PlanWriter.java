package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.base.Preconditions;

/**
 * Writes plans to XML.
 * 
 * @author Julien Leblay
 */
public abstract class PlanWriter {
	
	public static PlanWriterFactory to(PrintStream out) {
		Preconditions.checkArgument(out != null);
		return new PlanWriterFactory(out);
	}
	
	public static void write(Plan p) {
		Preconditions.checkArgument(p != null);
		new PlanWriterFactory(System.out).write(p);
	}
	
	public static class PlanWriterFactory {

		/** Print stream to write to */
		private final PrintStream out;

		private PlanWriterFactory() {
			this(System.out);
		}

		private PlanWriterFactory(PrintStream out) {
			this.out = out;
		}
		
		public void write(Plan plan) {
			if (plan instanceof LeftDeepPlan) {
				new LeftDeepPlanWriter().write(out, (LeftDeepPlan) plan);
				return;
			}
			new DAGPlanWriter().write(out, (DAGPlan) plan);
		}
	}
}
