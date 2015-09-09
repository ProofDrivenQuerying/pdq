package uk.ac.ox.cs.pdq.runtime.util;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

import com.google.common.eventbus.Subscribe;

/**
 * Prints a report of a physical operator tree usage.
 * 
 * @author Julien Leblay
 */
public class ExecutionReport implements EventHandler {

	/** PrintStream where to print tuples. */
	private final PrintStream out;
	
	Map<TupleIterator, Integer> counters = new LinkedHashMap<>();

	/**
	 * Default constructor
	 * @param out
	 */
	public ExecutionReport(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Empty constructor, causes the tuple to be printed to log.info.
	 */
	public ExecutionReport() {
		this(System.out);
	}

	/**
	 * Prints the given tuple of the default print stream, or log.info if null.
	 * @param t TupleIterator
	 */
	@Subscribe
	public void print(TupleIterator t) {
		Integer i = this.counters.get(t);
		if (i == null) {
			i = 0;
		}
		this.counters.put(t, ++i);
	}
	
	public void reset() {
		this.counters = new LinkedHashMap<>();
	}
	
	public void report() {
		for (TupleIterator t: this.counters.keySet()) {
			Integer i = this.counters.get(t);
			if (i == null) {
				i = 0;
			}
			this.out.println(i + "\t: " + t);
		}
	}
}
