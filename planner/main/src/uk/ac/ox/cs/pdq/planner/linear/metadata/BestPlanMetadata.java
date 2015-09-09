package uk.ac.ox.cs.pdq.planner.linear.metadata;

import java.util.List;

import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof;

import com.google.common.base.Preconditions;

/**
 * Success-related information.
 * 
 * @author Efthymia Tsamoura 
 */
public class BestPlanMetadata extends Metadata{
	
	private final Plan plan;
	private final Proof proof;
	private final List<Integer> path;	
	private final double timeSucess;

	public BestPlanMetadata(SearchNode parent, Plan plan, Proof proof, List<Integer> path, double timeSucess) {
		super(parent, timeSucess);
		Preconditions.checkArgument(proof != null);
		Preconditions.checkArgument(plan != null);
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(!path.isEmpty());
		this.plan = plan;
		this.proof = proof;
		this.path = path;
		this.timeSucess = timeSucess;
	}
	
	public List<Integer> getBestPathToSuccess() {
		return this.path;
	}

	public Proof getProof() {
		return this.proof;
	}

	public double getTimeSucess() {
		return timeSucess;
	}
	
	public Plan getPlan() {
		return this.plan;
	}


	
}
