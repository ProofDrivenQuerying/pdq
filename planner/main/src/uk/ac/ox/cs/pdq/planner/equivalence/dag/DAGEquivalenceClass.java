// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.planner.equivalence.dag;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.Dominance;
import uk.ac.ox.cs.pdq.planner.equivalence.FastStructuralEquivalence;
import uk.ac.ox.cs.pdq.planner.equivalence.StructuralEquivalence;

/**
 * A class of structurally equivalent configurations that supports multi-threading.
 * According to this implementation different threads can add, 
 * remove or perform domination detection inside the class concurrently.
 *
 * @author Efthymia Tsamoura
 *
 */
public class DAGEquivalenceClass {
	/**  The representative of this class. */
	private DAGChaseConfiguration representative;

	/**  The minimum depth configuration of this class. */
	protected Integer minHeight;

	/**  The non-representative configurations. */
	Collection<DAGChaseConfiguration> nonRepresentatives = new LinkedHashSet<>();

	/**  The minimum cost configurations. */
	private DAGChaseConfiguration minCostConfiguration = null;

	/** True if the class is sleeping. */
	private boolean isSleeping = false;

	/**  True if the class contains an ApplyRule configuration. */
	private boolean applyRule = false;
	
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
	 * @param configuration DAGChaseConfiguration
	 */
	public DAGEquivalenceClass(DAGChaseConfiguration configuration) {
		this.representative = configuration;
		this.minHeight = configuration.getHeight();
		if(configuration.isClosed()) {
			this.minCostConfiguration = configuration;
		}
		if(configuration instanceof ApplyRule) {
			this.applyRule = true;
		}
	}

	/**
	 * Adds the entry.
	 *
	 * @param configuration DAGChaseConfiguration
	 */
	protected void addEntry(DAGChaseConfiguration configuration) {
		this.write.lock();
		try {
			if(!this.isEmpty()) {
				this.nonRepresentatives.add(configuration);
			}
			else {
				this.representative = configuration;
			}
			if(configuration.isClosed() && (this.minCostConfiguration == null ||
					this.minCostConfiguration.getCost()== null || this.minCostConfiguration.getCost().greaterThan(configuration.getCost()))
					) {
				this.minCostConfiguration = configuration;
			}
			if(this.minHeight > configuration.getHeight()) {
				this.minHeight = configuration.getHeight();
			}
			if(configuration instanceof ApplyRule) {
				this.applyRule = true;
			}
		} finally {
			this.write.unlock();
		}
	}

	/**
	 * Removes the entry.
	 *
	 * @param configuration DAGChaseConfiguration
	 */
	public void removeEntry(DAGChaseConfiguration configuration) {
		this.removeAll(Lists.newArrayList(configuration));
	}

	/**
	 * Removes the all.
	 *
	 * @param configurations Collection<DAGChaseConfiguration>
	 */
	public void removeAll(Collection<DAGChaseConfiguration> configurations) {
		this.write.lock();
		try {
			this.applyRule = false;
			this.nonRepresentatives.removeAll(configurations);
			if (configurations.contains(this.representative)) {
				this.representative = null;
				Iterator<DAGChaseConfiguration> iterator = this.nonRepresentatives.iterator();
				if (iterator.hasNext()) {
					this.representative = iterator.next();
					iterator.remove();
				}
			}
			if (!this.isEmpty()) {
				this.minCostConfiguration = null;
				if(this.representative.isClosed()) {
					this.minCostConfiguration = this.representative;
				}
				if(this.representative instanceof ApplyRule) {
					this.applyRule = true;
				}

				this.minHeight = this.representative.getHeight();
				for (DAGChaseConfiguration configuration: this.nonRepresentatives) {
					if (configuration.isClosed()
							&& (this.minCostConfiguration == null
							|| configuration.getCost().lessThan(this.minCostConfiguration.getCost()))) {
						this.minCostConfiguration = configuration;
					}
					if(configuration.getHeight() < this.minHeight) {
						this.minHeight = configuration.getHeight();
					}
					if(configuration instanceof ApplyRule) {
						this.applyRule = true;
					}
				}
			}
			else {
				this.minCostConfiguration = null;
				this.minHeight = Integer.MAX_VALUE;
				this.isSleeping = true;
				this.applyRule = false;
			}
		} finally {
			this.write.unlock();
		}
	}

