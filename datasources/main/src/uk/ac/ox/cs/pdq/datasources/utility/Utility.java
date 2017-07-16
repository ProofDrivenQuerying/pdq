package uk.ac.ox.cs.pdq.datasources.utility;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;

public class Utility {

	/**
	 * Generates a list of terms matching the attributes of the input relation.
	 *
	 * @param r Relation
	 * @return List<Term>
	 */
	public static Term[] createVariables(Relation r) {
		Term[] result = new Term[r.getArity()];
		for (int i = 0, l = r.getArity(); i < l; i++) 
			result[i] = Variable.getFreshVariable();
		return result;
	}
}
