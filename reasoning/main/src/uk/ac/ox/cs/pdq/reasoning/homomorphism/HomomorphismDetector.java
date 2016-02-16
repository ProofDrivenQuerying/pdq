package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

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
	 * Gets the matches.
	 *
	 * @param <Q> the generic type
	 * @param q the q
	 * @param constraints the constraints
	 * @return matches of the input query q that satisfy the input constraints
	 */
	<Q extends Evaluatable> List<Match> getMatches(Q q, HomomorphismConstraint... constraints);

	/**
	 * Gets the matches.
	 *
	 * @param <Q> the generic type
	 * @param q the q
	 * @param constraints the constraints
	 * @return matches of the input queries q that satisfy the input constraints
	 */
	<Q extends Evaluatable> List<Match> getMatches(Collection<Q> q, HomomorphismConstraint... constraints);

	
	/**
	 * Clone.
	 *
	 * @return the homomorphism detector
	 */
	HomomorphismDetector clone();
}
