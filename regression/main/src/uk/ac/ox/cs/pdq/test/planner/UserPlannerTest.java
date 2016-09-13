package uk.ac.ox.cs.pdq.test.planner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.LimitReachedException;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.ReasoningParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.xml.DAGPlanReader;
import uk.ac.ox.cs.pdq.io.xml.LeftDeepPlanReader;
import uk.ac.ox.cs.pdq.io.xml.PlanWriter;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.DominanceTypes;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.SuccessDominanceTypes;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.test.RegressionTest;
import uk.ac.ox.cs.pdq.test.RegressionTestException;
import uk.ac.ox.cs.pdq.test.Bootstrap.Command;
import uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion;
import uk.ac.ox.cs.pdq.test.acceptance.ApproximateCostAcceptanceCheck;
import uk.ac.ox.cs.pdq.test.acceptance.SameCostAcceptanceCheck;

import com.beust.jcommander.Parameter;

// TODO: Auto-generated Javadoc
/**
 * Runs regression tests.
 * 
 * @author Efthymia Tsamoura
 */
public class UserPlannerTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(UserPlannerTest.class);

	/**  File name where planning related parameters must be stored in a test case directory. */
	private static final String PLAN_PARAMETERS_FILE = "case.properties";
	
	/** The Constant ACCESS_PLAN_FILE. */
	private static final String ACCESS_PLAN_FILE = "expected-plan.txt";

	/**  File name where the schema must be stored in a test case directory. */
	private static final String SCHEMA_FILE = "schema.xml";

	/**  File name where the query must be stored in a test case directory. */
	private static final String QUERY_FILE = "query.xml";

	/**  File name where the expected plan must be stored in a test case directory. */
	private static final String PLAN_FILE = "expected-plan.xml";
	
	/**
	 * The Class UserPlannerTestCommand.
	 */
	public static class UserPlannerTestCommand extends Command {

		/**
		 * Instantiates a new user planner test command.
		 */
		public UserPlannerTestCommand() {
			super("user_driven");
		}

		/* (non-Javadoc)
		 * @see uk.ac.ox.cs.pdq.test.Bootstrap.Command#execute()
		 */
		@Override
		public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
			new UserPlannerTest().recursiveRun(new File(getInput()));
		}
	}

	/**
	 * Sets up a regression test for the given test case directory.
	 *
	 * @throws ReflectiveOperationException the reflective operation exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws RegressionTestException the regression test exception
	 */
	public UserPlannerTest() throws ReflectiveOperationException, IOException, RegressionTestException {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.regression.RegressionTest#run(java.io.File)
	 */
	@Override
	protected boolean run(File directory) throws RegressionTestException, IOException, ReflectiveOperationException {
		return this.compare(directory);
	}

	/**
	 * Runs a single test case base on the .
	 *
	 * @param directory the directory
	 * @return boolean
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	private boolean compare(File directory) throws ReflectiveOperationException {
		try(FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
			FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE)) {

			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");
			PlannerParameters plannerParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			plannerParams.setAccessFile(directory.getAbsolutePath() + '/' + ACCESS_PLAN_FILE);
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			
			Schema schema = new SchemaReader().read(sis);
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			Plan expectedPlan = obtainPlan(directory, schema, query);
			if (schema == null || query == null) {
				throw new RegressionTestException("Schema and query must be provided for each regression test. (schema:" + schema + ", query: " + query + ", plan: " + expectedPlan + ")");
			}
			
			Plan observedPlan = null;
			try(ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				ExplorationSetUp planner = new ExplorationSetUp(plannerParams, costParams, reasoningParams, schema);
				planner.registerEventHandler(
						new IntervalEventDrivenLogger(
								pLog, plannerParams.getLogIntervals(),
								plannerParams.getShortLogIntervals()));
				observedPlan = planner.search(query);
			} catch (LimitReachedException lre) {
				log.warn(lre);
			}
			AcceptanceCriterion<Plan, Plan> acceptance = acceptance(plannerParams);
			this.out.print("Using " + acceptance.getClass().getSimpleName() + ": ");
			acceptance.check(expectedPlan, observedPlan).report(this.out);
			
			if (observedPlan != null
				&& (expectedPlan == null || expectedPlan.getCost().greaterThan(observedPlan.getCost())) ) {
				this.out.print("\twriting plan: " + observedPlan + " " + observedPlan.getCost());
				try (PrintStream o = new PrintStream(directory.getAbsolutePath() + '/' + PLAN_FILE)) {
					PlanWriter.to(o).write(observedPlan);
				}
			}
		} catch (FileNotFoundException e) {
			log.debug(e);
			this.out.println("SKIP: ('" + directory + "' not a valid case directory)");
		} catch (Exception e) {
			log.warn(e);
			this.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace(this.out);
			return false;
		} catch (Error e) {
			e.printStackTrace();
			log.error(e);
			this.out.println("ERROR: " + e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace(this.out);
			System.exit(-1);
		}
		return true;
	}

	/**
	 * Obtain plan.
	 *
	 * @param directory File
	 * @param schema Schema
	 * @param query Query
	 * @return Plan
	 */
	private Plan obtainPlan(File directory, Schema schema, Query<?> query) {
		try(FileInputStream pis = new FileInputStream(directory.getAbsolutePath() + '/' + PLAN_FILE);
			BufferedInputStream bis = new BufferedInputStream(pis)) {
			try {
				bis.mark(1024);
				return new LeftDeepPlanReader(schema).read(bis); 
			} catch (Exception re) {
				bis.reset();
			}
		} catch (IOException e) {
		}
		try(FileInputStream pis = new FileInputStream(directory.getAbsolutePath() + '/' + PLAN_FILE);
			BufferedInputStream bis = new BufferedInputStream(pis)) {
			try {
				bis.mark(1024);
				return new DAGPlanReader(schema).read(bis); 
			} catch (Exception re) {
				bis.reset();
			}
		} catch (IOException e) {
			log.warn(e);
		}
		return null;
	}
	
	/**
	 * Acceptance.
	 *
	 * @param params planning parameters
	 * @return a acceptance matching the given parameters
	 */
	private static AcceptanceCriterion<Plan, Plan> acceptance(PlannerParameters params) {
		switch (params.getPlannerType()) {
		case DAG_CHASEFRIENDLYDP:
		case DAG_GENERIC:
		case DAG_SIMPLEDP:
		case DAG_OPTIMIZED:
			if (params.getSuccessDominanceType() == SuccessDominanceTypes.OPEN
				|| params.getDominanceType() == DominanceTypes.OPEN) {
				return new ApproximateCostAcceptanceCheck();
			}
			break;
		default:
			return new SameCostAcceptanceCheck();
		}
		return new SameCostAcceptanceCheck();
	}
}
