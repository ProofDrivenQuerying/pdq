package uk.ac.ox.cs.pdq.db;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.reasoningdatabase.DataSink;
/**
 * Models a relational database instance. An Instance is a set of Atoms. 
 * 
 *
 */
public interface Instance extends DataSink {

	
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
	
}
