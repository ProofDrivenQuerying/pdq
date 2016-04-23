/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.equivalence;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.reasoning.chase.dominance.Dominance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


// TODO: Auto-generated Javadoc
/**
 * A collection of structurally equivalent classes that supports multi-threading.
 * According to this implementation different threads can add, remove or perform domination detection inside each class concurrently.
 *
 * @author Efthymia Tsamoura
 *
 */
public class SynchronizedAnnotatedPlanClasses implements DAGAnnotatedPlanClasses{
	
	/**  Maps each configuration to its class. */
	private final Map<DAGAnnotatedPlan, SynchronizedAnnotatedPlanClass> configurationToEquivalenceClass;

	/**
	 * Instantiates a new synchronized annotated plan classes.
	 */
	public SynchronizedAnnotatedPlanClasses() {
		this.configurationToEquivalenceClass = new ConcurrentHashMap<>(100, 10, 100);
	}

	/**
	 * Adds the entry.
	 *
	 * @param configuration DAGAnnotatedPlan
	 * @see uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses.dag.AnnotatedPlanClasses#addEntry(DAGAnnotatedPlan)
	 */
	@Override
	public void addEntry(DAGAnnotatedPlan configuration) {
		SynchronizedAnnotatedPlanClass e;
		DAGAnnotatedPlan equivalent = this.structurallyEquivalentTo(configuration);
		if(equivalent != null) {
			e = this.configurationToEquivalenceClass.get(equivalent);
			e.addEntry(configuration);
		}
		else {
			e = new SynchronizedAnnotatedPlanClass(configuration);
		}
		this.configurationToEquivalenceClass.put(configuration, e);
	}

	/**
	 * Removes the entry.
	 *
	 * @param configuration DAGAnnotatedPlan
	 * @see uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses.dag.AnnotatedPlanClasses#removeEntry(DAGAnnotatedPlan)
	 */
	@Override
	public void removeEntry(DAGAnnotatedPlan configuration) {
		this.removeAll(Lists.newArrayList(configuration));
	}

	/**
	 * Gets the configurations.
	 *
	 * @return Collection<DAGAnnotatedPlan>
	 * @see uk.DAGAnnotatedPlanClasses.ac.ox.cs.pdq.planner.dag.equivalence.AnnotatedPlanClasses#getConfigurations()
	 */
	@Override
	public Collection<DAGAnnotatedPlan> getConfigurations() {
		return this.configurationToEquivalenceClass.keySet();
	}

