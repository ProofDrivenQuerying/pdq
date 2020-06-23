// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import uk.ac.ox.cs.pdq.cost.io.jaxb.adapters.CostAdapter;

/**
 * Abstract plan cost.
 *
 * @author Efthymia Tsamoura
 */
@XmlJavaTypeAdapter(CostAdapter.class)
public abstract class Cost implements Comparable<Cost>{
	
	/**
	 * Less or equals.
	 *
	 * @param cost Input cost object
	 * @return true if this.cost <= cost
	 */
	public abstract boolean lessOrEquals(Cost cost);

	/**
	 * Less than.
	 *
	 * @param cost Input cost object
	 * @return true if this.cost < cost
	 */
	public abstract boolean lessThan(Cost cost);

	/**
	 * Greater or equals.
	 *
	 * @param cost Input cost object
	 * @return true if this.cost >= cost
	 */
	public abstract boolean greaterOrEquals(Cost cost);

	/**
	 * Greater than.
	 *
	 * @param cost Input cost object
	 * @return true if this.cost > cost
	 */
	public abstract boolean greaterThan(Cost cost);

	/**
	 * Gets the value.
	 *
	 * @return a numeric representation of the cost
	 */
	public abstract Number getValue();
}
