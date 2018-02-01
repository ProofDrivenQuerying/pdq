package uk.ac.ox.cs.pdq.reasoning.chase;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * 
 * Generates constants for existentially quantified variables. It is called when
 * a dependency is grounded
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */

public final class ChaseConstantGenerator {

	/**
	 * The DEFAULT_PREFIX for canonical names. Used for example to create cannonicalQuerries. 
	 */
	private static final String CANONICAL_CONSTANT_PREFIX = "c";

	/** The . */
	private static final String NON_CANONICAL_CONSTANT_PREFIX = "k";

	/**
	 * Index storing all canonical name stored so far (value) and the canonical
	 * string there where generated from (key).
	 */
	private static Map<String, String> cachedLabelledNulls = new LinkedHashMap<>();

	/**
	 * Gets the name if it exists, or generates a new skolem name.
	 *
	 * @param dependency
	 *            The input dependency
	 * @param universalVariables
	 *            A string of universal variable-universal variable grounding pairs
	 *            for all universally quantified variable
	 * @param existentialVariable
	 *            The existential variable
	 * @return a canonical name which equals to the dependency name + the assignment
	 *         of the canonical names to universal variables + the existential's
	 *         variable name
	 */
	public static String getTriggerWitness(Dependency dependency, Map<Variable, Constant> mapping, Variable existentialVariable) {
		String namesOfUniversalVariables = "";
		for (Variable variable : dependency.getUniversal()) {
			Variable variableTerm = variable;
			Preconditions.checkState(mapping.get(variableTerm) != null);
			namesOfUniversalVariables += variable.getSymbol() + mapping.get(variableTerm);
		}
		String key = "TGD" + dependency.getId() + existentialVariable.getSymbol() + namesOfUniversalVariables;
		String result = cachedLabelledNulls.get(key);
		if (result == null) {
			result = getName(NON_CANONICAL_CONSTANT_PREFIX);
			cachedLabelledNulls.put(key, result);
		}
		return result;
	}

	/**
	 * Gets the name by generating a new skolem name.
	 *
	 * @param prefix
	 *            the prefix
	 * @return a fresh constant name with the given prefix.
	 */
	private static String getName(String prefix) {
		return prefix + GlobalCounterProvider.getNext("CannonicalName");
	}

	/**
	 * Gets a new name (Canonical constant)
	 *
	 * @return a fresh constant name with the default prefix.
	 */
	public static String getName() {
		return getName(CANONICAL_CONSTANT_PREFIX);
	}
}