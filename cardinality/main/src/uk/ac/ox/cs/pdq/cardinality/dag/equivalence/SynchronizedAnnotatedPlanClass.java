/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.dag.equivalence;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import uk.ac.ox.cs.pdq.cardinality.dag.ConfigurationUtility;
import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.reasoning.chase.dominance.Dominance;
import uk.ac.ox.cs.pdq.cardinality.reasoning.chase.equivalence.FastStructuralEquivalence;
import uk.ac.ox.cs.pdq.cardinality.reasoning.chase.equivalence.StructuralEquivalence;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * A class of structurally equivalent configurations that supports multi-threading.
 * According to this implementation different threads can add, remove or perform domination detection inside the class concurrently.
 *
 * @author Efthymia Tsamoura
 *
 */
public class SynchronizedAnnotatedPlanClass extends DAGAnnotatedPlanClass{

	/**  The non-representative configurations. */
	Collection<DAGAnnotatedPlan> nonRepresentatives = new LinkedHashSet<>();

	/**  Performs structural equivalence checks. */
	private final StructuralEquivalence structuralEquivalence = new FastStructuralEquivalence();

	/** The read write lock. */
	private final ReentrantReadWriteLock readWriteLock =  new ReentrantReadWriteLock();
	
	/** The read. */
	private final Lock read  = this.readWriteLock.readLock();
	
	/** The write. */
	private final Lock write = this.readWriteLock.writeLock();

	/**
	 * Constructor for SynchronizedEquivalenceClass.
	 * @param configuration DAGAnnotatedPlan
	 */
	public SynchronizedAnnotatedPlanClass(DAGAnnotatedPlan configuration) {
		this.representative = configuration;
		this.minHeight = configuration.getHeight();
	}

	/**
	 * Adds the entry.
	 *
	 * @param configuration DAGAnnotatedPlan
	 */
	@Override
	public void addEntry(DAGAnnotatedPlan configuration) {
		this.write.lock();
		try {
			if(!this.isEmpty()) {
				this.nonRepresentatives.add(configuration);
			}
			else {
				this.representative = configuration;
			}
			if(this.minHeight > configuration.getHeight()) {
				this.minHeight = configuration.getHeight();
			}
		} finally {
			this.write.unlock();
		}
	}

	/**
	 * Removes the entry.
	 *
	 * @param configuration DAGAnnotatedPlan
	 */
	@Override
	public void removeEntry(DAGAnnotatedPlan configuration) {
		this.removeAll(Lists.newArrayList(configuration));
	}

	/**
	 * Removes the all.
	 *
	 * @param configurations Collection<DAGAnnotatedPlan>
	 */
	@Override
	public void removeAll(Collection<DAGAnnotatedPlan> configurations) {
		this.write.lock();
		try {
			this.nonRepresentatives.removeAll(configurations);
			if (configurations.contains(this.representative)) {
				this.representative = null;
				Iterator<DAGAnnotatedPlan> iterator = this.nonRepresentatives.iterator();
				if (iterator.hasNext()) {
					this.representative = iterator.next();
					iterator.remove();
				}
			}
			if (!this.isEmpty()) {
				this.minHeight = this.representative.getHeight();
				for (DAGAnnotatedPlan configuration: this.nonRepresentatives) {
					if(configuration.getHeight() < this.minHeight) {
						this.minHeight = configuration.getHeight();
					}
				}
			}
			else {
				this.minHeight = Integer.MAX_VALUE;
			}
		} finally {
			this.write.unlock();
		}
	}

	/**
	 * Gets the all.
	 *
	 * @return Collection<DAGAnnotatedPlan>
	 */
	@Override
	public Collection<DAGAnnotatedPlan> getAll() {
		this.read.lock();
		try {
			Collection<DAGAnnotatedPlan> entries = new LinkedHashSet<>();
			if(!this.isEmpty()) {
				entries.addAll(this.nonRepresentatives);
				entries.add(this.representative);
			}
			return entries;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * Dominated by.
	 *
	 * @param dominance the dominance
	 * @param input DAGAnnotatedPlan
	 * @return Collection<DAGAnnotatedPlan>
	 */
	@Override
	public Collection<DAGAnnotatedPlan> dominatedBy(Dominance[] dominance, DAGAnnotatedPlan input) {
		this.read.lock();
		try {
			Collection<DAGAnnotatedPlan> dominated = new LinkedHashSet<>();
			if (!this.isEmpty()){
				for (DAGAnnotatedPlan configuration: this.nonRepresentatives) {
					if(ConfigurationUtility.isDominatedBy(dominance, input, configuration)) {
						dominated.add(configuration);
					}
				}
				if(ConfigurationUtility.isDominatedBy(dominance, input, this.representative)) {
					dominated.add(this.representative);
				}
			}
			return dominated;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * Structurally equivalent to.
	 *
	 * @param configuration DAGAnnotatedPlan
	 * @return boolean
	 */
	@Override
	public boolean structurallyEquivalentTo(DAGAnnotatedPlan configuration) {
		this.read.lock();
		try {
			return !this.isEmpty() && this.structuralEquivalence.isEquivalent(this.representative, configuration);
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * Dominate.
	 *
	 * @param dominance the dominance
	 * @param input DAGAnnotatedPlan
	 * @return DAGAnnotatedPlan
	 */
	@Override
	public DAGAnnotatedPlan dominate(Dominance[] dominance, DAGAnnotatedPlan input) {
		this.read.lock();
		try {
			if (!this.isEmpty()){
				for (DAGAnnotatedPlan configuration: this.nonRepresentatives) {
					if (ConfigurationUtility.isDominatedBy(dominance, configuration, input)) {
						return configuration;
					}
				}
				if (ConfigurationUtility.isDominatedBy(dominance, this.representative, input)) {
					return this.representative;
				}
			}
			return null;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * Checks if is empty.
	 *
	 * @return boolean
	 */
	@Override
	public boolean isEmpty() {
		this.read.lock();
		try {
			return this.representative == null && this.nonRepresentatives.isEmpty();
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * Size.
	 *
	 * @return int
	 */
	@Override
	public int size() {
		this.read.lock();
		try {
			int size = this.nonRepresentatives.size();
			if(this.representative != null) {
				size++;
			}
			return size;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * To string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		String ret = "REPRESENTATIVE" + "\n\t" + this.representative +
				"\nNON-REPRESENTATIVES" + "\n\t" + Joiner.on("\n").join(this.nonRepresentatives); 
		return ret;
	}
}