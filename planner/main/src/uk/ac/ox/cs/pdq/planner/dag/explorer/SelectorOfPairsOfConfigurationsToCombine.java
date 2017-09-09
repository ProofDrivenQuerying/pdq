package uk.ac.ox.cs.pdq.planner.dag.explorer;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;

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
	private final List<Validator> validators;

	private final Set<RelationalTerm> plansOfConfigurationPairsReturnedInThePast = Sets.newLinkedHashSet();

	private Pair<DAGChaseConfiguration, DAGChaseConfiguration> inverseBinaryConfiguration = null;

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
			List<Validator> validators) {
		Preconditions.checkNotNull(left);
		Preconditions.checkNotNull(right);
		Preconditions.checkNotNull(validators);
		this.leftSideConfigurations = left;
		this.rightSideConfigurations = right;
		this.validators = validators;
	}

	/**
	 * Makes sure every returned configuration pair has the required depth and it is a unique pair that was never returned before. 
	 * Uses the validators to further shorten the list. 
	 * When only the default validator is in use it will make sure that the trivial case is filtered out (An access will not be combined with itself).  
	 *
	 * @param depth the depth
	 * @return the next pair of configurations of the given combined depth
	 */
	public Pair<DAGChaseConfiguration, DAGChaseConfiguration> getNextPairOfConfigurationsToCompose(int depth) {
		if(!this.returnInverseBinaryConfiguration) {
			if(this.indexOverConfigurationsInLeftList >= this.leftSideConfigurations.size() || this.indexOverConfigurationsInRightList >= this.rightSideConfigurations.size()) {
				return null;
			}
			DAGChaseConfiguration l = null;
			DAGChaseConfiguration r = null;
			boolean binaryConfigurationOverLeftAndRightInputsValid = false;
			boolean binaryConfigurationOverRightAndLeftInputsValid = false;
			do {
				l = this.leftSideConfigurations.get(this.indexOverConfigurationsInLeftList);
				r = this.rightSideConfigurations.get(this.indexOverConfigurationsInRightList);
				if (!this.plansOfConfigurationPairsReturnedInThePast.contains(PlanCreationUtility.createPlan(l.getPlan(), r.getPlan()))) {
					binaryConfigurationOverLeftAndRightInputsValid = ConfigurationUtility.validate(l, r, this.validators, depth);
					binaryConfigurationOverRightAndLeftInputsValid = ConfigurationUtility.validate(r, l, this.validators, depth);
					if (binaryConfigurationOverLeftAndRightInputsValid || binaryConfigurationOverRightAndLeftInputsValid) {
						break;
					}
				}
				this.moveIndicesOverLeftAndRightListsForward();
				if (this.indexOverConfigurationsInLeftList >= this.leftSideConfigurations.size()) {
					return null;
				}
			} while(this.indexOverConfigurationsInRightList < this.rightSideConfigurations.size());
			
			if(binaryConfigurationOverLeftAndRightInputsValid) {
				if(binaryConfigurationOverRightAndLeftInputsValid) {
					this.plansOfConfigurationPairsReturnedInThePast.add(PlanCreationUtility.createPlan(r.getPlan(), l.getPlan()));
					this.inverseBinaryConfiguration = Pair.of(r, l);
					this.returnInverseBinaryConfiguration = true;
				}
				this.plansOfConfigurationPairsReturnedInThePast.add(PlanCreationUtility.createPlan(l.getPlan(), r.getPlan()));
				return Pair.of(l, r);
			}
			else if(binaryConfigurationOverRightAndLeftInputsValid) {
				this.returnInverseBinaryConfiguration = false;
				this.plansOfConfigurationPairsReturnedInThePast.add(PlanCreationUtility.createPlan(r.getPlan(), l.getPlan()));
				return Pair.of(r,l);
			}
			else
				return null;
		}
		else {
			this.returnInverseBinaryConfiguration = false;
			return this.inverseBinaryConfiguration;
		}
	}

	private void moveIndicesOverLeftAndRightListsForward() {
		this.indexOverConfigurationsInRightList++;
		if (this.indexOverConfigurationsInRightList >= this.rightSideConfigurations.size()) {
			this.indexOverConfigurationsInLeftList++;
			this.indexOverConfigurationsInRightList = 0;
		}
	}
}
