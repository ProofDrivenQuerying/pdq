package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import com.google.common.base.Preconditions;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public final class UntypedConstant implements Constant {

	/**  The constant's name. */
	private final String symbol;

	/** Cached String representation. */
	private String rep = null;

	public UntypedConstant(String name) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(!name.isEmpty());
		this.symbol = name;
	}

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

	public String getSymbol() {
		return this.symbol;
	}

	@Override
	public boolean isUntypedConstant() {
		return true;
	}

	/**
	 * TOCOMMENT I suggest this goes, something is a variable if it is instance of Variable
	 *
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
	 * Gets the fresh constant.
	 *
	 */
	public static UntypedConstant getFreshConstant() {
		return new UntypedConstant(DEFAULT_CONSTANT_PREFIX + (freshConstantCounter++));
	}
}
