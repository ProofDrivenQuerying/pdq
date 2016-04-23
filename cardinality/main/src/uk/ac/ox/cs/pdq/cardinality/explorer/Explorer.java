/*
 * 
 */
package uk.ac.ox.cs.pdq.cardinality.explorer;


import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.LimitReachedException.Reasons;
import uk.ac.ox.cs.pdq.cardinality.CardinalityException;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.plan.Plan;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Searches for a feasible plan w.r.t. the relations' bindings and the schema dependencies.
 *
 * @author Efthymia Tsamoura
 * @param <P> the generic type
 */
public abstract class Explorer<P extends Plan> {

	/** The log. */
	protected static Logger log = Logger.getLogger(Explorer.class);

	/**  The best plan. */
	protected P bestPlan = null;
	
	/**  If true then the explorer must terminate immediately. */
	protected boolean forcedTermination = false;

	/**  Exploration time elapsed so far (in nanoseconds). */
	protected double elapsedTime = 0;

	/**  Clock mark to computer the elapsed time. */
	protected double tick;

	/**  Number of iterations. */
	protected int rounds = 0;

	/**  Maximum time the explorer could search for a plan (in milliseconds). */
	protected boolean exceptionOnLimit = false;

	/**  Maximum time the explorer could search for a plan (in milliseconds). */
	protected double maxElapsedTime = Double.POSITIVE_INFINITY;

	/**  Maximum number of iterations the explorer could search for a plan. */
	protected double maxRounds = Double.POSITIVE_INFINITY;

	/**  Event bus shared across explorer elements. */
	protected final EventBus eventBus;

	/**  Event bus shared across explorer elements. */
	protected final StatisticsCollector stats;

	/**
	 * Constructor for Explorer.
	 * @param eventBus EventBus
	 * @param collectStats boolean
	 */
	public Explorer(EventBus eventBus, boolean collectStats) {
		Preconditions.checkArgument(eventBus != null);
		this.eventBus = eventBus;
		this.stats = new StatisticsCollector(collectStats, eventBus);
	}

	/**
	 * Explores the search space until termination:
	 * 		 -the maximum elapsed time/the maximum number of iterations has reached or
	 * 		 -the best plan is found.
	 *
	 * @throws CardinalityException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	public void explore() throws CardinalityException, LimitReachedException {
		this.tick = System.nanoTime();
		this.post();
		while (!this.terminates() && !this.forcedTermination) {
			if (this.checkLimitReached()) {
				this.forcedTermination = true;
				break;
			}
			this._explore();
			this.rounds++;
			this.post();
		}
		this.checkLimitReached();
		this.post();
	}

	/**
	 * Update clock.
	 */
	public void updateClock() {
		long tack = System.nanoTime();
		this.elapsedTime += tack - this.tick;
		this.tick = tack;
	}

	/**
	 * Check limit reached.
	 *
	 * @return true if the time or iteration limit is reached.
	 * @throws LimitReachedException the limit reached exception
	 */
	protected boolean checkLimitReached() throws LimitReachedException {
		this.updateClock();
		boolean hasTimedOut = (this.elapsedTime / 1e6) > this.maxElapsedTime;
		if (hasTimedOut && this.exceptionOnLimit) {
			throw new LimitReachedException("Planning timeout reached: " + (this.elapsedTime / 1e6) + ">" + this.maxElapsedTime, Reasons.TIMEOUT);
		}
		boolean hasReachMaxRounds = this.rounds > this.maxRounds;
		if (hasReachMaxRounds && this.exceptionOnLimit) {
			throw new LimitReachedException("Planning max number of iterations reached: " + this.rounds + ">" + this.maxRounds, Reasons.MAX_ITERATION);
		}
		return hasTimedOut || hasReachMaxRounds;
	}

	/**
	 * Post.
	 */
	protected void post() {
		this.updateClock();
		this.eventBus.post(this);
		P plan = this.getBestPlan();
		if (plan != null) {
			this.eventBus.post(plan);
		}
	}

	/**
	 * Terminates.
	 *
	 * @return true if termination is reached. For more details look at specific implementations
	 */
	protected abstract boolean terminates();

	/**
	 * Does the actual exploration. For more details look at specific implementations
	 *
	 * @throws CardinalityException the planner exception
	 * @throws LimitReachedException the limit reached exception
	 */
	protected abstract void _explore() throws CardinalityException, LimitReachedException ;

	/**
	 * Gets the best plan.
	 *
	 * @return P
	 */
	public P getBestPlan() {
		return this.bestPlan;
	}

	/**
	 * Sets the max elapsed time.
	 *
	 * @param maxElapsedTime double
	 */
	public void setMaxElapsedTime(double maxElapsedTime) {
		this.maxElapsedTime = maxElapsedTime;
	}

	/**
	 * Sets the max rounds.
	 *
	 * @param maxRounds Double
	 */
	public void setMaxRounds(Double maxRounds) {
		this.maxRounds = maxRounds;
	}

	/**
	 * Gets the rounds.
	 *
	 * @return int
	 */
	public int getRounds() {
		return this.rounds;
	}

	/**
	 * Sets the exception on limit.
	 *
	 * @param exceptionOnLimit Boolean
	 */
	public void setExceptionOnLimit(Boolean exceptionOnLimit) {
		this.exceptionOnLimit = exceptionOnLimit;
	}

	/**
	 * Gets the exception on limit.
	 *
	 * @return boolean
	 */
	public boolean getExceptionOnLimit() {
		return this.exceptionOnLimit;
	}

	/**
	 * Gets the elapsed time.
	 *
	 * @return double
	 */
	public double getElapsedTime() {
		return this.elapsedTime;
	}
}
