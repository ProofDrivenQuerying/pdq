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
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.io.pretty.DataReader;
import uk.ac.ox.cs.pdq.io.xml.DAGPlanReader;
import uk.ac.ox.cs.pdq.io.xml.LinearPlanReader;
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

/**
 * Bootstrapping class for starting the runtime. 
 * 
 * @author Julien Leblay
 */
public class Bootstrap {

	/** Logger. */
	private static Logger log = Logger.getLogger(Bootstrap.class); 
	
	private static final String PROGRAM_NAME = "pdq-runtime-<version>.jar";
	
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	public boolean isHelp() {
		return this.help;
	}
	
	@Parameter(names = { "-s", "--schema" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input schema definition file.")
	private String schemaPath;
	public String getSchemaPath() {
		return this.schemaPath;
	}
	
	@Parameter(names = { "-q", "--query" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input query definition file.")
	private String queryPath;
	public String getQueryPath() {
		return this.queryPath;
	}
	
	@Parameter(names = { "-p", "--plan" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input plan definition file.")
	private String planPath;
	public String getPlanPath() {
		return this.planPath;
	}
	
	@Parameter(names = { "-d", "--data" }, required = false,
		validateWith=FileValidator.class,
		description ="Path to the input data file (for in-memory relation).")
	private String dataPath;
	public String getDataPath() {
		return this.dataPath;
	}
	
	@Parameter(names = { "-c", "--config" }, validateWith=FileValidator.class,
		description = "Path to a configuration file. If none is specified, "
		+ "A file with default name will be looked for in the current directory.")
	private File configFile;
	public File getConfigFile() {
		return this.configFile;
	}
	
	@Parameter(names = { "-v", "--verbose" }, required = false,
		description ="Path to the input query definition file.")
	private boolean verbose = false;
	public boolean isVerbose() {
		return this.verbose;
	}

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

			List<Predicate> facts = null;
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
			log.error("Evaluation aborted: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	/**
	 * @param directory File
	 * @param schema Schema
	 * @param query Query
	 * @param full boolean
	 * @return Plan
	 */
	private Plan obtainPlan(Schema schema, String planPath) {
		try (FileInputStream pis = new FileInputStream(planPath)) {
			try {
				return new LinearPlanReader(schema).read(pis); 
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
	 * Instantiates the bootstrap
	 * @param args String[]
	 */
	public static void main(String... args) {
		new Bootstrap(args);
	}
}
