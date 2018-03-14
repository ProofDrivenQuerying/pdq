package uk.ac.ox.cs.pdq.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.datasources.utility.TupleCounter;
import uk.ac.ox.cs.pdq.datasources.utility.TuplePrinter;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.runtime.io.DataReader;

/**
 * Bootstrapping class for starting the runtime. 
 * 
 * @author Julien Leblay
 */
public class Bootstrap {

	private static Logger log = Logger.getLogger(Bootstrap.class); 
	
	private static final String PROGRAM_NAME = "pdq-runtime-<version>.jar";
	
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	
	/**
	 *TOCOMMENT:???
	 * @return true, if is help
	 */
	public boolean isHelp() {
		return this.help;
	}
	
	/** TOCOMMENT:??? */
	@Parameter(names = { "-s", "--schema" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input schema definition file.")
	private String schemaPath;
	
	/**
	 *
	 * @return the schema path
	 */
	public String getSchemaPath() {
		return this.schemaPath;
	}
	
	/** TOCOMMENT: WHAT IS IT?. */
	@Parameter(names = { "-q", "--query" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input query definition file.")
	private String queryPath;
	
	/**
	 * TCOMMMENT: WHAT IS IT
	 *
	 * @return the query path
	 */
	public String getQueryPath() {
		return this.queryPath;
	}
	
	/** TOCOMMENT: WHAT IS IT. */
	@Parameter(names = { "-p", "--plan" }, required = true,
			validateWith=FileValidator.class,
			description ="Path to the input plan definition file.")
	private String planPath;
	
	/**
	 * TOCOMMENT: WHAT IS IT
	 *
	 * @return the plan path
	 */
	public String getPlanPath() {
		return this.planPath;
	}
	
	/** TOCOMMENT: WHAT IS IT*/
	@Parameter(names = { "-d", "--data" }, required = false,
		validateWith=FileValidator.class,
		description ="Path to the input data file (for in-memory relation).")
	private String dataPath;
	
	/**
	 * TOCOMMENT WHAT IS IT
	 *
	 * @return the data path
	 */
	public String getDataPath() {
		return this.dataPath;
	}
	
	@Parameter(names = { "-c", "--config" }, validateWith=FileValidator.class,
		description = "Path to a configuration file. If none is specified, "
		+ "A file with default name will be looked for in the current directory.")
	private File configFile;
	
	/**
	 *
	 * @return the config file
	 */
	public File getConfigFile() {
		return this.configFile;
	}
	
	@Parameter(names = { "-v", "--verbose" }, required = false,
		description ="Path to the input query definition file.")
	private boolean verbose = false;
	
	/**
	 *
	 * @return true, if is verbose
	 */
	public boolean isVerbose() {
		return this.verbose;
	}

	/**  */
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
			e.printStackTrace();
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
		try {
			Schema schema = DbIOManager.importSchema(new File(this.getSchemaPath()));
			ConjunctiveQuery query = IOManager.importQuery(new File(this.getQueryPath()));
			query = IOManager.convertQueryConstants(query,schema);
			RelationalTerm plan = obtainPlan(this.getPlanPath(), schema);

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
			e.printStackTrace();
			log.error("Evaluation aborted: " + e.getMessage(),e);
			System.exit(-1);
		}
	}
	
	public static RelationalTerm obtainPlan(String fileName, Schema schema) {
		File file = new File(fileName);
		try(FileInputStream pis = new FileInputStream(fileName) ){
			try {
				return IOManager.readRelationalTerm(file, schema);
			}catch(JAXBException e) {
				return CostIOManager.readRelationalTermFromRelationaltermWithCost(file, schema);
			}
		} catch (IOException | JAXBException e) {
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
