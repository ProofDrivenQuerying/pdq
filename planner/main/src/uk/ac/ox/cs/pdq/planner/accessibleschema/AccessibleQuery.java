package uk.ac.ox.cs.pdq.planner.accessibleschema;

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

/**
 * Change a conjunctive query Q to its "accessible version" infaccQ
 * @author gabor
 *
 */
public class AccessibleQuery extends ConjunctiveQuery {
	private static final long serialVersionUID = 1L;
	
	public AccessibleQuery(Variable[] freeVariables, Atom[] children) {
		super(freeVariables, children);
	}
	
	public AccessibleQuery(ConjunctiveQuery cq) {
		super(cq.getFreeVariables(), getAccessibleAtoms(cq));
	}
	
	/**
	 * Change a conjunctive query Q to its "accessible version" infaccQ
	 *
	 * 
	 * @param query the query
	 * @return the accessible query
	 * @see uk.ac.ox.cs.pdq.fol.Query#createAccessibleQuery(AccessibleSchema)
	 */
	public static AccessibleQuery createAccessibleQuery(ConjunctiveQuery query) {
		Atom[] atoms = getAccessibleAtoms(query);
		return new AccessibleQuery(query.getFreeVariables(), atoms);
	}
	
	private static Atom[] getAccessibleAtoms(ConjunctiveQuery query) {
		Atom[] atoms = new Atom[query.getNumberOfAtoms()];
		for (int atomIndex = 0; atomIndex < query.getNumberOfAtoms(); ++atomIndex) {
			Atom queryAtom = query.getAtom(atomIndex);
			Predicate predicate = null;
				predicate = Predicate.create(AccessibleSchema.inferredAccessiblePrefix + queryAtom.getPredicate().getName(), queryAtom.getPredicate().getArity());
			atoms[atomIndex] = Atom.create(predicate, queryAtom.getTerms());
		}
		return atoms;
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
