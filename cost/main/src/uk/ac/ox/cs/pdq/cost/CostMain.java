package uk.ac.ox.cs.pdq.cost;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import uk.ac.ox.cs.pdq.FileValidator;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.estimators.CostEstimator;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

/**
 *  Main entry point for cost calculation of plans. 
 * @author gabor
 *
 */
public class CostMain {

	/** The Constant PROGRAM_NAME. */
	private static final String PROGRAM_NAME = "pdq-cost-<version>.jar";
	
	/** The help. */
	@Parameter(names = { "-h", "--help" }, help = true, description = "Displays this help message.")
	private boolean help;
	
	/** The schema path. */
	@Parameter(names = { "-s", "--schema" }, required = true,
			 validateWith=FileValidator.class,
		description ="Path to the input schema definition file (xml).")
	private String schemaPath;
	
	/** The plan path. */
	@Parameter(names = { "-p", "--plan" }, required = true,
			 validateWith=FileValidator.class,
		description ="Path to the input plan definition file (xml).")
	private String planPath;
	
	/** The config file. */
	@Parameter(names = { "-c", "--config" }, validateWith=FileValidator.class,
			description = "Directory where to look for configuration files. "
			+ "Default is the current directory.")
	private File configFile;
	
	public CostMain(String[] args) {
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
		CostParameters costParams = new CostParameters(configFile);
		try {
			s = IOManager.importSchema(new File(schemaPath));
			CostEstimator estimator = CostEstimatorFactory.getInstance(costParams, s);
			System.out.println("Cost estimator created: "+ estimator);
			RelationalTerm relationalTerm = null;
			Cost originalCost = null;
			try {
				relationalTerm = IOManager.readRelationalTerm(new File(planPath), s);
			} catch (Throwable any) {
				// the file might contain cost:
				relationalTerm = CostIOManager.readRelationalTermFromRelationaltermWithCost(new File(planPath), s);
				originalCost = CostIOManager.readRelationalTermCost(new File(planPath), s);
			}
			System.out.println("Plan file read: " + relationalTerm);
			if (originalCost!=null)
				System.out.println("Cost in plan file: " + relationalTerm);
			System.out.println("\nRecalculated cost of plan: " + estimator.cost(relationalTerm));
		} catch (FileNotFoundException | JAXBException e) {
			System.err.println("Error while reading schema from file: " + schemaPath);
			e.printStackTrace();
		}
	}

	/**
	 * Instantiates the bootstrap.
	 *
	 * @param args String[]
	 */
	public static void main(String... args) {
		new CostMain(args);
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
