package uk.ac.ox.cs.pdq.ui.event;

import java.util.concurrent.ConcurrentLinkedQueue;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.planner.explorer.Explorer;
import uk.ac.ox.cs.pdq.ui.model.ObservableSearchState;
import uk.ac.ox.cs.pdq.ui.proof.Proof;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

/**
 * Event-handler for capturing an displaying data point generated during a 
 * planning phase.
 * 
 * @author Julien Leblay
 *
 */
public class PlanSearchVisualizer implements EventHandler {

	/** Queue holding the next dataQueue point to be display on the plan/search views by the main thread */
    private final ConcurrentLinkedQueue dataQueue;
    
    /** Number of rounds to wait before updating */
    private final int interval;
    
    /**
     * @param q
     */
	public PlanSearchVisualizer(ConcurrentLinkedQueue q, int interval) {
		Preconditions.checkArgument(q != null);
		Preconditions.checkArgument(interval >= 0);
		this.dataQueue = q;
		this.interval = interval;
	}

	/**
	 * Update the queue of dataQueue point to display next on the plan/search views.
	 * @param explorer
	 */
	@Subscribe
	public void processExplorerIteration(Explorer explorer) {
		Preconditions.checkArgument(explorer != null);
		int rounds = explorer.getRounds();
		if (rounds % this.interval == 0) {
			this.dataQueue.add(new ObservableSearchState(
					explorer.getElapsedTime() / 1e6,
					rounds,
					explorer.getBestPlan(),
					Proof.toProof(explorer.getBestPlan())));
			synchronized (this.dataQueue) {
				try {
					this.dataQueue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
