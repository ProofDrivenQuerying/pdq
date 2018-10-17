package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;


// TODO: Auto-generated Javadoc
/**
 * Top-level class for node dataguide. This main currently mainly used in the 
 * use interface.
 * 
 * @author Efthymia Tsamoura 
 *
 */
public class StatusUpdateMetadata extends Metadata{
	
	/** The time created. */
	protected final double timeCreated;
	
	/**
	 * Instantiates a new status update metadata.
	 *
	 * @param parent the parent
	 * @param timeCreated the time created
	 */
	public StatusUpdateMetadata(SearchNode parent, double timeCreated) {
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
