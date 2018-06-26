package uk.ac.ox.cs.pdq.regression.planner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Strings;

import uk.ac.ox.cs.pdq.InconsistentParametersException;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.logging.ProgressLogger;
import uk.ac.ox.cs.pdq.logging.SimpleProgressLogger;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.DominanceTypes;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.SuccessDominanceTypes;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.regression.RegressionTest;
import uk.ac.ox.cs.pdq.regression.RegressionTestException;
import uk.ac.ox.cs.pdq.regression.Bootstrap.Command;
import uk.ac.ox.cs.pdq.regression.acceptance.AcceptanceCriterion;
import uk.ac.ox.cs.pdq.regression.acceptance.ApproximateCostAcceptanceCheck;
import uk.ac.ox.cs.pdq.regression.acceptance.SameCostAcceptanceCheck;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;
import uk.ac.ox.cs.pdq.util.LimitReachedException;
import uk.ac.ox.cs.pdq.util.LimitReachedException.Reasons;

/**
 * Runs regression tests regarding the planner.
 * 
 * @author Julien Leblay
 */
public class PlannerTest extends RegressionTest {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(PlannerTest.class);

	/**
	 * The Enum Modes.
	 */
	private static enum Modes {/** The compare. */
		COMPARE, /** The prepare. */
		PREPARE, /** The validate. */
		VALIDATE};

		/**  File name where planning related parameters must be stored in a test case directory. */
		private static final String PLAN_PARAMETERS_FILE = "case.properties";

		/**  File name where the schema must be stored in a test case directory. */
		private static final String SCHEMA_FILE = "schema.xml";

		/**  File name where the query must be stored in a test case directory. */
		private static final String QUERY_FILE = "query.xml";

		/**  File name where the expected plan must be stored in a test case directory. */
		private static final String PLAN_FILE = "expected-plan.xml";

		/** The mode. */
		private final Modes mode;

		/** The param overrides. */
		private Map<String, String> paramOverrides = new HashMap<>();
		
		/**
		 * The Class PlannerTestCommand.
		 */
		@Parameters(separators = ",", commandDescription = "Runs regression tests on the planner libraries.")
		public static class PlannerTestCommand extends Command {

			/** The prepare. */
			@Parameter(names = { "-p", "--prepare" }, required = false, 
					description = "Prepare test case (outputs a plan.xml file in the input directory), without actually checking anything.")
			private boolean prepare = false;

			/** The validate. */
			@Parameter(names = { "-v", "--validate" }, required = false, 
					description = "Does not actually runs test, but merely checks whether the settings are correct.")
			private boolean validate = false;

			/**
			 * Instantiates a new planner test command.
			 */
			public PlannerTestCommand() {
				super("planner");
			}

			/* (non-Javadoc)
			 * @see uk.ac.ox.cs.pdq.test.Bootstrap.Command#execute()
			 */
			@Override
			public void execute() throws RegressionTestException, IOException, ReflectiveOperationException {
				new PlannerTest(
						this.prepare ? Modes.PREPARE : (this.validate ? Modes.VALIDATE : Modes.COMPARE),
								getParameterOverrides()).recursiveRun(new File(getInput()));
			}
		}

		/**
		 * Sets up a regression test for the given test case directory.
		 *
		 * @param mode the mode
		 * @param paramOverrides the param overrides
		 * @throws ReflectiveOperationException the reflective operation exception
		 * @throws IOException Signals that an I/O exception has occurred.
		 * @throws RegressionTestException the regression test exception
		 */
		public PlannerTest(Modes mode, Map<String, String> paramOverrides) throws ReflectiveOperationException, IOException, RegressionTestException {
			super();
			this.mode = mode;
			this.paramOverrides = paramOverrides;
		}

