package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Predicate;

/**
 *	The facts of this chase state are organised into a list of facts. This type of
 *  state is used in non-blocking chase implementations.
 * 	@author Efthymia Tsamoura
 *
 */
public interface ListState extends ChaseState{
	
	/**
	 * 
	 * @param facts
	 */
	void addFacts(Collection<Predicate> facts);

}
