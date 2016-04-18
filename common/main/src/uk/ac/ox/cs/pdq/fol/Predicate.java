package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * A predicate's signature, associate a symbol with an arity.
 *
 * @author Julien Leblay
 */
public class Predicate {

	/** Cached instance hash (only possible because predicates are immutable). */
	protected final int hash;

	/**  Cached string representation of the predicate. */
	protected String rep;

	/**  Predicate name. */
	protected final String name;

	/**  Predicate arity. */
	protected final int arity;

	/**  true, if this is the signature for an equality predicate. */
	protected final boolean equality;

	/**
	 * Constructor for Predicate.
	 * @param symbol String
	 * @param arity int
	 */
	public Predicate(String symbol, int arity) {
		this(symbol, arity, false);
	}

	/**
	 * Constructor for Predicate.
	 * @param symbol String
	 * @param arity int
	 * @param equality boolean
	 */
	public Predicate(String symbol, int arity, boolean equality) {
		Preconditions.checkArgument(symbol != null);
		Preconditions.checkArgument(!symbol.isEmpty());
		Preconditions.checkArgument(arity >= 0);
		this.name = symbol;
		this.arity = arity;
		this.equality = equality;
		this.hash = Objects.hash(this.name, this.arity);
		this.rep = this.makeString();
	}

	/**
	 * Gets the name.
	 *
	 * @return the name of the predicate.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the arity.
	 *
	 * @return the arity of the predicate.
	 */
	public int getArity() {
		return this.arity;
	}

	/**
	 * Checks if is equality.
	 *
	 * @return true if the signature is of an equality predicate,
	 * false otherwise
	 */
	public boolean isEquality() {
		return this.equality;
	}

	/**
	 * Equals.
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

	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
		return this.hash;
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return this.rep;
	}

	/**
	 * Make string.
	 *
	 * @return String
	 */
	private String makeString() {
		StringBuilder result = new StringBuilder();
		result.append(this.name).append('[').append(this.arity).append(']');
		return result.toString().intern();
	}
}
