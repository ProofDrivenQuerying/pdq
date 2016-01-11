package uk.ac.ox.cs.pdq.planner.reasoning;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.eventbus.EventBus;

/**
 * Creates reasoners based on the input arguments
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 *
 */
public final class ReasonerFactory extends uk.ac.ox.cs.pdq.reasoning.ReasonerFactory{

	protected static Logger log = Logger.getLogger(ReasonerFactory.class);

	/**
	 *
	 * @param eventBus
	 * @param collectStats
	 * @param params
	 */
	public ReasonerFactory(
			EventBus eventBus,
			boolean collectStats,
			ReasoningParameters params) {
		super(eventBus, collectStats, params);
	}

	/**
	 * @return
	 * 		a fresh instance of reasoner. Currently, all reasoners
	 *      implement the Chaser interface.
	 */
	public Chaser getInstance() {
		return super.getInstance();
	}
}
