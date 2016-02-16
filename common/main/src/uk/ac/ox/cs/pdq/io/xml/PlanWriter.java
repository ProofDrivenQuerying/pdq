package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Writes plans to XML.
 * 
 * @author Julien Leblay
 */
public abstract class PlanWriter {
	
	/**
	 * Sets the output stream.
	 *
	 * @param out the out
	 * @return the plan writer factory
	 */
	public static PlanWriterFactory to(PrintStream out) {
		Preconditions.checkArgument(out != null);
		return new PlanWriterFactory(out);
	}
	
	/**
	 * Writes the given plan to the last set output stream.
	 *
	 * @param p the p
	 */
	public static void write(Plan p) {
		Preconditions.checkArgument(p != null);
		new PlanWriterFactory(System.out).write(p);
	}
	
	/**
	 * A factory for creating PlanWriter objects.
	 */
	public static class PlanWriterFactory {

		/**  Print stream to write to. */
		private final PrintStream out;

		/**
		 * Instantiates a new plan writer factory.
		 */
		private PlanWriterFactory() {
			this(System.out);
		}

		/**
		 * Instantiates a new plan writer factory.
		 *
		 * @param out the out
		 */
		private PlanWriterFactory(PrintStream out) {
			this.out = out;
		}
		
		/**
		 * Writes the given plan to the last set output stream.
		 *
		 * @param plan the plan
		 */
		public void write(Plan plan) {
			if (plan instanceof LeftDeepPlan) {
				new LeftDeepPlanWriter().write(out, (LeftDeepPlan) plan);
				return;
			}
			new DAGPlanWriter().write(out, (DAGPlan) plan);
		}
	}
}
