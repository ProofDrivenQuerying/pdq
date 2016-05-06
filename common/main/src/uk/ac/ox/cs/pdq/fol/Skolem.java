package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import uk.ac.ox.cs.pdq.util.Named;

import com.google.common.base.Preconditions;


/**
 * A Skolem constant term.
 *
 * @author Julien Leblay
 */
public final class Skolem implements Named, Constant {

	/**  The default prefix of the constant terms. */
	public static final String DEFAULT_CONSTANT_PREFIX = "c";

	/**   A counter used to create new constant terms. */
	private static int freshConstantCounter = 0;

	/**
	 * Reset counter.
	 */
	public static void resetCounter() {
		Skolem.freshConstantCounter = 0;
	}

	/**
	 * Gets the fresh constant.
	 *
	 * @return Skolem
	 */
	public static Skolem getFreshConstant() {
		return new Skolem(DEFAULT_CONSTANT_PREFIX + (freshConstantCounter++));
	}

	/**  The constant's name. */
	private final String name;

	/** Cached instance hash (only possible because variables are immutable). */
	private int hash = Integer.MIN_VALUE;

	/** Cached String representation of a variable. */
	private String rep = null;

	/**
	 * Instantiates a new skolem.
	 *
	 * @param name The name of the constant
	 */
	public Skolem(String name) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(!name.isEmpty());
		this.name = name;
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
				&& this.name.equals(((Skolem) o).name);
	}


	/**
	 * Hash code.
	 *
	 * @return int
	 */
	@Override
	public int hashCode() {
//		if (this.hash == Integer.MIN_VALUE) {
			this.hash = Objects.hash(this.name);
//		}
		return this.hash;
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		if (this.rep == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.name);
			this.rep = result.toString().intern();
		}
		return this.rep;
	}

	/**
	 * Gets the name.
	 *
	 * @return String
	 * @see uk.ac.ox.cs.pdq.util.Named#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Checks if is skolem.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isSkolem()
	 */
	@Override
	public boolean isSkolem() {
		return true;
	}

	/**
	 * Checks if is variable.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isVariable()
	 */
	@Override
	public boolean isVariable() {
		return false;
	}
	
	@Override
	public Skolem clone() {
		return new Skolem(this.name);
	}
}
