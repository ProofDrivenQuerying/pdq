package uk.ac.ox.cs.pdq.cost;

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
	 * Gets the value.
	 *
	 * @return a numeric representation of the cost
	 */
	Number getValue();
}
