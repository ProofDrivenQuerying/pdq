// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;

// TODO: Auto-generated Javadoc
/**
 * Pruning-related information.
 * 
 * @author Efthymia Tsamoura 
 */
public class DominanceMetadata extends Metadata{

	/**
	 * The Enum PruningTypes.
	 */
	public enum PruningTypes {
		
		/** The cost. */
		COST, 
 /** The dominance. */
 DOMINANCE;
	}
	
	/** The type. */
	private final PruningTypes type;
	
	/** The dominance. */
	private final SearchNode dominance;
	
	/** The dominance plan. */
	private final Plan dominancePlan;
	
	/** The dominated plan. */
	private final Plan dominatedPlan;
	
	/** The time dominated. */
	private final double timeDominated;

	/**
	 * Instantiates a new dominance metadata.
	 *
	 * @param parent the parent
	 * @param dominance the dominance
	 * @param dominancePlan the dominance plan
	 * @param dominatedPlan the dominated plan
	 * @param timeDominated the time dominated
	 */
	public DominanceMetadata(SearchNode parent, SearchNode dominance,
			Plan dominancePlan, Plan dominatedPlan, double timeDominated) {
		super(parent, timeDominated);
		this.type = PruningTypes.DOMINANCE;
		this.dominance = dominance;
		this.dominancePlan = dominancePlan;
		this.dominatedPlan = dominatedPlan;
		this.timeDominated = timeDominated;
	}

	/**
	 * Instantiates a new dominance metadata.
	 *
	 * @param parent the parent
	 * @param dominancePlan the dominance plan
	 * @param dominatedPlan the dominated plan
	 * @param timeDominated the time dominated
	 */
	public DominanceMetadata(SearchNode parent, Plan dominancePlan,
			Plan dominatedPlan, double timeDominated) {
		super(parent, timeDominated);
		this.type = PruningTypes.COST;
		this.dominance = null;
		this.dominancePlan = dominancePlan;
		this.dominatedPlan = dominatedPlan;
		this.timeDominated = timeDominated;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public PruningTypes getType() {
		return this.type;
	}

	/**
	 * Gets the dominance.
	 *
	 * @return the dominance
	 */
	public SearchNode getDominance() {
		return this.dominance;
	}

	/**
	 * Gets the dominance plan.
	 *
	 * @return the dominance plan
	 */
	public Plan getDominancePlan() {
		return this.dominancePlan;
	}

	/**
	 * Gets the dominated plan.
	 *
	 * @return the dominated plan
	 */
	public Plan getDominatedPlan() {
		return this.dominatedPlan;
	}

	/**
	 * Gets the time dominated.
	 *
	 * @return the time dominated
	 */
	public double getTimeDominated() {
		return this.timeDominated;
	}

}
