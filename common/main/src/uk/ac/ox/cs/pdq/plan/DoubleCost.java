package uk.ac.ox.cs.pdq.plan;

import com.google.common.base.Preconditions;

/**
 * Cost of double value
 *
 * @author Efthymia Tsamoura
 *
 */
public final class DoubleCost implements Cost {
	
	public static final DoubleCost UPPER_BOUND = new DoubleCost(Double.POSITIVE_INFINITY);
	
	protected Double value;

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
	 * @return double
	 */
	public double getCost() {
		return this.value;
	}

	/**
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
	 * @see uk.ac.ox.cs.pdq.plan.Cost#setMax()
	 */
	@Override
	public void setMax() {
		this.value = UPPER_BOUND.getValue().doubleValue();
	}

	/**
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
	 * @return int
	 */
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return String.valueOf(this.value);
	}

	/**
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
	 * @return Number
	 * @see uk.ac.ox.cs.pdq.plan.Cost#getValue()
	 */
	@Override
	public Number getValue() {
		return this.value;
	}

	/**
	 * @return Cost
	 * @see uk.ac.ox.cs.pdq.plan.Cost#clone()
	 */
	@Override
	public Cost clone() {
		return new DoubleCost(this.value);
	}

	/**
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
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.plan.Cost#isUpperBound()
	 */
	@Override
	public boolean isUpperBound() {
		return this.equals(UPPER_BOUND);
	}
}
