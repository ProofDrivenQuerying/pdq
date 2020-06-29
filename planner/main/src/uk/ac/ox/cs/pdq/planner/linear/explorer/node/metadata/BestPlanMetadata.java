// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import java.util.List;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.algebra.Plan;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;

// TODO: Auto-generated Javadoc
/**
 * Success-related information.
 * 
 * @author Efthymia Tsamoura 
 */
public class BestPlanMetadata extends Metadata{
	
	/** The plan. */
	private final Plan plan;
	
	/** The path. */
	private final List<Integer> path;	
	
	/** The time sucess. */
	private final double timeSucess;
	
	/** The exposed candidates. */
	private final List<LinearChaseConfiguration> exposedCandidates;

	/**
	 * Instantiates a new best plan metadata.
	 *
	 * @param parent the parent
	 * @param plan the plan
	 * @param path the path
	 * @param exposedCandidates the exposed candidates
	 * @param timeSucess the time sucess
	 */
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
	
	/**
	 * Gets the best path to success.
	 *
	 * @return the best path to success
	 */
	public List<Integer> getBestPathToSuccess() {
		return this.path;
	}

	/**
	 * Gets the time sucess.
	 *
	 * @return the time sucess
	 */
	public double getTimeSucess() {
		return this.timeSucess;
	}
	
	/**
	 * Gets the plan.
	 *
	 * @return the plan
	 */
	public Plan getPlan() {
		return this.plan;
	}
	
	/**
	 * Gets the configurations.
	 *
	 * @return the configurations
	 */
	public List<LinearChaseConfiguration> getConfigurations() {
		return this.exposedCandidates;
	}
	
}
