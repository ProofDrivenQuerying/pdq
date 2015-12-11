package uk.ac.ox.cs.pdq.test.regression;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.CostParameters.CostTypes;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.xml.DAGPlanReader;
import uk.ac.ox.cs.pdq.io.xml.LinearPlanReader;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.explorer.CostEstimatorFactory;
import uk.ac.ox.cs.pdq.planner.io.xml.ProofReader;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.test.regression.Bootstrap.Command;
import uk.ac.ox.cs.pdq.test.util.ProofEventHandler;

import com.beust.jcommander.Parameter;

/**
 * Runs regression tests for the proof system.
 * 
 * @author Julien Leblay
 */
public class PlanGenerationTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(PlanGenerationTest.class);

	/** File name where planning related parameters must be stored in a test case directory */
	private static final String PLAN_PARAMETERS_FILE = "case.properties";

	/** File name where the schema must be stored in a test case directory */
	private static final String SCHEMA_FILE = "schema.xml";

	/** File name where the query must be stored in a test case directory */
	private static final String QUERY_FILE = "query.xml";

	/** File name where the proof must be stored in a test case directory */
	private static final String PROOF_FILE = "proof.xml";

	/** File name where the plan must be stored in a test case directory */
	private static final String PLAN_FILE = "plan.xml";

	private final boolean prepare;

	public static class PlanGenerationCommand extends Command {
		@Parameter(names = { "-p", "--prepare" }, required = false, description = "Prepare test case (outputs a plan.xml file in the input directory), without actually checking anything.")
		private boolean prepare = false;

		public PlanGenerationCommand() {
			super("plangen");
		}

		@Override
		public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
			new PlanGenerationTest(this.prepare).recursiveRun(new File(getInput()));
		}
	}

	/**
	 * Sets up a regression test for the given test case directory.
	 * 
	 * @param args the command line parameters as given by the main method.
	 * @throws ReflectiveOperationException
	 * @throws IOException
	 * @throws RegressionTestException
	 */
	public PlanGenerationTest(boolean prepare)
			throws ReflectiveOperationException, IOException,
			RegressionTestException {
		this.prepare = prepare;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ox.cs.pdq.test.regression.RegressionTest#run(java.io.File)
	 */
	@Override
	protected boolean run(File directory) throws RegressionTestException,
			IOException, ReflectiveOperationException {
		if (this.prepare) {
			return this.prepare(directory);
		}
		return this.compare(directory);
	}

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
				FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE);
				FileInputStream pis = new FileInputStream(directory.getAbsolutePath() + '/' + PROOF_FILE)) {

			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");

			// Loading schema
			Schema schema = new SchemaReader().read(sis);
			PlannerParameters planParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));

			// Loading query
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			if (schema == null || query == null) {
				this.out.println("\tSKIP: Could not read schema/query in " + directory.getAbsolutePath());
				return true;
			}

			// Loading proof
			Proof proof = new ProofReader(schema).read(pis);
			if (proof == null) {
				this.out.println("\tSKIP: Could not read schema/proof in " + directory.getAbsolutePath());
				return true;
			}

			// Loading expected plan
			Plan expectedPlan = obtainPlan(directory, schema, query);
			if (expectedPlan == null || expectedPlan.isEmpty()) {
				this.out.println("\tSKIP: Plan is empty"
						+ directory.getAbsolutePath());
				return true;
			}

			// Generate plan from proof and compare with expected plan
			AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
			CostEstimator<Plan> ce = CostEstimatorFactory.getEstimator(planParams, costParams, accessibleSchema);
			Plan plan = this.createPlanForProof(schema, accessibleSchema, proof, query, costParams.getCostType());
			ce.cost(plan);
			switch (plan.howDifferent(expectedPlan)) {
			case IDENTICAL:
				this.out.println("PASS: " + directory.getAbsolutePath());
				break;
			case EQUIVALENT:
				this.out.println("PASS: Results differ, but are equivalent - "
						+ directory.getAbsolutePath());
				this.out.println("\tdiff: " + expectedPlan.diff(plan));
				break;
			default:
				this.out.println("FAIL: " + directory.getAbsolutePath());
				this.out.println("\texpected: " + expectedPlan);
				this.out.println("\tobserved: " + plan);
				return false;
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

	/**
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
				return new LinearPlanReader(schema).read(bis); 
			} catch (Exception re) {
				bis.reset();
			}
			return new DAGPlanReader(schema).read(bis); 
		} catch (IOException e) {
			log.warn(e);
			return null;
		}
	}

	/**
	 * Prepares the case of the given directory, i.e. looks for a proof and
	 * writes it to a file.
	 * 
	 * @param directory
	 * @return boolean
	 */
	protected boolean prepare(File directory) {
		try (	
				FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
				FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE);) {

			this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");
			PlannerParameters planParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			
			Schema schema = new SchemaReader().read(sis);
			Query<?> query = new QueryReader(schema).read(qis);
			if (schema == null || query == null) {
				throw new RegressionTestException(
						"Schema and query must be provided for each regression test. "
						+ "(schema:" + schema + ", query: " + query + ")");
			}

			try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				Planner planner = new Planner(planParams, costParams, reasoningParams, schema);
				planner.registerEventHandler(new IntervalEventDrivenLogger(pLog, planParams.getLogIntervals(), planParams.getShortLogIntervals()));
				planner.registerEventHandler(new ProofEventHandler(directory.getAbsolutePath() + '/' + PROOF_FILE));
				planner.search(query);
			}
		} catch (FileNotFoundException e) {
			log.debug(e);
			this.out.println("SKIP: '" + directory.getAbsolutePath()
					+ "' (not a case directory)");
		} catch (Exception e) {
			this.out.println("FAIL: " + directory.getAbsolutePath());
			this.out.println("\texception thrown: "
					+ e.getClass().getSimpleName() + " " + e.getMessage());
			e.printStackTrace(this.out);
			return false;
		}
		return true;
	}

	/**
	 * Creates a plan from a proof
	 * @param proof
	 * @param query
	 * @param costType
	 * @return the plan created from the given proof, query and costType
	 */
	private Plan createPlanForProof(Schema schema, AccessibleSchema accessibleSchema, Proof proof, Query<?> query, CostTypes costType) {
		throw new java.lang.UnsupportedOperationException();
	}
}
