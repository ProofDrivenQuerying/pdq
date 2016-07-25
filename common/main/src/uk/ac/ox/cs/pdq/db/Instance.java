package uk.ac.ox.cs.pdq.db;

import java.util.Collection;
import java.util.LinkedHashSet;

import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.fol.Atom;

/**
 * Models a relational database instance. An Instance is a set of Atoms. 
 * 
 * @author George K
 *
 */
public interface Instance {

	
	/**  Queries and updates the storage of facts *. */
	public HomomorphismManager manager = null;
	
	
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
	void removeFacts(Collection<Atom> facts);
	
	/**
	 * 
	 * @return
	 */
	Collection<Atom> getFacts();

}
