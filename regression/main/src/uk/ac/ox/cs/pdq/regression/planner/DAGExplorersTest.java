package uk.ac.ox.cs.pdq.regression.planner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.regression.RegressionTest;
import uk.ac.ox.cs.pdq.regression.RegressionTestException;
import uk.ac.ox.cs.pdq.regression.Bootstrap.Command;

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
		try {
			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");

			// Loading schema
			Schema schema = IOManager.importSchema(new File(directory.getAbsolutePath() + '/' + SCHEMA_FILE));
			PlannerParameters plannerParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			DatabaseParameters databaseParams = new DatabaseParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));

			// Loading query
			ConjunctiveQuery query = IOManager.importQuery(new File(directory.getAbsolutePath() + '/' + QUERY_FILE));
			if (schema == null || query == null) {
				this.out.println("\tSKIP: Could not read schema/query in " + directory.getAbsolutePath());
				return true;
			}

			RelationalTerm plan = null;
			Cost cost = null;
			PlannerTypes masterType = null;
			for (PlannerTypes type: PlannerTypes.values()) {
				if (type.toString().startsWith("DAG")) {
						plannerParams.setPlannerType(type);
						ExplorationSetUp planner1 = new ExplorationSetUp(plannerParams, costParams, reasoningParams, databaseParams, schema);
						Entry<RelationalTerm, Cost> entry = planner1.search(query);
						if (plan == null) {
							masterType = type;
							plan = entry.getKey();
							cost = entry.getValue();
						} else {
							this.out.println("\nComparing " + masterType + " with " + type);
							switch (PlannerTestUtilities.howDifferent(plan, cost, entry.getKey(), entry.getValue())) {
							case IDENTICAL:
								this.out.println("PASS: " + directory.getAbsolutePath());
								break;
							case EQUIVALENT:
								this.out.println("PASS: Results differ, but are equivalent - "
										+ directory.getAbsolutePath());
								this.out.println("\tdiff: " + PlannerTestUtilities.diff(plan, cost, entry.getKey(), entry.getValue()));
								break;
							default:
								this.out.println("FAIL: " + directory.getAbsolutePath());
								this.out.println("\tPlan returned by first explorer: " + plan);
								this.out.println("\tPlan returned by this explorer: " + entry.getKey());
								return false;
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
