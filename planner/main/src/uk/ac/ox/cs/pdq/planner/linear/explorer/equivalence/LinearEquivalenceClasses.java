package uk.ac.ox.cs.pdq.planner.linear.explorer.equivalence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.planner.equivalence.FastStructuralEquivalence;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;

/**
 * Each equivalence class represents a group of nodes that has the same set of
 * inferred facts (structurally equivalent). Each node's cost can be different, the one with the lowest
 * cost is the representative, but this class does not maintain it, the representative have to be updated externally.
 * 
 * @author gabor
 *
 */
public class LinearEquivalenceClasses {
	/**
	 * Map of representative to the list of configurations belonging to that class.
	 */
	Map<SearchNode, List<SearchNode>> classes = new HashMap<>();

	public LinearEquivalenceClasses() {
	}

	/**
	 * If the input config is new adds it and returns its representative. Otherwise
	 * returns the representative without adding. Does not update the
	 * representative.
	 * 
	 * @param config
	 * @return
	 */
	public SearchNode add(SearchNode config) {
		// when config is already a known representative
		if (classes.containsKey(config))
			return config;

		// when config is part of a class just return the representative
		SearchNode representative = searchRepresentative(config);
		if (representative != null) {
			return representative;
		}
		// when config is new, check if it belongs to an existing class.
		for (SearchNode classRep : classes.keySet()) {

			if (new FastStructuralEquivalence().isEquivalent(classRep.getConfiguration(), config.getConfiguration())) {
				classes.get(classRep).add(config);
				return classRep;
			}
		}
		// config is new and belongs to no existing class, so we create it, having itself as representative
		List<SearchNode> eqClass = new ArrayList<>();
		eqClass.add(config);
		classes.put(config, eqClass);
		return config;
	}

	/**
	 * Updates the representative of the given configuration.
	 * 
	 * @param oldRep
	 * @param newRep
	 */
	public void updateRepresentative(SearchNode oldRep, SearchNode newRep) {
		List<SearchNode> eqClass = classes.get(oldRep);
		if (!eqClass.contains(newRep)) {
			eqClass.add(newRep);
		}
		classes.remove(oldRep);
		classes.put(newRep, eqClass);
	}

	/** Returns the list of all nodes in the given class. Returns null if the input representative is not a representative of any class. 
	 * @param representative
	 * @return
	 */
	public List<SearchNode> getEquivalenceClass(SearchNode representative) {
		return classes.get(representative);
	}
	/**
	 * Finds out if this is a known configuration and returns its representative (or
	 * itself if it is a representative)
	 * 
	 * @param config
	 * @return
	 */
	public SearchNode searchRepresentative(SearchNode config) {
		if (classes.containsKey(config))
			return config;

		for (SearchNode eqClassRep : classes.keySet()) {
			if (classes.get(eqClassRep).contains(config))
				return eqClassRep;
		}
		return null;
	}
}
