package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a plan to the given output.
 *
 * @author Julien Leblay
 */
public class PrettyLeftDeepPlanWriter extends PrettyWriter<LeftDeepPlan> implements Writer<LeftDeepPlan> {
	/**
	 * The default out to which plans should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * Instantiates a new pretty left deep plan writer.
	 *
	 * @param out the default output
	 */
	PrettyLeftDeepPlanWriter(PrintStream out) {
		this.out = out;
	}

	/**
	 * Instantiates a new pretty left deep plan writer.
	 */
	public PrettyLeftDeepPlanWriter() {
		this(System.out);
	}
	
	/**
	 * Fluent pretty writer provider.
	 *
	 * @param out the out
	 * @return a new PrettyLeftDeepPlanWriter with the given default output.
	 */
	public static PrettyLeftDeepPlanWriter to(PrintStream out) {
		return new PrettyLeftDeepPlanWriter(out);
	}
	
	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param plan LeftDeepPlan
	 */
	@Override
	public void write(PrintStream out, LeftDeepPlan plan) {
		out.println(plan + ": " + plan.getCost());
	}
	
	/**
	 * Write.
	 *
	 * @param q LeftDeepPlan
	 */
	@Override
	public void write(LeftDeepPlan q) {
		this.write(this.out, q);
	}
}
