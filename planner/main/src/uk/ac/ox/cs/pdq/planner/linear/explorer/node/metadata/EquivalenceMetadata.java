package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;

// TODO: Auto-generated Javadoc
/**
 * Equivalence-related information.
 *
 * @author Efthymia Tsamoura
 */
public class EquivalenceMetadata extends Metadata {

	/** The time equivalent. */
	private final double timeEquivalent;

	/**
	 * Constructor for EquivalenceMetadata.
	 *
	 * @param parent SearchNode
	 * @param timeEquivalent double
	 */
	public EquivalenceMetadata(SearchNode parent, double timeEquivalent) {
		super(parent, timeEquivalent);
		this.timeEquivalent = timeEquivalent;
	}

	/**
	 * Gets the time equivalent.
	 *
	 * @return double
	 */
	public double getTimeEquivalent() {
		return this.timeEquivalent;
	}

}
