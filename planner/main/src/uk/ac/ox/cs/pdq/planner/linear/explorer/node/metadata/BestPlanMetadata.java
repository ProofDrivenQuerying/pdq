package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

import com.google.common.base.Preconditions;

/**
 * Success-related information.
 * 
 * @author Efthymia Tsamoura 
 */
public class BestPlanMetadata extends Metadata{
	
	private final Plan plan;
	private final List<Integer> path;	
	private final double timeSucess;
	private final List<LinearChaseConfiguration> exposedCandidates;

	public BestPlanMetadata(SearchNode parent, Plan plan, List<Integer> path, List<LinearChaseConfiguration> exposedCandidates, double timeSucess) {
		super(parent, timeSucess);
		Preconditions.checkArgument(plan != null);
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(!path.isEmpty());
		Preconditions.checkArgument(exposedCandidates != null);
		Preconditions.checkArgument(!exposedCandidates.isEmpty());
		this.plan = plan;
		this.path = path;
		this.exposedCandidates = exposedCandidates;
		this.timeSucess = timeSucess;
	}
	
	public List<Integer> getBestPathToSuccess() {
		return this.path;
	}

	public double getTimeSucess() {
		return this.timeSucess;
	}
	
	public Plan getPlan() {
		return this.plan;
	}
	
	public List<LinearChaseConfiguration> getConfigurations() {
		return this.exposedCandidates;
	}
	
}
