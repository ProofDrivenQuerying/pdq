package uk.ac.ox.cs.pdq.fol;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * TOCOMMENT rename to ChaseConstantGenerator, move this to reasoning, connected to ConjunctiveQuery cleanup as well.
 * 
 * Generates constants for existentially quantified variables. It is called when
 * a dependency is grounded
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */

public final class CanonicalNameGenerator {

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
	 * TOCOMMENT The follwoing three methods probably return a new skolem name. Gets
	 * the name.
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
	 * Gets the name.
	 *
	 * @param prefix
	 *            the prefix
	 * @return a fresh constant name with the given prefix.
	 */
	private static String getName(String prefix) {
		return prefix + GlobalCounterProvider.getNext("CannonicalName");
	}

	/**
	 * Gets the name.
	 *
	 * @return a fresh constant name with the default prefix.
	 */
	public static String getName() {
		return getName(CANONICAL_CONSTANT_PREFIX);
	}
}