package uk.ac.ox.cs.pdq.generator.reverse;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Signature;

/**
 * A QuerySelector that accepts only conjunctive query where at least one 
 * predicate refers to some none-free access relation.
 * 
 * @author Julien Leblay
 *
 */
public class NoAllFreeAccessQuerySelector implements QuerySelector {

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.formula.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		boolean result = true;
		for (Predicate p: q.getBody().getPredicates()) {
			Signature s = p.getSignature();
			boolean hasFreeAccess = false;
			if (s instanceof Relation) {
				for (AccessMethod am: ((Relation) s).getAccessMethods()) {
					if (am.getType() == Types.FREE) {
						hasFreeAccess = true;
						break;
					}
				}
			} else {
				hasFreeAccess = false;
			}
			result &= hasFreeAccess;
		}
		return !result;
	}
}
