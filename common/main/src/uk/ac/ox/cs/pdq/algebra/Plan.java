package uk.ac.ox.cs.pdq.algebra;

import java.util.Set;

import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * A query plan.
 * 
 * @author Tim Hobson
 *
 */
public interface Plan {

	public Attribute[] getInputAttributes();
	
	public Attribute getInputAttribute(int index);
	
	public int getNumberOfInputAttributes();
	
	public Attribute[] getOutputAttributes();
	
	public Attribute getOutputAttribute(int index);
	
	public int getNumberOfOutputAttributes();
	
	public Integer[] getInputIndices();
	
	/**
	 * Gets the position of an output attribute.
	 * 
	 * @param attribute the attribute
	 * @return the index of the corresponding attribute, or -1 iff the plan has no such attribute. 
	 */
	int getAttributePosition(Attribute attribute);
	
	public Plan[] getChildren();
	
	public Plan getChild(int index);
	
	public Set<AccessTerm> accessPlans();
	
	public boolean isClosed();
}
