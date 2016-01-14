package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.NaryOperator;
import uk.ac.ox.cs.pdq.algebra.Projection;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.UnaryOperator;
import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.plan.EstimateProvider;
 
/**
 * Writes a concise representation of a plan to the given output
 * 
 * @author Julien Leblay
 *
 */
public class PrettyDAGPlanWriter extends PrettyWriter<DAGPlan> implements Writer<DAGPlan> {
	/**
	 * The default out to which plans should be written, if not 
	 * explicitly provided at write time.
	 */
	private final PrintStream out;
	
	/**
	 * 
	 * @param out the default output
	 */
	PrettyDAGPlanWriter(PrintStream out) {
		this.out = out;
	}

	/**
	 * 
	 */
	public PrettyDAGPlanWriter() {
		this(System.out);
	}
	
	/**
	 * Fluent pretty writer provider.
	 * @param out
	 * @return a new PrettyLeftDeepPlanWriter with the given default output.
	 */
	public static PrettyDAGPlanWriter to(PrintStream out) {
		return new PrettyDAGPlanWriter(out);
	}
	
	/**
	 * @param out PrintStream
	 * @param plan DAGPlan
	 */
	@Override
	public void write(PrintStream out, DAGPlan plan) {
		this.write(out, plan.getOperator(), "");
		out.println("Cost: " + plan.getCost());
	}
	
	/**
	 * @param out PrintStream
	 * @param op LogicalOperator
	 * @param prefix String
	 */
	private void write(PrintStream out, RelationalOperator op, String prefix) {
		EstimateProvider md = op.getMetadata();
		Double outputCard = null;
		if (md != null) {
			outputCard = md.getOutputCardinality();
		}
		if (op instanceof Access) {
			out.println(prefix + op.getClass().getSimpleName() + "[" + ((Access) op).getRelation().getName() + "/" + ((Access) op).getAccessMethod().getName()+ "] (" + outputCard + ")");
			this.write(out, ((UnaryOperator) op).getChild(), "\t" + prefix);
			return;
		}
		if (op instanceof DependentAccess) {
			out.println(prefix + op.getClass().getSimpleName() + "[" + ((DependentAccess) op).getRelation().getName() + "/" + ((DependentAccess) op).getAccessMethod().getName()+ "] (" + outputCard + ")");
			return;
		}
		if (op instanceof UnaryOperator) {
			if (op instanceof Projection) {
				out.println(prefix + op.getClass().getSimpleName() + "[" + ((Projection) op).getColumns() + "," + ((Projection) op).getRenaming() + "] (" + outputCard + ")");
				this.write(out, ((UnaryOperator) op).getChild(), "\t" + prefix);
				return;
			}
			if (op instanceof Selection) {
				out.println(prefix + op.getClass().getSimpleName() + "[" + ((Selection) op).getPredicate() + "] (" + outputCard + ")");
				this.write(out, ((UnaryOperator) op).getChild(), "\t" + prefix);
				return;
			}
		}
		if (op instanceof NaryOperator) {
			if (op instanceof Join) {
				out.println(prefix + op.getClass().getSimpleName() + "[" + ((Join) op).getPredicate() + "] (" + outputCard + ")");
			} else {
				out.println(prefix + op.getClass().getSimpleName() + "(" + outputCard + ")");
			}
			for (RelationalOperator child: ((NaryOperator) op).getChildren()) {
				this.write(out, child, "\t" + prefix);
			}
			return;
		}
		out.println(prefix + op);
	}
	
	/**
	 * @param q DAGPlan
	 */
	@Override
	public void write(DAGPlan q) {
		this.write(this.out, q);
	}
}
