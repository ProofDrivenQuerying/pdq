package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

/**
 * Pruning-related information.
 * 
 * @author Efthymia Tsamoura 
 */
public class DominanceMetadata extends Metadata{

	public enum PruningTypes {
		COST, DOMINANCE;
	}
	
	private final PruningTypes type;
	private final SearchNode dominance;
	private final Plan dominancePlan;
	private final Plan dominatedPlan;
	private final double timeDominated;

	public DominanceMetadata(SearchNode parent, SearchNode dominance,
			Plan dominancePlan, Plan dominatedPlan, double timeDominated) {
		super(parent, timeDominated);
		this.type = PruningTypes.DOMINANCE;
		this.dominance = dominance;
		this.dominancePlan = dominancePlan;
		this.dominatedPlan = dominatedPlan;
		this.timeDominated = timeDominated;
	}

	public DominanceMetadata(SearchNode parent, Plan dominancePlan,
			Plan dominatedPlan, double timeDominated) {
		super(parent, timeDominated);
		this.type = PruningTypes.COST;
		this.dominance = null;
		this.dominancePlan = dominancePlan;
		this.dominatedPlan = dominatedPlan;
		this.timeDominated = timeDominated;
	}

	public PruningTypes getType() {
		return this.type;
	}

	public SearchNode getDominance() {
		return this.dominance;
	}

	public Plan getDominancePlan() {
		return this.dominancePlan;
	}

	public Plan getDominatedPlan() {
		return this.dominatedPlan;
	}

	public double getTimeDominated() {
		return this.timeDominated;
	}

}
