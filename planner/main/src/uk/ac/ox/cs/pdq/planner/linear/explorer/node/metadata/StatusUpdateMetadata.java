package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;


/**
 * Top-level class for node dataguide. This main currently mainly used in the 
 * use interface.
 * 
 * @author Efthymia Tsamoura 
 *
 */
public class StatusUpdateMetadata extends Metadata{
	
	protected final double timeCreated;
	
	public StatusUpdateMetadata(SearchNode parent, double timeCreated) {
		super(parent, timeCreated);
		this.timeCreated = timeCreated;
	}

	public SearchNode getParent() {
		return this.parent;
	}
	
	public double getTimeCreated() {
		return this.timeCreated;
	}
	
}
