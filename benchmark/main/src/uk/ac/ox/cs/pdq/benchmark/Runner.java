package uk.ac.ox.cs.pdq.benchmark;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.Parameters;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Query;
import uk.ac.ox.cs.pdq.io.pretty.PrettyDependencyWriter;
import uk.ac.ox.cs.pdq.io.pretty.PrettyQueryWriter;
import uk.ac.ox.cs.pdq.io.xml.DAGPlanReader;
import uk.ac.ox.cs.pdq.io.xml.LeftDeepPlanReader;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;

/**
 * Reads settings from the command line and parameter file(s) and run the
 * program.
 * 
 * @author Julien LEBLAY
 */
public abstract class Runner implements Runnable {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(Runner.class);

	/** Program execution command (to appear un usage message). */
	protected static final String PROGRAM_NAME = "java -jar pdq-benchmark.jar";

	/** Default file name for a case input properties. */
	protected static final String CASE_FILE = "case.properties";

	/** Default file name for input schemas. */
	protected static final String SCHEMA_FILE = "schema.xml";

	/** Default file name for input queries. */
	protected static final String QUERY_FILE = "query.xml";

	/** Default file name for input plan. */
	protected static final String PLAN_FILE = "plan.xml";

	/** Default file name for input configuration. */
	protected static final String CONFIGURATION_FILE = "configuration.txt";

	@Parameter(required = true, description = "The type of test requested among 'planner', 'runtime' or 'lindag'.")
	private List<String> types = Lists.newArrayList(Types.planner.toString());
	private enum Types {planner, runtime}//, lindag, dag

	/** Default error code. */
	protected static final int ERROR_CODE = -1;

	/** Period in ms to wait before terminating all threads. */
	public static final long GRACE_PERIOD = 30000;

	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	protected boolean help;

	@Parameter(names = { "-v", "--verbose" }, required = false, description = "If true, environment settings, the schema and query used will be printed to stdout before each case.")
	protected boolean verbose;

	@Parameter(names = { "-i", "--input" }, required = true, description = "Path to a case folder.")
	protected String input;

	@Parameter(names = { "-o", "--output" }, required = false, description = "Path to the output file. If omitted, STDOUT is used.")
	protected String output;

	@Parameter(names = { "-f", "--filter-by" }, required = false, description = "Comma-separated list of parameters to include on the log in order to filter them.")
	protected List<String> filters;

	@Parameter(names = { "-t", "--threads" }, required = false, description = "The numbder of search thread to execute in parallel.")
	protected int nbThreads = 1;

	@Parameter(names = { "-to", "--timeout" }, required = false, description = "The overall timeout for all threads to complete. Default is 24h")
	protected long overallTimeout = 24*3600000;

	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the initialConfig files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();

	/**
	 * Sets up an experiment sample using external parameters (file and
	 * command-line arguments).
	 * 
	 * @param args
	 *            the command line parameters as given by the main method.
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public Runner(String... args) {
		log.debug("Parsing command line parameters...");

		JCommander jc = new JCommander(this);
		jc.setProgramName(PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		if (this.isHelp()) {
			jc.usage();
			return;
		}
	}

	protected abstract String getLockFilename();
	protected abstract String getOutputFilename();
	public abstract void run(File directory) throws BenchmarkException, IOException;

	/**
	 * Runs all the test case in the given directory
	 * @param directory
	 */
	protected void recursiveRun(File directory) throws BenchmarkException, ReflectiveOperationException, IOException {
		boolean isLeaf = true;
		if (!directory.exists()) {
			log.warn("Directory '" + directory.getAbsoluteFile() + "' does not exists. Skipping.");
			return;
		}
		File[] files = directory.listFiles();
		Arrays.sort(files);
		for (File f: files) {
			if (!f.equals(directory) && f.isDirectory()) {
				this.recursiveRun(f);
				isLeaf = false;
			}
		}
		if (isLeaf) {
			this.run(directory);
		}
	}


	/**
	 * Create a schema as described in the input parameters.
	 * 
	 * @return a schema which complies to the input parameters.
	 * @throws IOException
	 */
	protected Pair<Schema, ConjunctiveQuery> makeSchemaQuery(Parameters params) throws IOException {
		return this.makeSchemaQuery(params, null, null);
	}

	/**
	 * Create a schema as described in the input parameters.
	 * 
	 * @return a schema which complies to the input parameters.
	 * @throws IOException
	 */
	protected Pair<Schema, ConjunctiveQuery>  makeSchemaQuery(Parameters params, String inputSchema) throws IOException {
		return this.makeSchemaQuery(params, inputSchema, null);
	}

