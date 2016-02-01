package uk.ac.ox.cs.pdq.planner.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;

import com.google.common.collect.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class PlannerUtility {

	/**
	 * @param rule
	 * @param facts
	 * @return the constants of the input facts that correspond to the input positions of the rule
	 */
	public static Collection<Constant> getInputConstants(AccessibilityAxiom rule, Set<Predicate> facts) {
		Collection<Constant> inputs = new LinkedHashSet<>();
		for(Predicate fact:facts) {
			List<Constant> constants = fact.getConstants(rule.getAccessMethod().getZeroBasedInputs());
			for(Constant constant:constants) {
				if(constant.isSkolem()) {
					inputs.add(constant);
				}
			}
		}
		return inputs;
	}
	
	/**
	 *
	 * @param binding
	 * @param fact
	 * @return the constants in the input positions of the given fact
	 */
	public static List<Constant> getInputConstants(AccessMethod binding, Predicate fact) {
		List<Constant> ret  = fact.getConstants(binding.getZeroBasedInputs());
		return Lists.newArrayList(uk.ac.ox.cs.pdq.util.Utility.removeDuplicates(ret));
	}
}
