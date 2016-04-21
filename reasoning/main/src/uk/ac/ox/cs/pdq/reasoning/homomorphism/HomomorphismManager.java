package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;

// TODO: Auto-generated Javadoc
/**
 * Extends the HomomorphismDetector interface by providing a way to add facts.
 *
 * @author Efthymuia Tsamoura
 * @author Julien Leblay
 * @author George Konstantinidis
 */
public interface HomomorphismManager extends HomomorphismDetector {

	/**
	 * Keeps the facts of the list in the database.
	 *
	 * @param facts Input list of facts
	 */
	void addFacts(Collection<? extends Atom> facts);
	
	/**
	 * Deletes the facts of the list in the database.
	 *
	 * @param facts Input list of facts
	 */
	void deleteFacts(Collection<? extends Atom> facts);

	/**
	 * Initialises the manager.
	 */
	void initialize();

	/**
	 * Initializes the homomomorphism machinery (e.g., any indices) needed to later find a homomorphism from a specific query
	 * Note that in some implementations after you detect the homomorphisms from a query you have "consumed" any related machinery and you should make sure you cleeanup, by calling the manager's clearQuery method.
	 *
	 * @param query the query
	 */
	void addQuery(Query<?> query);

	/**
	 * Clears the homomomorphism machinery (e.g., any indices) constructed for an earlier query. In certain implementation one needs to call this before adding a new Query.
	 */
	void clearQuery();
}