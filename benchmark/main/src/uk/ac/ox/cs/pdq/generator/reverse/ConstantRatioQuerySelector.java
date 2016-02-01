package uk.ac.ox.cs.pdq.generator.reverse;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;

/**
 * A QuerySelector that accepts only conjunctive query where the number of
 * atoms with constants in the body is greater or equals to some given ratio.
 * 
 * @author Julien Leblay
 *
 */
public class ConstantRatioQuerySelector implements QuerySelector {

	/** The ratio. */
	private final double ratio;
	
	/**
	 * Instantiates a new constant ratio query selector.
	 *
	 * @param ratio the ratio
	 */
	public ConstantRatioQuerySelector(double ratio) {
		this.ratio = ratio;
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean accept(Query<?> q) {
		double constPred = 0.0;
		int count = 0;
		for (Predicate p: q.getBody().getPredicates()) {
			if (!p.getSchemaConstants().isEmpty()) {
				constPred++;
			}
			count++;
		}
		return (constPred / count) >= this.ratio;
	}

}
