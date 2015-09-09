package uk.ac.ox.cs.pdq.runtime.util;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.eventbus.Subscribe;

/**
 * Counts output tuples from of physical and interrupts when a limit is reached.
 * 
 * @author Julien Leblay
 */
public class TupleOutputLimitEnforcer implements EventHandler {

	private long count = 0;
	
	private final TupleIterator iterator;

	private final long limit;
	
	/**
	 * Default constructor
	 * @param out
	 */
	public TupleOutputLimitEnforcer(TupleIterator i, long l) {
		this.iterator = i;
		this.limit = l;
	}

	/**
	 * Counts the given tuple.
	 * @param tuple
	 */
	@Subscribe
	public void count(Tuple tuple) {
		this.count++;
		enforce();
	}

	/**
	 * Interrupts the tuple iterator if the output tuple limit was reached.
	 */
	private void enforce() {
		if (this.count > this.limit) {
			this.iterator.interrupt();
		}
	}
}
