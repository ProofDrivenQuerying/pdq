package uk.ac.ox.cs.pdq.runtime.query;

import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.util.Result;

import com.google.common.eventbus.EventBus;

/**
 * Interface for all query evaluators. Provides a mean to evaluate a query
 * and get its result.
 * 
 * @author Julien Leblay
 */
public interface QueryEvaluator {

	/**
	 * @return the result of the evaluation of q
	 * @throws EvaluationException
	 */
	Result evaluate() throws EvaluationException;
	
	/**
	 * Sets an event bus for the evaluator
	 * @param eb
	 */
	void setEventBus(EventBus eb);
}
