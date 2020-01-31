package uk.ac.ox.cs.pdq.regression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.CostParameters;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.PlanPrinter;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.PlannerParameters;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.DominanceTypes;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.PlannerTypes;
import uk.ac.ox.cs.pdq.planner.PlannerParameters.SuccessDominanceTypes;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.regression.acceptance.AcceptanceCriterion;
import uk.ac.ox.cs.pdq.regression.acceptance.AcceptanceCriterion.AcceptanceLevels;
import uk.ac.ox.cs.pdq.regression.acceptance.AcceptanceCriterion.AcceptanceResult;
import uk.ac.ox.cs.pdq.regression.acceptance.ApproximateCostAcceptanceCheck;
import uk.ac.ox.cs.pdq.regression.acceptance.ExpectedCardinalityAcceptanceCheck;
import uk.ac.ox.cs.pdq.regression.acceptance.SameCostAcceptanceCheck;
import uk.ac.ox.cs.pdq.runtime.Runtime;
import uk.ac.ox.cs.pdq.runtime.RuntimeParameters;
import uk.ac.ox.cs.pdq.util.GlobalCounterProvider;

/**
 * Main entry point to use PDQ. Runs regression tests or other folders with schema and query xml files.<br>
 * Usage: <br>
 * PDQ -m [mode] -i [folder]
 * <br>Folder: either a folder with a schema.xml and query.xml file in it, or a root directory containing many of such sub folders.<br>
 * Available modes: <br>
 *  - planner: creates the expected plan xml file. If it is already exists compares the result with it.<br>
 *  - runtime: executes the expected plan xml file.<br>
 *  - full: first two in sequence.<br>
 *  
 * @author gabor
 *
 */
public class PDQ {

	protected PrintStream out = System.out;

	private static final String PROGRAM_NAME = "pdq-regression-<version>.jar";

	/**  File name where planning related parameters must be stored in a test case directory. */
	private static final String CONFIGURATION_FILE = "case.properties";

	/**  File name where the schema must be stored in a test case directory. */
	private static final String SCHEMA_FILE = "schema.xml";

	/**  File name where the query must be stored in a test case directory. */
	private static final String QUERY_FILE = "query.xml";

	/**  File name where the expected plan must be stored in a test case directory. */
	private static final String EXPECTED_PLAN_FILE = "expected-plan.xml";

	private static final String CATALOG_FILE = "catalog.properties";

	private static enum Modes {
		planner,
		runtime,
		full
	};

	/** The help. */
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;

	@Parameter(names = { "-i", "--input" }, required = true,
			description = "Path to the regression test case directories.",
			validateWith=DirectoryValidator.class)
	private String input;

	@Parameter(names = { "-m", "--mode" }, required = true,
			description = "Run the planner, the runtime or an end-to-end test.")
	private Modes mode;

