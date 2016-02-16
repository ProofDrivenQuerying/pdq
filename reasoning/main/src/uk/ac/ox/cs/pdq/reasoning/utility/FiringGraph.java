package uk.ac.ox.cs.pdq.reasoning.utility;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.fol.Predicate;

// TODO: Auto-generated Javadoc
/**
 * Maintains information w.r.t. chasing plus answers proximity queries
 *
 * @author Efthymia Tsamoura
 *
 */
public interface FiringGraph {
	
	/**
	 * Updates the internal structures based given the input chasing information.
	 *
	 * @param dependency the dependency
	 * @param sources 			the facts that where used to ground the input dependency
	 * @param targets 			the consequence facts
	 */
	void put(Constraint dependency, Collection<Predicate> sources, Collection<Predicate> targets);

	/**
	 * Put.
	 *
	 * @param dependency Constraint
	 * @param source PredicateFormula
	 * @param target PredicateFormula
	 */
	void put(Constraint dependency, Predicate source, Predicate target);

	/**
	 * Gets the fact provenance.
	 *
	 * @param fact the fact
	 * @return 		the history of the input fact, i.e., the firings that leaded to the creation of this fact
	 */
	Pair<Constraint, Collection<Predicate>> getFactProvenance(Predicate fact);

	/**
	 * Gets the fact provenance.
	 *
	 * @return 		the history of the chase facts, i.e., the firings that leaded to the creation of each fact
	 */
	Map<Predicate, Pair<Constraint, Collection<Predicate>>> getFactProvenance();

	/**
	 * Checks if is fired.
	 *
	 * @param dependency the dependency
	 * @param facts the facts
	 * @return 		true if the input dependency has been already fired given the input facts
	 */
	boolean isFired(Constraint dependency, Collection<Predicate> facts);

	/**
	 * Gets the graph.
	 *
	 * @return 		the chase graph
	 */
	Graph<Predicate, DefaultEdge> getGraph();

	/**
	 * Clone.
	 *
	 * @return FiringGraph<S>
	 */
	FiringGraph clone();

	/**
	 * Merges two FiringGraph objects.
	 *
	 * @param source the source
	 * @return FiringGraph<S>
	 */
	FiringGraph merge(FiringGraph source);
	
	/**
	 * Gets the preconditions.
	 *
	 * @return the preconditions
	 */
	Set<Collection<Predicate>> getPreconditions();

	/**
	 * Gets the consequences.
	 *
	 * @param key the key
	 * @return the consequences
	 */
	public Collection<Collection<Predicate>> getConsequences(Collection<Predicate> key);
}
