package uk.ac.ox.cs.pdq.test.runtime;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.datasources.utility.Result;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.ExecutorTypes;
import uk.ac.ox.cs.pdq.runtime.exec.Middleware;
import uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor;
import uk.ac.ox.cs.pdq.runtime.exec.PlanExecutor.ExecutionModes;
import uk.ac.ox.cs.pdq.test.planner.PlannerTestUtilities;
import uk.ac.ox.cs.pdq.util.Utility;


// TODO: Auto-generated Javadoc
/**
 * Tests the correctness of pipelined executor.
 *
 * @author Efthymia Tsamoura
 */
public class PipelinedPlanExecutorTest {

	/** The path. */
	private static String PATH = "test/runtime/caching/";
	
	/** The event bus. */
	EventBus eventBus = new EventBus();

	/** The tuples. */
	Integer[] tuples = {1363,325,6962,4606,4290,7425,87};
	
	/** The schemata. */
	String schemata[] = {
			"schema.xml",
			"schema.xml",
			"schema.xml",
			"schema.xml",
			"schema.xml",
			"schema.xml",
			"schema.xml",

	};
	
	/** The queries. */
	String queries[] = {
			"query0.xml",
			"query1.xml",
			"query5.xml",
			"query6.xml",
			"query7.xml",
			"query8.xml",
			"query9.xml",

	};
	
	/** The plans. */
	String plans[] = {
			"plan0.xml",
			"plan1.xml",
			"plan5.xml",
			"plan6.xml",
			"plan7.xml",
			"plan8.xml",
			"plan9.xml",
	};


	/**
	 * Test caching.
	 */
	@Test
	public void testCaching() {

		for(int i = 0; i < schemata.length; ++i) {
			String s = schemata[i];
			String q = queries[i];
			String p = plans[i];

			System.out.println("RUNNING CASE: " + i/(double)schemata.length);
			try(FileInputStream sis = new FileInputStream(PATH + s);
					FileInputStream qis = new FileInputStream(PATH + q)) {

				Schema schema = new SchemaReader().read(sis);
				ConjunctiveQuery query = new QueryReader(schema).read(qis);

				if (schema == null || query == null) {
					throw new IllegalStateException("Schema and query must be provided.");
				}

				schema.addConstants(Utility.getTypedConstants(query));

				Entry<RelationalTerm, Cost> plan = PlannerTestUtilities.obtainPlan(PATH + p, schema); //readPlan(PATH + p, schema, query);

				RuntimeParameters runtimeParams = new RuntimeParameters();
				runtimeParams.setExecutorType(ExecutorTypes.PIPELINED);

				Result caching = this.evaluatePlan(runtimeParams, plan.getKey(), query, ExecutionModes.DEFAULT);
				assert(caching.size()==this.tuples[i]);

			} catch (FileNotFoundException e) {
				System.out.println("Cannot find input files");
			} catch (Exception e) {
				System.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
				e.printStackTrace();
			} catch (Error e) {
				System.out.println("ERROR: " + e.getClass().getSimpleName() + " " + e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}

		}


	}

//	/**
//	 * Read plan.
//	 *
//	 * @param plan the plan
//	 * @param schema Schema
//	 * @param query Query
//	 * @return Plan
//	 */
//	private Entry<RelationalTerm,Cost> readPlan(String plan, Schema schema, ConjunctiveQuery query) {
//		try(FileInputStream pis = new FileInputStream(plan);
//				BufferedInputStream bis = new BufferedInputStream(pis)) {
//			return new DAGPlanReader(schema).read(bis); 
//		} catch (IOException e) {
//			return null;
//		}
//	}

	/**
	 * Evaluates the given plan and returns its result. 
	 *
	 * @param runtimeParams the runtime params
	 * @param p Plan
	 * @param query Query
	 * @param mode ExecutionModes
	 * @return the result of the plan evaluation.
	 * @throws EvaluationException the evaluation exception
	 */
	private Result evaluatePlan(RuntimeParameters runtimeParams, RelationalTerm p, ConjunctiveQuery query, ExecutionModes mode)
			throws EvaluationException {
		//this.eventBus.register(new TuplePrinterTest(System.out));
		PlanExecutor executor = Middleware.newExecutor(runtimeParams, p, query);
		executor.setEventBus(this.eventBus);
		executor.setTuplesLimit(runtimeParams.getTuplesLimit());
		executor.setCache(runtimeParams.getDoCache());
		Result result = executor.execute(mode);
		return result;
	}

}