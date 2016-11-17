package uk.ac.ox.cs.pdq.planner.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class PlannerUtility.
 *
 * @author Efthymia Tsamoura
 */
public class PlannerUtility {

	/**
	 * Gets the input constants.
	 *
	 * @param rule the rule
	 * @param facts the facts
	 * @return the constants of the input facts that correspond to the input positions of the rule
	 */
	public static Collection<Constant> getInputConstants(AccessibilityAxiom rule, Set<Atom> facts) {
		Collection<Constant> inputs = new LinkedHashSet<>();
		for(Atom fact:facts) {
			List<Constant> constants = fact.getConstants(rule.getAccessMethod().getZeroBasedInputs());
			for(Constant constant:constants) {
				if(constant.isUntypedConstant()) {
					inputs.add(constant);
				}
			}
		}
		return inputs;
	}
	
	/**
	 * Gets the input constants.
	 *
	 * @param binding the binding
	 * @param fact the fact
	 * @return the constants in the input positions of the given fact
	 */
	public static List<Constant> getInputConstants(AccessMethod binding, Atom fact) {
		List<Constant> ret  = fact.getConstants(binding.getZeroBasedInputs());
		return Lists.newArrayList(uk.ac.ox.cs.pdq.util.Utility.removeDuplicates(ret));
	}
}
