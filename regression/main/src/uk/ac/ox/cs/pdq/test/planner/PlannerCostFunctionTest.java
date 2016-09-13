package uk.ac.ox.cs.pdq.test.planner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;
import uk.ac.ox.cs.pdq.db.ReasoningParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.test.RegressionTest;
import uk.ac.ox.cs.pdq.test.RegressionTestException;
import uk.ac.ox.cs.pdq.test.Bootstrap.Command;

// TODO: Auto-generated Javadoc
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
		try(FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
				FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE)) {

			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");

			// Loading schema
			Schema schema = new SchemaReader().read(sis);
			PlannerParameters planParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			
			// Loading query
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			if (schema == null || query == null) {
				this.out.println("\tSKIP: Could not read schema/query in " + directory.getAbsolutePath());
				return true;
			}

			Plan plan1, plan2;
			try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				costParams.setCostType(CostTypes.SIMPLE_CONSTANT);
				ExplorationSetUp planner1 = new ExplorationSetUp(planParams, costParams, reasoningParams, schema);
				planner1.registerEventHandler(new IntervalEventDrivenLogger(pLog, planParams.getLogIntervals(), planParams.getShortLogIntervals()));
				plan1 = planner1.search(query);
			}
			try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				costParams.setCostType(CostTypes.BLACKBOX);
				ExplorationSetUp planner2 = new ExplorationSetUp(planParams, costParams, reasoningParams, schema);
				planner2.registerEventHandler(new IntervalEventDrivenLogger(pLog, planParams.getLogIntervals(), planParams.getShortLogIntervals()));
				plan2 = planner2.search(query);
			}

			if (plan1 == null && plan2 == null) {
				this.out.println("PASS: " + directory.getAbsolutePath());
			} else if ((plan1 != null && plan2 == null)
					|| (plan1 == null && plan2 != null)) {
				this.out.println("FAIL: " + directory.getAbsolutePath());
				this.out.println("\tPlan returned by simple cost: " + plan1);
				this.out.println("\tPlan returned by black box: " + plan2);
			}

			else {
				this.out.println("PASS: " + directory.getAbsolutePath());
			}
			return true;

		} catch (FileNotFoundException e) {
			log.debug(e);
			this.out.println("Skipping '" + directory.getAbsolutePath()
					+ "' (not a case directory)");
		} catch (Exception e) {
			this.out.println("\tFAIL: " + directory.getAbsolutePath());
			this.out.println("\texception thrown: "
					+ e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace(this.out);
			return false;
		}
		return result;
	}
}
