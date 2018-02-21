package uk.ac.ox.cs.pdq.cost.converters;

import java.util.Collection;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Variable;

/**
 * Converts a plan given as a RelationalTerm object to a ConjunctiveQuery. The
 * conversation does not uses the sql WITH statement, instead it flattens the
 * plan to create one huge join with all conditions.
 * 
 * @author Gabor
 */
public class PlanToQueryConverter {

	/**
	 * Converts a relational term into a conjunctive query.
	 * 
	 * @param rt
	 * @return
	 */
	public static ConjunctiveQuery convertToConjunctiveQuery(RelationalTerm rt) {
		RelationalTermDescriptor td = new RelationalTermDescriptor(rt);
		Collection<Atom> queryAtoms = td.getQueryAtoms();
		Variable[] freeVariables = td.calculateFreeVariables();
		if (queryAtoms == null || queryAtoms.isEmpty())
			return null;
		if (queryAtoms.size() == 1)
			return ConjunctiveQuery.create(freeVariables, queryAtoms.iterator().next());
		return ConjunctiveQuery.create(freeVariables, (Conjunction) Conjunction.of(queryAtoms.toArray(new Atom[queryAtoms.size()])));
	}
}
