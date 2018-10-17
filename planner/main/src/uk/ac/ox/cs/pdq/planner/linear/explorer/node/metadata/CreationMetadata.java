package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;


// TODO: Auto-generated Javadoc
/**
 * The Class CreationMetadata.
 *
 * @author Efthymia Tsamoura
 */
public class CreationMetadata extends Metadata{
	
	/** The time created. */
	protected final double timeCreated;
	
	/**
	 * Instantiates a new creation metadata.
	 *
	 * @param parent the parent
	 * @param timeCreated the time created
	 */
	public CreationMetadata(SearchNode parent, double timeCreated) {
		super(parent, timeCreated);
		this.timeCreated = timeCreated;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata#getParent()
	 */
	public SearchNode getParent() {
		return this.parent;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata.Metadata#getTimeCreated()
	 */
	public double getTimeCreated() {
		return this.timeCreated;
	}
	
}
