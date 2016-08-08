package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;

/**
 * Writes plans to XML.
 * 
 * @author Julien Leblay
 * 
 */
public class LeftDeepPlanWriter extends AbstractXMLWriter<LeftDeepPlan> {

	/** The Constant TYPE_MARKER. */
	public static final String TYPE_MARKER = "linear"; 

	/** The Constant SUBPLAN_ALIAS. */
	public static final String SUBPLAN_ALIAS = "T"; 

	/**  Relations writer. */
	private OperatorWriter operatorWriter = null;
	
	/**
	 * Default constructor.
	 */
	LeftDeepPlanWriter() {
		this.operatorWriter = new OperatorWriter();
	}

	/**
	 * Writes the given plan to the given output.
	 *
	 * @param out the out
	 * @param plan LeftDeepPlan
	 */
	public void writePlan(PrintStream out, LeftDeepPlan plan) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.TYPE, TYPE_MARKER);
		if (plan != null) {
			att.put(QNames.COST, plan.getCost().toString());
			open(out, QNames.PLAN, att);
			int i = 1;
			Map<RelationalOperator, String> aliases = new LinkedHashMap<>();
			for (LeftDeepPlan step: plan) {
				String alias = SUBPLAN_ALIAS + (i++);
				aliases.put(step.getOperator(), alias);
				this.writeCommand(out, step, alias, aliases);
			}
			close(out, QNames.PLAN);
		} else {
			openclose(out, QNames.PLAN, att);
		}
	}
	
	/**
	 * Writes the given attribute to the given output.
	 *
	 * @param out the output stream being written to
	 * @param attribute the attribute being written
	 */
	public void writeAttribute(PrintStream out, Attribute attribute) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, attribute.getName());
		openclose(out, QNames.ATTRIBUTE, att);
	}

	/**
	 * Writes the given command to the given output.
	 *
	 * @param out the output stream being written to
	 * @param plan LeftDeepPlan
	 * @param alias String
	 * @param aliases Map<LogicalOperator,String>
	 */
	public void writeCommand(PrintStream out, LeftDeepPlan plan, String alias, Map<RelationalOperator, String> aliases) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, alias);
		open(out, QNames.COMMAND, att);
		this.operatorWriter.writeOperator(out, plan.getOperator(), aliases);
		close(out, QNames.COMMAND);
	}

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Writer#write(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, LeftDeepPlan o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writePlan(out, o);
	}
}
