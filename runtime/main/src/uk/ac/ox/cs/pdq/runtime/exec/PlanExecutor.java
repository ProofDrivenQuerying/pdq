package uk.ac.ox.cs.pdq.runtime.exec;

import uk.ac.ox.cs.pdq.datasources.Result;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;

import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Top class for all plan executors.
 * 
 * @author Julien LEBLAY
 */
public interface PlanExecutor {
	
	/**
	 * The Enum ExecutionModes.
	 */
	public static enum ExecutionModes {
/** The default. */
DEFAULT, 
 /** The profile. */
 PROFILE}; 
	
	/**
	 * Execute a plan executor and returns a result.
	 *
	 * @return the result of a plan execution
	 * @throws EvaluationException the evaluation exception
	 */
	Result execute() throws EvaluationException;
	
	/**
	 * Execute a plan executor and returns a result with a given mode.
	 *
	 * @param mode the mode
	 * @return Result
	 * @throws EvaluationException the evaluation exception
	 */
	Result execute(ExecutionModes mode) throws EvaluationException;
	
	/**
	 * Sets an event bus for the executor.
	 *
	 * @param eb the new event bus
	 */
	void setEventBus(EventBus eb);
	
	/**
	 * Sets the tuples limit.
	 *
	 * @param tuples the new tuples limit
	 */
	void setTuplesLimit(int tuples);
	
	/**
	 * Sets the cache.
	 *
	 * @param doCache the new cache
	 */
	public void setCache(boolean doCache);
}
