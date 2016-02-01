package uk.ac.ox.cs.pdq.generator.reverse;

import java.util.Collections;
import java.util.List;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;

import com.google.common.collect.Lists;

/**
 * A QuerySelector that accepts only conjunctive query where no two predicate
 * have the same name and their sequence of constants cover one another.
 * 
 * @author Julien Leblay
 */
public class DubiousRepeatedPredicateQuerySelector implements QuerySelector {

	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		List<Predicate> list = Lists.newArrayList(q.getBody().getPredicates());
		for (int i = 0, k = list.size(); i < k - 1; i++) {
			Predicate p1 = list.get(i);
			FactSignature s1 = FactSignature.make(p1);
			for (int j = i + 1, l = list.size(); j < l; j++) {
				Predicate p2 = list.get(j);
				FactSignature s2 = FactSignature.make(p2);
				if (s1.covers(s2) || s2.covers(s1)) {
					return false;
				}
				if (p2.getName().equals(p1.getName())
					&& !Collections.disjoint(list.get(i).getTerms(), list.get(j).getTerms())) {
					return false;
				}
			}
		}
		return true;
	}
}