	/**
	 * Create a schema as described in the input parameters.
	 * 
	 * @return a schema which complies to the input parameters.
	 * @throws IOException
	 */
	protected Pair<Schema, ConjunctiveQuery>  makeSchemaQuery(Parameters params, String inputSchema, String inputQuery) throws IOException {
		// Loading schema
		Schema schema = null;
		if (inputSchema != null) {
			try (FileInputStream fis = new FileInputStream(inputSchema)) {
				schema = Schema.builder(new SchemaReader().read(fis)).build();
			}
		} else {
			throw new java.lang.IllegalArgumentException();
		}

		ConjunctiveQuery query = null;
		// Creating query
		if (inputQuery != null) {
			try (FileInputStream fis = new FileInputStream(inputQuery)) {
				query = new QueryReader(schema).read(fis);
			}
		} else {
			throw new java.lang.IllegalArgumentException();
		}

		return Pair.of(schema, query);
	}

	/**
	 * Print some system environment information to the given output.
	 * 
	 * @param out
	 */
	public static void printSystemSettings(PrintStream out) {
		StringBuilder result = new StringBuilder();
		result.append("\n# StartTime: " + new Date(System.currentTimeMillis()));
		result.append("- OS: " + System.getProperty("os.name"));
		result.append("- OSArch: " + System.getProperty("os.arch"));
		result.append("- OSVersion: " + System.getProperty("os.version"));
		result.append("- StartUpMem: " + (Runtime.getRuntime().totalMemory() / 1000000) + "MB");
		result.append("- StartUpMaxMem: " + (Runtime.getRuntime().maxMemory() / 1000000) + "MB");
		result.append("- NumCPUs: " + Runtime.getRuntime().availableProcessors());
		out.println(result.toString());
	}

	/**
	 * Print the experiment's input parameters to the given output.
	 * 
	 * @param out
	 * @param params
	 */
	public static void printParameters(PrintStream out, Parameters params) {
		StringBuilder result = new StringBuilder();
		result.append("############## ").append("Input parameters");
		result.append("##############");
		result.append("\n# ").append(params.toString().replace("\n", "\n# "));
		result.append("\n# ");
		out.println(result.toString());
		// log.info(result.toString());
	}

	/**
	 * Prints the experiments case (schema and query) to the given output.
	 * 
	 * @param out
	 *            the output
	 * @param schema
	 * @param query
	 */
	public static void printCase(PrintStream out, Schema schema, ConjunctiveQuery query) {
		printCase(out, schema, null, query);
	}

	/**
	 * Prints the experiments case (schema, accessible schema (if not null) and
	 * query) to the given output.
	 * 
	 * @param out
	 * @param schema
	 * @param accSchema
	 * @param q
	 */
	public static void printCase(PrintStream out, Schema schema, Schema accSchema, ConjunctiveQuery q) {
		StringBuilder result = new StringBuilder();
		result.append("############## ").append("Schema & query");
		result.append("##############");
		result.append("\n# Schema: ");
		result.append("\n# {  ");
		for (Relation r : schema.getRelations()) {
			result.append("\n#\t").append(r);
		}
		result.append("\n# }");
		result.append("\n# { ");
		for (Constraint ic : schema.getDependencies()) {
			result.append("\n#\t").append(toShortString((TGD) ic));
		}
		result.append("\n# } ");
		if (accSchema != null) {
			result.append("\n# Accessible Schema: ");
			result.append("\n# {  ");
			for (Relation r : accSchema.getRelations()) {
				result.append("\n#\t").append(r);
			}
			result.append("\n# }");
			result.append("\n# { ");
			for (Constraint ic : accSchema.getDependencies()) {
				result.append("\n#\t").append(toShortString((TGD) ic));
			}
			result.append("\n# } ");
		}
		result.append("\n# Query: ");
		result.append(toShortString(q).replace("\n", "\n# "));
		result.append("\n# ");
		out.println(result.toString());
	}

