package uk.ac.ox.cs.pdq.test.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.pretty.DataReader;
import uk.ac.ox.cs.pdq.io.xml.DAGPlanReader;
import uk.ac.ox.cs.pdq.io.xml.LeftDeepPlanReader;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.Runtime;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.ExecutorTypes;
import uk.ac.ox.cs.pdq.runtime.exec.AccessException;
import uk.ac.ox.cs.pdq.runtime.exec.MiddlewareException;
import uk.ac.ox.cs.pdq.runtime.util.TuplePrinter;
import uk.ac.ox.cs.pdq.test.Bootstrap;
import uk.ac.ox.cs.pdq.test.RegressionParameters;
import uk.ac.ox.cs.pdq.test.RegressionTest;
import uk.ac.ox.cs.pdq.test.RegressionTestException;
import uk.ac.ox.cs.pdq.test.Bootstrap.Command;
import uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion;
import uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceLevels;
import uk.ac.ox.cs.pdq.test.acceptance.AcceptanceCriterion.AcceptanceResult;
import uk.ac.ox.cs.pdq.test.acceptance.ExpectedCardinalityAcceptanceCheck;
import uk.ac.ox.cs.pdq.test.acceptance.SetEquivalentResultSetsAcceptanceCheck;
import uk.ac.ox.cs.pdq.test.util.DataValidationImplementation;
import uk.ac.ox.cs.pdq.util.Result;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

// TODO: Auto-generated Javadoc
/**
 * Runs regression tests for the runtime, evaluated plans may come in
 * pre-serialized plan files or directly from the planner.
 * 
 * @author Julien Leblay
 */
