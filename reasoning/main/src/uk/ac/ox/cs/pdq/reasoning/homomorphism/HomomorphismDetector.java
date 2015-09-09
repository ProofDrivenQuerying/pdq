package uk.ac.ox.cs.pdq.reasoning.homomorphism;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.fol.Evaluatable;
import uk.ac.ox.cs.pdq.reasoning.Match;

/**
 * Top level interface for detecting homomorphisms.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public interface HomomorphismDetector extends AutoCloseable {

	
	/**
	 * 
	 * @param q
	 * @param constraints
	 * @return matches of the input query q that satisfy the input constraints
	 */
	<Q extends Evaluatable> List<Match> getMatches(Q q, HomomorphismConstraint... constraints);

	/**
	 * 
	 * @param q
	 * @param constraints
	 * @return matches of the input queries q that satisfy the input constraints
	 */
	<Q extends Evaluatable> List<Match> getMatches(Collection<Q> q, HomomorphismConstraint... constraints);

	
	HomomorphismDetector clone();
}
