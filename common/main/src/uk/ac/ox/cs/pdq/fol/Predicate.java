package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * A predicate's signature, associate a symbol with an arity.
 *
 * @author Julien Leblay
 */
public class Predicate {

	/**  Predicate name. */
	protected final String name;

	/**  Predicate arity. */
	protected final int arity;

	/**  true, if this is the signature for an equality predicate. */
	protected final boolean isEquality;

	/**
	 * Constructor for Predicate.
	 * @param name String
	 * @param arity int
	 * @param equality boolean
	 */
	public Predicate(String name, int arity) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(!name.isEmpty());
		Preconditions.checkArgument(arity >= 0);
		this.name = name;
		this.arity = arity;
		this.isEquality = false;
		this.hash = Objects.hash(this.name, this.arity);
		this.rep = this.makeString();
	}
	
	/**
	 * Constructor for Predicate.
	 * @param name String
	 * @param arity int
	 * @param equality boolean
	 */
	public Predicate(String name, int arity, boolean isEquality) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(!name.isEmpty());
		Preconditions.checkArgument(arity >= 0);
		this.name = name;
		this.arity = arity;
		this.isEquality = isEquality;
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
	 * Checks if this is an equality predicate.
	 *
	 * @return true if the signature is of an equality predicate,
	 * false otherwise
	 */
	public boolean isEquality() {
		return this.isEquality;
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
				&& this.name.equals(((Predicate) o).name)
				&& this.arity == ((Predicate) o).arity;
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