public class RuntimeTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(RuntimeTest.class);

	/**  File name where planning related parameters must be stored in a test case directory. */
	private static final String PLAN_PARAMETERS_FILE = "case.properties";

	/**  File name where the schema must be stored in a test case directory. */
	private static final String SCHEMA_FILE = "schema.xml";

	/**  File name where the query must be stored in a test case directory. */
	private static final String QUERY_FILE = "query.xml";

	/**  File name where the expected plan must be stored in a test case directory. */
	private static final String PLAN_FILE = "expected-plan.xml";

	/**  File name where the input data must be stored in a test case directory. */
	private static final String DATA_FILE_EXTENSION = ".txt";

	/** The full. */
	private final boolean full;

	/** The validation. */
	private final boolean validation;

	/**
	 * The Class RuntimeTestCommand.
	 */
	@Parameters(separators = ",", commandDescription = "Runs regression tests on the runtime libraries.")
	public static class RuntimeTestCommand extends Command {

		/** The full. */
		@Parameter(names = { "-f", "--full-run" }, required = false,
				description = "If true, the planner is used to create a plan, otherwise the plan is read from file. Default is false")
		private boolean full = false;

		/** The validation. */
		@Parameter(names = { "-d", "--data-validation" },
				required = false, description = "If true, we check whether the data satisfy the schema dependencies or not. Default is false")
		private boolean validation = false;

		/**
		 * Instantiates a new runtime test command.
		 */
		public RuntimeTestCommand() {
			super("runtime");
		}

		/* (non-Javadoc)
		 * @see uk.ac.ox.cs.pdq.test.Bootstrap.Command#execute()
		 */
		@Override
		public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
			new RuntimeTest(this.full, this.validation).recursiveRun(new File(getInput()));
		}
	}

	/**
	 * Sets up a regression test for the given test case directory.
	 *
	 * @param fr the fr
	 * @param dv the dv
	 * @throws ReflectiveOperationException the reflective operation exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws RegressionTestException the regression test exception
	 */
	public RuntimeTest(boolean fr, boolean dv) throws ReflectiveOperationException, IOException, RegressionTestException {
		this.full = fr;
		this.validation = dv;
	}

	/**
	 * Runs all the test case in the given directory.
	 *
	 * @param directory the directory
	 * @return boolean
	 * @throws RegressionTestException the regression test exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ReflectiveOperationException the reflective operation exception
	 */
	@Override
	protected boolean run(File directory) throws RegressionTestException, IOException, ReflectiveOperationException {
		return this.loadCase(directory, this.full);
	}
	
	/**
	 * Validate data.
	 *
	 * @param directory File
	 * @param schema Schema
	 * @param query Query
	 * @throws EvaluationException the evaluation exception
	 */
	private static void validateData(File directory, Schema schema, Query<?> query) throws EvaluationException {
		PlannerParameters plParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
		ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		Query<?> accessibleQuery = accessibleSchema.accessible(query, query.getVariablesToCanonical());
		try (HomomorphismManager manager = new HomomorphismManagerFactory().getInstance(accessibleSchema, reasoningParams)) {
			manager.addQuery(accessibleQuery);
			DataValidationImplementation dataValidator = new DataValidationImplementation(schema, (DatabaseHomomorphismManager) manager);
			dataValidator.validate();
			manager.clearQuery();
		} catch (Exception e) {
			throw new EvaluationException(e.getMessage(), e);
		}
	}

	/**
	 * Obtain plan.
	 *
	 * @param directory File
	 * @param schema Schema
	 * @param query Query
	 * @param full boolean
	 * @return Plan
	 */
	private Plan obtainPlan(File directory, Schema schema, Query<?> query, boolean full) {
		if (full) {
			PlannerParameters plParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			
			try (ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
				ExplorationSetUp planner = new ExplorationSetUp(plParams, costParams, reasoningParams, schema);
				planner.registerEventHandler(new IntervalEventDrivenLogger(pLog, plParams.getLogIntervals(), plParams.getShortLogIntervals()));
				return planner.search(query);
			} catch (Exception e) {
				log.debug(e);
				return null;
			}
		}
		try (FileInputStream pis = new FileInputStream(directory.getAbsolutePath() + '/' + PLAN_FILE)) {
			try {
				return new LeftDeepPlanReader(schema).read(pis); 
			} catch (Exception re) {
				try (FileInputStream bis = new FileInputStream(directory.getAbsolutePath() + '/' + PLAN_FILE)) {
					return new DAGPlanReader(schema).read(bis); 
				}
			}
		} catch (IOException e) {
			log.debug(e);
			return null;
		}
	}
	
	/**
	 * Runs a single test case base on the.
	 *
	 * @param directory the directory
	 * @param full boolean
	 * @return boolean
	 * @throws ReflectiveOperationException the reflective operation exception
	 * @throws AccessException the access exception
	 */
	private boolean loadCase(File directory, boolean full) throws ReflectiveOperationException {
		boolean result = true;
		try(FileInputStream sis = new FileInputStream(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
			FileInputStream qis = new FileInputStream(directory.getAbsolutePath() + '/' + QUERY_FILE);) {

			this.out.println("Starting case '" + directory.getAbsolutePath() + "'");

			// Loading schema & query
			Schema schema = new SchemaReader().read(sis);
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			if (schema == null || query == null) {
				this.out.println("\tSKIP: Could not read schema/query in " + directory.getAbsolutePath());
				return true;
			}
			if (this.validation) {
				validateData(directory, schema, query);
			}

			Plan plan = this.obtainPlan(directory, schema, query, full);
			if (plan == null || plan.isEmpty()) {
				this.out.println("\tSKIP: Plan is empty in " + directory.getAbsolutePath());
				return true;
			}

			// Loading data
			List<Atom> facts = null;
			String[] dataFiles = directory.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(DATA_FILE_EXTENSION);
				}
			});
			if (dataFiles != null && dataFiles.length > 0) {
				for (String d : dataFiles) {
					File dataFile = new File(directory.getAbsolutePath() + '/' + d);
					if (dataFile.exists()) {
						try (FileInputStream dis = new FileInputStream(dataFile)) {
							facts = new DataReader(schema).read(dis);
						}
					}

					this.out.println("\tData file '" + dataFile.getAbsolutePath() + "'");
					result &= this.run(directory, schema, query, plan, facts);
				}
			} else {
				this.out.println("\tUsing underlying database ");
				result &= this.run(directory, schema, query, plan, facts);
			}

		} catch (FileNotFoundException e) {
			log.debug(e);
			this.out.println("SKIP: '" + directory.getAbsolutePath() + "' (not a case directory)");
		} catch (Exception e) {
			e.printStackTrace(this.out);
			this.out.println("\tEXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
			return false;
		}
		return result;
	}
	
	/**
	 * Compare result.
	 *
	 * @param params RuntimeParameters
	 * @param s Schema
	 * @param q Query
	 * @param p Plan
	 * @param f List<PredicateFormula>
	 * @param queryResult Result
	 * @return boolean
	 */
	private boolean compareResult(RuntimeParameters params, Schema s, Query<?> q, Plan p, List<Atom> f, Result queryResult) {
		boolean accepted = true;
		ExecutorTypes[] types = new ExecutorTypes[] {
				ExecutorTypes.PIPELINED, 
				ExecutorTypes.SQL_TREE, 
				ExecutorTypes.SQL_WITH
		};
		ExecutorTypes forcedType = params.getExecutorType(); 
		if (forcedType != null) {
			types = new ExecutorTypes[1];
			types[0] = forcedType;
		}
		
		AcceptanceCriterion<Result, Result> acceptance = new SetEquivalentResultSetsAcceptanceCheck();
		Runtime runtime = null;
		for (ExecutorTypes type: types) {
			params.setExecutorType(type);
			runtime = new Runtime(params, s, f);
			try {
				this.out.print("\tExecutor type " + type + " - ");
				AcceptanceResult result = acceptance.check(queryResult, runtime.evaluatePlan(p, q)); 
				accepted &= result.getLevel() == AcceptanceLevels.PASS;
				result.report(this.out);
			} catch (MiddlewareException e) {
				log.debug(e);
				this.out.println("\tSKIP: not applicable");
			} catch (EvaluationException e) {
				log.debug(e);
				this.out.println("\tSKIP: Evaluation exception. " + e.getMessage());
			} catch (Exception e) {
				this.out.println("\tFAIL: exception thrown " + e.getMessage());
				e.printStackTrace(this.out);
				accepted &= false;
			}
		}
		if (forcedType != null) {
			params.setExecutorType(forcedType);
		} else {
			params.unsetExecutorType();
		}
		return accepted;
	}

	/**
	 * Runs a single case.
	 *
	 * @param directory the directory
	 * @param s the s
	 * @param q the q
	 * @param p the p
	 * @param f the f
	 * @return boolean
	 * @throws EvaluationException the evaluation exception
	 * @throws AccessException the access exception
	 */
	private boolean run(File directory, Schema s, ConjunctiveQuery q, Plan p, List<Atom> f)
			throws EvaluationException, AccessException {
		RuntimeParameters params = new RuntimeParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
		Runtime runtime = new Runtime(params, s, f);
		
		try {
			Result queryResult = runtime.evaluateQuery(q);
			return this.compareResult(params, s, q, p, f, queryResult);
		} catch (EvaluationException e) {
			log.debug(e);
			RegressionParameters regParams = new RegressionParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
			if (!regParams.getSkipRuntime()) {
				AcceptanceResult result = new ExpectedCardinalityAcceptanceCheck()
					.check(regParams.getExpectedCardinality(), runtime.evaluatePlan(p, q));
				result.report(this.out);
			} else {
				this.out.println("\tSKIP: runtime test explicitly skipped.");
			}
			return true;
		}
	}

	/**
	 * Gets the full run.
	 *
	 * @return the value of the "full-run" argument
	 */
	public boolean getFullRun() {
		return this.full;
	}
}
