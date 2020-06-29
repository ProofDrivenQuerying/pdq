// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DagChaseConfigurationValidation;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.PairValidator;
import uk.ac.ox.cs.pdq.planner.plancreation.PlanCreationUtility;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;

/**
 * Returns pairs of configurations to combine.
 *
 * @author Efthymia Tsamoura
 * @param <S> the generic type
 */
public class SelectorOfPairsOfConfigurationsToCombine<S extends AccessibleChaseInstance> {

	/**  Configurations to consider on the left. */
	private List<DAGChaseConfiguration> leftSideConfigurations;

	/**  Configurations to consider on the right. */
	private List<DAGChaseConfiguration> rightSideConfigurations;
	
	/** Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions. */
	private final List<PairValidator> validators;

	private final Set<RelationalTerm> plansOfConfigurationPairsReturnedInThePast = Sets.newLinkedHashSet();

	private Pair<DAGChaseConfiguration, DAGChaseConfiguration> inverseBinaryConfiguration = null;
	private RelationalTerm inverseBinaryPlan = null;

	private int indexOverConfigurationsInLeftList = 0;

	private int indexOverConfigurationsInRightList = 0;

	private boolean returnInverseBinaryConfiguration = false;

	/**
	 * Instantiates a new pair selector.
	 *
	 * @param left 		Configurations to consider on the left
	 * @param right 		Configurations to consider on the right
	 * @param validators 		Checks whether the binary configuration composed from a given configuration pair satisfies given shape restrictions
	 */
	public SelectorOfPairsOfConfigurationsToCombine(
			List<DAGChaseConfiguration> left,
			List<DAGChaseConfiguration> right,
			List<PairValidator> validators) {
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		Preconditions.checkNotNull(validators);
		this.leftSideConfigurations = left;
		this.rightSideConfigurations = right;
		this.validators = validators;
	}

	/**
	 * Creates combinations of accesses and on each call it returns the next
	 * combination.Makes sure every returned combination is unique. Uses validators
	 * to filter out not required combinations.
	 * 
	 * Using the default validator it makes sure - every returned configuration pair
	 * has the required depth - the trivial case is filtered out (An access will not
	 * be combined with itself).
	 * 
	 *
	 * @param depth
	 *            the required depth, used only as input for validators.
	 * @return the next pair of configurations of the given combined depth
	 */
	public Pair<DAGChaseConfiguration, DAGChaseConfiguration> getNextPairOfConfigurationsToCompose(int depth) {
		if (this.returnInverseBinaryConfiguration) {
			
			this.plansOfConfigurationPairsReturnedInThePast.add(this.inverseBinaryPlan);
			this.inverseBinaryPlan = null;
			this.returnInverseBinaryConfiguration = false;
			return this.inverseBinaryConfiguration;
		}
		if (this.indexOverConfigurationsInLeftList >= this.leftSideConfigurations.size() || this.indexOverConfigurationsInRightList >= this.rightSideConfigurations.size()) {
			return null;
		}
		do {
			DAGChaseConfiguration l = this.leftSideConfigurations.get(this.indexOverConfigurationsInLeftList);
			DAGChaseConfiguration r = this.rightSideConfigurations.get(this.indexOverConfigurationsInRightList);
			RelationalTerm leftToRightPlan = PlanCreationUtility.createJoinPlan(l.getPlan(), r.getPlan());
			RelationalTerm rightToleftPlan = PlanCreationUtility.createJoinPlan(r.getPlan(), l.getPlan());
			if (!this.plansOfConfigurationPairsReturnedInThePast.contains(leftToRightPlan)
					&& DagChaseConfigurationValidation.validate(l, r, this.validators, depth)) {
				// check if we can return the inverse next time
				if (!this.plansOfConfigurationPairsReturnedInThePast.contains(rightToleftPlan)
						&& DagChaseConfigurationValidation.validate(r, l, this.validators, depth)) {
					this.inverseBinaryConfiguration = Pair.of(r, l);
					this.inverseBinaryPlan = rightToleftPlan;
					this.returnInverseBinaryConfiguration = true;
				}
				this.plansOfConfigurationPairsReturnedInThePast.add(leftToRightPlan);
				return Pair.of(l, r);
				// in case (l,r) is not new and valid we check the r,l combination
			} else if (!this.plansOfConfigurationPairsReturnedInThePast.contains(leftToRightPlan)
					&& DagChaseConfigurationValidation.validate(r, l, this.validators, depth)) {
				this.plansOfConfigurationPairsReturnedInThePast.add(leftToRightPlan);
				return Pair.of(l, r);
			}
			this.moveIndicesOverLeftAndRightListsForward();
		} while (this.indexOverConfigurationsInLeftList < this.leftSideConfigurations.size());
		return null;
	}

	private void moveIndicesOverLeftAndRightListsForward() {
		this.indexOverConfigurationsInRightList++;
		if (this.indexOverConfigurationsInRightList >= this.rightSideConfigurations.size()) {
			this.indexOverConfigurationsInLeftList++;
			this.indexOverConfigurationsInRightList = 0;
		}
	}
}
