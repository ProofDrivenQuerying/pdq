package uk.ac.ox.cs.pdq.plan;

// TODO: Auto-generated Javadoc
/**
 * Abstract plan cost.
 *
 * @author Efthymia Tsamoura
 */
public interface Cost extends Comparable<Cost>{
	
	/**
	 * Less or equals.
	 *
	 * @param cost Input cost object
	 * @return true if this.cost <= cost
	 */
	boolean lessOrEquals(Cost cost);

	/**
	 * Less than.
	 *
	 * @param cost Input cost object
	 * @return true if this.cost < cost
	 */
	boolean lessThan(Cost cost);

	/**
	 * Greater or equals.
	 *
	 * @param cost Input cost object
	 * @return true if this.cost >= cost
	 */
	boolean greaterOrEquals(Cost cost);

	/**
	 * Greater than.
	 *
	 * @param cost Input cost object
	 * @return true if this.cost > cost
	 */
	boolean greaterThan(Cost cost);

	/**
	 * Initialises the cost value to a maximum value.
	 */
	void setMax();

	/**
	 * Checks if is upper bound.
	 *
	 * @return true, if the cost is equals to the upper bound
	 */
	public abstract boolean isUpperBound();

	/**
	 * Adds the.
	 *
	 * @param cost Input cost object
	 * @return A new cost object that has cost equal to the cost of this object and of the input one
	 */
	Cost add(Cost cost);

	/**
	 * Gets the value.
	 *
	 * @return a numeric representation of the cost
	 */
	Number getValue();

	/**
	 * Clone.
	 *
	 * @return Cost
	 */
	Cost clone();
}
