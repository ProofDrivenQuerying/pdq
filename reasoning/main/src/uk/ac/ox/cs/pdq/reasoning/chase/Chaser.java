package uk.ac.ox.cs.pdq.reasoning.chase;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;

/**
 * (From A. C. Onet) 
 * The chase procedure is an iteration of steps that either adds a new tuple to satisfy
	a TGD, either changes the instance to model some equality-generating-dependency,
	or fails when the instance could not be changed to satisfy an equality-generating dependency. 
 * @author Efthymia Tsamoura
 */
public abstract class Chaser {

	/** The log. */
	protected static Logger log = Logger.getLogger(Chaser.class);

	/**  Collects statistics related to chasing *. */
	protected final StatisticsCollector statistics;

	/**
	 * Constructor for Chaser.
	 *
	 * @param statistics the statistics
	 */
	public Chaser(StatisticsCollector statistics) {
		this.statistics = statistics;
	}
	

	/**
	 * Chases the input state until termination.
	 *
	 * @param <S> the generic type
	 * @param instance the instance
	 * @param dependencies the dependencies
	 */
	public abstract <S extends ChaseInstance> void reasonUntilTermination(S instance, Dependency[] dependencies);
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public abstract Chaser clone();
}
