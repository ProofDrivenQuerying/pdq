package uk.ac.ox.cs.pdq.planner.linear.explorer.node.metadata;

import uk.ac.ox.cs.pdq.planner.linear.explorer.node.SearchNode;


/**
 * 
 * @author Efthymia Tsamoura 
 *
 */
public class CreationMetadata extends Metadata{
	
	protected final double timeCreated;
	
	public CreationMetadata(SearchNode parent, double timeCreated) {
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
