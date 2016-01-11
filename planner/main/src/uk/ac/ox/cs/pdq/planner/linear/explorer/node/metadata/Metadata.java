package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

/**
 * Top-level class for node dataguide. This main currently mainly used in the
 * use interface.
 *
 * @author Efthymia Tsamoura
 */
public class Metadata {

	protected final SearchNode parent;
	protected final double timeCreated;

	/**
	 * Constructor for Metadata.
	 * @param parent SearchNode
	 * @param timeCreated double
	 */
	public Metadata(SearchNode parent, double timeCreated) {
		this.parent = parent;
		this.timeCreated = timeCreated;
	}

	/**
	 * @return SearchNode
	 */
	public SearchNode getParent() {
		return this.parent;
	}

	/**
	 * @return double
	 */
	public double getTimeCreated() {
		return this.timeCreated;
	}
}
