package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * A predicate's signature, associate a symbol with an arity.
 *
 * @author Julien Leblay
 */
public class Function {

	/** Cached instance hash (only possible because predicates are immutable). */
	protected final int hash;

	/**  Cached string representation of the predicate. */
	protected String rep;

	/**  Predicate name. */
	protected final String name;

	/**  Predicate arity. */
	protected final int arity;

	/**
	 * Constructor for Predicate.
	 * @param name String
	 * @param arity int
	 * @param equality boolean
	 */
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

	/**
	 * Gets the arity of the predicate.
	 *
	 * @return the arity of the predicate.
	 */
	public int getArity() {
		return this.arity;
	}
	
	/**
	 * Two predicates are equal if their names and arities are equal.
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
