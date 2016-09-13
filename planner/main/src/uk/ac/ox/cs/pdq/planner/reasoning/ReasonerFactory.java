package uk.ac.ox.cs.pdq.planner.reasoning;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;

import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Creates reasoners based on the input arguments.
 *
 * @author Efthymia Tsamoura
 * @author Julien Leblay
 */
public final class ReasonerFactory extends uk.ac.ox.cs.pdq.reasoning.ReasonerFactory{

	/** The log. */
	protected static Logger log = Logger.getLogger(ReasonerFactory.class);

	/**
	 * Instantiates a new reasoner factory.
	 *
	 * @param eventBus the event bus
	 * @param collectStats the collect stats
	 * @param params the params
	 */
	public ReasonerFactory(
			EventBus eventBus,
			boolean collectStats,
			ReasoningParameters params) {
		super(eventBus, collectStats, params);
	}

	/**
	 * Gets the single instance of ReasonerFactory.
	 *
	 * @return 		a fresh instance of reasoner. Currently, all reasoners
	 *      implement the Chaser interface.
	 */
	public Chaser getInstance() {
		return super.getInstance();
	}
}
