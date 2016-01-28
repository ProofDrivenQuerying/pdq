package uk.ac.ox.cs.pdq.test.planner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.ReasoningTypes;
import uk.ac.ox.cs.pdq.test.RegressionTest;
import uk.ac.ox.cs.pdq.test.RegressionTestException;
import uk.ac.ox.cs.pdq.test.Bootstrap.Command;

/**
 * Runs regression tests for the optimized explorer. 
 * First, it calls the planner using the input chase algorithm. 
 * Then, it calls the planner using the KTermination chaser. 
 * Finally, it compares the resulting plans.
 * An exception is thrown when the plans are different.
 * 
 * @author Efthymia Tsamoura
 */
public class KStepBlockingTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(KStepBlockingTest.class);

	/** File name where planning related parameters must be stored in a test case directory */
	private static final String PLAN_PARAMETERS_FILE = "case.properties";

	/** File name where the schema must be stored in a test case directory */
	private static final String SCHEMA_FILE = "schema.xml";

	/** File name where the query must be stored in a test case directory */
	private static final String QUERY_FILE = "query.xml";

	public static class KStepBlockingCommand extends Command {
		public KStepBlockingCommand() {
			super("kstep");
		}

		@Override
		public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
			new KStepBlockingTest().recursiveRun(new File(getInput()));
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
		return this.compare(directory);	}

	/**
	 * Runs all the test case in the given directory
	 * 
	 * @param directory
	 * @return boolean
	 */
	protected boolean compare(File directory) {
		boolean result = true;
		try (
				FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
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
			
			if (!schema.isCyclic() || schema.containsViews()) {
				this.out.println("\tSKIP: Non blocking chaser would be applied");
				return true;
			}
			
			if(reasoningParams.getReasoningType() != null && reasoningParams.getReasoningType().equals(ReasoningTypes.KTERMINATION_CHASE)) {
				throw new java.lang.IllegalArgumentException();
			}
			
			Plan plan1, plan2;
			try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				Planner planner1 = new Planner(planParams, costParams, reasoningParams, schema);
				planner1.registerEventHandler(new IntervalEventDrivenLogger(pLog, planParams.getLogIntervals(), planParams.getShortLogIntervals()));
				plan1 = planner1.search(query);
			}
			try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				reasoningParams.setReasoningType(ReasoningTypes.KTERMINATION_CHASE);			
				Planner planner2 = new Planner(planParams, costParams, reasoningParams, schema);
				planner2.registerEventHandler(new IntervalEventDrivenLogger(pLog, planParams.getLogIntervals(), planParams.getShortLogIntervals()));
				plan2 = planner2.search(query);
			}

			if (plan1 != null) {
				switch (plan1.howDifferent(plan2)) {
				case IDENTICAL:
					this.out.println("PASS: " + directory.getAbsolutePath());
					break;
				case EQUIVALENT:
					this.out.println("PASS: Results differ, but are equivalent - "
							+ directory.getAbsolutePath());
					this.out.println("\tdiff: " + plan1.diff(plan2));
					break;
				default:
					this.out.println("FAIL: " + directory.getAbsolutePath());
					this.out.println("\tPlan returned when blocking at every step: " + plan1);
					this.out.println("\tPlan returned when blocking at every " + reasoningParams.getBlockingInterval() + " steps: " + plan2);
					return false;
				}
				return true;
			}
			return plan2 == null;

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