		/*
		 * (non-Javadoc)
		 * @see uk.ac.ox.cs.pdq.test.regression.RegressionTest#run(java.io.File)
		 */
		@Override
		protected boolean run(File directory) throws RegressionTestException, IOException, ReflectiveOperationException {
			switch(this.mode) {
			case PREPARE:
				return this.prepare(directory);
			case VALIDATE:
				return this.validate(directory);
			default:
				return this.compare(directory);
			}
		}
		private static FileWriter summary = null;
		/**
		 * Runs a single test case base on the .
		 *
		 * @param directory the directory
		 * @return boolean
		 * @throws ReflectiveOperationException the reflective operation exception
		 */
		private boolean compare(File directory) throws ReflectiveOperationException {       
			if (summary == null) {
				try {
					summary = new FileWriter(new File("summary.txt"));
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			try {
		        GlobalCounterProvider.resetCounters();
		        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		        uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
				this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");
				PlannerParameters plannerParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
				CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
				override(plannerParams, paramOverrides);
				override(costParams, paramOverrides);
				ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
				DatabaseParameters dbParams = new DatabaseParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
				Schema schema = DbIOManager.importSchema(new File(directory.getAbsolutePath() + '/' + SCHEMA_FILE));
				ConjunctiveQuery query = IOManager.importQuery(new File(directory.getAbsolutePath() + '/' + QUERY_FILE));
				query = IOManager.convertQueryConstants(query, schema);
				DbIOManager.exportSchemaToXml(schema, new File(directory.getAbsolutePath() + '/' + SCHEMA_FILE));
				Entry<RelationalTerm, Cost> expectedPlan = PlannerTestUtilities.obtainPlan(directory.getAbsolutePath() + '/' + PLAN_FILE, schema);
				if (schema == null || query == null) {
					throw new RegressionTestException(
							"Schema and query must be provided for each regression test. "
									+ "(schema:" + schema + ", query: " + query + ", plan: " + expectedPlan + ")");
				}
				if (costParams.getCatalog() == null) {
					File catalog = new File(directory, "catalog.properties");
					if (catalog.exists())
						costParams.setCatalog(catalog.getAbsolutePath());
				}
				long start = System.currentTimeMillis();
				schema = addAccessibleToSchema(schema);
				Entry<RelationalTerm, Cost> observedPlan = null;
				try(ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
					ExplorationSetUp planner = new ExplorationSetUp(plannerParams, costParams, reasoningParams, dbParams, schema);
					observedPlan = planner.search(query);
				} catch (LimitReachedException lre) {
					log.warn(lre);
				}
				double duration = (System.currentTimeMillis() - start) / 1000.0;
				DecimalFormat myFormatter = new DecimalFormat("####.##");
				String duration_s = " Duration: " + myFormatter.format(duration) + "sec.";				
				
				if (observedPlan!=null)
					summary.write(directory.getAbsolutePath() + " Cost = "+observedPlan.getValue()+" plan: "+observedPlan.getKey()+ duration_s + " \n");
				else 
					summary.write(directory.getAbsolutePath() + " No Plan "+duration_s + "\n");
				summary.flush();
				AcceptanceCriterion<Entry<RelationalTerm, Cost>, Entry<RelationalTerm, Cost>> acceptance = acceptance(plannerParams, costParams);
				this.out.print("Using " + acceptance.getClass().getSimpleName() + ": ");
				acceptance.check(expectedPlan, observedPlan).report(this.out);
				
				if (observedPlan != null
						&& (expectedPlan == null || expectedPlan.getValue().greaterThan(observedPlan.getValue())) ) {
					this.out.print("\twriting plan: " + observedPlan + " " + observedPlan.getValue());
					CostIOManager.writeRelationalTermAndCost(new File(directory.getAbsolutePath() + '/' + PLAN_FILE),  observedPlan.getKey(), observedPlan.getValue());
					
				}
				this.out.println("\n " + duration_s);
			} catch (Throwable e) {
				try {
					e.printStackTrace();
					summary.write(directory.getAbsolutePath() + " Crashed : "+e.getMessage()+" \n");
					summary.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return handleException(e, directory);
			}
			return true;
		}

		private Schema addAccessibleToSchema(Schema schema) {
			List<Dependency> dep = new ArrayList<>();
			dep.addAll(Arrays.asList(schema.getAllDependencies()));
			dep.addAll(Arrays.asList(schema.getKeyDependencies()));
			List<Relation> rel = new ArrayList<>(); 
			rel.addAll(Arrays.asList(schema.getRelations()));
			rel.add(AccessibleSchema.accessibleRelation);
			return new Schema(rel.toArray(new Relation[rel.size()]),dep.toArray(new Dependency[dep.size()]));
		}

		/**
		 * Handle exception.
		 *
		 * @param e the e
		 * @param directory the directory
		 * @return true, if successful
		 */
		private boolean handleException(Throwable e, File directory) {
			if (e instanceof InconsistentParametersException) {
				log.debug(e);
				this.out.println("SKIP: Inconsistent parameters " + e.getMessage());
				return true;
			}
			if (e instanceof FileNotFoundException) {
				log.debug(e);
				this.out.println("SKIP: ('" + directory + "' not a valid case directory)");
				return true;
			}
			if (e.getCause() != null && e.getCause() instanceof UnsupportedOperationException && e.getCause().getMessage()!=null && e.getCause().getMessage().contains("BLACKBOX_DB cost estimator is not currently supported.")) {
				this.out.println("Error: BLACKBOX_DB cost estimator is not currently supported.");
				return false;
			} else
			if (e instanceof PlannerException) {
				e.printStackTrace();
				log.warn(e);
				Throwable cause = e.getCause();
				if (cause instanceof LimitReachedException) {
					Reasons reason = ((LimitReachedException) cause).getReason();
					this.out.println("LIMIT: " 
							+ (reason != null ? " " + reason : " ") 
							+ Strings.nullToEmpty(cause.getMessage()));
				} else if (cause != null) {
					this.out.println("FAIL: " + cause.getClass().getSimpleName() + " " + cause.getMessage());
					cause.printStackTrace(this.out);
				}
				return false;
			}
			if (e instanceof Exception) {
				log.warn(e);
				e.printStackTrace(this.out);
				this.out.println("FAIL: " + e.getClass().getSimpleName() + " " + e.getMessage());
				return false;
			}
			log.error(e);
			this.out.println("ERROR: " + e.getClass().getSimpleName() + " " + e.getMessage());
			System.exit(-1);
			return false;
		}

		/**
		 * Acceptance.
		 *
		 * @param params planning parameters
		 * @param cost the cost
		 * @return a acceptance matching the given parameters
		 */
		private static AcceptanceCriterion<Entry<RelationalTerm, Cost>, Entry<RelationalTerm, Cost>> acceptance(
				PlannerParameters params, CostParameters cost) {
			switch (params.getPlannerType()) {
			case DAG_CHASEFRIENDLYDP:
			case DAG_GENERIC:
			case DAG_SIMPLEDP:
			case DAG_OPTIMIZED:
				if (params.getSuccessDominanceType() == SuccessDominanceTypes.OPEN
				|| params.getDominanceType() == DominanceTypes.OPEN) {
					return new ApproximateCostAcceptanceCheck();
				}
				switch (params.getValidatorType()) {
				case DEPTH_VALIDATOR:
				case APPLYRULE_DEPTH_VALIDATOR:
				case RIGHT_DEPTH_VALIDATOR:
					return new ApproximateCostAcceptanceCheck();
				default:
					break;
				}
				if (params.getFilterType() != null) {
					return new ApproximateCostAcceptanceCheck();
				}
				break;
			default:
				return new SameCostAcceptanceCheck();
			}
			return new SameCostAcceptanceCheck();
		}

		/**
		 * Checks if is prepare.
		 *
		 * @return the value of the prepare argument
		 */
		public boolean isPrepare() {
			return this.mode == Modes.PREPARE;
		}

		/**
		 * Checks if is validate.
		 *
		 * @return the value of the valide argument
		 */
		public boolean isValidate() {
			return this.mode == Modes.VALIDATE;
		}

		/**
		 * Runs a single test case base on the .
		 *
		 * @param directory the directory
		 * @return boolean
		 * @throws ReflectiveOperationException the reflective operation exception
		 */
		private boolean validate(File directory) throws ReflectiveOperationException {
			try {
				this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");
				Schema schema = DbIOManager.importSchema(new File(directory.getAbsolutePath() + '/' + SCHEMA_FILE));
				ConjunctiveQuery query = IOManager.importQuery(new File(directory.getAbsolutePath() + '/' + QUERY_FILE));
				if (schema == null || query == null) {
					throw new RegressionTestException("Schema and query must be provided for each regression test. (schema:" + schema + ", query: " + query + ")");
				}
			} catch (IllegalStateException e) {
				log.warn(e);
				this.out.println("WARN: " + e.getMessage());
			} catch (FileNotFoundException e) {
				log.debug(e);
				this.out.println("SKIP: (not a valid case directory)");
			} catch (Exception e) {
				this.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
				e.printStackTrace(this.out);
				return false;
			}
			return true;
		}

		/**
		 * Runs a single test case base on the .
		 *
		 * @param directory the directory
		 * @return boolean
		 * @throws ReflectiveOperationException the reflective operation exception
		 */
		private boolean prepare(File directory) throws ReflectiveOperationException {
			try {
				this.out.println("\nStarting case '" + directory.getAbsolutePath() + "'");
				PlannerParameters plannerParams = new PlannerParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
				CostParameters costParams = new CostParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
				ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
				DatabaseParameters dbParams = new DatabaseParameters(new File(directory.getAbsolutePath() + '/' + PLAN_PARAMETERS_FILE));
				
				Schema schema = IOManager.importSchema(new File(directory.getAbsolutePath() + '/' + SCHEMA_FILE));
				ConjunctiveQuery query = IOManager.importQuery(new File(directory.getAbsolutePath() + '/' + QUERY_FILE));
				if (schema == null || query == null) {
					throw new RegressionTestException("Schema and query must be provided for each regression test. (schema:" + schema + ", query: " + query + ")");
				}

				Entry<RelationalTerm, Cost> plan = null;
				try(ProgressLogger pLog = new SimpleProgressLogger(this.out)) {
					ExplorationSetUp planner = new ExplorationSetUp(plannerParams, costParams, reasoningParams, dbParams, schema);
					plan = planner.search(query);
				} catch (LimitReachedException lre) {
					log.warn(lre);
				}
				if (plan != null) {
					this.out.print("\twriting plan: " + plan + " " + plan.getValue());
					CostIOManager.writeRelationalTermAndCost(new File(directory.getAbsolutePath() + '/' + PLAN_FILE),  plan.getKey(), plan.getValue());
				} else {
					this.out.print("\tno plan found.");
				}
			} catch (FileNotFoundException e) {
				log.debug(e);
				this.out.println("SKIP: (not a valid case directory)");
			} catch (Exception e) {
				this.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
				e.printStackTrace(this.out);
				return false;
			}
			return true;
		}
}
