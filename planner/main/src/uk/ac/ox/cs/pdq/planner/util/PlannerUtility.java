package uk.ac.ox.cs.pdq.planner.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * The Class PlannerUtility.
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class PlannerUtility {

	/**
	 * Given a set of facts F=F1.... on the same relation R,
	 * and an accessibility axiom on R, corresponding to performing a particular access method mt
	 * on R; find the constants that lie within the input positions of each Fi for mt 
	 *
	 * TOCOMMENT: It seems like we only get untyped constants. Why?
	 * TOCOMMENT: should we have an assert that checks that all facts use the correct relation
	 *  
	 * @param rule the accessibility axiom for some method being fired
	 * @param facts the facts
	 * @return the constants of the input facts that correspond to the input positions of the method
	 */
	public static Collection<Constant> getInputConstants(AccessibilityAxiom rule, Set<Atom> facts) {
		Collection<Constant> inputs = new LinkedHashSet<>();
		for(Atom fact:facts) {
			List<Constant> constants = Utility.getTypedAndUntypedConstants(fact,rule.getAccessMethod().getInputs());
			for(Constant constant:constants) {
				if(constant.isUntypedConstant()) {
					inputs.add(constant);
				}
			}
		}
		return inputs;
	}

	/**
	 * Gets the constants lying at the input positions.
	 *
	 * @throws IllegalArgumentException if there is a non-constant at one of the input positions
	 * @param atom the atom where we want some values
	 * @param positions  the positions we are interested in
	 * @return the constants at the given positions.
	 */
	public static List<Constant> getTypedAndUntypedConstants(Atom atom, Integer[] positions) {
		List<Constant> result = new ArrayList<>();
		for(Integer i: positions) {
			if(i < atom.getTerms().length && !atom.getTerm(i).isVariable()) 
				result.add((Constant) atom.getTerm(i));
			else 
				throw new java.lang.IllegalArgumentException();
		}
		return result;
	}

	/**
	 * Change a conjunctive query Q to its "accessible version" infaccQ
	 *
	 * 
	 * @param query the query
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#createAccessibleQuery(AccessibleSchema)
	 */
	public static ConjunctiveQuery createAccessibleQuery(ConjunctiveQuery query) {
		Atom[] atoms = new Atom[query.getNumberOfAtoms()];
		for (int atomIndex = 0; atomIndex < query.getNumberOfAtoms(); ++atomIndex) {
			Atom queryAtom = query.getAtom(atomIndex);
			Predicate predicate = null;
				predicate = Predicate.create(AccessibleSchema.inferredAccessiblePrefix + queryAtom.getPredicate().getName(), queryAtom.getPredicate().getArity());
			atoms[atomIndex] = Atom.create(predicate, queryAtom.getTerms());
		}
		return ConjunctiveQuery.create(query.getFreeVariables(), atoms);
	}
	
	public static Map<Variable, Constant> generateCanonicalMappingForQuery(ConjunctiveQuery query) {
		Map<Variable, Constant> canonicalMapping = new LinkedHashMap<>();
		List<Variable> freeVariables = Arrays.asList(query.getFreeVariables());
		for (Atom atom : query.getBody().getAtoms()) {
			for (Term t : atom.getTerms()) {
				if (t.isVariable()) {
					Constant c = canonicalMapping.get(t);
					if (c == null && !freeVariables.contains(t)) {
						c = UntypedConstant.create("v_" + ((Variable)t).getSymbol() + query.getId());
						canonicalMapping.put((Variable) t, c);
					} else if (c==null) {
						// c is a free variable we want to preserve its name in the new constant.
						c = UntypedConstant.create("fv_" + ((Variable)t).getSymbol() + query.getId());
						canonicalMapping.put((Variable) t, c);
					}
				}
			}
		}
		return canonicalMapping;
	}

}
