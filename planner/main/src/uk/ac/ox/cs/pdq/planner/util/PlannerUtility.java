package uk.ac.ox.cs.pdq.planner.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class PlannerUtility.
 *
 * @author Efthymia Tsamoura
 */
public class PlannerUtility {

	/**
	 * Gets the input constants.
	 *
	 * @param rule the rule
	 * @param facts the facts
	 * @return the constants of the input facts that correspond to the input positions of the rule
	 */
	public static Collection<Constant> getInputConstants(AccessibilityAxiom rule, Set<Atom> facts) {
		Collection<Constant> inputs = new LinkedHashSet<>();
		for(Atom fact:facts) {
			List<Constant> constants = Utility.getTypedAndUntypedConstants(fact,rule.getAccessMethod().getZeroBasedInputs());
			for(Constant constant:constants) {
				if(constant.isUntypedConstant()) {
					inputs.add(constant);
				}
			}
		}
		return inputs;
	}
	
	/**
	 * Gets the input constants.
	 *
	 * @param binding the binding
	 * @param fact the fact
	 * @return the constants in the input positions of the given fact
	 */
	public static List<Constant> getInputConstants(AccessMethod binding, Atom fact) {
		List<Constant> ret  = Utility.getTypedAndUntypedConstants(fact,binding.getZeroBasedInputs());
		return Lists.newArrayList(uk.ac.ox.cs.pdq.util.Utility.removeDuplicates(ret));
	}
	
	/**
	 * Gets the constants lying at the input positions.
	 *
	 * @throws IllegalArgumentException if there is a non-constant at one of the input positions
	 * @param positions List<Integer>
	 * @return the List<Constant> at the given positions.
	 */
	public static List<Constant> getTypedAndUntypedConstants(Atom atom, List<Integer> positions) {
		List<Constant> result = new ArrayList<>();
		for(Integer i: positions) {
			if(i < atom.getTerms().size() && !atom.getTerms().get(i).isVariable()) {
				result.add((Constant) atom.getTerms().get(i));
			}
			else {
				throw new java.lang.IllegalArgumentException();
			}
		}
		return result;
	}
	
	/**
	 * Accessible.
	 *
	 * @param <Q> the generic type
	 * @param query the query
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#accessible(AccessibleSchema)
	 */
	public ConjunctiveQuery accessible(ConjunctiveQuery query) {
		List<Formula> atoms = new ArrayList<>();
		for (Atom af: query.getAtoms()) {
			atoms.add(
					new Atom(this.getInferredAccessibleRelation((Relation) af.getPredicate()), af.getTerms()));
		}
		if(atoms.size() == 1) {
			return new ConjunctiveQuery(query.getFreeVariables(), (Atom)atoms.get(0));
		}
		else {
			return new ConjunctiveQuery(query.getFreeVariables(), (Conjunction) Conjunction.of(atoms));
		}
	}

	/**
	 * Accessible.
	 *
	 * @param <Q> the generic type
	 * @param query the query
	 * @param canonicalMapping the canonical mapping
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#accessible(AccessibleSchema)
	 */
	public ConjunctiveQuery accessible(ConjunctiveQuery query, Map<Variable, Constant> canonicalMapping) {
		List<Formula> atoms = new ArrayList<>();
		for (Atom af: query.getAtoms()) {
			atoms.add(
					new Atom(this.getInferredAccessibleRelation((Relation) af.getPredicate()), af.getTerms()));
		}
		if(atoms.size() == 1) {
			return new ConjunctiveQuery(query.getFreeVariables(), (Atom)atoms.get(0), canonicalMapping);
		}
		else {
			return new ConjunctiveQuery(query.getFreeVariables(), (Conjunction) Conjunction.of(atoms), canonicalMapping);
		}
	}
}