	/**
	 * Returns a short String representation of the given query. This by-passes
	 * toString which is too verbose for non-debug purpose.
	 * 
	 * @param q
	 * @return a short String representation of the query.
	 */
	protected static String toShortString(ConjunctiveQuery q) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrettyQueryWriter.to(ps).write(q);
		return baos.toString();
	}

	/**
	 * Returns a short String representation of the given dependency. This
	 * by-passes toString which is too verbose for non-debug purpose.
	 * 
	 * @param query
	 * @return a short String representation of the dependency.
	 */
	private static String toShortString(TGD ic) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrettyDependencyWriter.to(ps).write(ic);
		return baos.toString();
	}

	/**
	 * Prints the given plan the output.
	 * 
	 * @param p
	 */
	protected void printPlan(Plan p, PrintStream out) {
		StringBuilder result = new StringBuilder();
		result.append("############## ").append("Best plan found ");
		result.append("##############");
		if (p != null) {
			result.append("\n# ").append(p).append(" Costs: ").append(p.getCost());
		} else {
			result.append("\n# No plan");
		}
		out.println(result.toString());
		// log.info(result.toString());
	}

	/**
	 * @param directory File
	 * @param schema Schema
	 * @param query Query
	 * @return Plan
	 */
	protected Plan obtainPlan(File directory, Schema schema, Query<?> query) {
		try(FileInputStream pis = new FileInputStream(directory.getAbsolutePath() + '/' + PLAN_FILE);
				BufferedInputStream bis = new BufferedInputStream(pis)) {
			try {
				return new LeftDeepPlanReader(schema).read(bis); 
			} catch (Exception re) {
			}
			return new DAGPlanReader(schema).read(bis); 
		} catch (IOException e) {
			log.warn("No such file " + directory.getAbsolutePath() + '/' + PLAN_FILE, e);
			return null;
		}
	}

	/**
	 * @return true if the line command asked for help.
	 */
	public boolean isHelp() {
		return this.help;
	}

	/**
	 * @param help
	 */
	public void setHelp(boolean help) {
		this.help = help;
	}

	/**
	 * @return the path to the output file, if any. If none, System.out is used
	 *         as the output.
	 */
	public String getOutputFile() {
		return this.output;
	}

	/**
	 * @param output
	 */
	public void setOutputFile(String outputFile) {
		this.output = outputFile;
	}

	/**
	 * @return the path to the input file, if any. If none, System.out is used
	 *         as the output.
	 */
	public String getInputFile() {
		return this.input;
	}

	/**
	 * @param input
	 */
	public void setInputFile(String inputFile) {
		this.input = inputFile;
	}

	/**
	 * @return true, if the runner runs in verbose mode
	 */
	public boolean isVerbose() {
		return this.verbose;
	}

	/**
	 * Set the runner's verbose mode.
	 * @param verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * @return the number of threads running in parallel
	 */
	public int getNbThreads() {
		return this.nbThreads;
	}

	/**
	 * Set the number of threads to run in parallel
	 * @param verbose
	 */
	public void setNbThreads(int nbThreads) {
		this.nbThreads = nbThreads;
	}

	/**
	 * @return the overall timeout for threads to complete
	 */
	public long getOverallTimeout() {
		return this.overallTimeout;
	}

	/**
	 * Sets the overall timeout for threads to complete
	 * @param verbose
	 */
	public void setOverallTimeout(long nbThreads) {
		this.overallTimeout = nbThreads;
	}

	/**
	 * Instantiates an experiment and runs it.
	 * @param args
	 */
	public static void main(String... args) {
		try {
			Runner runner = null;
			if (args.length > 0 && args[0] != null
					&& !args[0].trim().isEmpty()) {
				switch (args[0]) {
				case "runtime":
					runner = new RuntimeBenchmark(args);
					break;
				case "planner":
					runner = new PlannerBenchmark(args);
					break;
				default:
					System.err.println("First parameter should be one of 'planner', 'runtime'");
					System.exit(ERROR_CODE);
					break;
				}
			}
			ExecutorService execService = Executors.newFixedThreadPool(runner.getNbThreads());
			for (int i = 0; i < runner.getNbThreads(); i++) {
				execService.execute(runner);
			}
			execService.shutdown();
			try {
				execService.awaitTermination(runner.getOverallTimeout() + GRACE_PERIOD, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				log.error("Main thread interrupted. Shutdown down executions", e);
				execService.shutdownNow();
			}
		} catch (IOException
				| ReflectiveOperationException
				| BenchmarkException e) {
			log.error(e.getMessage(),e);
			System.exit(ERROR_CODE);
		}
		System.exit(0);
	}

	protected static void printHeader(PrintStream out) {
		out.print("# PDQ Common (version:" + Parameters.getVersion() + ") ");
		out.print("- PDQ Planner (version:" + PlannerParameters.getVersion() + ") ");
		out.print("- PDQ Runtime (version:" + RuntimeParameters.getVersion() + ") ");
		out.print("- PDQ Benchmark (version:" + BenchmarkParameters.getVersion() + ") ");
		out.println();
	}
}
