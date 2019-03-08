package uk.ac.ox.cs.pdq.reasoning;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import uk.ac.ox.cs.pdq.FileValidator;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.cache.MultiInstanceFactCache;

/**
 * Bootstrapping class for starting the reasoner.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class Reason {
	private static Logger log = Logger.getLogger(Reason.class);
	private static final String PROGRAM_NAME = "pdq-reasoning-<version>.jar";
	/* Main function parameters */

	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;

	@Parameter(names = { "-s",
			"--schema" }, required = true, validateWith = FileValidator.class, description = "Path to the input schema definition file.")
	private String schemaPath;
	
	@Parameter(names = { "-q",
			"--query" }, required = false, validateWith = FileValidator.class, description = "Path to the input query definition file. Either facts or a query is mandatory.")
	private String queryPath;

	@Parameter(names = { "-f",
			"--facts" }, required = false, description = "Path to the folder containing [RelationName].csv files containing data for the given relation. Either facts or a query is mandatory.")
	private String factsPath;

	@Parameter(names = { "-c",
			"--config" }, validateWith = FileValidator.class, description = "Directory where to look for configuration files. "
					+ "Default is the current directory.")
	private File configFile;
	
	@Parameter(names = { "-v",
			"--verbose" }, required = false, description = "Activates verbose mode.")
	private boolean verbose = false;
	
	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();
	
	/**
	 * Run with --help for options
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		new Reason(args);
	}

	/**
	 * Initialize the Reason class by reading command line parameters, and running the
	 * resoner on them.
	 * 
	 * @param args
	 *            String[]
	 */
	private Reason(String... args) {
		JCommander jc = new JCommander(this);
		jc.setProgramName(PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		if (this.isHelp() || this.getQueryPath() == null && this.getFactsPath() == null) {
			jc.usage();
			return;
		}
		run();
	}

	/**
	 * Runs the resoner from the input parameters, schema, and query or facts.
	 */
	public void run() {
		long start = System.currentTimeMillis();
		ReasoningParameters reasoningParams = this.getConfigFile() != null
				? new ReasoningParameters(this.getConfigFile())
				: new ReasoningParameters();


		for (String k : this.dynamicParams.keySet()) {
			reasoningParams.set(k, this.dynamicParams.get(k));
		}
		try {
			Schema schema = DbIOManager.importSchema(new File(this.getSchemaPath()));
			ConjunctiveQuery query = null;
			if (this.getQueryPath() != null)
				query = IOManager.importQuery(new File(this.getQueryPath()));
			List<Atom> facts = new ArrayList<>();
			if (this.getFactsPath() != null) {
				for (Relation r : schema.getRelations()) {
					File rXml = new File(this.getFactsPath(),r.getName() + ".csv");
					if (rXml.exists())
						facts.addAll(DbIOManager.importFacts(r, rXml));
				}
			}

			if (schema == null) {
				throw new IllegalStateException("Schema must be provided.");
			}
			if (query == null && facts.isEmpty()) {
				throw new IllegalStateException("Query or facts must be provided.");
			}
			if (query != null && !facts.isEmpty()) {
				throw new IllegalStateException("One one of query or facts should be provided.");
			}
			ReasonerFactory reasonerFactory = new ReasonerFactory(reasoningParams);

			Chaser reasoner = reasonerFactory.getInstance();
			DatabaseParameters dbParams = this.getConfigFile() != null ? new DatabaseParameters(this.getConfigFile())
				: DatabaseParameters.Postgres;
			
			DatabaseManager manager = null;
			if (dbParams.getUseInternalDatabaseManager())
				manager = new InternalDatabaseManager();
			else manager = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(dbParams), 0);
			manager.initialiseDatabaseForSchema(schema);
			ChaseInstance state;
			if (query != null) {
				// Creates a chase state that consists of the canonical database of the input
				// query.
				state = new DatabaseChaseInstance(query, manager);
			} else {
				// Creates a chase state that from the provided facts.
				state = new DatabaseChaseInstance(facts, manager);
			}
			reasoner.reasonUntilTermination(state, schema.getAllDependencies());
			System.out.println("Reasoning results generated in " + (System.currentTimeMillis() - start)/1000.0 + " sec.");
			for (Atom a: state.getFacts())
				System.out.println(a);
			
		} catch (Throwable e) {
			log.error("Reasoning aborted: " + e.getMessage(), e);
			System.exit(-1);
		}
	}

	public boolean isHelp() {
		return this.help;
	}
	public String getSchemaPath() {
		return this.schemaPath;
	}

	public String getQueryPath() {
		return this.queryPath;
	}

	public String getFactsPath() {
		return this.factsPath;
	}

	public File getConfigFile() {
		return this.configFile;
	}

	public boolean isVerbose() {
		return this.verbose;
	}
}
