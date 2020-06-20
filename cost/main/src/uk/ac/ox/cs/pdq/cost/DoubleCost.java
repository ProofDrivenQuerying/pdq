package uk.ac.ox.cs.pdq.cost;

import com.google.common.base.Preconditions;


/**
 * Cost of double value.
 *
 * @author Efthymia Tsamoura
 */
public final class DoubleCost extends Cost {
	
	/** The Constant UPPER_BOUND. */
	public static final DoubleCost UPPER_BOUND = new DoubleCost(Double.POSITIVE_INFINITY);
	
	/**  */
	protected Double value;

	/**
	 * 
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
	 * 
	 *
	 * @param cost Cost
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.datasources.Cost#lessOrEquals(Cost)
	 */
	@Override
	public boolean lessOrEquals(Cost cost) {
		if (cost==null)
			return true;
		Preconditions.checkState(cost instanceof DoubleCost);
		return this.value <= ((DoubleCost) cost).value;
	}

	/**
	 * 
	 *
	 * @param cost Cost
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.datasources.Cost#greaterOrEquals(Cost)
	 */
	@Override
	public boolean greaterOrEquals(Cost cost) {
		if (cost==null)
			return false;
		Preconditions.checkState(cost instanceof DoubleCost);
		return this.value >= ((DoubleCost) cost).value;

	}

	/**
	 * 
	 *
	 * @param cost Cost
	 * @return boolean
	 * 
	 */
	@Override
	public boolean lessThan(Cost cost) {
		if (cost == null)
			return true;
		Preconditions.checkState(cost instanceof DoubleCost);
		return this.value < ((DoubleCost) cost).value;
	}

	/**
	 * 
	 *
	 * @param cost Cost
	 * @return boolean
	 * 
	 */
	@Override
	public boolean greaterThan(Cost cost) {
		if (cost == null)
			return true;
		if (!(cost instanceof DoubleCost)) {
			Preconditions.checkState(cost instanceof DoubleCost);
		}
		return this.value > ((DoubleCost) cost).value;
	}

	/**
	 * 
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
	 * 
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	/**
	 * 
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return String.valueOf(this.value);
	}

	/**
	 * .
	 *
	 * @return Number
	 * 
	 */
	@Override
	public Number getValue() {
		return this.value;
	}

	/**
	 * 
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
}
