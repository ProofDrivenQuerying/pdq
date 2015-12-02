package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.reasoning.Match;
import uk.ac.ox.cs.pdq.reasoning.chase.FiringGraph;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismConstraint;

/**
 *
 * A collection of facts produced during chasing.
 * It also keeps a graph of the rule firings that took place during chasing.
 *
 * @author Efthymia Tsamoura
 *
 */
public interface ChaseState {

	/**
	 *
	 * @param query
	 * 		An input query
	 * @return
	 * 		the list of matches of the input query to the facts of this state.
	 */
	List<Match> getMatches(Query<?> query);
	
	/**
	 *
	 * @param query
	 * 		An input query
	 * @return
	 * 		the list of matches of the input query to the facts of this state that satisfy the input constraints
	 */
	List<Match> getMatches(Query<?> query, HomomorphismConstraint... constraints);

	/**
	 * 
	 * @param dependency
	 * @param constraints
	 * 		The homomorphism constraints that should be satisfied 
	 * @return
	 * 		the list of matches of the input dependency to the facts of this state.
	 * 		
	 */
	List<Match> getMaches(Constraint dependency, HomomorphismConstraint... constraints);
	
	/**
	 * 
	 * @param dependency
	 * @param constraints
	 * 		The homomorphism constraints that should be satisfied 
	 * @return
	 * 		the list of matches of the input dependency to the facts of this state.
	 * 		
	 */
	List<Match> getMaches(Collection<? extends Constraint> dependency, HomomorphismConstraint... constraints);
	
	/**
	 * 
	 * @return
	 * 		true if the input state is successful
	 */
	boolean isSuccessful(Query<?> query);
	
	
	/**
	 * 
	 * @return
	 * 		true if the input state is failed
	 */
	boolean isFailed();
	
	
	/**
	 *
	 * @return
	 * 		the rule firings that took place in this state
	 */
	FiringGraph  getFiringGraph();
	

	
	/**
	 * @return the facts of this state
	 */
	Collection<Predicate> getFacts();
	
	
	/**
	 * Applies the input rule firing.
	 * A chase step appends to a database instance additional facts that were produced during grounding a dependency. 
	 * @param match
	 * @return
	 * 		true if the step has been applied successfully 
	 */
	boolean chaseStep(Match match);
	
	/**
	 * Applies the input rule firings.
	 * A chase step appends to a database instance additional facts that were produced during grounding a dependency. 
	 * @param match
	 * @return
	 * 		true if the step has been applied successfully 
	 */
	boolean chaseStep(Collection<Match> matches);
	
	/**
	 *
	 * @param s
	 * 		An input chase configuration
	 * @return
	 * 		a merged state
	 */
	ChaseState merge(ChaseState s);

	ChaseState clone();
}
