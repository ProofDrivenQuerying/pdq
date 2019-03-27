package uk.ac.ox.cs.pdq.planner.equivalence.dag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;


/**
 * A collection of structurally equivalent classes that supports multi-threading.
 * According to this implementation different threads can add, remove or perform domination detection inside each class concurrently.
 *
 * @author Efthymia Tsamoura
 *
 */
public class DAGEquivalenceClasses {
	
	/**  Maps each configuration to its class. */
	private final Map<DAGChaseConfiguration, DAGEquivalenceClass> configurationToEquivalenceClass;

	/**
	 * Instantiates a new equivalence classes.
	 */
	public DAGEquivalenceClasses() {
		this.configurationToEquivalenceClass = new ConcurrentHashMap<>();
	}

	/**
	 * Adds the entry.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.equivalence.dag.DAGEquivalenceClasses#addEntry(DAGChaseConfiguration)
	 */
	public void addEntry(DAGChaseConfiguration configuration) {
		DAGEquivalenceClass e;
		DAGChaseConfiguration equivalent = this.structurallyEquivalentTo(configuration);
		if(equivalent != null) {
			e = this.configurationToEquivalenceClass.get(equivalent);
			if (e==null) {
				List<DAGChaseConfiguration> toUpdate = new ArrayList<>();
				for (DAGChaseConfiguration oldKey:this.configurationToEquivalenceClass.keySet()) {
					if (!this.configurationToEquivalenceClass.get(oldKey).getRepresentative().equals(oldKey)) {
						toUpdate.add(oldKey);
					}
				}
				for (DAGChaseConfiguration oldKey:toUpdate) {
					DAGEquivalenceClass oldClass = this.configurationToEquivalenceClass.get(oldKey);
					this.configurationToEquivalenceClass.remove(oldKey);
					this.configurationToEquivalenceClass.put(oldClass.getRepresentative(), oldClass);
				}
				e = this.configurationToEquivalenceClass.get(equivalent);
			}
			DAGChaseConfiguration oldRep = e.getRepresentative();
			e.addEntry(configuration);
			if (oldRep!=e.getRepresentative()) {
				configurationToEquivalenceClass.remove(oldRep); 
				configurationToEquivalenceClass.put(e.getRepresentative(),e);
			}
		}
		else {
			e = new DAGEquivalenceClass(configuration);
		}
		this.configurationToEquivalenceClass.put(configuration, e);
	}

	/**
	 * Removes the entry.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.equivalence.dag.DAGEquivalenceClasses#removeEntry(DAGChaseConfiguration)
	 */
	public void removeEntry(DAGChaseConfiguration configuration) {
		this.removeAll(Lists.newArrayList(configuration));
	}

	/**
	 * Gets the configurations.
	 *
	 * @return Collection<DAGChaseConfiguration>
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.DAGEquivalenceClasses#getConfigurations()
	 */
	public Collection<DAGChaseConfiguration> getConfigurations() {
		return this.configurationToEquivalenceClass.keySet();
	}

	/**
	 * Removes the all.
	 *
	 * @param configurations Collection<DAGChaseConfiguration>
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#removeAll(Collection<DAGChaseConfiguration>)
	 */
	public void removeAll(Collection<DAGChaseConfiguration> configurations) {
		for(DAGChaseConfiguration configuration: configurations) {
			DAGEquivalenceClass e = this.configurationToEquivalenceClass.get(configuration);
			if (e != null) {
				this.configurationToEquivalenceClass.remove(configuration);
				e.removeEntry(configuration);
			}
		}
	}

	/**
	 * Dominated by.
	 *
	 * @param dominance the dominance
	 * @param configuration DAGChaseConfiguration
	 * @return Collection<DAGChaseConfiguration>
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.equivalence.dag.DAGEquivalenceClasses#dominatedBy(DAGChaseConfiguration)
	 */
	public Collection<DAGChaseConfiguration> dominatedBy(Dominance[] dominance, DAGChaseConfiguration configuration) {
		Collection<DAGChaseConfiguration> dominated = new LinkedHashSet<>();
		for(DAGEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
			dominated.addAll(c.dominatedBy(dominance, configuration));
		}
		return dominated;
	}

	/**
	 * Structurally equivalent to.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @return DAGChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.equivalence.dag.DAGEquivalenceClasses#structurallyEquivalentTo(DAGChaseConfiguration)
	 */
	public DAGChaseConfiguration structurallyEquivalentTo(DAGChaseConfiguration configuration) {
		for(DAGEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
			if(c.structurallyEquivalentTo(configuration)) {
				return c.getRepresentative();
			}
		}	 
		return null;
	}

	/**
	 * Dominate.
	 *
	 * @param dominance the dominance
	 * @param configuration DAGChaseConfiguration
	 * @return DAGChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.equivalence.dag.DAGEquivalenceClasses#dominate(DAGChaseConfiguration)
	 */
	public DAGChaseConfiguration dominate(Dominance[] dominance,DAGChaseConfiguration configuration) {
		for(DAGEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
			DAGChaseConfiguration dominating = c.dominate(dominance, configuration);
			if(dominating != null) {
				return dominating;
			}
		}
		return null;
	}


	/**
	 * Gets the equivalence classes.
	 *
	 * @return Collection<DAGEquivalenceClass>
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.DAGEquivalenceClasses#getEquivalenceClasses()
	 */
	public Collection<DAGEquivalenceClass> getEquivalenceClasses() {
		return Sets.<DAGEquivalenceClass>newHashSet(this.configurationToEquivalenceClass.values());
	}

	/**
	 * Gets the equivalence class.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @return DAGEquivalenceClass
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.equivalence.dag.DAGEquivalenceClasses#getEquivalenceClass(DAGChaseConfiguration)
	 */
	public DAGEquivalenceClass getEquivalenceClass(DAGChaseConfiguration configuration) {
		return this.configurationToEquivalenceClass.get(configuration);
	}

	/**
	 * Checks if is empty.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.DAGEquivalenceClasses#isEmpty()
	 */
	public boolean isEmpty() {
		return this.configurationToEquivalenceClass.isEmpty();
	}
	
	/**
	 * Number of classes
	 * @return int
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.DAGEquivalenceClasses#numberOfConfigurations()
	 */
	public int numberOfConfigurations() {
		return this.configurationToEquivalenceClass.size();
	}

	/**
	 * @param configuration DAGChaseConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.equivalence.dag.DAGEquivalenceClasses#isExistingRepresentative(DAGChaseConfiguration)
	 */
	public boolean isExistingRepresentative(DAGChaseConfiguration configuration) {
		return this.configurationToEquivalenceClass.containsKey(configuration);
	}

	/**
	 * Average class size.
	 *
	 * @return double
	 * @see uk.ac.ox.cs.pdq.planner.equivalence.dag.DAGEquivalenceClasses#averageClassSize()
	 */
	public double averageClassSize() {
		double total = 0.0;
		for(DAGEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
			total += c.size();
		}
		return total / this.configurationToEquivalenceClass.size();
	}
}