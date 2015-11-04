package uk.ac.ox.cs.pdq.generator.reverse;

import uk.ac.ox.cs.pdq.fol.Query;


/**
 * A QuerySelector that accepts only conjunctive query with a number of atoms
 * between (inclusive) to given length bounds.
 * 
 * @author Julien Leblay
 *
 */
public class LengthBasedQuerySelector implements QuerySelector {

	private final int min;
	private final int max;
	
	public LengthBasedQuerySelector(int mn, int mx) {
		this.min = mn;
		this.max = mx;
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.builder.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.formula.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		return q.getBody().getPredicates().size() >= this.min
				&& q.getBody().getPredicates().size() <= this.max;
	}

}
