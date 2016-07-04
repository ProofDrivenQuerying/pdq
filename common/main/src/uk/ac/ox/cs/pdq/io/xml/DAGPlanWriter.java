package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.plan.DAGPlan;

import com.google.common.base.Preconditions;

/**
 * Writes plans to XML.
 * 
 * @author Julien Leblay
 */
public class DAGPlanWriter extends AbstractXMLWriter<DAGPlan> {
	
	/** The Constant TYPE_MARKER. */
	public static final String TYPE_MARKER = "dag"; 
	
	/** The operator writer. */
	private final OperatorWriter operatorWriter;
	
	/**
	 * Default constructor.
	 */
	DAGPlanWriter() {
		this.operatorWriter = new OperatorWriter();
	}

	/**
	 * Writes the given plan to the given output.
	 *
	 * @param out the out
	 * @param plan DAGPlan
	 */
	public void writePlan(PrintStream out, DAGPlan plan) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.TYPE, TYPE_MARKER);
		if (plan != null) {
			att.put(QNames.COST, String.valueOf(plan.getCost()));
		}
		open(out, QNames.PLAN, att);
		this.operatorWriter.writeOperator(out, plan.getOperator());
		close(out, QNames.PLAN);
	}

	/**
	 * Writes plans to XML.
	 *
	 * @param out PrintStream
	 * @param o DAGPlan
	 */
	@Override
	public void write(PrintStream out, DAGPlan o) {
		Preconditions.checkArgument(o != null);
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writePlan(out, o);
	}
}
