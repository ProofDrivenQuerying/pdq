// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.logging;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

/**
 * This class help collecting statistics without prior knowledge of what is to be
 * logged.
 * Logged items are identified by a key (String). Logging event can be time
 * interval, numeric increment or ad-hoc Object values.
 *
 * The statistics collection can be turned on/off through a boolean method.
 *
 * @author Julien Leblay
 *
 */
public class StatisticsCollector implements Cloneable {
	
	/**  Logger. */
	protected static Logger log = Logger.getLogger(StatisticsCollector.class);

	/** If false, no statistics are actually collected, i.e. most method have no effect. */
	private boolean collecting = false;

	/** The event bus where to post statistics events. */
	private final EventBus eventBus;

	/** Map keeps timing informations. */
	private final Map<StatKey, Long> ticks = new LinkedHashMap<>();

	/**
	 * Initialising a non-collecting instance.
	 *
	 * @param eventBus the event bus
	 */
	public StatisticsCollector(EventBus eventBus) {
		this(false, eventBus);
	}

	/**
	 * Default constructor.
	 *
	 * @param collecting the collecting
	 * @param eventBus the event bus
	 */
	public StatisticsCollector(boolean collecting, EventBus eventBus) {
		super();
		Preconditions.checkArgument(eventBus == null ? !collecting: true, "Statisitics collection enable while event bus is not initialized");
		this.collecting = collecting;
		this.eventBus = eventBus;
	}

	/**
	 * Clone.
	 *
	 * @return StatisticsCollector
	 */
	@Override
	public StatisticsCollector clone() {
		return new StatisticsCollector(this.collecting, this.eventBus);
	}

	/**
	 * Turns on/off the statistics collection. When set to false, methods
	 * start, stop, increase and set have no effect.
	 *
	 * @param collectStats the collect stats
	 */
	public void collect(boolean collectStats) {
		Preconditions.checkArgument(collectStats ? this.eventBus != null : true, "Statisitics collection enabled while event bus is not initialized");
		this.collecting = collectStats;
	}

	/**
	 * Starts a timer for the given key.
	 *
	 * @param key the key
	 */
	public void start(StatKey key) {
		if (this.collecting) {
			assert !this.ticks.containsKey(key): "Concurrent use of '" + key + "' in stats collector";
			this.ticks.put(key, System.nanoTime());
		}
	}

	/**
	 * Stops the timer for the given key, and posts the corresponding event to
	 * the bus.
	 *
	 * @param key the key
	 */
	public void stop(StatKey key) {
		if (this.collecting) {
			Long n = this.ticks.get(key);
			assert n != null : "Never started timer for '" + key + "'";
			this.eventBus.post(new DynamicStatistics.Increment(key, (System.nanoTime() - n) / 1e6));
			this.ticks.remove(key);
		}
	}

	/**
	 * Post the given key/value pair to the event bus as a value increment.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void increase(StatKey key, Number value) {
		if (this.collecting) {
			this.eventBus.post(new DynamicStatistics.Increment(key, value));
		}
	}

	/**
	 * Post the given key/value pair to the event bus as a value assignment.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void set(StatKey key, Object value) {
		if (this.collecting) {
			this.eventBus.post(new DynamicStatistics.Assignment(key, value));
		}
	}

	/**
	 * Checks if it is collecting.
	 *
	 * @return boolean
	 */
	public boolean isCollecting() {
		return this.collecting;
	}
}
