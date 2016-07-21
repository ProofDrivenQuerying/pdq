package uk.ac.ox.cs.pdq.reasoning.chase.state;

import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.TriggerProperty;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;

// TODO: Auto-generated Javadoc
/**
 *
 * A collection of facts produced during chasing.
 * It also keeps a graph of the rule firings that took place during chasing. //TOCOMMENT: I'm not sure where the latter sentence is true 
 *
 * @author George K
 * @author Efthymia Tsamoura
 *
 */
public interface ChaseState {

	/**
	 *
	 * (Conjunctive query match definition) If Q′ is a conjunctive query and v is a chase configuration
	 * having elements for each free variable of Q′, then a homomorphism of Q′ into v
	 * mapping each free variable into the corresponding element is called a match for Q′ in v.
	 * @param query
	 * 		An input query
	 * @return
	 * 		the list of matches of the input query to the facts of this state.
	 */
	List<Match> getMatches(ConjunctiveQuery query);
	
	/**
	 * (Candidate match definition).
	 * Given a set of facts I and a TGD
	 * 		delta = \forall x_1, ..., x_j \phi(\vec{x}) --> \exists  y_1, ..., y_k \rho(\vec{x},\vec{y})
	 * 		a candidate match for d is \vec{e} such that \phi(\vec{e}) holds but there is no \vec{f} such that \rho(\vec{e},\vec{f})
	 * 		holds in I.
	 *
	 * @param dependencies the dependencies
	 * @param t 		The TriggerProperty constraints that should be satisfied 
	 * @return 		the list of matches (both candidates and not candidates) of the input dependencies in this database instance.
	 */
	List<Match> getTriggers(Collection<? extends Dependency> dependencies, TriggerProperty t);
	
	/**
	 * Checks if is failed.
	 *
	 * @return 		true if this database instance is failed
	 */
	boolean isFailed();
		
	/**
	 * Gets the facts.
	 *
	 * @return the facts of this instance
	 */
	Collection<Atom> getFacts();
	
	/**
	 * Augments the internal facts with the new ones.
	 *
	 * @param facts the facts
	 */
	void addFacts(Collection<Atom> facts);
	
	
	/**
	 * Performs a chase step.
	 * 	(From modern dependency theory notes) Given trigger h for a dependency \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
	 * 		in I, a chase step appends to I additional facts for every atom R(\vec{x}, \vec{y}) in \tau ,
	 * 		with a position containing a variable x_i filled with h(x_i), a position containing a
	 * 		variable y_i filled with a value c_i that is distinct from each value in I and from
	 * 		each other c_j, and a position containing the constant using the corresponding
	 * 		constant.
	 *
	 * @param trigger the trigger
	 * @return 		true if the step has been applied successfully
	 */
	boolean chaseStep(Match trigger);
	
	/**
	 * Performs multiple chase steps.
	 * 	(From modern dependency theory notes) Given trigger h for a dependency \delta = \forall x  \sigma(\vec{x}) --> \exists y  \tau(\vec{x}, \vec{y})
	 * 		in I, a chase step appends to I additional facts for every atom R(\vec{x}, \vec{y}) in \tau ,
	 * 		with a position containing a variable x_i filled with h(x_i), a position containing a
	 * 		variable y_i filled with a value c_i that is distinct from each value in I and from
	 * 		each other c_j, and a position containing the constant using the corresponding
	 * 		constant.
	 *
	 * @param triggers the triggers
	 * @return 		true if the steps have been applied successfully
	 */
	boolean chaseStep(Collection<Match> triggers);
	
	/**
	 * Merge.
	 *
	 * @param s 		An input chase configuration
	 * @return 		a database instance with facts the union of the facts of the two database instances.
	 */
	ChaseState merge(ChaseState s);
	
	ChaseState clone();
}