	/** The dynamic params. */
	@DynamicParameter(names = "-D", 
			description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();

	private FileWriter statsFile = null;
	private String statsMsg = "";
	

	/**
	 * Prints the first created plan as a png image and opens it in a new window.
	 */
	private boolean printPlan=false;

	/**
	 * Main functionality of this class. 
	 */
	public void runRegression() {				
		Set<File> testDirectories = getTestDirectories(new File(this.input));
		
		for(File directory:testDirectories) {
			String stats = directory.getAbsolutePath() + " : ";
			try {
				GlobalCounterProvider.resetCounters();
				uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
				uk.ac.ox.cs.pdq.db.Cache.reStartCaches();
				uk.ac.ox.cs.pdq.algebra.Cache.reStartCaches();
				this.out.println("\nStarting case '" + directory.getAbsolutePath() + "' using java: " + System.getProperty("java.version"));
				// Loading schema 
				Schema schema = DbIOManager.importSchema(new File(directory, SCHEMA_FILE));

				Entry<RelationalTerm, Cost> observation = null;

				if(mode.equals(Modes.planner) || mode.equals(Modes.full)) {
					// PLANNING
					PlannerTypes plannerType = null;
					if (isInMadatoryPassFolder(directory)) {
						List<Entry<RelationalTerm, Cost>> observations = new ArrayList<Entry<RelationalTerm, Cost>>();
						System.out.println("Running DAG GENERIC");
						observations.add( doPlanning(directory,schema, PlannerTypes.DAG_GENERIC ) );
						System.out.println("Running DAG OPTIMISED");
						observations.add( doPlanning(directory,schema, PlannerTypes.DAG_OPTIMIZED ) );
						if (observations.get(0)!=null)
							observation = observations.get(0);
					} else {
						observation = doPlanning(directory,schema, plannerType );
					}
				}


				if(mode.equals(Modes.runtime) || mode.equals(Modes.full)) {
					// RUNTIME
					doRuntime(directory, schema, observation);
				}
			} catch (FileNotFoundException | JAXBException e) {
				e.printStackTrace();
				stats+= "Failed: " + e.getMessage();
				stats = "FAIL" + stats;
			} catch (PlannerException e) {
				e.printStackTrace();
				stats+= "Failed: " + e.getMessage();				
				stats = "FAIL" + stats;
			} catch (SQLException e) {
				e.printStackTrace();
				stats+= "Failed: " + e.getMessage();				
				stats = "FAIL" + stats;
			} catch (Throwable e) {
				e.printStackTrace();
				stats+= "Failed: " + e.getMessage();				
				stats = "FAIL" + stats;
			}
			stats+= this.statsMsg;
			this.statsMsg = "";
			printStats(stats + "\n");				
			
		}
	}

	private boolean isInMadatoryPassFolder(File directory) {
		if (directory.getName().equals("MandatoryPass"))
			return true;
		if (directory.getParentFile()!=null)
			return isInMadatoryPassFolder(directory.getParentFile());
		return false;
	}

	private void doRuntime(File directory, Schema schema, Entry<RelationalTerm, Cost> observation)
			throws JAXBException, Exception, FileNotFoundException {
		// Load the runtime parameters
		RuntimeParameters runtimeParams = new RuntimeParameters(new File(directory, CONFIGURATION_FILE));
		RegressionParameters regParams = new RegressionParameters(new File(directory, CONFIGURATION_FILE));

		for (String k : this.dynamicParams.keySet()) {
			runtimeParams.set(k, this.dynamicParams.get(k));
			regParams.set(k, this.dynamicParams.get(k));
		}

		// Create a runtime object with an empty list of facts (data will be loaded from a third party database)
		Runtime runtime = new Runtime(runtimeParams, schema);
		
		// Specify the directory where the access methods are described 
		File accesses = new File(directory, runtimeParams.getAccessDirectory());
		AccessRepository ar = AccessRepository.getRepository(accesses.getAbsolutePath());
		runtime.setAccessRepository(ar);
		for (String className: runtimeParams.getCustomAccessClassNames()) {
			ar.addAccessFromJar(className);
		}

		Table results = null;

		if(mode.equals(Modes.full)) {
			if (observation.getKey() == null) 
				this.out.println("\tSKIP: Plan is empty in " + directory.getAbsolutePath());
			else
				results = runtime.evaluatePlan(observation.getKey());
		}
		else {
			// Load expected plan and cost
			File expectedPlanFile = new File(directory, EXPECTED_PLAN_FILE);
			RelationalTerm expectedPlan = CostIOManager.readRelationalTermFromRelationaltermWithCost(expectedPlanFile, schema);

			if (expectedPlan == null) 
				this.out.println("\tSKIP: Plan is empty in " + directory.getAbsolutePath());
			else
				results = runtime.evaluatePlan(expectedPlan);
		}

		AcceptanceResult accResult = new ExpectedCardinalityAcceptanceCheck().check(regParams.getExpectedCardinality(), results);
		accResult.report(this.out);
	}

	private Entry<RelationalTerm, Cost> doPlanning(File directory,Schema schema, PlannerTypes replacePlanner) throws JAXBException, PlannerException, SQLException, IOException, InterruptedException {
		boolean isFailed=false;
		// Loading planner configuration
		PlannerParameters plParams = new PlannerParameters(new File(directory, CONFIGURATION_FILE));
		if (replacePlanner!= null) {
			plParams.setPlannerType(replacePlanner);
		}
		CostParameters costParams = new CostParameters(new File(directory, CONFIGURATION_FILE));
		costParams.setCatalog(directory + "/" + CATALOG_FILE);
		ReasoningParameters reasoningParams = new ReasoningParameters(new File(directory, CONFIGURATION_FILE));
		DatabaseParameters databaseParams = new DatabaseParameters(new File(directory, CONFIGURATION_FILE));

		for (String k : this.dynamicParams.keySet()) {
			plParams.set(k, this.dynamicParams.get(k));
			costParams.set(k, this.dynamicParams.get(k));
			reasoningParams.set(k, this.dynamicParams.get(k));
			databaseParams.set(k, this.dynamicParams.get(k));
		}

		//Load a conjunctive query from an XML
		ConjunctiveQuery query = IOManager.importQuery(new File(directory, QUERY_FILE));

		
		// Call the planner to find a plan
		long start = System.currentTimeMillis();
		ExplorationSetUp planner = new ExplorationSetUp(plParams, costParams, reasoningParams, databaseParams, schema);
		Entry<RelationalTerm, Cost> observation = planner.search(query);
		double duration = (System.currentTimeMillis() - start) / 1000.0;
		if (printPlan && observation!=null) {
			printPlan = false;
			//PlanPrinter.openPngPlan(observation.getKey());
		}
		DecimalFormat myFormatter = new DecimalFormat("####.##");
		String duration_s = " Duration: " + myFormatter.format(duration) + "s.";				

		// Load expected plan and cost
		AcceptanceCriterion<Entry<RelationalTerm, Cost>, Entry<RelationalTerm, Cost>> acceptance = acceptance(plParams, costParams);
		this.out.print("Using " + acceptance.getClass().getSimpleName() + ": ");
		File expectedPlanFile = new File(directory, EXPECTED_PLAN_FILE);
		RelationalTerm expectedPlan = null;
		Cost expectedCost = null;
		AcceptanceResult results = null;
		if (expectedPlanFile.exists()) {
			try {
				expectedPlan = CostIOManager.readRelationalTermFromRelationaltermWithCost(expectedPlanFile, schema);
				expectedCost = CostIOManager.readRelationalTermCost(expectedPlanFile, schema);
				if (observation==null) {
					String msg = "FAIL: No new plan, while old exists."; 
					System.out.println(msg);
					statsMsg+=msg;
					isFailed = true;
				} else {
					results = acceptance.check(
							new AbstractMap.SimpleEntry<RelationalTerm,Cost>(expectedPlan, expectedCost), observation);
					results.report(this.out);
					statsMsg+=results.report();
					isFailed = results.getLevel() == AcceptanceLevels.FAIL;
				}
			} catch(Throwable t) {
				t.printStackTrace();
				statsMsg+="Failed to read previous plan: " + t.getMessage() ;
			}
		} else {
			if (observation==null)
				System.out.println("No previous or current plan result.");
			else
				System.out.println("No previous plan found, but there is a new plan");
			statsMsg+="No previous plan found.";
		}
		if (observation != null && (expectedPlan == null || expectedCost.greaterThan(observation.getValue())) ) {
			this.out.print("\tWriting plan: " + observation + " " + observation.getValue());
			CostIOManager.writeRelationalTermAndCost(new File(directory.getAbsolutePath() + '/' + EXPECTED_PLAN_FILE),  observation.getKey(), observation.getValue());
			statsMsg+= "New Plan written.";
		}
		if (observation!=null && observation.getValue()!= null)
			statsMsg+= "Cost: " + observation.getValue();
		this.out.println("\n " + duration_s);
		statsMsg+= "Finished, " + duration_s;
		if (isFailed)
			statsMsg = " NOT ACCEPTED " + statsMsg;
		else
			statsMsg = " SUCCESS " + statsMsg;
		return observation;
	}
	/** Checks the content of each sub directory and selects the ones that look like a test folder.
	 * @param directory
	 * @return
	 */
	private Set<File> getTestDirectories(File directory) {
		Set<File> subdirectories = new LinkedHashSet<>();
		File[] files = directory.listFiles();
		Arrays.sort(files);
		for (File f:files) {
			if (!f.equals(directory) && f.isDirectory() && isLeaf(f))
				subdirectories.add(f);
			else if(!f.equals(directory) && f.isDirectory())
				subdirectories.addAll(this.getTestDirectories(f));
		}
		if (isLeaf(directory)) {
			subdirectories.add(directory);
		}
		return subdirectories;
	}

	/** It is a leaf if it has no sub-directories.
	 * @param directory
	 * @return
	 */
	private boolean isLeaf(File directory) {
		File[] files = directory.listFiles();
		String caseProperties = "case.properties";
		for (File f:files) {
			if (f.getName().equals(caseProperties))
				return true;
		}
		return false;
	}


	/** Checks results
	 * @param params
	 * @param cost
	 * @return
	 */
	private AcceptanceCriterion<Entry<RelationalTerm, Cost>, Entry<RelationalTerm, Cost>> acceptance(PlannerParameters params, CostParameters cost) {
		switch (params.getPlannerType()) {
		case DAG_GENERIC:
		case DAG_OPTIMIZED:
			if (params.getSuccessDominanceType() == SuccessDominanceTypes.OPEN || params.getDominanceType() == DominanceTypes.OPEN) 
				return new ApproximateCostAcceptanceCheck();
			switch (params.getValidatorType()) {
			case DEPTH_VALIDATOR:
			case RIGHT_DEPTH_VALIDATOR:
				return new ApproximateCostAcceptanceCheck();
			default:
				break;
			}
			if (params.getFilterType() != null) 
				return new ApproximateCostAcceptanceCheck();
			break;
		default:
			return new SameCostAcceptanceCheck();
		}
		return new SameCostAcceptanceCheck();
	}

	/**
	 * Instantiates the bootstrap.
	 *
	 * @param args String[]
	 */
	public static void main(String... args) {
		new PDQ(args);
	}

	private void printStats(String message) {
		if (statsFile!=null) {
			try {
				statsFile.write(message);
				statsFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Initialise the Bootstrap by reading command line parameters, and running
	 * the planner on them.
	 * @param args String[]
	 */
	private PDQ(String... args) {
		JCommander jc = new JCommander(this);
		jc.setProgramName(PROGRAM_NAME);
		try {
			jc.parse(args);
		} catch (ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			return;
		}
		if (this.help) {
			jc.usage();
			return;
		}
		File folder = new File("TestResults/current");
		folder.mkdirs();
		File stats = new File(folder,"summary"+System.currentTimeMillis() + ".txt");
		System.out.println("Creating log file: " + stats.getAbsolutePath());
		stats.delete();
		try {
			statsFile = new FileWriter(stats);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		printStats("Start running regression test: " + mode.toString() + " in folder: " + input + "\n");
		try {
			runRegression();
			printStats("regression test finished successfully");
		} catch(Throwable t) {
			printStats("regression test failed " + t.getMessage());
			t.printStackTrace();
		}
		try {
			if (statsFile!=null)
				statsFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The Class DirectoryValidator.
	 */
	public static class DirectoryValidator implements IParameterValidator {
		
		/* (non-Javadoc)
		 * @see com.beust.jcommander.IParameterValidator#validate(java.lang.String, java.lang.String)
		 */
		@Override
		public void validate(String name, String value) throws ParameterException {
			try {
				File f = new File(value);
				if (!(f.exists() && f.isDirectory())) {
					throw new ParameterException(name + " must be a valid directory.");
				}
			} catch (Exception e) {
				throw new ParameterException(name + " must be a valid directory.");
			}
		}
		
	}	
}
