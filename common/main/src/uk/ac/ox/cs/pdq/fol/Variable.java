package uk.ac.ox.cs.pdq.fol;

import java.util.Objects;

import uk.ac.ox.cs.pdq.util.Named;

import com.google.common.base.Preconditions;

/**
 * A variable term
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public class Variable implements Named, Term {

	/** The default prefix of the variable terms */
	public static final String DEFAULT_VARIABLE_PREFIX = "_";

	/** A counter used to create new variable terms */
	private static int freshVariableCounter = 0;

	/** The variable's name*/
	private final String name;

	public static void resetCounter() {
		Variable.freshVariableCounter = 0;
	}

	/**
	 * @return a new variable using the default variable prefix an integer
	 */
	public static Variable getFreshVariable() {
		return new Variable(DEFAULT_VARIABLE_PREFIX + (freshVariableCounter++));
	}

	/**
	 * @param name The name of this variable
	 */
	public Variable(String name) {
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(!name.isEmpty());
		this.name = name;
	}

	/**
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isVariable()
	 */
	@Override
	public boolean isVariable() {
		return true;
	}

	/**
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.fol.Term#isSkolem()
	 */
	@Override
	public boolean isSkolem() {
		return false;
	}

	/**
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
				&& this.name.equals(((Variable) o).name);
	}

	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}

	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * @return String
	 * @see uk.ac.ox.cs.pdq.util.Named#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}
}