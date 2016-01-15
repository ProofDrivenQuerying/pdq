package uk.ac.ox.cs.pdq.test.runtime;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters;
import uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor.ExecutionModes;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Julien Leblay
 */
@Ignore
public class RuntimeTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	@Test public void initRuntime(RuntimeParameters params, Schema schema, List<Predicate> facts) {
	}
	
	@Test public void initRuntime(RuntimeParameters params, Schema schema) {
	}
	
	@Test public void registerEventHandler(EventHandler handler) {
	}
	
	@Test public void unregisterEventHandler(EventHandler handler) {
	}

	@Test public void evaluatePlan(Plan p, Query<?> query) throws EvaluationException {
	}

	@Test public void evaluatePlanWithMode(Plan p, Query<?> query, ExecutionModes mode) throws EvaluationException {
	}

	@Test public void evaluateQuery(Query<?> query) {
	}
}
