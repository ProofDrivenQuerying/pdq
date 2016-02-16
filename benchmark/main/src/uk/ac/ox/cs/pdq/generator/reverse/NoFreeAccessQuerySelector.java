package uk.ac.ox.cs.pdq.generator.reverse;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.fol.Signature;

// TODO: Auto-generated Javadoc
/**
 * A QuerySelector that accepts only conjunctive query where at least one 
 * predicate refers to some none-free access relation.
 * 
 * @author Julien Leblay
 */
public class NoFreeAccessQuerySelector implements QuerySelector {

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		for (Predicate p: q.getBody().getPredicates()) {
			Signature s = p.getSignature();
			if (s instanceof Relation) {
				for (AccessMethod am: ((Relation) s).getAccessMethods()) {
					if (am.getType() == Types.FREE) {
						return false;
					}
				}
			}
		}
		return false;
	}
}
