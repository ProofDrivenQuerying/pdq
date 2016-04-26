package uk.ac.ox.cs.pdq.planner.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.fol.Atom;

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
	void put(Dependency dependency, Collection<Atom> sources, Collection<Atom> targets);

	/**
	 * Put.
	 *
	 * @param dependency Constraint
	 * @param source PredicateFormula
	 * @param target PredicateFormula
	 */
	void put(Dependency dependency, Atom source, Atom target);

	/**
	 * Gets the fact provenance.
	 *
	 * @param fact the fact
	 * @return 		the history of the input fact, i.e., the firings that leaded to the creation of this fact
	 */
	Pair<Dependency, Collection<Atom>> getFactProvenance(Atom fact);

	/**
	 * Gets the fact provenance.
	 *
	 * @return 		the history of the chase facts, i.e., the firings that leaded to the creation of each fact
	 */
	Map<Atom, Pair<Dependency, Collection<Atom>>> getFactProvenance();

	/**
	 * Checks if is fired.
	 *
	 * @param dependency the dependency
	 * @param facts the facts
	 * @return 		true if the input dependency has been already fired given the input facts
	 */
	boolean isFired(Dependency dependency, Collection<Atom> facts);

	/**
	 * Gets the graph.
	 *
	 * @return 		the chase graph
	 */
	Graph<Atom, DefaultEdge> getGraph();

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
	Set<Collection<Atom>> getPreconditions();

	/**
	 * Gets the consequences.
	 *
	 * @param key the key
	 * @return the consequences
	 */
	public Collection<Collection<Atom>> getConsequences(Collection<Atom> key);
}