	/**
	 * Gets all configurations in the class.
	 *
	 * @return Collection<DAGChaseConfiguration>
	 */
	public Collection<DAGChaseConfiguration> getAll() {
		this.read.lock();
		try {
			Collection<DAGChaseConfiguration> entries = new LinkedHashSet<>();
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
	 * @param input DAGChaseConfiguration
	 * @return Collection<DAGChaseConfiguration>
	 */
	public Collection<DAGChaseConfiguration> dominatedBy(Dominance[] dominance, DAGChaseConfiguration input) {
		this.read.lock();
		try {
			Collection<DAGChaseConfiguration> dominated = new LinkedHashSet<>();
			if (!this.isEmpty()){
				for (DAGChaseConfiguration configuration: this.nonRepresentatives) {
					if(isDominatedBy(dominance, input, configuration)) {
						dominated.add(configuration);
					}
				}
				if(isDominatedBy(dominance, input, this.representative)) {
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
	 * @param configuration DAGChaseConfiguration
	 * @return boolean
	 */
	public boolean structurallyEquivalentTo(DAGChaseConfiguration configuration) {
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
	 * @param input DAGChaseConfiguration
	 * @return DAGChaseConfiguration
	 */
	public DAGChaseConfiguration dominate(Dominance[] dominance, DAGChaseConfiguration input) {
		this.read.lock();
		try {
			if (!this.isEmpty()){
				for (DAGChaseConfiguration configuration: this.nonRepresentatives) {
					if (isDominatedBy(dominance, configuration, input)) {
						return configuration;
					}
				}
				if (isDominatedBy(dominance, this.representative, input)) {
					return this.representative;
				}
			}
			return null;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * Checks if is sleeping.
	 *
	 * @return boolean
	 */
	public boolean isSleeping() {
		this.read.lock();
		try {
			return this.isSleeping;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * Wakeup sleep.
	 *
	 * @param bestPlan Plan
	 */
	public void wakeupSleep(Cost bestCost) {
		if(!this.isEmpty() &&
				!this.applyRule &&
				this.minCostConfiguration != null &&
				bestCost != null &&
				this.minCostConfiguration.getCost().greaterThan(bestCost)) {
			this.isSleeping = true;
		}
		else {
			this.isSleeping = false;
		}
	}

	/**
	 * Checks if is empty.
	 *
	 * @return boolean
	 */
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
	public String toString() {
		String ret = "REPRESENTATIVE" + "\n\t" + this.representative +
				"\nMINCOST" + "\n\t" + this.minCostConfiguration +
				"\nNON-REPRESENTATIVES" + "\n\t" + Joiner.on("\n").join(this.nonRepresentatives) +
				"\nISSLEEPING" + "\n\t" + this.isSleeping();
		return ret;
	}

	/**
	 *
	 * @param dominance the detector of dominance
	 * @param target the target
	 * @param source the source
	 * @return true if the source configuration is dominated by the target one
	 */
	public static boolean isDominatedBy(Dominance[] dominance, DAGChaseConfiguration target, DAGChaseConfiguration source) {
		for(Dominance detector:dominance) {
			if(detector.isDominated(source, target)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Gets the representative.
	 *
	 * @return DAGChaseConfiguration
	 */
	public DAGChaseConfiguration getRepresentative() {
		return this.representative;
	}

	/**
	 * Gets the min height.
	 *
	 * @return Integer
	 */
	public Integer getMinHeight() {
		return this.minHeight;
	}
	
}