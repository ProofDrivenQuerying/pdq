package uk.ac.ox.cs.pdq.planner;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import uk.ac.ox.cs.pdq.FileValidator;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;

/**
 * Entry point for the planner. 
 * 
 * @author Julien Leblay
 * @author Gabor
 */
public class Planner {

	/** Logger. */
	private static Logger log = Logger.getLogger(Planner.class); 
	
	/** The Constant PROGRAM_NAME. */
	private static final String PROGRAM_NAME = "pdq-planner-<version>.jar";
	
	/** The help. */
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	
	/** The schema path. */
	@Parameter(names = { "-s", "--schema" }, required = true,
			 validateWith=FileValidator.class,
		description ="Path to the input schema definition file.")
	private String schemaPath;
	
	/** The query path. */
	@Parameter(names = { "-q", "--query" }, required = true,
			 validateWith=FileValidator.class,
		description ="Path to the input query definition file.")
	private String queryPath;
	
	/** The config file. */
	@Parameter(names = { "-c", "--config" }, validateWith=FileValidator.class,
			description = "Directory where to look for configuration files. "
			+ "Default is the current directory.")
	private File configFile;
	
	@Parameter(names = { "-v", "--verbose" }, required = false,
			description ="Activates verbose mode.")
	private boolean verbose = false;
	
	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();
	
	/**
	 * Gets the schema path.
	 *
	 * @return the schema path
	 */
	public String getSchemaPath() {
		return this.schemaPath;
	}
	
	
	/**
	 * Gets the query path.
	 *
	 * @return the query path
	 */
	public String getQueryPath() {
		return this.queryPath;
	}
	
	/**
	 * Gets the config file.
	 *
	 * @return the config file
	 */
	public File getConfigFile() {
		return this.configFile;
	}
	
	
	/**
	 * Checks if  verbose is set.
	 *
	 * @return true, if is verbose
	 */
	public boolean isVerbose() {
		return this.verbose;
	}


	/**
	 * Initialize the Bootstrap by reading command line parameters, and running
	 * the planner on them.
	 * @param args String[]
	 */
	private Planner(String... args) {
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
	 * Runs the planner from the input parameters, schema and query.
	 */
	public void run() {
		PlannerParameters planParams = this.getConfigFile() != null ?
				new PlannerParameters(this.getConfigFile()) :
				new PlannerParameters() ;
		CostParameters costParams = this.getConfigFile() != null ?
				new CostParameters(this.getConfigFile()) :
				new CostParameters() ;
				
		ReasoningParameters reasoningParams = this.getConfigFile() != null ?
				new ReasoningParameters(this.getConfigFile()) :
				new ReasoningParameters() ;
				
		DatabaseParameters dbParams = this.getConfigFile() != null ?
						new DatabaseParameters(this.getConfigFile()) :
						DatabaseParameters.Postgres ;
				
		for (String k : this.dynamicParams.keySet()) {
			planParams.set(k, this.dynamicParams.get(k));
		}
		try {
			Schema schema = IOManager.importSchema(new File(this.getSchemaPath()));
			ConjunctiveQuery query = IOManager.importQuery(new File(this.getQueryPath()));

			Entry<RelationalTerm, Cost> entry = null;
				ExplorationSetUp planner = new ExplorationSetUp(planParams, costParams, reasoningParams, dbParams, schema);
				entry = planner.search(query);

			if (entry != null) {
				System.out.println(entry.getKey());
				return;
			} 
			System.out.println("No plan found.");
			log.trace("No plan found.");
		} catch (Throwable e) {
			log.error("Planning aborted: " + e.getMessage(), e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	/**
	 * Checks if this is called with help as an argument.
	 *
	 * @return true, if it is called with help
	 */
	public boolean isHelp() {
		return this.help;
	}
	/**
	 * Instantiates the bootstrap.
	 *
	 * @param args String[]
	 */
	public static void main(String... args) {
		new Planner(args);
	}
	
}
