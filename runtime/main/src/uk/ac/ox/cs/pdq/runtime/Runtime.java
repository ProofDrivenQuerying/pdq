package uk.ac.ox.cs.pdq.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import uk.ac.ox.cs.pdq.FileValidator;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;
import uk.ac.ox.cs.pdq.util.Table;

/**
 *  Decorates a plan, and executes queries or the plan itself.
 *  Main entry point for the runtime. 
 * @author gabor
 *
 */
public class Runtime {

	/** The Constant PROGRAM_NAME. */
	private static final String PROGRAM_NAME = "pdq-runtime-<version>.jar";
	
	/** The help. */
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	
	/** The schema path. */
	@Parameter(names = { "-s", "--schema" }, required = true,
			 validateWith=FileValidator.class,
		description ="Path to the input schema definition file.")
	private String schemaPath;
	
	/** The config file. */
	@Parameter(names = { "-c", "--config" }, validateWith=FileValidator.class,
			description = "Directory where to look for configuration files. "
			+ "Default is the current directory.")
	private File configFile;
	
	@Parameter(names = { "-v", "--verbose" }, required = false,
			description ="Path to the input query definition file.")
	private boolean verbose = false;
	
	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();

	/** Runtime's parameters. */
	private RuntimeParameters params;

	/** Runtime's internal schema. */
	private Schema schema;

	private AccessRepository repository;

	/**
	 * Constructor for Runtime.
	 * 
	 * @param params
	 *            RuntimeParameters
	 * @param schema
	 *            Schema
	 */
	public Runtime(RuntimeParameters params, Schema schema) {
		super();
		this.params = params;
		this.schema = schema;
	}

	public Runtime(String[] args) {
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
		Schema s;
		RuntimeParameters rp = new RuntimeParameters(configFile);
		try {
			s = IOManager.importSchema(new File(schemaPath));
			new Runtime(rp,s);
		} catch (FileNotFoundException | JAXBException e) {
			System.err.println("Error while reading schema from file: " + schemaPath);
			e.printStackTrace();
		}
	}

	/**
	 * Evaluates the given plan and returns its result.
	 *
	 * @param p
	 *            Plan
	 * @param query
	 *            Query
	 * @return the result of the plan evaluation.
	 * @throws EvaluationException
	 *             the evaluation exception
	 */
	public Table evaluatePlan(RelationalTerm p) throws Exception {
		AccessRepository repo = this.repository;
		if (repo == null)
				repo = AccessRepository.getRepository();
		try {
			ExecutablePlan executable = new PlanDecorator(repo,schema).decorate(p);
			System.out.println("Executing plan " + p.hashCode());
			Table res = executable.execute();
			System.out.println("plan " + p.hashCode() + " finished.");
			return res;
		}catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	public RuntimeParameters getParams() {
		return params;
	}

	public void setAccessRepository(AccessRepository repository) {
		this.repository = repository;
		
	}

	/**
	 * Instantiates the bootstrap.
	 *
	 * @param args String[]
	 */
	public static void main(String... args) {
		new Runtime(args);
	}
	/**
	 * Checks if this is called with help as an argument.
	 *
	 * @return true, if it is called with help
	 */
	public boolean isHelp() {
		return this.help;
	}
	
	
}
