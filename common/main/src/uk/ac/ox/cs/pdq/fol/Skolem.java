package uk.ac.ox.cs.pdq.fol;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import uk.ac.ox.cs.pdq.util.Named;

import com.google.common.base.Preconditions;


// TODO: Auto-generated Javadoc
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
		if (this.hash == Integer.MIN_VALUE) {
			this.hash = Objects.hash(this.name);
		}
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
	
	/**
	 * Generates constants for existentially quantified variables.
	 * It is called when a dependency is grounded
	 *
	 * @author Efthymia Tsamoura
	 * @author Julien Leblay
	 *
	 */

	public static final class Generator {

		/** The Constant DEFAULT_PREFIX. */
		private static final String DEFAULT_PREFIX = "c";
		
		/** The Constant CANONICAL_PREFIX. */
		private static final String CANONICAL_PREFIX = "k";

		/** The global id. */
		private static int globalId = 0;

		/** Index storing all canonical name stored so far (value) and the canonical string there where generated from (key). */
		private static Map<String, String> skolems = new LinkedHashMap<>();

		/**
		 * Gets the name.
		 *
		 * @param dependency 		The input dependency
		 * @param universalVariables 		A string of universal variable-universal variable grounding pairs for all universally quantified variable
		 * @param existentialVariable 		The existential variable
		 * @return 		a canonical name which equals to the dependency name + the assignment
		 * 		of the canonical names to universal variables + the existential's variable name
		 */
		public static String getName(String dependency, String universalVariables, String existentialVariable) {
			String key = dependency + existentialVariable + universalVariables;
			String result = skolems.get(key);
			if (result == null) {
				result = getName(CANONICAL_PREFIX);
				skolems.put(key, result);
			}
			return result;
		}

		/**
		 * Gets the name.
		 *
		 * @param prefix the prefix
		 * @return a fresh constant name with the given prefix.
		 */
		private static String getName(String prefix) {
			return prefix + (++globalId);
		}

		/**
		 * Gets the name.
		 *
		 * @return a fresh constant name with the default prefix.
		 */
		public static String getName() {
			return getName(DEFAULT_PREFIX);
		}
	}
}
