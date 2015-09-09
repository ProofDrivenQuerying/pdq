package uk.ac.ox.cs.pdq.planner.linear.node;

import java.util.List;

import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;

import com.google.common.collect.Lists;

/**
 * A SearchNode class implementation where the best path to success is kept.
 * Used when employing a simple cost function to cost plans
 *
 * @author Efthymia Tsamoura
 */
public class SimpleNode extends SearchNode {

	/** Path to success. We keep the node identifiers for each path */
	List<Integer> pathToSuccess = null;

	/**
	 * Constructor for SimpleNode.
	 * @param configuration LinearConfiguration
	 * @throws PlannerException
	 */
	public SimpleNode(LinearChaseConfiguration configuration) throws PlannerException {
		super(configuration);
	}

	/**
	 * Constructor for SimpleNode.
	 * @param parent SimpleNode
	 * @param configuration LinearConfiguration
	 * @throws PlannerException
	 */
	public SimpleNode(SimpleNode parent, LinearChaseConfiguration configuration) throws PlannerException {
		super(parent, configuration);
	}

	/**
	 * @return List<Integer>
	 */
	public List<Integer> getPathToSuccess() {
		return this.pathToSuccess;
	}

	/**
	 * @param pathToSuccess List<Integer>
	 */
	@Override
	public void setPathToSuccess(List<Integer> pathToSuccess) {
		this.pathToSuccess = pathToSuccess;
	}

	public void ground() {
		this.pathToSuccess = Lists.newArrayList();
	}

	/**
	 * @return SimpleNode
	 */
	@Override
	public SimpleNode getPointer() {
		return (SimpleNode) super.getPointer();
	}
}
