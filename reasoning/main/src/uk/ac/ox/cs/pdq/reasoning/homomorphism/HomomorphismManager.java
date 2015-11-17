package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;

import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.fol.Predicate;

/**
 * Extends the HomomorphismDetector interface by providing a way to add facts.
 *
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public interface HomomorphismManager extends HomomorphismDetector {

	/**
	 * Keeps the facts of the list in the database
	 * @param facts Input list of facts
	 */
	void addFacts(Collection<? extends Predicate> facts);
	
	/**
	 * Deletes the facts of the list in the database
	 * @param facts Input list of facts
	 */
	void deleteFacts(Collection<? extends Predicate> facts);

	/**
	 * Initialises the manager.
	 */
	void initialize();

	/**
	 * Initialises the manager with queries.
	 * @param queries
	 */
	void initialize(Collection<Evaluatable> queries);
}
