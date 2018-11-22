package uk.ac.ox.cs.pdq.planner.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;

/**
 * The Class PlannerUtility.
 *
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class PlannerUtility {

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
