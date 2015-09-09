package uk.ac.ox.cs.pdq.ui.event;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

/**
 * Event-handler for capturing an displaying tuple generated during a runtime
 * evaluation.
 * 
 * @author Julien Leblay
 *
 */
public class ResultVisualizer implements EventHandler {
	private static Logger log = Logger.getLogger(ResultVisualizer.class);

	/** Queue holding the next tuple to be display on the result views by the main thread */
    private final ConcurrentLinkedQueue<Tuple> data;
    
    /**
     * @param q
     */
	public ResultVisualizer(ConcurrentLinkedQueue<Tuple> q) {
		Preconditions.checkArgument(q != null);
		this.data = q;
	}

	/**
	 * Update the queue of tuples to display next on the result views.
	 * @param t
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
