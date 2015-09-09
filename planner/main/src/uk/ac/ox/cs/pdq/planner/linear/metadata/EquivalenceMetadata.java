package uk.ac.ox.cs.pdq.planner.linear.metadata;

import uk.ac.ox.cs.pdq.planner.linear.node.SearchNode;

/**
 * Equivalence-related information.
 *
 * @author Efthymia Tsamoura
 */
public class EquivalenceMetadata extends Metadata {

	private final double timeEquivalent;

	/**
	 * Constructor for EquivalenceMetadata.
	 * @param parent SearchNode
	 * @param timeAdded double
	 * @param timeEquivalent double
	 */
	public EquivalenceMetadata(SearchNode parent, double timeEquivalent) {
		super(parent, timeEquivalent);
		this.timeEquivalent = timeEquivalent;
	}

	/**
	 * @return double
	 */
	public double getTimeEquivalent() {
		return this.timeEquivalent;
	}

}
