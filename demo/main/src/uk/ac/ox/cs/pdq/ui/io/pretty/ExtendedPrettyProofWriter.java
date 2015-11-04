package uk.ac.ox.cs.pdq.ui.io.pretty;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.io.pretty.PrettyFormulaWriter;
import uk.ac.ox.cs.pdq.io.pretty.PrettyWriter;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.InferredAccessibleAxiom;
import uk.ac.ox.cs.pdq.ui.proof.Proof;
import uk.ac.ox.cs.pdq.ui.proof.Proof.State;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Writes a concise representation of a query to the given output
 * 
 * @author Julien Leblay
 *
 */
public class ExtendedPrettyProofWriter extends PrettyWriter<Proof> implements Writer<Proof> {

	/**
	 * The default out to which queries should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;
	
	private final AccessibleSchema accSchema;

	/**
	 * 
	 * @param out the default output
	 * @param accSchema AccessibleSchema
	 */
	private ExtendedPrettyProofWriter(PrintStream out, AccessibleSchema accSchema) {
		this.out = out;
		this.accSchema = accSchema;
	}
	
	/**
	 * Fluent pretty writer provider.
	 * @param out
	 * @param accSchema AccessibleSchema
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static ExtendedPrettyProofWriter to(PrintStream out, AccessibleSchema accSchema) {
		return new ExtendedPrettyProofWriter(out, accSchema);
	}
	
	/**
	 * @param out PrintStream
	 * @param p Proof
	 */
	@Override
	public void write(PrintStream out, Proof p) {
		String sep = "";
		for (State state: p.getStates()) {
			AccessibilityAxiom axiom = state.getAxiom();
			out.print("Axiom: ");
			out.println(axiom.getBaseRelation().getName() + '/' + axiom.getAccessMethod().getName());
			
			
			PrettyFormulaWriter<Formula> dw = PrettyFormulaWriter.to(out).indented();
			out.print("Groundings: ");
			for (Map<Variable, Constant> candidate: state.getMatches()) {
				out.print('\n');
				dw.write(axiom.ground(candidate));
				out.println();
			}
			out.println();
		}
	}
	
	/**
	 * @param proof Proof
	 * @param target Iterable<? extends PredicateFormula>
	 * @return String
	 */
	public static String traceBack(Proof proof, Iterable<? extends Predicate> target) {
		StringBuilder result = new StringBuilder();
		Set<Predicate> todo = Sets.newLinkedHashSet(target);
		Map<Predicate, Pair<Constraint, HashSet<Predicate>>> provenance = null;
		while (!todo.isEmpty()) {
			for (Predicate p: Lists.newArrayList(todo)) {
				Pair<Constraint, HashSet<Predicate>> rule = provenance.get(p);
				if (rule != null) {
					String s = Joiner.on(" & ").join(rule.getRight()) + " -> " + p + "..." + '\n';
					result.insert(0, s);
					for (Predicate q: rule.getRight()) {
						if (!q.getSignature().equals(AccessibleRelation.getInstance())) {
							todo.add(q);
						}
					}
				}
				todo.remove(p);
			}
		}
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.io.PrettyWriter#write(java.lang.Object)
	 */
	/**
	 * @param q Proof
	 */
	@Override
	public void write(Proof q) {
		this.write(this.out, q);
	}
}
