package uk.ac.ox.cs.pdq.generator.reverse;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

// TODO: Auto-generated Javadoc
/**
 * A QuerySelector that accepts only conjunctive query with a number of atoms
 * between (inclusive) to given length bounds.
 * 
 * @author Julien Leblay
 */
public class LengthBasedQuerySelector implements QuerySelector {

	/** The minimum length. */
	private final int min;
	
	/** The maximun length. */
	private final int max;
	
	/**
	 * Instantiates a new length based query selector.
	 *
	 * @param mn the mn
	 * @param mx the mx
	 */
	public LengthBasedQuerySelector(int mn, int mx) {
		this.min = mn;
		this.max = mx;
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.generator.reverse.QuerySelector#accept(uk.ac.ox.cs.pdq.fol.Query)
	 */
	@Override
	public boolean accept(ConjunctiveQuery q) {
		return q.getAtoms().size() >= this.min
				&& q.getAtoms().size() <= this.max;
	}

}
