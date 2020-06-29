// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.tuple;

import java.io.PrintStream;

import org.apache.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;


/**
 * Prints tuple to the given print stream, if provided, log.info otherwise.
 * 
 * @author Julien Leblay
 */
public class TupleCounter {

	/** TupleCounterTest logger. */
	private static Logger log = Logger.getLogger(TupleCounter.class);

	/** PrintStream where to print tuples. */
	private final PrintStream out;

	/** The count. */
	private long count = 0;
	
	/**
	 * Default constructor.
	 *
	 * @param out the out
	 */
	public TupleCounter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Empty constructor, causes the tuple to be printed to log.info.
	 */
	public TupleCounter() {
		this(null);
	}

	/**
	 * Counts the given tuple.
	 *
	 * @param tuple the tuple
	 */
	@Subscribe
	public void count(Tuple tuple) {
		this.count++;
	}
	
	/**
	 * Reset.
	 */
	public void reset() {
		this.count = 0;
	}
	
	/**
	 * Report.
	 */
	public void report() {
		String message = "Tuple count: " + this.count;
		if (this.out != null) {
			this.out.println(message);
		} else {
			log.info(message);
		}
	}
}
