package uk.ac.ox.cs.pdq.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.runtime.EvaluationException;
import uk.ac.ox.cs.pdq.runtime.Runtime;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters.ExecutorTypes;
import uk.ac.ox.cs.pdq.runtime.TimeoutException;
import uk.ac.ox.cs.pdq.runtime.exec.AccessException;
import uk.ac.ox.cs.pdq.util.Result;

import com.beust.jcommander.Parameter;

/**
 * Reads settings from the command line and parameter file(s) and run the
 * program.
 * 
 * @author Julien LEBLAY
 */
public class RuntimeBenchmark extends Runner {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(RuntimeBenchmark.class);

	/** File name for lock file. */
	protected static final String RUNTIME_LOCK = "runtime.lock";

	/** Default file name for a case output file. */
	protected static final String RUNTIME_OUTPUT_FILENAME = "runtime-{datetime}.log";

	@Parameter(names = { "-n", "--nb-runs" }, required = false, description = "The number of runs performed for each plan. By default n=1, when n>1 the runtimes over a given plans are averaged.")
	protected int nbRuns;

	@Parameter(names = { "-rto", "--run-timeout" }, required = false, description = "Max time allocated to each single runs. None by default.")
	protected long runTimeout;

	/**
	 * Sets up an experiment sample using external parameters (file and
	 * command-line arguments).
	 * 
	 * @param args
	 *            the command line parameters as given by the main method.
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 * @throws EvaluationException 
	 * @throws AccessException 
	 */
	public RuntimeBenchmark(String... args) {
		super(args);
		// Quick fix: overring jcommander default value mechanism.
		if (this.nbRuns == 0) {
			this.nbRuns = 1;
		}
		if (this.runTimeout == 0) {
			this.runTimeout = Long.MAX_VALUE;
		}
	}

	@Override
	public void run() {
		try {
			this.recursiveRun(new File(this.getInputFile()));
		} catch (BenchmarkException | ReflectiveOperationException | IOException e) {
			log.error(e);
		}
	}

	@Override
	protected String getLockFilename() {
		return RUNTIME_LOCK;
	}

