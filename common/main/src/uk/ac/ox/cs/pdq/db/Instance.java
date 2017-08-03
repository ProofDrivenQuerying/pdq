package uk.ac.ox.cs.pdq.db;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Atom;
/**
 * Models a relational database instance. An Instance is a set of Atoms. 
 * 
 *
 */
public interface Instance {

	
	/**
	 * Augments the internal facts with the new ones.
	 *
	 * @param facts the Atom objects to be added
	 */
	void addFacts(Collection<Atom> facts);
	
	
	
	/**
	 * Removes the input facts from this instance's.
	 *
	 * @param facts the Atom objects to be removed
	 */
	void deleteFacts(Collection<Atom> facts);
	
	/**
	 * 
	 * @return
	 */
	Collection<Atom> getFacts();
	
//	/**
//	 * Answers queries on this instance's facts.
//	 *
//	 * @param
//	 */
//	List<Match> answerQuery(ConjunctiveQuery q);

}
