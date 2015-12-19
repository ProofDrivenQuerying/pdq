package uk.ac.ox.cs.pdq.planner.linear.explorer.node;

import java.util.List;
import java.util.Set;

import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Tree nodes that keep every path to success.
 * Used when costing plans using a blackbox cost function.
 * @author Efthymia Tsamoura
 *
 */
public class BlackBoxNode extends SearchNode {

	/** Paths to success. We keep the node identifiers of each path */
	private Set<List<Integer>> pathsToSuccess = null;

	/**
	 * Constructor for BlackBoxNode.
	 * @param configuration LinearConfiguration
	 * @throws PlannerException
	 */
	public BlackBoxNode(LinearChaseConfiguration configuration) throws PlannerException {
		super(configuration);
	}

	/**
	 * Constructor for BlackBoxNode.
	 * @param parent BlackBoxNode
	 * @param configuration LinearConfiguration
	 * @throws PlannerException
	 */
	public BlackBoxNode(BlackBoxNode parent, LinearChaseConfiguration configuration) throws PlannerException {
		super(parent, configuration);
	}

	/**
	 * @return Set<List<Integer>>
	 */
	public Set<List<Integer>> getPathsToSuccess() {
		return this.pathsToSuccess;
	}

	/**
	 * @param pathsToSuccess Set<List<Integer>>
	 */
	public void setPathsToSuccess(Set<List<Integer>> pathsToSuccess) {
		this.pathsToSuccess = pathsToSuccess == null ? null : Sets.newLinkedHashSet(pathsToSuccess);
	}

	/**
	 * @param pathToSuccess List<Integer>
	 */
	@Override
	public void setPathToSuccess(List<Integer> pathToSuccess) {
		this.pathsToSuccess = null;
		if(pathToSuccess != null) {
			this.pathsToSuccess = Sets.newLinkedHashSet();
			this.pathsToSuccess.add(pathToSuccess);
		}
	}

	public void ground() {
		this.pathsToSuccess = Sets.newLinkedHashSet();
		List<Integer> l = Lists.newArrayList();
		this.pathsToSuccess.add(l);
	}

	/**
	 * @param paths List<List<Integer>>
	 */
	public void addPathsToSuccess(List<List<Integer>> paths) {
		this.pathsToSuccess.addAll(paths);
	}

	/**
	 * @param pathToSuccess List<Integer>
	 */
	public void addPathToSuccess(List<Integer> pathToSuccess) {
		if (this.pathsToSuccess == null) {
			this.pathsToSuccess = Sets.newLinkedHashSet();
		}
		this.pathsToSuccess.add(pathToSuccess);
	}

	/**
	 * @return BlackBoxNode
	 */
	@Override
	public BlackBoxNode getPointer() {
		return (BlackBoxNode) super.getPointer();
	}
}
