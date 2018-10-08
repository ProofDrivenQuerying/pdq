package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import com.google.common.base.Preconditions;

// TODO: Auto-generated Javadoc
/**
 * A variable term.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Variable implements Term {
	
	/**  The variable's name. */
	private final String symbol;

	/**
	 * Instantiates a new variable.
	 *
	 * @param name The name of this variable
	 */
	public Variable(String name) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(!name.isEmpty());
		this.symbol = name;
	}


	@Override
	public boolean isVariable() {
		return true;
	}


	@Override
	public boolean isUntypedConstant() {
		return false;
	}

	/**
	 * Two variables are equal of their names are equal (using equals()).
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
				&& this.symbol.equals(((Variable) o).symbol);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.symbol);
	}

	@Override
	public String toString() {
		return this.symbol;
	}
	
	public String getSymbol() {
		return this.symbol;
	}
	
	@Override
	public Variable clone() {
		return new Variable(this.symbol);
	}
	
	/**  The default prefix of the variable terms. */
	public static final String DEFAULT_VARIABLE_PREFIX = "_";

	/**  A counter used to create new variable terms. */
	private static int freshVariableCounter = 0;

	/**
	 * Reset counter.
	 */
	public static void resetCounter() {
		Variable.freshVariableCounter = 0;
	}

	/**
	 * Gets the fresh variable.
	 *
	 * @return a new variable using the default variable prefix an integer
	 */
	public static Variable getFreshVariable() {
		return new Variable(DEFAULT_VARIABLE_PREFIX + (freshVariableCounter++));
	}
}