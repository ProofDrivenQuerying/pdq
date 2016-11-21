package uk.ac.ox.cs.pdq.ui.io.pretty;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.Writer;
import uk.ac.ox.cs.pdq.io.pretty.PrettyFormulaWriter;
import uk.ac.ox.cs.pdq.io.pretty.PrettyWriter;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.reasoning.chase.Utility;
import uk.ac.ox.cs.pdq.ui.proof.Proof;
import uk.ac.ox.cs.pdq.ui.proof.Proof.State;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * Writes a concise representation of a query to the given output.
 *
 * @author Julien Leblay
 */
public class ExtendedPrettyProofWriter extends PrettyWriter<Proof> implements Writer<Proof> {

	/**
	 * The default out to which queries should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;
	
	/** The acc schema. */
	private final AccessibleSchema accSchema;

	/**
	 * Instantiates a new extended pretty proof writer.
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
	 *
	 * @param out the out
	 * @param accSchema AccessibleSchema
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static ExtendedPrettyProofWriter to(PrintStream out, AccessibleSchema accSchema) {
		return new ExtendedPrettyProofWriter(out, accSchema);
	}
	
	/**
	 * Write.
	 *
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
			
			
			PrettyFormulaWriter dw = PrettyFormulaWriter.to(out).indented();
			out.print("Groundings: ");
			for (Map<Variable, Constant> candidate: state.getMatches()) {
				out.print('\n');
				dw.write(Utility.fire(axiom,candidate,true));
				out.println();
			}
			out.println();
		}
	}
	
	/**
	 * Trace back.
	 *
	 * @param proof Proof
	 * @param target Iterable<? extends PredicateFormula>
	 * @return String
	 */
	public static String traceBack(Proof proof, Iterable<? extends Atom> target) {
		StringBuilder result = new StringBuilder();
		Set<Atom> todo = Sets.newLinkedHashSet(target);
		Map<Atom, Pair<Dependency, HashSet<Atom>>> provenance = null;
		while (!todo.isEmpty()) {
			for (Atom p: Lists.newArrayList(todo)) {
				Pair<Dependency, HashSet<Atom>> rule = provenance.get(p);
				if (rule != null) {
					String s = Joiner.on(" & ").join(rule.getRight()) + " -> " + p + "..." + '\n';
					result.insert(0, s);
					for (Atom q: rule.getRight()) {
						if (!q.getPredicate().equals(AccessibleRelation.getInstance())) {
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
	 * Write.
	 *
	 * @param q Proof
	 */
	@Override
	public void write(Proof q) {
		this.write(this.out, q);
	}
}
