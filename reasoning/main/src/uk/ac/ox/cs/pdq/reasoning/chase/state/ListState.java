package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Atom;

// TODO: Auto-generated Javadoc
/**
 * 	A collection of facts produced during chasing.
 *	The facts are organised into a list.
 *	This chase state is used for non-blocking chase implementations 
 * 	@author Efthymia Tsamoura
 *
 */
public interface ListState extends ChaseState{
	
	/**
	 * Augments the internal facts with the new ones.
	 *
	 * @param facts the facts
	 */
	void addFacts(Collection<Atom> facts);

}
