package uk.ac.ox.cs.pdq.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.io.pretty.DataReader;
import uk.ac.ox.cs.pdq.io.xml.DAGPlanReader;
import uk.ac.ox.cs.pdq.io.xml.LeftDeepPlanReader;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.plan.Plan;
import uk.ac.ox.cs.pdq.runtime.util.TupleCounter;
import uk.ac.ox.cs.pdq.runtime.util.TuplePrinter;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

// TODO: Auto-generated Javadoc
/**
 * Bootstrapping class for starting the runtime. 
 * 
 * @author Julien Leblay
 */
public class Bootstrap {

	/** Logger. */
	private static Logger log = Logger.getLogger(Bootstrap.class); 
	
	/** The program name. */
	private static final String PROGRAM_NAME = "pdq-runtime-<version>.jar";
	
	/** The help. */
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	
	/**
	 * Checks if is help.
	 *
	 * @return true, if is help
	 */
	public boolean isHelp() {
		return this.help;
	}
	
	/** The schema path. */
	@Parameter(names = { "-s", "--schema" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input schema definition file.")
	private String schemaPath;
	
	/**
	 * Gets the schema path.
	 *
	 * @return the schema path
	 */
	public String getSchemaPath() {
		return this.schemaPath;
	}
	
	/** The query path. */
	@Parameter(names = { "-q", "--query" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input query definition file.")
	private String queryPath;
	
	/**
	 * Gets the query path.
	 *
	 * @return the query path
	 */
	public String getQueryPath() {
		return this.queryPath;
	}
	
	/** The plan path. */
	@Parameter(names = { "-p", "--plan" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input plan definition file.")
	private String planPath;
	
	/**
	 * Gets the plan path.
	 *
	 * @return the plan path
	 */
	public String getPlanPath() {
		return this.planPath;
	}
	
	/** The data path. */
	@Parameter(names = { "-d", "--data" }, required = false,
		validateWith=FileValidator.class,
		description ="Path to the input data file (for in-memory relation).")
	private String dataPath;
	
	/**
	 * Gets the data path.
	 *
	 * @return the data path
	 */
	public String getDataPath() {
		return this.dataPath;
	}
	
	/** The config file. */
	@Parameter(names = { "-c", "--config" }, validateWith=FileValidator.class,
		description = "Path to a configuration file. If none is specified, "
		+ "A file with default name will be looked for in the current directory.")
	private File configFile;
	
	/**
	 * Gets the config file.
	 *
	 * @return the config file
	 */
	public File getConfigFile() {
		return this.configFile;
	}
	
	/** The verbose mode. */
	@Parameter(names = { "-v", "--verbose" }, required = false,
		description ="Path to the input query definition file.")
	private boolean verbose = false;
	
	/**
	 * Checks if is verbose.
	 *
	 * @return true, if is verbose
	 */
	public boolean isVerbose() {
		return this.verbose;
	}

	/** The dynamic params. */
	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();

	/**
	 * Initialize the Bootstrap by reading command line parameters, and running
	 * the runtime on them.
	 * @param args String[]
	 */
	private Bootstrap(String... args) {
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
		run();
	}

	/**
	 * Runs the runtime from the input parameters, schema, query and plan.
	 */
	public void run() {
		RuntimeParameters params = this.getConfigFile() != null ?
				new RuntimeParameters(this.getConfigFile()) :
				new RuntimeParameters() ;
		for (String k : this.dynamicParams.keySet()) {
			params.set(k, this.dynamicParams.get(k));
		}
		try(FileInputStream sis = new FileInputStream(this.getSchemaPath());
			FileInputStream qis = new FileInputStream(this.getQueryPath())) {

			Schema schema = new SchemaReader().read(sis);
			ConjunctiveQuery query = new QueryReader(schema).read(qis);
			Plan plan = obtainPlan(schema, this.getPlanPath());

			List<Atom> facts = null;
			if (this.getDataPath() != null) {
				try (FileInputStream dis = new FileInputStream(this.getDataPath())) {
					facts = new DataReader(schema).read(dis);
				}
			}
			TupleCounter counter = new TupleCounter(System.out);
			try {
				Runtime runtime = new Runtime(params, schema, facts);
				runtime.registerEventHandler(counter);
				if (verbose) {
					runtime.registerEventHandler(new TuplePrinter(System.out));
				}
				runtime.evaluatePlan(plan, query);
			} catch (EvaluationException e) {
				log.error(e.getMessage());
			}
			counter.report();
		} catch (Throwable e) {
			log.error("Evaluation aborted: " + e.getMessage(),e);
			System.exit(-1);
		}
	}
	/**
	 * Obtain plan.
	 *
	 * @param schema Schema
	 * @param planPath the plan path
	 * @return Plan
	 */
	private Plan obtainPlan(Schema schema, String planPath) {
		try (FileInputStream pis = new FileInputStream(planPath)) {
			try {
				return new LeftDeepPlanReader(schema).read(pis); 
			} catch (Exception re) {
				try (FileInputStream bis = new FileInputStream(planPath)) {
					return new DAGPlanReader(schema).read(bis); 
				}
			}
		} catch (IOException e) {
			log.warn(e);
			return null;
		}
	}

	/**
	 * Filters out files that do not exist or are directories.
	 * @author Julien LEBLAY
	 */
	public static class FileValidator implements IParameterValidator {
		
		/* (non-Javadoc)
		 * @see com.beust.jcommander.IParameterValidator#validate(java.lang.String, java.lang.String)
		 */
		@Override
		public void validate(String name, String value) throws ParameterException {
			try {
				File f = new File(value);
				if (!f.exists() || f.isDirectory()) {
					throw new ParameterException(name + " must be a valid configuration file.");
				}
			} catch (Exception e) {
				throw new ParameterException(name + " must be a valid configuration file.");
			}
		}
	}

	/**
	 * Instantiates the bootstrap.
	 *
	 * @param args String[]
	 */
	public static void main(String... args) {
		new Bootstrap(args);
	}
}
