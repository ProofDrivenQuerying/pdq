package uk.ac.ox.cs.pdq.planner.dag.explorer.validators;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.collections4.CollectionUtils;

import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.Filter;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.ReachabilityFilter;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;


/**
 * Requires the input configurations to be non trivial and to be either neighbouring in the chase graph or the left configuration to expose constants
 * that are input for the right configuration
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class ReachabilityValidator implements Validator{

	/** Currently not used*/
	private final Collection<DAGChaseConfiguration> forbidden;
	/** Maintains information w.r.t. chasing plus answers proximity queries*/
	private final FiringGraph firingGraph;
	/** The database core*/
	private final Collection<Predicate> core;

	/**
	 * Constructor for ReachabilityValidator.
	 * @param query Query
	 * @param input Collection<DAGConfiguration>
	 * @param firingGraph FiringGraph
	 */
	public ReachabilityValidator(Query<?> query, Collection<DAGChaseConfiguration> input, FiringGraph firingGraph) {
		Filter reachabilityFilter = new ReachabilityFilter<>(query);
		this.firingGraph = firingGraph;
		this.forbidden = reachabilityFilter.filter(input);
		this.core = query.getCanonical().getPredicates();
	}

	/**
	 * Constructor for ReachabilityValidator.
	 * @param forbidden Collection<DAGConfiguration>
	 * @param core Collection<PredicateFormula>
	 * @param firingGraph FiringGraph
	 */
	private ReachabilityValidator(Collection<DAGChaseConfiguration> forbidden, Collection<Predicate> core, FiringGraph firingGraph) {
		this.forbidden = forbidden;
		this.core = core;
		this.firingGraph = firingGraph;
	}

	/**
	 *
	 * @param right
	 * @return true if the right configuration contains at least one core fact
	 */
	private boolean isCoreConfiguration(DAGChaseConfiguration right) {
		for(ApplyRule applyRule:right.getApplyRules()) {
			if(CollectionUtils.containsAny(applyRule.getFacts(), this.core)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right) {
		return ConfigurationUtility.isNonTrivial(left, right)
				&& (CollectionUtils.containsAny(left.getProperOutput(), right.getInput())
						|| this.isCoreConfiguration(right)
						|| this.areNeighbors(this.firingGraph, left, right));
	}

	/**
	 * @param left DAGConfiguration
	 * @param right DAGConfiguration
	 * @param depth int
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.dag.explorer.validators.Validator#validate(DAGConfiguration, DAGConfiguration, int)
	 */
	@Override
	public boolean validate(DAGChaseConfiguration left, DAGChaseConfiguration right, int depth) {
		return left.getHeight() + right.getHeight() == depth && this.validate(left, right);
	}

	/**
	 * @return Validator
	 * @see uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator#clone()
	 */
	@Override
	public Validator clone() {
		return new ReachabilityValidator(this.forbidden, this.core, this.firingGraph);
	}

	/**
	 * @return Collection<DAGConfiguration>
	 */
	public Collection<DAGChaseConfiguration> getForbidden() {
		return this.forbidden;
	}
	
	
	/**
	 *
	 * @param source
	 * 		Input dag configuration
	 * @param target
	 * 		Input dag configuration
	 * @return
	 * 		true if the input dag configurations are neighbours.
	 * 		Two dag configurations are neighbours if they corresponding ApplyRule sub-configurations consists of facts that are neighbouring in the chase graph. * @see uk.ac.ox.cs.pdq.chase.FiringGraph#areNeighbors(DAGConfiguration, DAGConfiguration)
	 */
	public boolean areNeighbors(FiringGraph firingGraph, DAGChaseConfiguration source, DAGChaseConfiguration target) {

		//Get the ApplyRule configurations of the source configuration
		Collection<Predicate> sources = new HashSet<>();
		for(ApplyRule applyRule:source.getApplyRules()) {
			sources.addAll(applyRule.getFacts());
		}

		//Get the ApplyRule configurations of the target configuration
		Collection<Predicate> targets = new HashSet<>();
		for(ApplyRule applyRule:target.getApplyRules()) {
			targets.addAll(applyRule.getFacts());
		}

		for(Collection<Predicate> key:this.firingGraph.getPreconditions()) {
			//For each ApplyRule in the source configuration,
			//if any of its facts was used to produce any of the facts in any ApplyRule configuration of the target configuration
			//then the corresponding configurations are neighbours
			if(CollectionUtils.containsAny(sources, key)) {
				for(Collection<Predicate> facts:this.firingGraph.getConsequences(key)) {
					if(CollectionUtils.containsAny(facts, targets)) {
						return true;
					}
				}
			}
			if(CollectionUtils.containsAny(targets, key)) {
				for(Collection<Predicate> facts:this.firingGraph.getConsequences(key)) {
					if(CollectionUtils.containsAny(facts, sources)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
