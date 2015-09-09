package uk.ac.ox.cs.pdq.planner.dag.priority;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor;

import com.google.common.base.Preconditions;

/**
 * Prioritises configurations
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */

public abstract class PriorityAssessor {

	/**  Validate pairs of configurations to be composed, for example,
	they check whether or not the binary configuration given pair satisfies given shape restrictions.  */
	private final List<Validator> validators;

	/** Assesses the potential of a configuration to lead to the minimum-cost configuration */
	private final PotentialAssessor potential;

	/** Number of configurations to retain*/
	private final Integer topk;

	/**
	 *
	 * @param validators
	 * 		Validate pairs of configurations to be composed
	 * @param potential
	 * 		Assesses the potential of a configuration to lead to the minimum-cost configuration
	 * @param topk
	 * 		Number of configurations to retain
	 */
	public PriorityAssessor(List<Validator> validators, PotentialAssessor potential, Integer topk) {
		Preconditions.checkNotNull(validators);
		Preconditions.checkArgument(!validators.isEmpty());
		Preconditions.checkNotNull(potential);
		this.validators = validators;
		this.potential = potential;
		this.topk = topk;
	}

	/**
	 * @param left
	 * @param right Candidate configurations to combine with the left input one
	 * @param depth The target depth of the composition
	 * @return the highest-priority configurations to combine with the left input one
	 */
	public abstract Collection<DAGChaseConfiguration> select(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right, int depth);

	/**
	 * @return List<Validator>
	 */
	public List<Validator> getValidators() {
		return this.validators;
	}

	/**
	 * @return PotentialAssessor
	 */
	public PotentialAssessor getPotential() {
		return this.potential;
	}

	/**
	 * @param left
	 * @param right
	 * @param depth
	 * @return
	 * 		true if the binary configuration composed from the left and right input configurations passes the validation tests,
	 * 		i.e., satisfies given shape restrictions.
	 * 		If depth > 0, then the corresponding binary configuration must be of the given depth.
	 */
	protected boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth) {
		return ConfigurationUtility.validate(left, right, this.validators, depth);
	}

	/**
	 * @return PriorityAssessor
	 */
	@Override
	public abstract PriorityAssessor clone();

	/**
	 * @return Integer
	 */
	public Integer getTopk() {
		return this.topk;
	}
}
