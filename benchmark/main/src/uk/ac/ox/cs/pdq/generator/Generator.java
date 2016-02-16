package uk.ac.ox.cs.pdq.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.runner.Runner;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.generator.queryfromids.GeneratorSecond;
import uk.ac.ox.cs.pdq.generator.queryfromids2.GeneratorThird;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.GeneratorFirst;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

// TODO: Auto-generated Javadoc
/**
 * The Class Generator.
 */
public class Generator {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(Runner.class);

	/** Program execution command (to appear un usage message). */
	private static final String PROGRAM_NAME = "java -jar pdq-builders.jar";

	/** Default error code. */
	private static final int ERROR_CODE = -1;

	/** The help. */
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	
	/** The config file. */
	@Parameter(names = { "-c", "--config" }, required = false,
			description = "Path to the config file. By default values are defined in " + BenchmarkParameters.DEFAULT_CONFIG_FILE_NAME)
	private String configFile;

	/** The schema file. */
	@Parameter(names = { "-s", "--query-with-schema" }, required = false,
			description = "Generate a query rather then a schema. Then, the path the schema file to use must be provided.")
	private String schemaFile;

	/** The query file. */
	@Parameter(names = { "-q", "--schema-with-query" }, required = false,
			description = "Generate a schema with dependencies generated from the query. Then, the path to the query file to use must be provided.")
	private String queryFile;

	/** The generate bindings. */
	@Parameter(names = { "-b", "--generate-bindings" }, required = false,
			description = "Generate bindings for the given schema. Assume the -s option is correctly set")
	private boolean generateBindings;

	/** The output file. */
	@Parameter(names = { "-o", "--output" }, required = false,
			description = "Path to the output file. If omitted, STDOUT is used.")
	private String outputFile;

	/** The dynamic params. */
	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the initialConfig files.")
	private Map<String, String> dynamicParams = new LinkedHashMap<>();

	/** The parameters. */
	private BenchmarkParameters parameters;

	/** The out. */
	private PrintStream out;
	
	/** The generator. */
	private AbstractGenerator generator;

	/**
	 * Sets up an experiment sample using external parameters (file and 
	 * command-line arguments).
	 *
	 * @param args            the command line parameters as given by the main method.
	 * @throws ReflectiveOperationException the reflective operation exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Generator(String... args) throws ReflectiveOperationException, IOException {
		this(null, args);
	}

	/**
	 * Sets up an experiment sample using external parameters (file and 
	 * command-line arguments).
	 *
	 * @param out the out
	 * @param args            the command line parameters as given by the main method.
	 * @throws ReflectiveOperationException the reflective operation exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Generator(PrintStream out, String... args) throws ReflectiveOperationException, IOException {

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
		// Load initialConfig
		if (this.getConfigFile() != null) {
			this.parameters = new BenchmarkParameters(new File(this.getConfigFile()));
		} else {
			this.parameters = new BenchmarkParameters();
		}

		// Override with dynamic parameters (specified in command line)
		for (String k : this.dynamicParams.keySet()) {
			this.parameters.set(k, this.dynamicParams.get(k));
		}

		// Set the output stream
		this.out = out;
		if (this.getOutputFile() != null) {
			File f = new File(this.getOutputFile());
			if (!f.createNewFile()) {
				log.warn(this.getOutputFile() + " exists. Will be erased.");
			}
			this.out = new PrintStream(f);
		}
		
		switch (this.parameters.getGeneratorType()) {
		case FIRST:
			this.generator = new GeneratorFirst(this.parameters, this.schemaFile, this.queryFile, this.out);
			break;
		case SECOND:
			this.generator = new GeneratorSecond(this.parameters, this.schemaFile, this.queryFile, this.out);
			break;
		case THIRD:
			this.generator = new GeneratorThird(this.parameters, this.schemaFile, this.queryFile, this.out);
			break;
		default:
			throw new java.lang.IllegalArgumentException("Unsupported generator type");
		}
	}
	
	
	
	/**
	 * Make.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void make() throws IOException {
		this.generator.make();
	}

	/**
	 * Checks if is help.
	 *
	 * @return true if the line command asked for help.
	 */
	public boolean isHelp() {
		return this.help;
	}

	/**
	 * Sets the help.
	 *
	 * @param help the new help
	 */
	public void setHelp(boolean help) {
		this.help = help;
	}

	/**
	 * Gets the config file.
	 *
	 * @return the path the initialConfig file to use.
	 */
	public String getConfigFile() {
		return this.configFile;
	}

	/**
	 * Sets the config file.
	 *
	 * @param configFile the new config file
	 */
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * Gets the output file.
	 *
	 * @return the path to the output file, if any. If none, System.out is used
	 * as the output.
	 */
	public String getOutputFile() {
		return this.outputFile;
	}

	/**
	 * Sets the output file.
	 *
	 * @param outputFile the new output file
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * Gets the query file.
	 *
	 * @return the query file
	 */
	public String getQueryFile() {
		return this.queryFile;
	}

	/**
	 * Sets the query file.
	 *
	 * @param queryFile the new query file
	 */
	public void setQueryFile(String queryFile) {
		this.queryFile = queryFile;
	}

	/**
	 * Gets the schema file.
	 *
	 * @return the input schema file path
	 */
	public String getSchemaFile() {
		return this.schemaFile;
	}

	/**
	 * Sets the input schema file path.
	 *
	 * @param schemaFile the new schema file
	 */
	public void setSchemaFile(String schemaFile) {
		this.schemaFile = schemaFile;
	}

	/**
	 * Instantiates an experiment and runs it.
	 *
	 * @param args the arguments
	 */
	public static void main(String... args) {
		try {
			new Generator(System.out, args).make();
		} catch (IOException | ReflectiveOperationException e) {
			log.error(e.getMessage(),e);
			System.exit(ERROR_CODE);
		}
	}

}
