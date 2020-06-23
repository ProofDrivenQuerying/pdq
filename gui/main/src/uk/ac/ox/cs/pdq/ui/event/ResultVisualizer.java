// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.event;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

import uk.ac.ox.cs.pdq.db.tuple.Tuple;

// TODO: Auto-generated Javadoc
/**
 * Event-handler for capturing an displaying tuple generated during a runtime
 * evaluation.
 * 
 * @author Julien Leblay
 *
 */
public class ResultVisualizer {
	
	/** The log. */
	private static Logger log = Logger.getLogger(ResultVisualizer.class);

	/**  Queue holding the next tuple to be display on the result views by the main thread. */
    private final ConcurrentLinkedQueue<Tuple> data;
    
    /**
     * Instantiates a new result visualizer.
     *
     * @param q the q
     */
	public ResultVisualizer(ConcurrentLinkedQueue<Tuple> q) {
		Preconditions.checkArgument(q != null);
		this.data = q;
	}

	/**
	 * Update the queue of tuples to display next on the result views.
	 *
	 * @param t the t
	 */
	@Subscribe
	public void processExplorerIteration(Tuple t) {
		Preconditions.checkArgument(t != null);
		this.data.add(t);
		synchronized (this.data) {
			try {
				this.data.wait();
			} catch (InterruptedException e) {
				log.warn(e);
			}
		}
	}
}
