package uk.ac.ox.cs.pdq.planner.dag.priority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.LinearValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.ReachabilityValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;

/**
 * Distance2CoreTopK variation designed such ILP builds up all the closed left-deep configurations that are also built up by Linear.
 * For each input configuration c it returns (i) top-k configurations C' such that BinaryConfiguration(c,c'), c' belongs to C', is
 * a closed left-deep configuration and (ii) top-k configurations C'' such that BinaryConfiguration(c,c''), c'' belongs to C'', satisfies
 * the shape restrictions originally specified by the user
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class Distance2CoreTopKHardCoded extends Distance2CoreTopK{

	/** Restricts to closed left-deep configurations */
	private final Validator linear = new LinearValidator();
	private final List<Validator> additionalValidators = Lists.newArrayList(this.linear);

	/**
	 *
	 * @param validators
	 * 		Validate pairs of configurations to be composed
	 * @param potential
	 * 		Assesses the potential of a configuration to lead to the minimum-cost configuration
	 * @param topk
	 * 		Number of configurations to retain
	 * @param query
	 * 		The input query
	 * @param firingGraph
	 * 		Maintains the chase graph
	 * @param seed
	 */
	public Distance2CoreTopKHardCoded(List<Validator> validators,
			PotentialAssessor potential,
			Integer topk,
			Query<?> query,
			FiringGraph firingGraph,
			Integer seed) {
		super(validators, potential, topk, query, firingGraph, seed);
		for(Validator validator: validators) {
			if(validator instanceof ReachabilityValidator) {
				this.additionalValidators.add(validator);
			}
		}
	}

	/**
	 *
	 * @param validators
	 * 		Validate pairs of configurations to be composed
	 * @param potential
	 * 		Assesses the potential of a configuration to lead to the minimum-cost configuration
	 * @param topk
	 * 		Number of configurations to retain
	 * @param ranking
	 * 		Keeps for each chase fact its distance from the core (the canonical database)
	 * @param seed
	 */
	public Distance2CoreTopKHardCoded(
			List<Validator> validators,
			PotentialAssessor potential,
			Integer topk,
			Map<Predicate, Double> ranking,
			Integer seed) {
		super(validators, potential, topk, ranking, seed);
		for(Validator validator: validators) {
			if(validator instanceof ReachabilityValidator) {
				this.additionalValidators.add(validator);
			}
		}
	}

	/**
	 * @param left DAGConfiguration
	 * @param right Collection<DAGConfiguration>
	 * @param depth int
	 * @return Collection<DAGConfiguration>
	 */
	@Override
	public Collection<DAGChaseConfiguration> select(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right, int depth) {
		Set<DAGChaseConfiguration> openClosed = new TreeSet<>(new DAGConfigurationComparator());
		openClosed.addAll(right);
		Set<DAGChaseConfiguration> closed = new TreeSet<>(new DAGConfigurationComparator());
		for(DAGChaseConfiguration configuration:right) {
			if(configuration instanceof ApplyRule || configuration.isClosed()){
				closed.add(configuration);
			}
		}
		return this.hiddenSelect(left, openClosed, closed, depth);
	}


	/**
	 * It extracts the closed configurations from the input collection of candidate ones.
	 * This is done as Linear builds up only closed left-deep configurations (and, thus, the collections of candidates consists only from left-deep configurations)
	 * It then selects top-k configurations from the closed set C', such that C' such that BinaryConfiguration(c,c'), c' belongs to C', is
	 * a closed left-deep configuration and (ii) top-k configurations C'' such that BinaryConfiguration(c,c''), c'' belongs to C'', satisfies
	 * the shape restrictions originally specified by the user
	 * @param left
	 * @param openClosed
	 * 		Input collection of candidate configurations
	 * @param closed
	 * 		Input collection of candidate configurations that are all closed. This is a subset of the configurations in openClosed
	 * @param depth
	 * 		The target depth of the composition
	 * @return
	 * 		the 2 * top-k highest-priority configurations to combine with the left input one
	 */
	private Collection<DAGChaseConfiguration> hiddenSelect(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> openClosed, Collection<DAGChaseConfiguration> closed, int depth) {
		for(DAGChaseConfiguration configuration:openClosed) {
			Preconditions.checkNotNull(configuration.getEquivalenceClass());
			Preconditions.checkState(!configuration.getEquivalenceClass().isEmpty());
			if(!configuration.getEquivalenceClass().isSleeping() &&
					this.getPotential().getPotential(left, configuration) ){
				this.getRanking(configuration);
			}
		}

		MinMaxPriorityQueue<DAGChaseConfiguration> sOpen = MinMaxPriorityQueue.orderedBy(this.getComparator()).maximumSize(super.getTopk()).create();
		for(DAGChaseConfiguration configuration:openClosed) {
			if(this.validate(left, configuration, depth, this.getValidators()) &&
					(!left.getOutput().containsAll(configuration.getInput())) ) {
				sOpen.add(configuration);
			}
		}

		MinMaxPriorityQueue<DAGChaseConfiguration> sClosed = MinMaxPriorityQueue.orderedBy(this.getComparator()).maximumSize(super.getTopk()).create();
		for(DAGChaseConfiguration configuration:closed) {
			if(this.validate(left, configuration, depth, this.additionalValidators)) {
				sClosed.add(configuration);
			}
		}
		return CollectionUtils.union(sOpen, sClosed);
	}

	/**
	 * @return Distance2CoreTopKHardCoded
	 */
	@Override
	public Distance2CoreTopKHardCoded clone() {
		return new Distance2CoreTopKHardCoded(DefaultValidator.deepCopy(this.getValidators()),
				this.getPotential().clone(), super.getTopk(), this.getRanking(), this.getSeed());
	}

	/**
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @param depth int
	 * @param validators List<Validator>
	 * @return boolean
	 */
	protected boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth, List<Validator> validators) {
		return ConfigurationUtility.validate(left, right, validators, depth);
	}

	/**
	 * @return Integer
	 */
	@Override
	public Integer getTopk() {
		return 2 * super.getTopk();
	}

}
