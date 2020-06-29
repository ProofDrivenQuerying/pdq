// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoning.chase.dependencyAssessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;

/**
 * Finds for each chase round which dependencies are most likely to be fired and
 * returns those dependencies. It works as follows: after each rule firing this
 * class keeps track of the generated facts. After a chase round is completed it
 * returns all the dependencies that have in their left-hand side at least one
 * atom with predicate that matches one of the predicates in the generated
 * facts.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 *
 */

public final class DependencyAssessor {
	public static enum EGDROUND{
		/** The egd. */ EGD, 
		/** The tgd. */ TGD,
		/** Both . */ BOTH};

	/** The facts of this database instance*. */
	private Collection<Atom> newFacts = null;

	/** All schema dependencies *. */
	private final Collection<Dependency> dependencies;

	/**
	 * Instantiates a new default restricted chase dependency assessor.
	 *
	 * @param dependencies the dependencies
	 */
	public DependencyAssessor(Dependency[] dependencies) {
		Preconditions.checkNotNull(dependencies);
		// Build the dependency map
		this.dependencies = new ArrayList<Dependency>();
		this.dependencies.addAll(Arrays.asList(dependencies));
		// cache of new recent facts
		this.newFacts = new LinkedHashSet<>();
	}

	public void addNewFacts(Collection<Atom> facts) {
		if (facts!=null) this.newFacts.addAll(facts);
	}

	/**
	 * For every fact that was added since the last call of getDependencies it will
	 * check if there is any predicate match between these new facts and the
	 * dependencies.
	 *
	 * @return the dependencies that have a chance to fire in the next chase
	 *         round.
	 */
	public Dependency[] getDependencies(EGDROUND round) {
		Collection<Dependency> constraints = new LinkedHashSet<>();
		Multimap<String, Atom> newFactsMap = ArrayListMultimap.create();
		for (Atom fact : newFacts)
			newFactsMap.put(fact.getPredicate().getName(), fact);

		for (Dependency dependency : this.dependencies) {
			// check if the dependency is the type we need
			if (round.equals(EGDROUND.BOTH)
					|| (dependency instanceof TGD && round.equals(EGDROUND.TGD)) 
					|| (dependency instanceof EGD && round.equals(EGDROUND.EGD))) {
				
				if (newFacts.isEmpty()) {
					// this can only happen when we are in the first round
					constraints.add(dependency);
				} else {
					for (Atom atom : dependency.getBody().getAtoms()) {
						// check if there is a match between the new facts and the left side of the dependency.
						Predicate s = atom.getPredicate();
						if (dependency instanceof Dependency && newFactsMap.keySet().contains(s.getName())) {
							constraints.add(dependency);
							break;
						}
					}
				}
			}
		}
		this.newFacts = new LinkedHashSet<>();
		return constraints.toArray(new Dependency[constraints.size()]);
	}
}
