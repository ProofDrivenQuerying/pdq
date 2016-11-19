package uk.ac.ox.cs.pdq.db.homomorphism;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.Parameters.EnumParameterValue;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

// TODO: Auto-generated Javadoc
/**
 * Top level interface for detecting homomorphisms.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public interface HomomorphismDetector extends AutoCloseable {

	/**
	 * Gets the matches of a set of constraints.
	 *
	 * @param <Q> the generic type
	 * @param q the q
	 * @param constraints the constraints
	 * @return matches of the input queries q that satisfy the input constraints
	 */
	List<Match> getTriggers(Collection<Dependency> q, TriggerProperty t);

	
	/**
	 * Gets the matches/homomorphisms of a cq.
	 *
	 * @param <Q> the generic type
	 * @param q the q
	 * @param constraints the constraints
	 * @return matches of the input queries q that satisfy the input constraints
	 */
	List<Match> getMatches(ConjunctiveQuery q);
	
	/**
	 * Clone.
	 *
	 * @return the homomorphism detector
	 */
	HomomorphismDetector clone();
	
	/**
	 * The Enum HomomorphismDetectorTypes.
	 */
	public static enum HomomorphismDetectorTypes {
		
		/** The database. */
		@EnumParameterValue(description = "Homomorphism detection relying on an internal relational database")
		DATABASE;
	}
}
