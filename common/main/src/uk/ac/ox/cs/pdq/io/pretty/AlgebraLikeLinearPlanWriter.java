package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import uk.ac.ox.cs.pdq.algebra.Access;
import uk.ac.ox.cs.pdq.algebra.DependentAccess;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.plan.AccessOperator;
import uk.ac.ox.cs.pdq.plan.LinearPlan;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

/**
 * Writes a concise representation of a plan to the given output
 * 
 * @author Julien Leblay
 *
 */
public class AlgebraLikeLinearPlanWriter extends PrettyWriter<LinearPlan> implements Writer<LinearPlan> {

	public static final String SUBPLAN_ALIAS = "T"; 
	public static final String ASSIGN = " := ";
	public static final String EMPTY = " \u2205";
	public static final String INPUT = " \u21D0 ";

	/**
	 * The default out to which plan should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/** Relations writer */
	private RelationalOperatorWriter operatorWriter = null;
	
	/**
	 * Default constructor.
	 */
	public AlgebraLikeLinearPlanWriter() {
		this(System.out);
	}
	
	/**
	 * Default constructor.
	 * @param out PrintStream
	 */
	private AlgebraLikeLinearPlanWriter(PrintStream out) {
		this.out = out;
		this.operatorWriter = new RelationalOperatorWriter();
	}
	
	/**
	 * @param out PrintStream
	 * @return AlgebraLikeLinearPlanWriter
	 */
	public static AlgebraLikeLinearPlanWriter to(PrintStream out) {
		return new AlgebraLikeLinearPlanWriter(out);
	}

	/**
	 * Writes the given plan to the given output.
	 * @param out
	 * @param plan LinearPlan
	 */
	public void writePlan(PrintStream out, LinearPlan plan) {
		if (plan != null) {
			int i = 1;
			Map<RelationalOperator, String> aliases = new LinkedHashMap<>();
			String previous = null;
			for (LinearPlan step: plan) {
				AccessOperator access = step.getAccess();
				String accAlias = SUBPLAN_ALIAS + (i++);
				aliases.put((RelationalOperator) access, accAlias);
				out.print(accAlias + INPUT);
				out.print(access.getRelation().getName() + "/");
				out.print(access.getAccessMethod().getName() + INPUT);
				if (access.getAccessMethod().getType() == Types.FREE) {
					out.println(EMPTY);
				} else if (access instanceof Access) {
					StringBuilder sb = new StringBuilder();
					this.operatorWriter.writeOperator(sb, ((Access) access).getChild(), aliases);
					out.println(sb);
				} else if (access instanceof DependentAccess) {
					Map<Integer, TypedConstant<?>> inputs = ((DependentAccess) access).getStaticInputs();
					if (inputs != null && !inputs.isEmpty()) {
						StringBuilder sb = new StringBuilder();
						List<TypedConstant<?>> inputTuple = Lists.newArrayList(new TreeMap<Integer, TypedConstant<?>>(inputs).values());
						TupleType inputType = TupleType.DefaultFactory.createFromTyped(inputTuple);
						RelationalOperatorWriter.formatTuples(sb, Lists.newArrayList(inputType.createTuple(inputTuple))); 
						out.println(sb);
					}
				} else {
					out.println(previous);
				}
				String alias = SUBPLAN_ALIAS + (i++);
				out.print(alias + ASSIGN);
				this.writeCommand(out, step, alias, aliases);
				out.println();
				aliases.put(step.getOperator(), alias);
				previous = alias;
			}
		} else {
			out.print(EMPTY);
		}
	}

	/**
	 * Writes the given command to the given output.
	 * @param out
	 * @param plan LinearPlan
	 * @param alias String
	 * @param aliases Map<LogicalOperator,String>
	 */
	public void writeCommand(PrintStream out, LinearPlan plan, String alias, Map<RelationalOperator, String> aliases) {
		StringBuilder sb = new StringBuilder();
		this.operatorWriter.writeOperator(sb, plan.getOperator(), aliases);
		out.print(sb);
	}

	/**
	 * @param out PrintStream
	 * @param o LinearPlan
	 */
	@Override
	public void write(PrintStream out, LinearPlan o) {
		this.writePlan(out, o);
	}

	/**
	 * @param p LinearPlan
	 */
	@Override
	public void write(LinearPlan p) {
		this.write(this.out, p);
	}
}
