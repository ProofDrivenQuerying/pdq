package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;

import uk.ac.ox.cs.pdq.reasoning.chase.Bag;
import uk.ac.ox.cs.pdq.reasoning.chase.BagsTree;


/**
 * The facts of this chase state are organised into a tree of bags. This type of
 * state is used in blocking chase implementations.
 * @author Efthymia Tsamoura
 *
 */
public interface TreeState extends ChaseState {
	
	
	/**
	 * Updates the list of satisfied queries/dependencies of each bag
	 */
	void updateTree();
		
	/**
	 * 
	 * @return
	 * 		the tree of bags of this chase state
	 */
	BagsTree getTree();
	
	/**
	 * 
	 * @return
	 * 		the not-updated bags
	 */
	Collection<Bag> getUnupdated();

}
