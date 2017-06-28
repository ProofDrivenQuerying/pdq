package uk.ac.ox.cs.pdq.plan;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * Cost of double value.
 *
 * @author Efthymia Tsamoura
 */
public final class DoubleCost implements Cost {
	
	/** The Constant UPPER_BOUND. */
	public static final DoubleCost UPPER_BOUND = new DoubleCost(Double.POSITIVE_INFINITY);
	
	/** The value. */
	protected Double value;

	/**
	 * Instantiates a new double cost.
	 */
	public DoubleCost() {
		this.value = 0.0;
	}

	/**
	 * Constructor for DoubleCost.
	 * @param cost double
	 */
	public DoubleCost(double cost) {
		this.value = cost;
	}

	/**
	 * Gets the cost.
	 *
	 * @return double
	 */
	public double getCost() {
		return this.value;
	}

	/**
	 * Less or equals.
	 *
	 * @param cost Cost
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.plan.Cost#lessOrEquals(Cost)
	 */
	@Override
	public boolean lessOrEquals(Cost cost) {
		Preconditions.checkState(cost instanceof DoubleCost);
		return this.value <= ((DoubleCost) cost).value;
	}

	/**
	 * Sets the max.
	 *
	 * @see uk.ac.ox.cs.pdq.plan.Cost#setMax()
	 */
	@Override
	public void setMax() {
		this.value = UPPER_BOUND.getValue().doubleValue();
	}

	/**
	 * Greater or equals.
	 *
	 * @param cost Cost
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.plan.Cost#greaterOrEquals(Cost)
	 */
	@Override
	public boolean greaterOrEquals(Cost cost) {
		Preconditions.checkState(cost instanceof DoubleCost);
		return this.value >= ((DoubleCost) cost).value;

	}

	/**
	 * Less than.
	 *
	 * @param cost Cost
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.plan.Cost#lessThan(Cost)
	 */
	@Override
	public boolean lessThan(Cost cost) {
		Preconditions.checkState(cost instanceof DoubleCost);
		return this.value < ((DoubleCost) cost).value;
	}

	/**
	 * Greater than.
	 *
	 * @param cost Cost
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.plan.Cost#greaterThan(Cost)
	 */
	@Override
	public boolean greaterThan(Cost cost) {
		Preconditions.checkState(cost instanceof DoubleCost);
		return this.value > ((DoubleCost) cost).value;
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		return this.getClass().isInstance(o)
				&& this.value.equals(((DoubleCost) o).value);
	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return String.valueOf(this.value);
	}

	/**
	 * Adds the.
	 *
	 * @param cost Cost
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.plan.Cost#add(Cost)
	 */
	@Override
	public Cost add(Cost cost) {
		Preconditions.checkState(cost instanceof DoubleCost);
		return new DoubleCost(this.value + ((DoubleCost) cost).value);
	}

	/**
	 * Gets the value.
	 *
	 * @return Number
	 * @see uk.ac.ox.cs.pdq.plan.Cost#getValue()
	 */
	@Override
	public Number getValue() {
		return this.value;
	}

	/**
	 * Clone.
	 *
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.plan.Cost#clone()
	 */
	@Override
	public Cost clone() {
		return new DoubleCost(this.value);
	}

	/**
	 * Compare to.
	 *
	 * @param o Cost
	 * @return int
	 */
	@Override
	public int compareTo(Cost o) {
		if(this.equals(o)) {
			return 0;
		}
		else if(this.greaterThan(o)) {
			return 1;
		}
		else if(this.lessThan(o)) {
			return -1;
		}
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Checks if is upper bound.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.plan.Cost#isUpperBound()
	 */
	@Override
	public boolean isUpperBound() {
		return this.equals(UPPER_BOUND);
	}
}
