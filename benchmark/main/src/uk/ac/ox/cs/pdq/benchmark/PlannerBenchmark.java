package uk.ac.ox.cs.pdq.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.algebra.CrossProduct;
import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.NaryOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.UnaryOperator;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.logging.performance.ChainedStatistics;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.Planner;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.events.BestPlanWriter;
import uk.ac.ox.cs.pdq.planner.events.MultiplePlanWriter;
import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;

import com.beust.jcommander.Parameter;

/**
 * Reads settings from the command line and parameter file(s) and run the
 * program.
 * 
 * @author Julien LEBLAY
 */
public class PlannerBenchmark extends Runner {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(PlannerBenchmark.class);

	@Parameter(names = { "-w", "--write-plan" }, required = false, description = "If true, write the resulting plan to a plan.xml file.")
	protected boolean writePlan;

	@Parameter(names = { "-W", "--write-multiple-plans" }, required = false, description = "If true, write the resulting plan to a plan.xml file.")
	protected boolean writePlanMultiple;

	@Parameter(names = { "-n", "--no-dependencies" }, required = false, description = "If true, the planner will be run without taking dependencies into account.")
	protected boolean noDep;

	/** Name of the lock files. */
	protected static final String PLANNER_LOCK = "planner.lock";

	/** Default file name for a case output file. */
	protected static final String PLANNER_OUTPUT_FILENAME = "planner-{datetime}.log";

	/**
	 * Sets up an experiment sample using external parameters (file and
	 * command-line arguments).
	 * 
	 * @param args
	 *            the command line parameters as given by the main method.
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 * @throws BenchmarkException 
	 * @throws ProofException 
	 */
	public PlannerBenchmark(String... args) throws BenchmarkException, 
			ReflectiveOperationException, IOException {
		super(args);
	}

	@Override
	public void run() {
		try {
			this.recursiveRun(new File(this.getInputFile()));
		} catch (RuntimeException e) {
			log.error(e.getMessage());
		} catch (BenchmarkException | ReflectiveOperationException | IOException e) {
			log.error(Thread.currentThread().getName() + " stopping due to " + e.getMessage());
		}
	}
	
	@Override
	protected String getLockFilename() {
		return PLANNER_LOCK;
	}

