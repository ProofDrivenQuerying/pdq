package uk.ac.ox.cs.pdq.planner.linear.node;

import java.util.Set;

import uk.ac.ox.cs.pdq.cost.estimators.SimpleCostEstimator;
import uk.ac.ox.cs.pdq.plan.LeftDeepPlan;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.explorer.ConfigurationFactory;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.Candidate;

import com.google.common.base.Preconditions;

/**
 * Creates nodes based on the input arguments
 *
 * @author Efthymia Tsamoura
 */
public final class NodeFactory {

	private final ConfigurationFactory<LeftDeepPlan> configurationFactory;

	/**
	 * Constructor for NodeFactory.
	 * @param configurationFactory ConfigurationFactory<S,LeftDeepPlan>
	 */
	public NodeFactory(ConfigurationFactory<LeftDeepPlan> configurationFactory) {
		Preconditions.checkArgument(configurationFactory != null);
		this.configurationFactory = configurationFactory;
	}

	/**
	 * @param configuration LinearConfiguration
	 * @return a node with the input configuration * @throws PlannerException
	 */
	public SearchNode getInstance(LinearChaseConfiguration configuration) throws PlannerException {
		if (this.configurationFactory.getCostEstimator() instanceof SimpleCostEstimator) {
			return new SimpleNode(configuration);
		}
		return new BlackBoxNode(configuration);
	}

	/**
	 * @param parentNode
	 * @param exposedCandidates
	 * @return a node having the given parent node and the given exposed candidate facts
	 * @throws PlannerException
	 */
	public SearchNode getInstance(SearchNode parentNode, Set<Candidate> exposedCandidates) throws PlannerException {
		LinearChaseConfiguration configuration = this.configurationFactory.getLinearInstance(parentNode.getConfiguration(), exposedCandidates);
		if (parentNode instanceof SimpleNode) {
			return new SimpleNode((SimpleNode) parentNode, configuration);
		}
		return new BlackBoxNode((BlackBoxNode) parentNode, configuration);
	}
}