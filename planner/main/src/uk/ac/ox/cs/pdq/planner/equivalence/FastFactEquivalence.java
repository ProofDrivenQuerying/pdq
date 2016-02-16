package uk.ac.ox.cs.pdq.planner.equivalence;

import java.util.Set;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

import com.google.common.collect.Sets;


// TODO: Auto-generated Javadoc
/**
 * Fast fact equivalence.
 * According to this implementation two configurations c and c' are equivalent if the have the same inferred accessible facts. 
 * In order to perform this kind of equivalence check Skolem constants must be assigned to formula variables during chasing.
 *
 * @author Efthymia Tsamoura
 */
public class FastFactEquivalence implements FactEquivalence{

	/**
	 * Checks if is equivalent.
	 *
	 * @param source ChaseConfiguration
	 * @param target ChaseConfiguration
	 * @return true if source and target configurations are equivalent
	 */
	@Override
	public boolean isEquivalent(ChaseConfiguration source, ChaseConfiguration target) {
		Set<Constant> inputs1 = Sets.newLinkedHashSet(source.getInput());
		Set<Constant> inputs2 = Sets.newLinkedHashSet(target.getInput());
		if(inputs1.equals(inputs2)) {
			if (source.getState().getInferred().equals(target.getState().getInferred())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Clone.
	 *
	 * @return FastFactDominance
	 */
	@Override
	public FastFactEquivalence clone() {
		return new FastFactEquivalence();
	}
}