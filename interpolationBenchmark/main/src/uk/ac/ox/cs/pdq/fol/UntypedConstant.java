package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import com.google.common.base.Preconditions;


/**
 * A Skolem constant term.
 *
 * @author Julien Leblay
 */
public final class UntypedConstant implements Constant {

	/**  The constant's name. */
	private final String symbol;

	/** Cached String representation of a variable. */
	private String rep = null;

	/**
	 * Instantiates a new skolem.
	 *
	 * @param name The name of the constant
	 */
	public UntypedConstant(String name) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(!name.isEmpty());
		this.symbol = name;
	}

	/**
	 * Two skolems are equal if their names are equal.
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
				&& this.symbol.equals(((UntypedConstant) o).symbol);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.symbol);
	}

	@Override
	public String toString() {
		if (this.rep == null) {
			StringBuilder result = new StringBuilder();
			result.append(this.symbol);
			this.rep = result.toString().intern();
		}
		return this.rep;
	}

	/**
	 * Gets the skolem's name.
	 *
	 * @return String
	 * @see uk.ac.ox.cs.pdq.util.Named#getName()
	 */
	public String getSymbol() {
		return this.symbol;
	}

	/**
	 * TOCOMMENT I suggest this goes, something is a skolem if it is instance of Skolem
	 * 
	 * Checks if is skolem.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isSkolem()
	 */
	@Override
	public boolean isUntypedConstant() {
		return true;
	}

	/**
	 * TOCOMMENT I suggest this goes, something is a variable if it is instance of Variable
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isVariable()
	 */
	@Override
	public boolean isVariable() {
		return false;
	}
	
	@Override
	public UntypedConstant clone() {
		return new UntypedConstant(this.symbol);
	}

	/**  The default prefix of the constant terms. */
	public static final String DEFAULT_CONSTANT_PREFIX = "c";

	/**   A counter used to create new constant terms. */
	private static int freshConstantCounter = 0;

	/**
	 * Reset counter.
	 */
	public static void resetCounter() {
		UntypedConstant.freshConstantCounter = 0;
	}

	/**
	 * Gets the fresh constant.
	 *
	 * @return Skolem
	 */
	public static UntypedConstant getFreshConstant() {
		return new UntypedConstant(DEFAULT_CONSTANT_PREFIX + (freshConstantCounter++));
	}
}