	@Override
	protected String getOutputFilename() {
		return PLANNER_OUTPUT_FILENAME.replace("{datetime}", new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date(System.currentTimeMillis())));
	}
	

	@Override
	public void run(File directory) throws BenchmarkException, IOException {
		String threadName = Thread.currentThread().getName();
		PlannerParameters plannerParams = null;
		CostParameters costParams = null;
		ReasoningParameters reasoningParams = null;
		
		if (this.getInputFile() != null) {
			synchronized (this) {
				File lock = new File(directory.getAbsolutePath() + '/' + this.getLockFilename());
				if (this.getNbThreads() > 1) {
					if (lock.exists()) {
						log.debug(threadName + ": '" + directory.getAbsolutePath() + "' locked");
						return;
					}
					log.debug(threadName + ": Locking '" + directory.getAbsolutePath() + "'");
					// Lock the directory
					lock.createNewFile();
				}
			}

			File f = new File(directory.getAbsolutePath() + '/' + CASE_FILE); 
			if (!f.exists()) {
				f = new File(directory.getAbsolutePath() + '/' + BenchmarkParameters.DEFAULT_CONFIG_FILE_NAME); 
				if (!f.exists()) {
					throw new BenchmarkException(this.getInputFile() + " does not contain any valid initialConfig.");
				} 
			}
			plannerParams = new PlannerParameters(new File(f.getAbsolutePath()));
			costParams = new CostParameters(new File(f.getAbsolutePath()));
			reasoningParams = new ReasoningParameters(new File(f.getAbsolutePath()));
		} else {
			plannerParams = new PlannerParameters();
			costParams = new CostParameters();
			reasoningParams = new ReasoningParameters();
		}

		// Override with dynamic parameters (specified in command line)
		for (String k : this.dynamicParams.keySet()) {
			plannerParams.set(k, this.dynamicParams.get(k));
			costParams.set(k, this.dynamicParams.get(k));
			reasoningParams.set(k, this.dynamicParams.get(k));
		}
		
		
		if (plannerParams != null) {
			// Set the output stream
			PrintStream out = System.out;
			if (this.getOutputFile() != null) {
				File f = new File(plannerParams.getOutputLogPath() + this.getOutputFile());
				if (!f.createNewFile()) {
					log.warn(threadName + ": " + this.getOutputFile() + " exists. Logs will be appended.");
					out = new PrintStream(f);
				}
			} else if (this.getNbThreads() > 1) {
				String outputFile = directory.getAbsolutePath() + '/' + this.getOutputFilename();
				File f = new File(outputFile);
				if (f.createNewFile()) {
					out = new PrintStream(f);
				}
			}
			
			
			log.info(threadName + ": Running planner on '" + directory.getAbsolutePath() + "'");
			Pair<Schema, ConjunctiveQuery> sq = null;
			// Load the statistic collector/logger
			// Run the actual benchmark case
			try (ChainedStatistics stats = new ChainedStatistics(out)) {
				stats.addFilter("case", directory.getAbsolutePath());
				if (this.filters != null) {
					for (String f : this.filters) {
						stats.addFilter(f, plannerParams.get(f));
					}
				}
				if (this.dynamicParams != null) {
					for (String f : this.dynamicParams.keySet()) {
						stats.addSuffix(f, this.dynamicParams.get(f));
					}
				}
				// Setup & run
				sq = this.makeSchemaQuery(plannerParams,
							directory.getAbsolutePath() + '/' + SCHEMA_FILE,
							directory.getAbsolutePath() + '/' + QUERY_FILE);
				this.run(directory, plannerParams, costParams, reasoningParams, sq.getKey(), sq.getValue(), stats, out);
			}
			log.info(threadName + ": " + "Complete.");
		} else {
			log.debug(threadName + ": Skipping " + directory.getAbsolutePath());
		}
	}

	/**
	 * Run an experiments and returns as a result a pair containing the result
	 * of the evaluation of the best plan found, and the result of the original
	 * query evaluation, respectively.
	 * @param directory
	 * @param plannerParams
	 * @param costParams
	 * @param schema
	 * @param query
	 * @param stats
	 * @param out
	 * @throws BenchmarkException
	 * @throws IOException
	 */
	public void run(File directory, PlannerParameters plannerParams, CostParameters costParams, 
			ReasoningParameters reasoningParams, Schema schema, ConjunctiveQuery query, ChainedStatistics stats, PrintStream out)
			throws BenchmarkException, IOException {
		// Print experiments environment
		if (this.isVerbose()) {
		    printParameters(out, plannerParams);
		    printCase(out, schema, query);
		}
	    printSystemSettings(out);
		printHeader(out);
		Planner planner = new Planner(plannerParams, costParams, reasoningParams, schema, query, stats);

		IntervalEventDrivenLogger logger = new IntervalEventDrivenLogger(stats, plannerParams.getLogIntervals(), plannerParams.getShortLogIntervals());
		planner.registerEventHandler(logger);
		if (0 < this.getOverallTimeout() && this.getOverallTimeout() < Long.MAX_VALUE) {
			Executors.newSingleThreadExecutor().execute(new ShowStopper(this.getOverallTimeout(), logger));
		}
		if (this.isWritePlan()) {
			planner.registerEventHandler(new BestPlanWriter(directory.getAbsolutePath() + '/' + PLAN_FILE));
		}
		if (this.isWritePlanMultiple()) {
			planner.registerEventHandler(new MultiplePlanWriter(directory.getAbsolutePath() + '/' + PLAN_FILE, schema));
//			planner.registerEventHandler(new MultipleConfigurationWriter<>(directory.getAbsolutePath() + '/' + CONFIGURATION_FILE, schema));
		}

		Plan bestPlan = null;
		try {
			bestPlan = planner.search(this.isNoDependencies());
			if (bestPlan == null) {
				log.info("No plan found.");
			}
		} catch (PlannerException e) {
			stats.addSuffix("reason", "exception");
			stats.addSuffix("detail", e.getClass().getSimpleName());
			log.error(Thread.currentThread().getName() + " stopping due to " + e.getMessage() +
					"\n" + directory.getAbsolutePath());
			//throw new BenchmarkException(e.getMessage(), e);
		} catch (Throwable e) {
			stats.addSuffix("reason", "error");
			stats.addSuffix("detail", e.getClass().getSimpleName());
			log.error(Thread.currentThread().getName() +
					" stopping due to " + e.getMessage() +
					"\n" + directory.getAbsolutePath());
			//throw e;
		}
		
		// Epilog
		if (bestPlan != null) {
			stats.addSuffix("CrossProducts", this.countCrossProducts((RelationalOperator) bestPlan.getOperator()));
			stats.addSuffix("Bushiness", this.bushiness((RelationalOperator) bestPlan.getOperator()));
		}
		stats.addSuffix("reason", "final");
		stats.addSuffix("input_query", toShortString(query));
		stats.addSuffix("best_plan", bestPlan);
	}

	/**
	 * 
	 */
	public int countCrossProducts(RelationalOperator op) {
		int result = 0;
		if (op instanceof CrossProduct
				|| (op instanceof Join && ((ConjunctivePredicate<?>) ((Join) op).getPredicate()).isEmpty())) {
			result++;
		}
		if (op instanceof UnaryOperator) {
			result += this.countCrossProducts(((UnaryOperator) op).getChild());
		}
		if (op instanceof NaryOperator) {
			for (RelationalOperator child: ((NaryOperator) op).getChildren()) {
				result += this.countCrossProducts(child);
			}
		}
		return result;
	}

	/**
	 * 
	 */
	public int bushiness(RelationalOperator op) {
		int result = 0;
		if (op instanceof NaryOperator) {
			int i = 0;
			for (RelationalOperator child : ((NaryOperator) op).getChildren()) {
				result += this.bushiness(child);
				if (i > 0 && !(child.isQuasiLeaf())) {
					result++;
				}
				i++;
			}
		}
		if (op instanceof UnaryOperator) {
			result = this.bushiness(((UnaryOperator) op).getChild());
		}
		return result;
	}

	/**
	 * @return true if the plan found is to be written to a file.
	 */
	public boolean isWritePlan() {
		return this.writePlan;
	}

	/**
	 * @return true if all the best plans found are to be written to separate files.
	 */
	public boolean isWritePlanMultiple() {
		return this.writePlanMultiple;
	}

	/**
	 * Set the plan write policy
	 * @param b
	 */
	public void setWritePlan(boolean b) {
		this.writePlan = b;
	}

	/**
	 * @return true if dependencies should be taken into account or not during planning.
	 */
	public boolean isNoDependencies() {
		return this.noDep;
	}

	/**
	 * Set whether dependencies should be taken into account or not during planning.
	 * @param b
	 */
	public void setNoDependencies(boolean b) {
		this.noDep = b;
	}
    
	/**
	 * Instantiates an experiment and runs it.
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		try {
			new PlannerBenchmark(args);
		} catch (IOException
				| ReflectiveOperationException
				| BenchmarkException e) {
			log.error(e.getMessage(),e);
			System.exit(ERROR_CODE);
		}
	}
}
