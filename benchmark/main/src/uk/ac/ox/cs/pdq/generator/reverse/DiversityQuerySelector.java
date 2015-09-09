package uk.ac.ox.cs.pdq.generator.reverse;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

import uk.ac.ox.cs.pdq.fol.Query;

/**
 * A QuerySelector that accepts conjunctive query where that do not look like
 * already observed queries.
 * 
 * @author Julien Leblay
 *
 */
public class DiversityQuerySelector implements QuerySelector {

	private Set<SortedSet<FactSignature>> signatures = new LinkedHashSet<>();
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.formula.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		SortedSet<FactSignature> signature = FactSignature.make(q.getBody().getPredicates());
		if (this.signatures.contains(signature)) {
			return false;
		}
		this.signatures.add(signature);
		return true;
	}
}
