package uk.ac.ox.cs.pdq.planner.linear.explorer.node;

import java.util.List;

import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * Tree nodes that keep the single best path to success.
 * Used when costing plans using a simple cost function.
 *
 * @author Efthymia Tsamoura
 */
public class SimpleNode extends SearchNode {

	/** Path to success. We keep the node identifiers for each path */
	List<Integer> pathToSuccess = null;

	/**
	 * Constructor for SimpleNode.
	 *
	 * @param configuration LinearConfiguration
	 * @throws PlannerException the planner exception
	 */
	public SimpleNode(LinearChaseConfiguration configuration) throws PlannerException {
		super(configuration);
	}

	/**
	 * Constructor for SimpleNode.
	 *
	 * @param parent SimpleNode
	 * @param configuration LinearConfiguration
	 * @throws PlannerException the planner exception
	 */
	public SimpleNode(SimpleNode parent, LinearChaseConfiguration configuration) throws PlannerException {
		super(parent, configuration);
	}

	/**
	 * Gets the path to success.
	 *
	 * @return List<Integer>
	 */
	public List<Integer> getPathToSuccess() {
		return this.pathToSuccess;
	}

	/**
	 * Sets the path to success.
	 *
	 * @param pathToSuccess List<Integer>
	 */
	@Override
	public void setPathToSuccess(List<Integer> pathToSuccess) {
		this.pathToSuccess = pathToSuccess;
	}

	/**
	 * Ground.
	 */
	public void ground() {
		this.pathToSuccess = Lists.newArrayList();
	}

	/**
	 * Gets the pointer.
	 *
	 * @return SimpleNode
	 */
	@Override
	public SimpleNode getEquivalentNode() {
		return (SimpleNode) super.getEquivalentNode();
	}
}
