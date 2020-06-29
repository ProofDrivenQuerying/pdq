// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.event;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

import uk.ac.ox.cs.pdq.planner.Explorer;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearExplorer;
import uk.ac.ox.cs.pdq.ui.model.ObservableSearchState;

// TODO: Auto-generated Javadoc
/**
 * Event-handler for capturing an displaying data point generated during a 
 * planning phase.
 * 
 * @author Julien Leblay
 *
 */
public class PlanSearchVisualizer {

	/** The log. */
	private static Logger log = Logger.getLogger(PlanSearchVisualizer.class);
	
	/**  Queue holding the next dataQueue point to be display on the plan/search views by the main thread. */
	private final ConcurrentLinkedQueue dataQueue;

	/**  Number of rounds to wait before updating. */
	private final int interval;

	/**
	 * Instantiates a new plan search visualizer.
	 *
	 * @param q the q
	 * @param interval the interval
	 */
	public PlanSearchVisualizer(ConcurrentLinkedQueue q, int interval) {
		Preconditions.checkArgument(q != null);
		Preconditions.checkArgument(interval >= 0);
		this.dataQueue = q;
		this.interval = interval;
	}

	/**
	 * Update the queue of dataQueue point to display next on the plan/search views.
	 *
	 * @param explorer the explorer
	 */
	@Subscribe
	public void processExplorerIteration(Explorer explorer) {
		try
		{
			Preconditions.checkArgument(explorer != null);
			int rounds = explorer.getRounds();
			if(explorer instanceof LinearExplorer) {
				if (rounds % this.interval == 0 && explorer.getBestPlan() != null) {
					this.dataQueue.add(new ObservableSearchState(
						explorer.getElapsedTime() / 1e6,
						rounds,
						explorer.getBestPlan(),
						explorer.getBestCost(),
						Arrays.asList(new LinearChaseConfiguration[] {((LinearExplorer)explorer).getBestNode().getConfiguration()})));
					synchronized (this.dataQueue) {
						try {
							this.dataQueue.wait();
						} catch (InterruptedException e) {
							log.error(e.getMessage(),e);
						}
					}
				}
			}
			else {
				throw new java.lang.UnsupportedOperationException();
			}
		}
		catch(Exception e)
		{
			System.out.println("processExplorerIteration(): " + e.toString());
		}
	}
}
