package uk.ac.ox.cs.pdq.planner.dag.equivalence;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.dag.ApplyRule;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * A class of structurally equivalent configurations that supports multi-threading.
 * According to this implementation different threads can add, remove or perform domination detection inside the class concurrently.
 *
 * @author Efthymia Tsamoura
 *
 * @param 
 */
public class SynchronizedEquivalenceClass extends DAGEquivalenceClass{

	/** The non-representative configurations*/
	Collection<DAGChaseConfiguration> nonRepresentatives = new LinkedHashSet<>();

	/** The minimum cost configurations*/
	private DAGChaseConfiguration minCostConfiguration = null;

	/** True if the class is sleeping. */
	private boolean isSleeping = false;

	/** True if the class contains an ApplyRule configuration*/
	private boolean applyRule = false;

	private final ReentrantReadWriteLock readWriteLock =  new ReentrantReadWriteLock();
	private final Lock read  = this.readWriteLock.readLock();
	private final Lock write = this.readWriteLock.writeLock();

	/**
	 * Constructor for SynchronizedEquivalenceClass.
	 * @param configuration DAGChaseConfiguration
	 */
	public SynchronizedEquivalenceClass(DAGChaseConfiguration configuration) {
		this.representative = configuration;
		this.minHeight = configuration.getHeight();
		if(configuration.isClosed()) {
			this.minCostConfiguration = configuration;
		}
		configuration.setEquivalenceClass(this);
		if(configuration instanceof ApplyRule) {
			this.applyRule = true;
		}
	}

	/**
	 * @param configuration DAGChaseConfiguration
	 */
	@Override
	public void addEntry(DAGChaseConfiguration configuration) {
		this.write.lock();
		try {
			if(!this.isEmpty()) {
				this.nonRepresentatives.add(configuration);
			}
			else {
				this.representative = configuration;
			}
			if(configuration.isClosed() && (this.minCostConfiguration == null ||
					this.minCostConfiguration.getPlan().getCost().greaterThan(configuration.getPlan().getCost()))
					) {
				this.minCostConfiguration = configuration;
			}
			if(this.minHeight > configuration.getHeight()) {
				this.minHeight = configuration.getHeight();
			}
			configuration.setEquivalenceClass(this);
			if(configuration instanceof ApplyRule) {
				this.applyRule = true;
			}
		} finally {
			this.write.unlock();
		}
	}

	/**
	 * @param configuration DAGChaseConfiguration
	 */
	@Override
	public void removeEntry(DAGChaseConfiguration configuration) {
		this.removeAll(Lists.newArrayList(configuration));
	}

	/**
	 * @param configurations Collection<DAGChaseConfiguration>
	 */
	@Override
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
							|| configuration.getPlan().getCost().lessThan(this.minCostConfiguration.getPlan().getCost()))) {
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
	 * @return Collection<DAGChaseConfiguration>
	 */
	@Override
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
	 * @param input DAGChaseConfiguration
	 * @return Collection<DAGChaseConfiguration>
	 */
	@Override
	public Collection<DAGChaseConfiguration> dominatedBy(DAGChaseConfiguration input) {
		this.read.lock();
		try {
			Collection<DAGChaseConfiguration> dominated = new LinkedHashSet<>();
			if (!this.isEmpty()){
				for (DAGChaseConfiguration configuration: this.nonRepresentatives) {
					if(configuration.isDominatedBy(input)) {
						dominated.add(configuration);
					}
				}
				if(this.representative.isDominatedBy(input)) {
					dominated.add(this.representative);
				}
			}
			return dominated;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * @param configuration DAGChaseConfiguration
	 * @return boolean
	 */
	@Override
	public boolean structurallyEquivalentTo(DAGChaseConfiguration configuration) {
		this.read.lock();
		try {
			return !this.isEmpty() && this.representative.isStructurallyEquivalentTo(configuration);
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * @param input DAGChaseConfiguration
	 * @return DAGChaseConfiguration
	 */
	@Override
	public DAGChaseConfiguration dominate(DAGChaseConfiguration input) {
		this.read.lock();
		try {
			if (!this.isEmpty()){
				for (DAGChaseConfiguration configuration: this.nonRepresentatives) {
					if (input.isDominatedBy(configuration)) {
						return configuration;
					}
				}
				if (input.isDominatedBy(this.representative)) {
					return this.representative;
				}
			}
			return null;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * @return boolean
	 */
	@Override
	public boolean isSleeping() {
		this.read.lock();
		try {
			return this.isSleeping;
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * @param bestPlan Plan
	 */
	public void wakeupSleep(Plan bestPlan) {
		if(!this.isEmpty() &&
				!this.applyRule &&
				this.minCostConfiguration != null &&
				bestPlan != null &&
				this.minCostConfiguration.getPlan().getCost().greaterThan(bestPlan.getCost())) {
			this.isSleeping = true;
		}
		else {
			this.isSleeping = false;
		}
	}

	/**
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
	 * @return String
	 */
	@Override
	public String toString() {
		String ret = "REPRESENTATIVE" + "\n\t" + this.representative +
				"\nMINCOST" + "\n\t" + this.minCostConfiguration +
				"\nNON-REPRESENTATIVES" + "\n\t" + Joiner.on("\n").join(this.nonRepresentatives) +
				"\nISSLEEPING" + "\n\t" + this.isSleeping();
		return ret;
	}
}