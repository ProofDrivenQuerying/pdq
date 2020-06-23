// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.sqlserverhistogram;

import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * Density vectors for SQL Server 2014.
 * For more details see https://msdn.microsoft.com/en-gb/library/ms174384.aspx 
 * @author Efthymia Tsamoura
 *
 */
public class SQLServerDensity {
	
	/** The attributes over which we build a density vector. **/
	private final List<Attribute> attributes;
	
	/** Density is 1 / distinct values. **/
	private final double density;
	
	/**
	 * Constructor for SQL density vectors.
	 * @param attributes
	 * 		The attributes over which we build a density vector.
	 * @param density
	 * 		Density is 1 / distinct values.
	 */
	public SQLServerDensity (List<Attribute> attributes, double density) {
		Preconditions.checkNotNull(attributes);
		Preconditions.checkArgument(!attributes.isEmpty());
		Preconditions.checkNotNull(density);
		this.attributes = attributes;
		this.density = density;
	}

	/**
	 * Gets the attributes.
	 *
	 * @return the attributes
	 */
	public List<Attribute> getAttributes() {
		return this.attributes;
	}

	/**
	 * Gets the density.
	 *
	 * @return the density
	 */
	public double getDensity() {
		return this.density;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Joiner.on("\t").join(this.attributes) + "\t" + this.density;
	}

	/**
	 * Equals.
	 *
	 * @param o Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		return super.equals(o)
				&& this.getClass().isInstance(o)
				&& this.attributes.equals(((SQLServerDensity) o).attributes) 
				&& this.density == ((SQLServerDensity) o).density
				;

	}

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.attributes, this.density);
	}
}
