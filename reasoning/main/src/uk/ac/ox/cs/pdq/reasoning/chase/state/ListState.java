package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Predicate;

/**
 *	Organises the chase facts into a list.
 * 	@author Efthymia Tsamoura
 *
 */
public interface ListState extends ChaseState{
	
	/**
	 * Adds the facts into a database instance. 
	 * @param facts
	 */
	void addFacts(Collection<Predicate> facts);

}