	@Override
	protected String getOutputFilename() {
		return RUNTIME_OUTPUT_FILENAME.replace("{datetime}", new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new Date(System.currentTimeMillis())));
	}
	
	@Override
	public void run(File directory) throws BenchmarkException, IOException {
		RuntimeParameters runtimeParams = null;
		String threadName = Thread.currentThread().getName();
		
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
			runtimeParams = new RuntimeParameters(new File(f.getAbsolutePath()));
		} else {
			runtimeParams = new RuntimeParameters();
		}

		// Override with dynamic parameters (specified in command line)
		for (String k : this.dynamicParams.keySet()) {
			runtimeParams.set(k, this.dynamicParams.get(k));
		}
	
		if (runtimeParams != null) {
			// Set the output stream
			PrintStream out = System.out;
			if (this.getOutputFile() != null) {
				File f = new File(this.getOutputFile());
				if (!f.createNewFile()) {
					log.warn(this.getOutputFile() + " exists. Logs will be appended.");
					out = new PrintStream(f);
				}
			} else if (this.getNbThreads() > 1) {
				String outputFile = directory.getAbsolutePath() + '/' + this.getOutputFilename();
				File f = new File(outputFile);
				if (f.createNewFile()) {
					out = new PrintStream(f);
				}
			}

			log.info("Running runtime on '" + directory.getAbsolutePath() + "'");
			Pair<Schema, ConjunctiveQuery> sq = null;
			// Setup schema and query
			if (this.getInputFile() != null) {
				File sf = new File(directory.getAbsolutePath() + '/' + SCHEMA_FILE);
				File qf = new File(directory.getAbsolutePath() + '/' + QUERY_FILE);
				if (sf.exists()) {
					if (qf.exists()) {
						sq = this.makeSchemaQuery(runtimeParams, sf.getAbsolutePath(), qf.getAbsolutePath());
					} else {
						sq = this.makeSchemaQuery(runtimeParams, sf.getAbsolutePath());
					}
				}
			}
			
			Plan plan = obtainPlan(directory, sq.getLeft(), sq.getRight());
			if (0 < this.getOverallTimeout() && this.getOverallTimeout() < Long.MAX_VALUE) {
				Executors.newSingleThreadExecutor().execute(
						new ShowStopper(this.getOverallTimeout()));
			}
			
			// Run the actual benchmark case
			try {
			    this.run(directory, runtimeParams, sq.getKey(), sq.getValue(), plan, out);
			} catch (TimeoutException e) {
			    log.warn("Timeout detected on " + directory.getAbsolutePath());
			} catch (EvaluationException e) {
			    throw new BenchmarkException(e.getMessage(), e);
			} catch (RuntimeException e) {
			    e.printStackTrace();
			    log.error(e);
			    //throw new BenchmarkException(e.getMessage(), e);
			}
		}
	}

	
	/**
	 * Run an experiments and returns as a result a pair containing the result
	 * of the evaluation of the best plan found, and the result of the original
	 * query evaluation, respectively.
	 * 
	 * @param searchOnly
	 * @return If searchOnly is true, returns null, otherwise returns a pair,
	 *         whose first element is the result of the evaluation of the best
	 *         plan found by the algorithm, and the result of the evaluation of
	 *         the original query if it could be run. null otherwise.
	 * @throws IOException
	 * @throws SQLException
	 * @throws ReflectiveOperationException
	 * @throws AccessException
	 * @throws EvaluationException
	 * @throws ProofException 
	 */
    public void run(File directory, RuntimeParameters params, Schema schema, ConjunctiveQuery query, Plan plan, PrintStream out) throws IOException, EvaluationException {
		// Print experiments environment
		if (this.isVerbose()) {
		    printParameters(out, params);
		    printCase(out, schema, query);
		}

	    printSystemSettings(out);
	    printHeader(out, this.nbRuns);
	    out.print(directory.getAbsolutePath());

		long overallStart = System.currentTimeMillis();
		if (plan != null && !plan.isEmpty()) {
		    params.setExecutorType(ExecutorTypes.PIPELINED);
		    params.setTimeout(this.runTimeout);

		    long totalEval = 0;
		    long results = -1;
		    long min = Long.MAX_VALUE;
		    long max = Long.MIN_VALUE;
			long minResponse = Long.MAX_VALUE;
			long maxResponse = Long.MIN_VALUE;
		    try {
			    for (int i = 0; i < this.nbRuns; i++) {
				    Runtime runtime = new Runtime(params, schema);

				    Result result = null;
				    long start = -1;

				    //runtime.registerEventHandler(new TuplePrinterTest(System.out));
				    start = System.currentTimeMillis();
				    result = runtime.evaluatePlan(plan, query);
				    long planEval = System.currentTimeMillis() - start;
				    if (planEval < min) {
				    	min = planEval;
				    }
				    if (planEval > max) {
				    	max = planEval;
				    }
				    totalEval += planEval;
				    
				    // Print plan evaluation stats
				    out.print("\t" + planEval);
				    if (results >= 0 && result.size() != results) {
				    	log.error("Cardinality mismatch in subsequent runs." + result.size() + "!=" + results);
				    }
				    results = result.size();
				    if ((System.currentTimeMillis() - overallStart) > this.overallTimeout) {
					    printLine(out, "T/O", this.nbRuns - i);
					    return;
				    }
				    
					minResponse = Math.min(planEval, minResponse);
					maxResponse = Math.max(planEval, maxResponse);
			    }
			    out.print("\t" + min);
			    out.print("\t" + max);
			    out.print("\t" + (totalEval / this.nbRuns));
			    out.print("\t" + results);
				out.print("\t" + minResponse);
				out.print("\t" + maxResponse);
		    } catch (TimeoutException e) {
			    printLine(out, "T/O", this.nbRuns);
		    } catch (OutOfMemoryError e) {
		    	e.printStackTrace();
			    printLine(out, "O/M", this.nbRuns);
			    log.error(e.getMessage());
			    throw e;
		    } catch (Exception e) {
		    	String msg = e.getMessage();
		    	if (msg == null) {
		    		msg = "";
		    	}
		    	if (msg.length() >= 1024) {
			    	msg = msg.substring(0, 1021) + "...";
		    	}
			printLine(out, e.getClass().getSimpleName(), this.nbRuns);
			log.error(msg, e);
		    }
		} else {
		    printLine(out, "N/A", this.nbRuns);
		}

		out.println();
		log.info("Complete.");
	}
    
    private static void printLine(PrintStream out, String message, int n) {
    	for (int i = 0, l=n + 4; i<l; i++) {
		    out.print("\t" + message);
    	}
	    out.println();
    }
   
    protected static void printHeader(PrintStream out, int n) {
		Runner.printHeader(out);
    	out.print("# case");
    	for (int i = 0, l=n; i<l; i++) {
		    out.print("\trun_" + (i + 1));
    	}
	    out.print("\tmin");
	    out.print("\tmax");
	    out.print("\tavg");
	    out.print("\tcard");
		out.print("\tminrtt");
		out.println("\tmaxrtt");
    }
    
    public int getNbRuns() {
    	return this.nbRuns;
    }
	
	/**
	 * 
	 * @return true, if all the relation in the schema have free access.
	 */
	private boolean isSchemaAllFreeAccess(Schema schema) {
		for (Relation r: schema.getRelations()) {
			boolean hasFree = false;
			for (AccessMethod b: r.getAccessMethods()) {
				if (b.getType() == Types.FREE) {
					hasFree = true;
					break;
				}
			}
			if (!hasFree) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Instantiates an experiment and runs it.
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		try {
			new RuntimeBenchmark(args);
		} catch (Exception  e) {
			e.printStackTrace();
			System.exit(ERROR_CODE);
		}
		System.exit(0);
	}
}
