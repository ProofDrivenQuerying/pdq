package uk.ac.ox.cs.pdq.plan;

/**
 * Abstract plan cost
 * @author Efthymia Tsamoura
 *
 */
public interface Cost extends Comparable<Cost>{
	/**
	 * @param cost Input cost object
	 * @return true if this.cost <= cost
	 */
	boolean lessOrEquals(Cost cost);

	/**
	 * @param cost	Input cost object
	 * @return	true if this.cost < cost
	 */
	boolean lessThan(Cost cost);

	/**
	 * @param cost Input cost object
	 * @return true if this.cost >= cost
	 */
	boolean greaterOrEquals(Cost cost);

	/**
	 * @param cost Input cost object
	 * @return true if this.cost > cost
	 */
	boolean greaterThan(Cost cost);

	/**
	 * Initialises the cost value to a maximum value
	 */
	void setMax();

	/**
	 * @return true, if the cost is equals to the upper bound
	 */
	public abstract boolean isUpperBound();

	/**
	 * @param cost Input cost object
	 * @return A new cost object that has cost equal to the cost of this object and of the input one
	 */
	Cost add(Cost cost);

	/**
	 * @return a numeric representation of the cost
	 */
	Number getValue();

	/**
	 * @return Cost
	 */
	Cost clone();
}
