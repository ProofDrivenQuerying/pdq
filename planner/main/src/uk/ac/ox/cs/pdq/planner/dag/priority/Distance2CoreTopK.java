package uk.ac.ox.cs.pdq.planner.dag.priority;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.DefaultValidator;
import uk.ac.ox.cs.pdq.planner.dag.explorer.validators.Validator;
import uk.ac.ox.cs.pdq.planner.dag.potential.PotentialAssessor;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.state.AccessibleChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;

import com.google.common.base.Preconditions;
import com.google.common.collect.MinMaxPriorityQueue;


/**
 * Returns the top-k configurations that are closest to the core
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class Distance2CoreTopK extends PriorityAssessor{

	/** Compares configurations based on their ranking */
	private final RankingComparator comparator = new RankingComparator();

	/** Keeps for each chase fact its distance from the core (the canonical database) */
	private final Map<Predicate, Double> ranking;

	/** Random number generator and its side. Breaks ties when two configurations have the same ranking */
	private final Integer seed;

	private final Random random;

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
	public Distance2CoreTopK(List<Validator> validators,
			PotentialAssessor potential,
			Integer topk,
			Query<?> query,
			FiringGraph firingGraph,
			Integer seed) {
		super(validators, potential, topk);
		this.ranking = this.getRanking(query, firingGraph);
		this.seed = seed;
		this.random = new Random(this.seed);
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
	public Distance2CoreTopK(
			List<Validator> validators,
			PotentialAssessor potential,
			Integer topk,
			Map<Predicate, Double> ranking,
			Integer seed) {
		super(validators, potential, topk);
		Preconditions.checkNotNull(ranking);
		Preconditions.checkArgument(!ranking.isEmpty());
		this.ranking = ranking;
		this.seed = seed;
		this.random = new Random(this.seed);
	}

	/**
	 * @param query
	 * @param firingGraph Maintains the chase graph
	 * @return the distance of each chase fact from the core
	 */
	public Map<Predicate, Double> getRanking(Query<?> query, FiringGraph firingGraph) {
		Map<Predicate, Double> ranking = new LinkedHashMap<>();
		Conjunction<Predicate> core = (Conjunction<Predicate>) query.getCanonical();

		for(Predicate fact: core.getPredicates()) {
			ranking.put(fact, 0.0);
		}

		for(Predicate vertex:firingGraph.getGraph().vertexSet()) {
			Double distance = Double.MAX_VALUE;
			for(Predicate fact:core) {
				if(firingGraph.getGraph().containsVertex(fact)) {
					DijkstraShortestPath<Predicate, DefaultEdge> djk = new DijkstraShortestPath<>(firingGraph.getGraph(), vertex, fact);
					if(distance > djk.getPathLength()) {
						distance = djk.getPathLength();
					}
				}
			}
			ranking.put(vertex, distance);
		}
		return ranking;
	}

	/**
	 * @param left DAGConfiguration
	 * @param right Collection<DAGConfiguration>
	 * @param depth int
	 * @return Collection<DAGConfiguration>
	 */
	@Override
	public Collection<DAGChaseConfiguration> select(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right, int depth) {
		/** Orders the input configurations based on their string signatures. Used to remove non-determinism,
		 * when the input configurations are ordered differently */
		Set<DAGChaseConfiguration> pool = new TreeSet<>(new DAGConfigurationComparator());
		pool.addAll(right);
		return this.hiddenSelect(left, pool, depth);
	}

	/**
	 *
	 * @param left
	 * @param right Candidate configurations to combine with the left input one
	 * @param depth The target depth of the composition
	 * @return the top-k highest-priority configurations to combine with the left input one
	 */
	private Collection<DAGChaseConfiguration> hiddenSelect(DAGChaseConfiguration left, Collection<DAGChaseConfiguration> right, int depth) {
		MinMaxPriorityQueue<DAGChaseConfiguration> selected = MinMaxPriorityQueue.orderedBy(this.comparator).maximumSize(this.getTopk()).create();
		for(DAGChaseConfiguration configuration:right) {
			Preconditions.checkNotNull(configuration.getEquivalenceClass());
			Preconditions.checkState(!configuration.getEquivalenceClass().isEmpty());
			if(!configuration.getEquivalenceClass().isSleeping() &&
					this.validate(left, configuration, depth) &&
					this.getPotential().getPotential(left, configuration) ){
				this.getRanking(configuration);
				selected.add(configuration);
			}
		}
		return selected;
	}

	/**
	 * @param right
	 * @return a configuration's distance from core
	 */
	protected Double getRanking(DAGChaseConfiguration right) {
		Double result = Double.MAX_VALUE;
		for(ApplyRule applyRule:right.getApplyRules()) {
			for(Predicate fact:applyRule.getFacts()) {
				if(result > this.ranking.get(fact)) {
					result = this.ranking.get(fact);
				}
			}
		}
		right.setRanking(result);
		return result;
	}

	/**
	 * @return Distance2CoreTopK
	 */
	@Override
	public Distance2CoreTopK clone() {
		return new Distance2CoreTopK(DefaultValidator.deepCopy(this.getValidators()), this.getPotential().clone(), this.getTopk(), this.ranking, this.seed);
	}

	/**
	 * Compares configurations based on their rankings
	 *
	 * @author Efthymia Tsamoura
	 */
	private class RankingComparator implements Comparator<DAGChaseConfiguration> {
		/**
		 * @param o1 DAGConfiguration
		 * @param o2 DAGConfiguration
		 * @return int
		 */
		@Override
		public int compare(DAGChaseConfiguration o1, DAGChaseConfiguration o2) {
			if(o1.getRanking() < o2.getRanking()) {
				return -1;
			}
			else if(o1.getRanking() == o2.getRanking()) {
				return 0;
			}
			return 1;
		}
	}

	/**
	 * @return Map<PredicateFormula,Double>
	 */
	public Map<Predicate, Double> getRanking() {
		return this.ranking;
	}

	/**
	 * @return Integer
	 */
	public Integer getSeed() {
		return this.seed;
	}

	/**
	 * @return RankingComparator
	 */
	protected RankingComparator getComparator() {
		return this.comparator;
	}

	/**
	 * @return Random
	 */
	protected Random getRandom() {
		return this.random;
	}

	/**
	 * Orders configurations based on their string signatures
	 *
	 * @author Efthymia Tsamoura
	 */
	public static class DAGConfigurationComparator<S extends AccessibleChaseState> implements Comparator<DAGChaseConfiguration>{
		/**
		 * @param c1 DAGConfiguration
		 * @param c2 DAGConfiguration
		 * @return int
		 */
		@Override
		public int compare(DAGChaseConfiguration c1, DAGChaseConfiguration c2){
			return new Integer(c1.toString().hashCode()).compareTo(new Integer(c2.toString().hashCode()));
		}
	}
}