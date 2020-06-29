// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.explorer;

import java.util.List;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;

/**
 * Tree nodes that keep the single best path to success.
 * Used when costing plans using a simple cost function.
 *
 * @author Efthymia Tsamoura
 */
public class LinearConfigurationNode extends SearchNode {

	/** Path to success. We keep the node identifiers for each path */
	List<Integer> pathToSuccess = null;

	/**
	 * Constructor for SimpleNode.
	 *
	 * @param configuration LinearConfiguration
	 * @throws PlannerException the planner exception
	 */
	public LinearConfigurationNode(LinearChaseConfiguration configuration) throws PlannerException {
		super(configuration);
	}

	/**
	 * Constructor for SimpleNode.
	 *
	 * @param parent SimpleNode
	 * @param configuration LinearConfiguration
	 * @throws PlannerException the planner exception
	 */
	public LinearConfigurationNode(LinearConfigurationNode parent, LinearChaseConfiguration configuration) throws PlannerException {
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
	public LinearConfigurationNode getEquivalentNode() {
		return (LinearConfigurationNode) super.getEquivalentNode();
	}
}
