package uk.ac.ox.cs.pdq.runtime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import uk.ac.ox.cs.pdq.FileValidator;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.datasources.tuple.Table.ResetableIterator;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.runtime.exec.PlanDecorator;
import uk.ac.ox.cs.pdq.runtime.exec.spliterator.ExecutablePlan;

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
	
	/** The plan to execute in the xml format. */
	@Parameter(names = { "-p", "--plan" }, validateWith=FileValidator.class, required = true,
			description = "The plan (xml) to execute.")
	private File planFile;
	
	/** AccessRepository folder for the memory database or web service executable access methods. */
	@Parameter(names = { "-a", "--accesses" },
			description = "Directory where to look for executable access method descriptor xml files. ")
	private File accessRepo;
	
	@Parameter(names = { "-v", "--verbose" }, required = false,
			description ="Path to the input query definition file.")
	private boolean verbose = false;
	
	@Parameter(names = { "-o", "--output" }, required = false,
			description ="Path to the output csv file.")
	private File output;
	
	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();

	/** Runtime's parameters. */
	private RuntimeParameters params;

	/** Runtime's internal schema. */
	private Schema schema;

	private AccessRepository repository;

	private Table results;
	private long tupleCount = 0;

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

	public Runtime(String[] args) throws Exception {
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
		this.params = new RuntimeParameters(configFile);
		RelationalTerm plan = null;
		this.schema = IOManager.importSchema(new File(schemaPath));
		if (accessRepo!=null && accessRepo.isDirectory())
			this.repository = AccessRepository.getRepository(accessRepo.getAbsolutePath());

		plan = CostIOManager.readRelationalTermFromRelationaltermWithCost(planFile, schema);
		long start = System.currentTimeMillis();
		this.results = this.evaluatePlan(plan);
		if (output!=null) {
			writeOutput();
		} else {
			ResetableIterator<Tuple> it = getResults().iterator();
			this.tupleCount = 0;
			// print output
			while(it.hasNext()) {
				this.tupleCount++;
				Tuple t = it.next();
				if (verbose) System.out.println(t);
			}
		}
		System.out.println();
		System.out.println("Finished, " + getTupleCount() + " amount of tuples found in " + (System.currentTimeMillis() - start)/1000.0 + " sec.");
	}

	private void writeOutput() throws IOException {
		File target = output;
		if (output.isDirectory())
			target = new File(output, "results.csv");
		this.tupleCount = 0;
		try (FileWriter fw = new FileWriter(target,true)) {
			// Header
			StringBuilder builder = null;
			for (uk.ac.ox.cs.pdq.db.Attribute attribute : results.getHeader()) {
				if (builder == null) {
					builder = new StringBuilder();
				} else {
					builder.append(",");
				}
				builder.append(attribute.getName());
			}
			builder.append("\r\n");
			fw.write(builder.toString());
			
			//Data
			ResetableIterator<Tuple> it = results.iterator();
			while(it.hasNext()) {
				this.tupleCount++;
				Tuple t = it.next();
				builder = null;
				int attributeCounter = 0;
				for (Object value : t.getValues()) {
					if (builder == null) {
						builder = new StringBuilder();
					} else {
						builder.append(",");
					}
					if (results.getHeader()[attributeCounter].getType().equals(String.class))
						builder.append(value.toString().replaceAll(",", "/c"));
					else 
						builder.append(value);
					attributeCounter++;
				}
				builder.append("\r\n");
				fw.write(builder.toString());
			}
			fw.close();
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
		try {
			new Runtime(args);
		} catch (Exception e) {
			e.printStackTrace();
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

	public File getPlanFile() {
		return planFile;
	}

	public void setPlanFile(File planFile) {
		this.planFile = planFile;
	}

	public File getAccessRepo() {
		return accessRepo;
	}

	public void setAccessRepo(File accessRepo) {
		this.accessRepo = accessRepo;
	}

	public long getTupleCount() {
		return tupleCount;
	}

	public Table getResults() {
		return results;
	}

	public File getOutput() {
		return output;
	}

	public void setOutput(File output) {
		this.output = output;
	}

}
