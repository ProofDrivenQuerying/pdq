// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;

/**
 * Top-level class for node metadata, used to pass information from the Planner to the GUI.
 *
 * @author Efthymia Tsamoura
 * @author Mark Ridler
 */
public class Metadata {

	/** The parent. */
	protected final SearchNode parent;
	
	/** The time created. */
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
	 * Gets the parent.
	 *
	 * @return SearchNode
	 */
	public SearchNode getParent() {
		return this.parent;
	}

	/**
	 * Gets the time created.
	 *
	 * @return double
	 */
	public double getTimeCreated() {
		return this.timeCreated;
	}
}
