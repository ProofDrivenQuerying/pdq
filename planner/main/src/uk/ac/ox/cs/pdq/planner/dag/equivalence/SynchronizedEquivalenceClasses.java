package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.dominance.Dominance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * A collection of structurally equivalent classes that supports multi-threading.
 * According to this implementation different threads can add, remove or perform domination detection inside each class concurrently.
 *
 * @author Efthymia Tsamoura
 *
 */
public class SynchronizedEquivalenceClasses implements DAGEquivalenceClasses{
	
	/** Maps each configuration to its class*/
	private final Map<DAGChaseConfiguration, SynchronizedEquivalenceClass> configurationToEquivalenceClass;

	public SynchronizedEquivalenceClasses() {
		this.configurationToEquivalenceClass = new ConcurrentHashMap<>(100, 10, 100);
	}

	/**
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
	 * @param configuration DAGChaseConfiguration
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#removeEntry(DAGChaseConfiguration)
	 */
	@Override
	public void removeEntry(DAGChaseConfiguration configuration) {
		this.removeAll(Lists.newArrayList(configuration));
	}

	/**
	 * @return Collection<DAGChaseConfiguration>
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#getConfigurations()
	 */
	@Override
	public Collection<DAGChaseConfiguration> getConfigurations() {
		return this.configurationToEquivalenceClass.keySet();
	}

	/**
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
	 * @return Collection<DAGEquivalenceClass>
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#getEquivalenceClasses()
	 */
	@Override
	public Collection<DAGEquivalenceClass> getEquivalenceClasses() {
		return Sets.<DAGEquivalenceClass>newHashSet(this.configurationToEquivalenceClass.values());
	}

	/**
	 * @param configuration DAGChaseConfiguration
	 * @return DAGEquivalenceClass
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#getEquivalenceClass(DAGChaseConfiguration)
	 */
	@Override
	public DAGEquivalenceClass getEquivalenceClass(DAGChaseConfiguration configuration) {
		return this.configurationToEquivalenceClass.get(configuration);
	}

	/**
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.configurationToEquivalenceClass.isEmpty();
	}

	/**
	 * @param plan Plan
	 */
	public void wakeupSleep(Plan plan) {
		for(SynchronizedEquivalenceClass c: this.configurationToEquivalenceClass.values()) {
			c.wakeupSleep(plan);
		}
	}

	/**
	 * @return int
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#size()
	 */
	@Override
	public int size() {
		return this.configurationToEquivalenceClass.size();
	}

	/**
	 * @param configuration DAGChaseConfiguration
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.DAGEquivalenceClasses#contains(DAGChaseConfiguration)
	 */
	@Override
	public boolean contains(DAGChaseConfiguration configuration) {
		return this.configurationToEquivalenceClass.containsKey(configuration);
	}

	/**
	 * @return Iterator<Entry<DAGChaseConfiguration,SynchronizedEquivalenceClass>>
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#iterator()
	 */
	@Override
	public Iterator<Entry<DAGChaseConfiguration, SynchronizedEquivalenceClass>> iterator() {
		return this.configurationToEquivalenceClass.entrySet().iterator();
	}

	/**
	 * @see uk.ac.ox.cs.pdq.planner.dag.equivalence.DAGEquivalenceClasses#clear()
	 */
	@Override
	public void clear() {
		this.configurationToEquivalenceClass.clear();
	}

	/**
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