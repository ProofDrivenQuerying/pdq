package uk.ac.ox.cs.pdq.logging;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

/**
 * A logger that allows logging data point as key-value pair,
 * Dynamice in the sense that it is ithout prior knowledge of what is to be logged.
 * The class also offers some facilities to increment value that are numeric.
 *
 * @author Julien Leblay
 *
 */
public class DynamicStatistics extends StatisticsLogger {

	/** Map of keys to numeric data points. */
	private final Map<StatKey, Object> values = new LinkedHashMap<>();

	/**
	 * Instantiates a new dynamic statistics.
	 *
	 * @param presets the presets
	 */
	public DynamicStatistics(StatKey... presets) {
		if (presets != null) {
			for (StatKey key: presets) {
				this.values.put(key, -1);
			}
		}
	}

	/**
	 * Increases the value of the data point whose key is define in the
	 * increment i by the value defined in i.
	 *
	 * @param i the i
	 */
	@Subscribe
	public void increase(Increment i) {
		Object o = this.values.get(i.key);
		if (o != null && !(o instanceof Number)) {
			throw new IllegalArgumentException("Increment excepts numeric values.");
		}
		Number n = (Number) o;
		if (n == null || n.intValue() == -1) {
			n = i.value;
		} else {
			n = add(n, i.value);
		}
		this.values.put(i.key, n);
	}

	/**
	 * Assigns the value of the data point whose key is defined in the
	 * increment i to the value defined in i.
	 * @param a Assignment
	 */
	@Subscribe
	public void assign(Assignment a) {
		this.values.put(a.key, a.value);
	}

	/**
	 * Adds n to value, without prior knowledge of their actual type.
	 *
	 * @param n the n
	 * @param m the m
	 * @return the some of n + m, typed as m
	 */
	private static Number add(Number n, Number m) {
		Double result = n.doubleValue() + m.doubleValue();
		if (m instanceof Double) {
			return result;
		}
		if (m instanceof Long) {
			return result.longValue();
		}
		if (m instanceof Integer) {
			return result.intValue();
		}
		if (m instanceof Float) {
			return result.floatValue();
		}
		if (m instanceof Short) {
			return result.shortValue();
		}
		if (m instanceof Byte) {
			return result.byteValue();
		}
		throw new IllegalStateException();
	}

	@Override
	protected String makeLine() {
		StringBuilder result = new StringBuilder();
		for (StatKey k : this.values.keySet()) {
			result.append(this.values.get(k)).append(FIELD_SEPARATOR);
		}
		return result.toString();
	}

	@Override
	protected String makeHeader() {
		StringBuilder result = new StringBuilder();
		for (StatKey k : this.values.keySet()) {
			result.append(k).append(FIELD_SEPARATOR);
		}
		return result.toString();
	}

	/**
	 * A DynamicStatistic increment is key/value pair generally post through
	 * an event bus, carry a value to add to the existing value under this key.
	 *
	 * @author Julien Leblay
	 */
	public static class Increment {
		
		/** The key. */
		public final StatKey key;
		
		/** The value. */
		public final Number value;
		/**
		 * Constructor for Increment.
		 * @param k StatKeys
		 * @param v Number
		 */
		public Increment(StatKey k, Number v) {
			Preconditions.checkArgument(k != null);
			Preconditions.checkArgument(v != null);
			this.key = k;
			this.value = v;
		}
	}

	/**
	 * A DynamicStatistic increment is key/value pair generally post through
	 * an event bus, carry a value to assign to this key, regardless of any
	 * pre-existing value for this key.
	 *
	 * @author Julien Leblay
	 */
	public static class Assignment {
		
		/** The key. */
		public final StatKey key;
		
		/** The value. */
		public final Object value;
		/**
		 * Constructor for Assignment.
		 * @param k StatKeys
		 * @param v Object
		 */
		public Assignment(StatKey k, Object v) {
			Preconditions.checkArgument(k != null);
			Preconditions.checkArgument(v != null);
			this.key = k;
			this.value = v;
		}
	}
}
