package uk.ac.ox.cs.pdq.runtime.util;

import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.EventHandler;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.eventbus.Subscribe;


// TODO: Auto-generated Javadoc
/**
 * Counts output tuples from of physical and interrupts when a limit is reached.
 * 
 * @author Julien Leblay
 */
public class TupleOutputLimitEnforcer implements EventHandler {

	/** The count. */
	private long count = 0;
	
	/** The iterator. */
	private final TupleIterator iterator;

	/** The limit. */
	private final long limit;
	
	/**
	 * Default constructor.
	 *
	 * @param i the i
	 * @param l the l
	 */
	public TupleOutputLimitEnforcer(TupleIterator i, long l) {
		this.iterator = i;
		this.limit = l;
	}

	/**
	 * Counts the given tuple.
	 *
	 * @param tuple the tuple
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
