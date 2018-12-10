package uk.ac.ox.cs.pdq.planner.linear.explorer.equivalence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.planner.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.equivalence.FastStructuralEquivalence;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;

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
	Map<ChaseConfiguration, List<ChaseConfiguration>> classes = new HashMap<>();
	List<DAGChaseConfiguration> allConfigurations = new ArrayList<>();

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
	public ChaseConfiguration add(ChaseConfiguration config) {
		// when config is already a known representative
		if (classes.containsKey(config))
			return config;
		allConfigurations.add((DAGChaseConfiguration)config);
		// when config is part of a class just return the representative
		ChaseConfiguration representative = searchRepresentative(config);
		if (representative != null) {
			return representative;
		}
		// when config is new, check if it belongs to an existing class.
		for (ChaseConfiguration classRep : classes.keySet()) {

			if (new FastStructuralEquivalence().isEquivalent(classRep, config)) {
				classes.get(classRep).add(config);
				return classRep;
			}
		}
		// config is new and belongs to no existing class, so we create it, having itself as representative
		List<ChaseConfiguration> eqClass = new ArrayList<>();
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
	public void updateRepresentative(ChaseConfiguration oldRep, ChaseConfiguration newRep) {
		List<ChaseConfiguration> eqClass = classes.get(oldRep);
		if (!eqClass.contains(newRep)) {
			eqClass.add(newRep);
		}
		classes.remove(oldRep);
		classes.put(newRep, eqClass);
	}

	/**
	 * Finds out if this is a known configuration and returns its representative (or
	 * itself if it is a representative)
	 * 
	 * @param config
	 * @return
	 */
	public ChaseConfiguration searchRepresentative(ChaseConfiguration config) {
		if (classes.containsKey(config))
			return config;

		for (ChaseConfiguration eqClassRep : classes.keySet()) {
			if (classes.get(eqClassRep).contains(config))
				return eqClassRep;
		}
		return null;
	}

	public List<DAGChaseConfiguration> getConfigurations() {
		return allConfigurations;
	}

	public void removeAll(Collection<DAGChaseConfiguration> toRemove) {
		allConfigurations.removeAll(toRemove);
		// for each equality classes
		for (ChaseConfiguration eqClassRep : classes.keySet()) 
			// for each element in the toRemove list
			for (DAGChaseConfiguration c:toRemove)
				// check if it contained.
				if (classes.get(eqClassRep).contains(c))
					// then remove.
					classes.remove(c);
		
	}

	public boolean dominatedByAnything(Dominance[] dominance, DAGChaseConfiguration configuration) {
		for(DAGChaseConfiguration c: this.allConfigurations) {
			if (ConfigurationUtility.isDominatedBy(dominance, c, configuration))
				return true;
		}
		return false;
	}

	public Collection<DAGChaseConfiguration> dominatedBy(Dominance[] dominance, DAGChaseConfiguration configuration) {
		List<DAGChaseConfiguration> dominatedBy = new ArrayList<>();
		for(DAGChaseConfiguration c: this.allConfigurations) {
			if (ConfigurationUtility.isDominatedBy(dominance, c, configuration))
				dominatedBy.add(c);
		}
		
		return dominatedBy;
	}

	/** If config belongs to an equivalence class it will return the list of all configs in that class.
	 * @param config
	 * @return
	 */
	public List<ChaseConfiguration> getEquivalenceClass(DAGChaseConfiguration config) {
		return classes.get(searchRepresentative(config));
	}
}
