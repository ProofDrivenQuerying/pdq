// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 *
 * @author Efthtymia Tsamoura
 */
public class Function {

	/** Cached instance hash. */
	protected final int hash;

	/**  Cached string representation of the function. */
	protected String rep;

	/**  Function name. */
	protected final String name;

	/**  Function arity. */
	protected final int arity;

	public Function(String name, int arity) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(!name.isEmpty());
		Preconditions.checkArgument(arity >= 0);
		this.name = name;
		this.arity = arity;
		this.hash = Objects.hash(this.name, this.arity);
		this.rep = this.makeString();
	}

	/**
	 * Gets the name of the predicate.
	 *
	 * @return the name of the predicate.
	 */
	public String getName() {
		return this.name;
	}
	

	public int getArity() {
		return this.arity;
	}
	
	/**
	 * Two functions are equal if their names and arities are equal.
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
				&& this.name.equals(((Function) o).name)
				&& this.arity == ((Function) o).arity;
	}

	@Override
	public int hashCode() {
		return this.hash;
	}

	@Override
	public String toString() {
		return this.rep;
	}

	/**
	 * Helper printing method.
	 *
	 * @return String
	 */
	private String makeString() {
		StringBuilder result = new StringBuilder();
		result.append(this.name).append('[').append(this.arity).append(']');
		return result.toString().intern();
	}
}
