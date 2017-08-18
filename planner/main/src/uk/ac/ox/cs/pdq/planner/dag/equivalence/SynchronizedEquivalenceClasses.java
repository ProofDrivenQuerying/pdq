package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;


// TODO: Auto-generated Javadoc
/**
 * A collection of structurally equivalent classes that supports multi-threading.
 * According to this implementation different threads can add, remove or perform domination detection inside each class concurrently.
 *
 * @author Efthymia Tsamoura
 *
 */
public class SynchronizedEquivalenceClasses implements DAGEquivalenceClasses{
	
	/**  Maps each configuration to its class. */
	private final Map<DAGChaseConfiguration, SynchronizedEquivalenceClass> configurationToEquivalenceClass;

	/**
	 * Instantiates a new synchronized equivalence classes.
	 */
	public SynchronizedEquivalenceClasses() {
		this.configurationToEquivalenceClass = new ConcurrentHashMap<>(100, 10, 100);
	}

	/**
	 * Adds the entry.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#addEntry(DAGChaseConfiguration)
	 */
	@Override
	public void addEntry(DAGChaseConfiguration configuration) {
		SynchronizedEquivalenceClass e;
		DAGChaseConfiguration equivalent = this.structurallyEquivalentTo(configuration);
		if(equivalent != null) {
			e = this.configurationToEquivalenceClass.get(equivalent);
			e.addEntry(configuration);
		}
		else {
			e = new SynchronizedEquivalenceClass(configuration);
		}
		this.configurationToEquivalenceClass.put(configuration, e);
	}

	/**
	 * Removes the entry.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#removeEntry(DAGChaseConfiguration)
	 */
	@Override
	public void removeEntry(DAGChaseConfiguration configuration) {
		this.removeAll(Lists.newArrayList(configuration));
	}

	/**
	 * Gets the configurations.
	 *
	 * @return Collection<DAGChaseConfiguration>
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#getConfigurations()
	 */
	@Override
	public Collection<DAGChaseConfiguration> getConfigurations() {
		return this.configurationToEquivalenceClass.keySet();
	}

	/**
	 * Removes the all.
	 *
	 * @param configurations Collection<DAGChaseConfiguration>
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#removeAll(Collection<DAGChaseConfiguration>)
	 */
	@Override
	public void removeAll(Collection<DAGChaseConfiguration> configurations) {
		for(DAGChaseConfiguration configuration: configurations) {
			SynchronizedEquivalenceClass e = this.configurationToEquivalenceClass.get(configuration);
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
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#dominatedBy(DAGChaseConfiguration)
	 */
	@Override
	public Collection<DAGChaseConfiguration> dominatedBy(Dominance[] dominance, DAGChaseConfiguration configuration) {
		Collection<DAGChaseConfiguration> dominated = new LinkedHashSet<>();
		for(SynchronizedEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
			dominated.addAll(c.dominatedBy(dominance, configuration));
		}
		return dominated;
	}

	/**
	 * Structurally equivalent to.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @return DAGChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#structurallyEquivalentTo(DAGChaseConfiguration)
	 */
	@Override
	public DAGChaseConfiguration structurallyEquivalentTo(DAGChaseConfiguration configuration) {
		for(SynchronizedEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
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
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#dominate(DAGChaseConfiguration)
	 */
	@Override
	public DAGChaseConfiguration dominate(Dominance[] dominance,DAGChaseConfiguration configuration) {
		for(SynchronizedEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
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
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#getEquivalenceClasses()
	 */
	@Override
	public Collection<DAGEquivalenceClass> getEquivalenceClasses() {
		return Sets.<DAGEquivalenceClass>newHashSet(this.configurationToEquivalenceClass.values());
	}

	/**
	 * Gets the equivalence class.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @return DAGEquivalenceClass
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#getEquivalenceClass(DAGChaseConfiguration)
	 */
	@Override
	public DAGEquivalenceClass getEquivalenceClass(DAGChaseConfiguration configuration) {
		return this.configurationToEquivalenceClass.get(configuration);
	}

	/**
	 * Checks if is empty.
	 *
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.configurationToEquivalenceClass.isEmpty();
	}

	/**
	 * Wakeup sleep.
	 *
	 * @param plan Plan
	 */
	public void wakeupSleep(Cost cost) {
		for(SynchronizedEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
			c.wakeupSleep(cost);
		}
	}

	/**
	 * Size.
	 *
	 * @return int
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#size()
	 */
	@Override
	public int size() {
		return this.configurationToEquivalenceClass.size();
	}

	/**
	 * Contains.
	 *
	 * @param configuration DAGChaseConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#contains(DAGChaseConfiguration)
	 */
	@Override
	public boolean contains(DAGChaseConfiguration configuration) {
		return this.configurationToEquivalenceClass.containsKey(configuration);
	}

	/**
	 * Iterator.
	 *
	 * @return Iterator<Entry<DAGChaseConfiguration,SynchronizedEquivalenceClass>>
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#iterator()
	 */
	@Override
	public Iterator<Entry<DAGChaseConfiguration, SynchronizedEquivalenceClass>> iterator() {
		return this.configurationToEquivalenceClass.entrySet().iterator();
	}

	/**
	 * Clear.
	 *
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#clear()
	 */
	@Override
	public void clear() {
		this.configurationToEquivalenceClass.clear();
	}

	/**
	 * Average class size.
	 *
	 * @return double
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#averageClassSize()
	 */
	@Override
	public double averageClassSize() {
		double total = 0.0;
		for(SynchronizedEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
			total += c.size();
		}
		return total / this.configurationToEquivalenceClass.size();
	}

	/**
	 * Median class size.
	 *
	 * @return int
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#medianClassSize()
	 */
	@Override
	public int medianClassSize() {
		int[] totals = new int[this.configurationToEquivalenceClass.size()];
		if (totals.length == 0) {
			return -1;
		}
		Iterator<SynchronizedEquivalenceClass> it = this.configurationToEquivalenceClass.values().iterator();
		for (int i = 0, l = totals.length; i < l; i ++) {
			totals[i] = it.next().size();
		}
		Arrays.sort(totals);
		return totals[totals.length / 2];
	}

	/**
	 * Retain all.
	 *
	 * @param configurations Collection<DAGChaseConfiguration>
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#retainAll(Collection<DAGChaseConfiguration>)
	 */
	@Override
	public void retainAll(Collection<DAGChaseConfiguration> configurations) {
		for(DAGChaseConfiguration configuration: this.configurationToEquivalenceClass.keySet()) {
			if(!configurations.contains(configuration)) {
				SynchronizedEquivalenceClass e = this.configurationToEquivalenceClass.get(configuration);
				Preconditions.checkNotNull(e);
				this.configurationToEquivalenceClass.remove(configuration);
				e.removeEntry(configuration);
			}
		}
	}
}