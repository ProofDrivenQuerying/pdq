package uk.ac.ox.cs.pdq.planner.dag.priority;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Returns all configurations that satisfy given shape restrictions
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class DefaultPriorityAssessor extends PriorityAssessor{

	/**
	 *
	 * @param validators
	 * 		Validate pairs of configurations to be composed
	 * @param potential
	 * 		Assesses the potential of a configuration to lead to the minimum-cost configuration
	 */
	public DefaultPriorityAssessor(
			List<Validator> validators,
			PotentialAssessor potential) {
		super(validators, potential, Integer.MAX_VALUE);
	}

	/**
	 *
	 * @param left
	 * @param right
	 * 		Candidate configurations to combine with the left input one
	 * @param depth
	 * 		The target depth of the composition
	 * @return all configurations in the right input collection that satisfy given shape restrictions
	 */
	@Override
	public Collection<DAGChaseConfiguration> select(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right, int depth) {
		Set<DAGChaseConfiguration> selected = Sets.newLinkedHashSet();
		for(DAGChaseConfiguration configuration:right) {
			Preconditions.checkNotNull(configuration.getEquivalenceClass());
			Preconditions.checkState(!configuration.getEquivalenceClass().isEmpty());
			if(!configuration.getEquivalenceClass().isSleeping() &&
					this.validate(left, configuration, depth) &&
					this.getPotential().getPotential(left, configuration) ){
				selected.add(configuration);
			}
		}
		return selected;
	}

	/**
	 * @return DefaultPriorityAssessor
	 */
	@Override
	public DefaultPriorityAssessor clone() {
		return new DefaultPriorityAssessor(DefaultValidator.deepCopy(this.getValidators()), this.getPotential().clone());
	}
}