	/**
	 * Removes the all.
	 *
	 * @param configurations Collection<DAGAnnotatedPlan>
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.AnnotatedPlanClasses#removeAll(Collection<DAGAnnotatedPlan>)
	 */
	@Override
	public void removeAll(Collection<DAGAnnotatedPlan> configurations) {
		for(DAGAnnotatedPlan configuration: configurations) {
			SynchronizedAnnotatedPlanClass e = this.configurationToEquivalenceClass.get(configuration);
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
	 * @param configuration DAGAnnotatedPlan
	 * @return Collection<DAGAnnotatedPlan>
	 * @see uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses.dag.AnnotatedPlanClasses#dominatedBy(DAGAnnotatedPlan)
	 */
	@Override
	public Collection<DAGAnnotatedPlan> dominatedBy(Dominance[] dominance, DAGAnnotatedPlan configuration) {
		Collection<DAGAnnotatedPlan> dominated = new LinkedHashSet<>();
		for(SynchronizedAnnotatedPlanClass c: this.configurationToEquivalenceClass.values()) {
			dominated.addAll(c.dominatedBy(dominance, configuration));
		}
		return dominated;
	}

	/**
	 * Structurally equivalent to.
	 *
	 * @param configuration DAGAnnotatedPlan
	 * @return DAGAnnotatedPlan
	 * @see uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses.dag.AnnotatedPlanClasses#structurallyEquivalentTo(DAGAnnotatedPlan)
	 */
	@Override
	public DAGAnnotatedPlan structurallyEquivalentTo(DAGAnnotatedPlan configuration) {
		for(SynchronizedAnnotatedPlanClass c: this.configurationToEquivalenceClass.values()) {
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
	 * @param configuration DAGAnnotatedPlan
	 * @return DAGAnnotatedPlan
	 * @see uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses.dag.AnnotatedPlanClasses#dominate(DAGAnnotatedPlan)
	 */
	@Override
	public DAGAnnotatedPlan dominate(Dominance[] dominance,DAGAnnotatedPlan configuration) {
		for(SynchronizedAnnotatedPlanClass c: this.configurationToEquivalenceClass.values()) {
			DAGAnnotatedPlan dominating = c.dominate(dominance, configuration);
			if(dominating != null) {
				return dominating;
			}
		}
		return null;
	}


	/**
	 * Gets the equivalence classes.
	 *
	 * @return Collection<AnnotatedPlanClass>
	 * @see uk.DAGAnnotatedPlanClasses.ac.ox.cs.pdq.planner.dag.equivalence.AnnotatedPlanClasses#getEquivalenceClasses()
	 */
	@Override
	public Collection<DAGAnnotatedPlanClass> getEquivalenceClasses() {
		return Sets.<DAGAnnotatedPlanClass>newHashSet(this.configurationToEquivalenceClass.values());
	}

	/**
	 * Gets the equivalence class.
	 *
	 * @param configuration DAGAnnotatedPlan
	 * @return AnnotatedPlanClass
	 * @see uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses.dag.AnnotatedPlanClasses#getEquivalenceClass(DAGAnnotatedPlan)
	 */
	@Override
	public DAGAnnotatedPlanClass getEquivalenceClass(DAGAnnotatedPlan configuration) {
		return this.configurationToEquivalenceClass.get(configuration);
	}

	/**
	 * Checks if is empty.
	 *
	 * @return boolean
	 * @see uk.DAGAnnotatedPlanClasses.ac.ox.cs.pdq.planner.dag.equivalence.AnnotatedPlanClasses#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.configurationToEquivalenceClass.isEmpty();
	}

	/**
	 * Size.
	 *
	 * @return int
	 * @see uk.DAGAnnotatedPlanClasses.ac.ox.cs.pdq.planner.dag.equivalence.AnnotatedPlanClasses#size()
	 */
	@Override
	public int size() {
		return this.configurationToEquivalenceClass.size();
	}

	/**
	 * Contains.
	 *
	 * @param configuration DAGAnnotatedPlan
	 * @return boolean
	 * @see uk.ac.ox.cs.pdq.cardinality.dag.equivalence.DAGAnnotatedPlanClasses.dag.AnnotatedPlanClasses#contains(DAGAnnotatedPlan)
	 */
	@Override
	public boolean contains(DAGAnnotatedPlan configuration) {
		return this.configurationToEquivalenceClass.containsKey(configuration);
	}

	/**
	 * Iterator.
	 *
	 * @return Iterator<Entry<DAGAnnotatedPlan,SynchronizedAnnotatedPlanClass>>
	 * @see uk.DAGAnnotatedPlanClasses.ac.ox.cs.pdq.planner.dag.equivalence.AnnotatedPlanClasses#iterator()
	 */
	@Override
	public Iterator<Entry<DAGAnnotatedPlan, SynchronizedAnnotatedPlanClass>> iterator() {
		return this.configurationToEquivalenceClass.entrySet().iterator();
	}

	/**
	 * Clear.
	 *
	 * @see uk.DAGAnnotatedPlanClasses.ac.ox.cs.pdq.planner.dag.equivalence.AnnotatedPlanClasses#clear()
	 */
	@Override
	public void clear() {
		this.configurationToEquivalenceClass.clear();
	}

	/**
	 * Average class size.
	 *
	 * @return double
	 * @see uk.DAGAnnotatedPlanClasses.ac.ox.cs.pdq.planner.dag.equivalence.AnnotatedPlanClasses#averageClassSize()
	 */
	@Override
	public double averageClassSize() {
		double total = 0.0;
		for(SynchronizedAnnotatedPlanClass c: this.configurationToEquivalenceClass.values()) {
			total += c.size();
		}
		return total / this.configurationToEquivalenceClass.size();
	}

	/**
	 * Median class size.
	 *
	 * @return int
	 * @see uk.DAGAnnotatedPlanClasses.ac.ox.cs.pdq.planner.dag.equivalence.AnnotatedPlanClasses#medianClassSize()
	 */
	@Override
	public int medianClassSize() {
		int[] totals = new int[this.configurationToEquivalenceClass.size()];
		if (totals.length == 0) {
			return -1;
		}
		Iterator<SynchronizedAnnotatedPlanClass> it = this.configurationToEquivalenceClass.values().iterator();
		for (int i = 0, l = totals.length; i < l; i ++) {
			totals[i] = it.next().size();
		}
		Arrays.sort(totals);
		return totals[totals.length / 2];
	}

	/**
	 * Retain all.
	 *
	 * @param configurations Collection<DAGAnnotatedPlan>
	 * @see uk.ac.ox.cs.pdq.equivalence.dag.AnnotatedPlanClasses#retainAll(Collection<DAGAnnotatedPlan>)
	 */
	@Override
	public void retainAll(Collection<DAGAnnotatedPlan> configurations) {
		for(DAGAnnotatedPlan configuration: this.configurationToEquivalenceClass.keySet()) {
			if(!configurations.contains(configuration)) {
				SynchronizedAnnotatedPlanClass e = this.configurationToEquivalenceClass.get(configuration);
				Preconditions.checkNotNull(e);
				this.configurationToEquivalenceClass.remove(configuration);
				e.removeEntry(configuration);
			}
		}
	}
}