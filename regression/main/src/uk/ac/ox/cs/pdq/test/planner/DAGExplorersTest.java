package uk.ac.ox.cs.pdq.test.planner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.test.RegressionTest;
import uk.ac.ox.cs.pdq.test.RegressionTestException;
import uk.ac.ox.cs.pdq.test.Bootstrap.Command;

// TODO: Auto-generated Javadoc
/**
 * Runs regression tests for the optimised explorer. Runs a search with and
 * without optimisation (global equivalence, global dominance) and compares the
 * resulting plans. An exception is thrown when the plans are different.
 * 
 * @author Efthymia Tsamoura
 */
public class DAGExplorersTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(DAGExplorersTest.class);

	/**  File name where planning related parameters must be stored in a test case directory. */
	private static final String PLAN_PARAMETERS_FILE = "case.properties";

	/**  File name where the schema must be stored in a test case directory. */
	private static final String SCHEMA_FILE = "schema.xml";

	/**  File name where the query must be stored in a test case directory. */
	private static final String QUERY_FILE = "query.xml";


	/**
	 * The Class DAGExplorersCommand.
	 */
	public static class DAGExplorersCommand extends Command {
		
		/**
		 * Instantiates a new DAG explorers command.
		 */
		public DAGExplorersCommand() {
			super("dagexp");
		}

		/* (non-Javadoc)
		 * @see uk.ac.ox.cs.pdq.test.Bootstrap.Command#execute()
		 */
		@Override
		public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
			new DAGExplorersTest().recursiveRun(new File(getInput()));
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
		try (
				FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
				FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE)) {

			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");

			// Loading schema
			Schema schema = new SchemaReader().read(sis);
			PlannerParameters planParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			DatabaseParameters dbParams = new DatabaseParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));

			// Loading query
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			if (schema == null || query == null) {
				this.out.println("\tSKIP: Could not read schema/query in " + directory.getAbsolutePath());
				return true;
			}

			Plan plan = null;
			PlannerTypes masterType = null;
			for (PlannerTypes type: PlannerTypes.values()) {
				if (type.toString().startsWith("DAG")) {
					try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
						planParams.setPlannerType(type);
						ExplorationSetUp planner1 = new ExplorationSetUp(planParams, costParams, reasoningParams, dbParams, schema);
						planner1.registerEventHandler(new IntervalEventDrivenLogger(pLog, planParams.getLogIntervals(), planParams.getShortLogIntervals()));
						Plan p = planner1.search(query);
						if (plan == null) {
							masterType = type;
							plan = p;
						} else {
							this.out.println("\nComparing " + masterType + " with " + type);
							switch (plan.howDifferent(p)) {
							case IDENTICAL:
								this.out.println("PASS: " + directory.getAbsolutePath());
								break;
							case EQUIVALENT:
								this.out.println("PASS: Results differ, but are equivalent - "
										+ directory.getAbsolutePath());
								this.out.println("\tdiff: " + plan.diff(p));
								break;
							default:
								this.out.println("FAIL: " + directory.getAbsolutePath());
								this.out.println("\tPlan returned by first explorer: " + plan);
								this.out.println("\tPlan returned by this explorer: " + p);
								return false;
							}

						}
					}
				}
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
