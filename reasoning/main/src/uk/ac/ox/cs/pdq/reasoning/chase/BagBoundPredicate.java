package uk.ac.ox.cs.pdq.reasoning.chase;

import uk.ac.ox.cs.pdq.fol.Predicate;

/**
 * Wrapper for fact that belongs to a bag.
 * This class allows reference to a fact's bag, without having to manage
 * the reference externally.
 *
 * @author Julien Leblay
 */
public class BagBoundPredicate extends Predicate {

	private final int bag;

	/**
	 * Constructor for BagBoundPredicate.
	 * @param predicate PredicateFormula
	 * @param bag int
	 */
	public BagBoundPredicate(Predicate predicate, int bag) {
		super(predicate.getSignature(), predicate.getTerms());
		this.bag = bag;
	}

	/**
	 * @return int
	 */
	public int getBag() {
		return this.bag;
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return "{" + super.toString() + "," + this.bag + "}";
	}
}
