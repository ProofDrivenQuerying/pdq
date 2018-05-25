package uk.ac.ox.cs.pdq.regression.planner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.regression.RegressionTest;
import uk.ac.ox.cs.pdq.regression.RegressionTestException;
import uk.ac.ox.cs.pdq.regression.Bootstrap.Command;

/**
 * Runs the planner with simple and black box cost estimators and compares the resulting plans.
 * An exception is thrown when the planner finds a plan in one case but not the other. 
 * 
 * @author Julien Leblay
 */
public class PlannerCostFunctionTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(PlannerCostFunctionTest.class);

	/**  File name where planning related parameters must be stored in a test case directory. */
	private static final String PLAN_PARAMETERS_FILE = "case.properties";

	/**  File name where the schema must be stored in a test case directory. */
	private static final String SCHEMA_FILE = "schema.xml";

	/**  File name where the query must be stored in a test case directory. */
	private static final String QUERY_FILE = "query.xml";


	/**
	 * The Class CostCommand.
	 */
	public static class CostCommand extends Command {
		
		/**
		 * Instantiates a new cost command.
		 */
		public CostCommand() {
			super("cost");
		}

		/* (non-Javadoc)
		 * @see uk.ac.ox.cs.pdq.test.Bootstrap.Command#execute()
		 */
		@Override
		public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
			new PlannerCostFunctionTest().recursiveRun(new File(getInput()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.test.regression.RegressionTest#run(java.io.File)
	 */
	@Override
	protected boolean run(File directory) throws RegressionTestException,
			IOException, ReflectiveOperationException {
		return this.compare(directory);
	}

	/**
	 * Runs all the test case in the given directory.
	 *
	 * @param directory the directory
	 * @return boolean
	 */
	protected boolean compare(File directory) {
		boolean result = true;
		try {
			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");

			// Loading schema
			Schema schema = IOManager.importSchema(new File(directory.getAbsolutePath() + '/' + SCHEMA_FILE));
			PlannerParameters planParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			DatabaseParameters dbParams = new DatabaseParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			
			// Loading query
			ConjunctiveQuery query = IOManager.importQuery(new File(directory.getAbsolutePath() + '/' + QUERY_FILE));
			if (schema == null || query == null) {
				this.out.println("\tSKIP: Could not read schema/query in " + directory.getAbsolutePath());
				return true;
			}

			Entry<RelationalTerm, Cost> plan1;
			Entry<RelationalTerm, Cost> plan2;
			try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				costParams.setCostType(CostTypes.FIXED_COST_PER_ACCESS);
				ExplorationSetUp planner1 = new ExplorationSetUp(planParams, costParams, reasoningParams,dbParams, schema);
				planner1.registerEventHandler(new IntervalEventDrivenLogger(pLog, planParams.getLogIntervals(), planParams.getShortLogIntervals()));
				plan1 = planner1.search(query);
			}
			try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				costParams.setCostType(CostTypes.TEXTBOOK);
				ExplorationSetUp planner2 = new ExplorationSetUp(planParams, costParams, reasoningParams,dbParams, schema);
				planner2.registerEventHandler(new IntervalEventDrivenLogger(pLog, planParams.getLogIntervals(), planParams.getShortLogIntervals()));
				plan2 = planner2.search(query);
			}

			if (plan1 == null && plan2 == null) {
				this.out.println("PASS: " + directory.getAbsolutePath());
			} else if ((plan1 != null && plan2 == null) || (plan1 == null && plan2 != null)) {
				this.out.println("FAIL: " + directory.getAbsolutePath());
				this.out.println("\tPlan returned by simple cost: " + plan1.getKey());
				this.out.println("\tPlan returned by black box: " + plan2.getKey());
			}

			else {
				this.out.println("PASS: " + directory.getAbsolutePath());
			}
			return true;

		} catch (FileNotFoundException e) {
			log.debug(e);
			this.out.println("Skipping '" + directory.getAbsolutePath() + "' (not a case directory)");
		} catch (Exception e) {
			this.out.println("\tFAIL: " + directory.getAbsolutePath());
			this.out.println("\texception thrown: " + e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace(this.out);
			return false;
		}
		return result;
	}
}
