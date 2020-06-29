// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
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
	 * 
	 *
	 * @param cost Input cost object
	 * @return true if this.cost <= cost
	 */
	public abstract boolean lessOrEquals(Cost cost);

	/**
	 * 
	 *
	 * @param cost Input cost object
	 * @return true if this.cost < cost
	 */
	public abstract boolean lessThan(Cost cost);

	/**
	 * 
	 *
	 * @param cost Input cost object
	 * @return true if this.cost >= cost
	 */
	public abstract boolean greaterOrEquals(Cost cost);

	/**
	 * 
	 *
	 * @param cost Input cost object
	 * @return true if this.cost > cost
	 */
	public abstract boolean greaterThan(Cost cost);

	/**
	 * 
	 *
	 * @return a numeric representation of the cost
	 */
	public abstract Number getValue();
}
