package uk.ac.ox.cs.pdq.planner.dag.priority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;

import com.google.common.base.Preconditions;
import com.google.common.collect.MinMaxPriorityQueue;


/**
 * Returns all configurations that are within a specific distance from the core
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class Distance2CoreRange extends Distance2CoreTopK{

	/** The maximum distance of a configuration from the core */
	private final Integer range;

	/**
	 * @param validators Validate pairs of configurations to be composed
	 * @param potential Assesses the potential of a configuration to lead to the minimum-cost configuration
	 * @param range The maximum distance of a configuration from the core
	 * @param query The input query
	 * @param firingGraph Maintains the chase graph
	 * @param seed
	 */
	public Distance2CoreRange(List<Validator> validators,
			PotentialAssessor potential,
			Integer range,
			Query<?> query,
			FiringGraph firingGraph,
			Integer seed) {
		super(validators, potential, Integer.MAX_VALUE, query, firingGraph, seed);
		this.range = range;
	}

	/**
	 * @param validators Validate pairs of configurations to be composed
	 * @param potential Assesses the potential of a configuration to lead to the minimum-cost configuration
	 * @param range The maximum distance of a configuration from the core
	 * @param ranking Keeps for each chase fact its distance from the core (the canonical database)
	 * @param seed
	 */
	public Distance2CoreRange(
			List<Validator> validators,
			PotentialAssessor potential,
			Integer range,
			Map<Predicate, Double> ranking,
			Integer seed) {
		super(validators, potential, Integer.MAX_VALUE, ranking, seed);
		this.range = range;
	}

	/**
	 *
	 * @param left
	 * @param right Candidate configurations to combine with the left input one
	 * @param depth The target depth of the composition
	 * @return the configurations to combine with the left input one that are within a specific distance from the core
	 */
	@Override
	public Collection<DAGChaseConfiguration> select(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right, int depth) {
		Set<DAGChaseConfiguration> pool = new TreeSet<>(new DAGConfigurationComparator());
		pool.addAll(right);
		return this.hiddenSelect(left, pool, depth);
	}

	/**
	 * @param left DAGConfiguration
	 * @param right Collection<DAGConfiguration>
	 * @param depth int
	 * @return Collection<DAGConfiguration>
	 */
	private Collection<DAGChaseConfiguration> hiddenSelect(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right, int depth) {
		MinMaxPriorityQueue<DAGChaseConfiguration> selected = MinMaxPriorityQueue.orderedBy(this.getComparator()).maximumSize(this.getTopk()).create();
		for(DAGChaseConfiguration configuration:right) {
			Preconditions.checkNotNull(configuration.getEquivalenceClass());
			Preconditions.checkState(!configuration.getEquivalenceClass().isEmpty());
			if(!configuration.getEquivalenceClass().isSleeping() &&
					this.validate(left, configuration, depth) &&
					this.getPotential().getPotential(left, configuration) ){
				this.getRanking(configuration);
				if(configuration.getRanking() <= this.range) {
					selected.add(configuration);
				}
			}
		}
		return selected;
	}

	/**
	 * @return Distance2CoreRange
	 */
	@Override
	public Distance2CoreRange clone() {
		return new Distance2CoreRange(DefaultValidator.deepCopy(this.getValidators()),
				this.getPotential().clone(), this.range, this.getRanking(), this.getSeed());
	}
}
