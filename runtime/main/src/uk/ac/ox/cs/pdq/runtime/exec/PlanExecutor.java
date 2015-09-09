package uk.ac.ox.cs.pdq.runtime.exec;

import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.util.Result;

import com.google.common.eventbus.EventBus;

/**
 * Top class for all plan executors.
 * 
 * @author Julien LEBLAY
 */
public interface PlanExecutor {
	
	/**
	 */
	public static enum ExecutionModes {DEFAULT, PROFILE}; 
	
	/**
	 * Execute a plan executor and returns a result.
	
	 * @return the result of a plan execution
	 * @throws EvaluationException
	 */
	Result execute() throws EvaluationException;
	
	/**
	 * Execute a plan executor and returns a result with a given mode.
	 * @param mode
	 * @return Result
	 * @throws EvaluationException
	 */
	Result execute(ExecutionModes mode) throws EvaluationException;
	
	/**
	 * Sets an event bus for the executor
	 * @param eb
	 */
	void setEventBus(EventBus eb);
	
	void setTuplesLimit(int tuples);
	
	public void setCache(boolean doCache);
}
