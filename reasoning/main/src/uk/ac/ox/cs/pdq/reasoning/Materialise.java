package uk.ac.ox.cs.pdq.reasoning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseRestrictedState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
/**
 * Bootstrapping class for starting the reasoner. 
 * 
 * @author Efthymia Tsamoura
 */
public class Materialise {

	/** Logger. */
	private static Logger log = Logger.getLogger(Materialise.class); 
	
	/** The Constant PROGRAM_NAME. */
	private static final String PROGRAM_NAME = "pdq-reasoning-<version>.jar";
	
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
	@Parameter(names = { "-q", "--query" }, required = false,
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
	
	/** The config file. */
	@Parameter(names = { "-c", "--config" }, validateWith=FileValidator.class,
			description = "Directory where to look for configuration files. "
			+ "Default is the current directory.")
	private File configFile;
	
	/**
	 * Gets the config file.
	 *
	 * @return the config file
	 */
	public File getConfigFile() {
		return this.configFile;
	}
	
	/** The verbose. */
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
	 * the planner on them.
	 * @param args String[]
	 */
	private Materialise(String... args) {
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
	 *
	 * @param <S> the generic type
	 */
	public <S extends ChaseState> void run() {				
		ReasoningParameters reasoningParams = this.getConfigFile() != null ?
				new ReasoningParameters(this.getConfigFile()) :
				new ReasoningParameters() ;
		for (String k : this.dynamicParams.keySet()) {
			reasoningParams.set(k, this.dynamicParams.get(k));
		}
		try(FileInputStream sis = new FileInputStream(this.getSchemaPath());
			) {

			Schema schema = new SchemaReader().read(sis);
			HomomorphismManager detector = new HomomorphismManagerFactory().getInstance(schema, reasoningParams);
			
			//Load the facts 
			//Store them in database
			Collection<Atom> facts = Sets.newHashSet();
			facts.addAll(this.loadFacts(schema, "hospital", "C:\\Users\\tsamoura\\Dropbox\\chaseBench\\datasets\\doctors\\data\\1m\\hospital.csv"));
			facts.addAll(this.loadFacts(schema, "medprescription", "C:\\Users\\tsamoura\\Dropbox\\chaseBench\\datasets\\doctors\\data\\1m\\medprescription.csv"));
			facts.addAll(this.loadFacts(schema, "physician", "C:\\Users\\tsamoura\\Dropbox\\chaseBench\\datasets\\doctors\\data\\1m\\physician.csv"));
			facts.addAll(this.loadFacts(schema, "treatment", "C:\\Users\\tsamoura\\Dropbox\\chaseBench\\datasets\\doctors\\data\\1m\\treatment.csv"));
			
			
			ReasonerFactory reasonerFactory = new ReasonerFactory(
					new EventBus(),
					true,
					reasoningParams);
								
			Chaser reasoner = reasonerFactory.getInstance();
			
			ChaseState state = new DatabaseRestrictedState((DBHomomorphismManager)detector, facts);
			facts.clear();
			System.gc();
			
			reasoner.reasonUntilTermination(state, schema.getDependencies());//
			//CollectionUtils.union(schema.getDependencies(), this.loadEGDs(schema))
			//TODO show something 
		} catch (Throwable e) {
			log.error("Planning aborted: " + e.getMessage(), e);
			System.exit(-1);
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
		new Materialise(args);
	}
	
	
	public Collection<Atom> loadFacts(Schema schema, String table, String csvFile) {
		
		Collection<Atom> facts = Sets.newHashSet();
		int id = 0;
		BufferedReader reader = null;
		int row = 0;

		try {

			//Open the csv file for reading
			reader = new BufferedReader(new FileReader(csvFile));
			String line;
			while ((line = reader.readLine()) != null) {

				if(row > 0) {
					String[] tuple = line.split(",");
					List<Term> constants = Lists.newArrayList();
					for(int i = 0; i < tuple.length; ++i ) {
						constants.add(new Skolem(tuple[i]));
//						if(Character.isUpperCase(tuple[i].charAt(0))) {
//							tuple[i] = "\"" + tuple[i] + "\"";
//						}
					}
					facts.add(new Atom(schema.getRelation(table), constants));
				}
				++row;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return facts;
	}
	
	
	public Collection<EGD> loadEGDs(Schema schema) {
		Term id = new Variable("id");
		Term patient1 = new Variable("patient1");
		Term npi1 = new Variable("npi1");
		Term conf1 = new Variable("conf1");
		
		Term patient2 = new Variable("patient2");
		Term npi2 = new Variable("npi2");
		Term conf2 = new Variable("conf2");
		
		Atom prescription1 = new Atom(schema.getRelation("prescription"), id, patient1, npi1, conf1);
		Atom prescription2 = new Atom(schema.getRelation("prescription"), id, patient2, npi2, conf2);
		
		EGD egd1 = new EGD(Conjunction.of(prescription1, prescription2), Conjunction.<Equality>of(new Equality(patient1, patient2),
				new Equality(npi1, npi2), new Equality(conf1, conf2)));
		
		Term npi = new Variable("npi");
		Term doctor1 = new Variable("doctor1");
		Term spec1 = new Variable("spec1");
		Term hospital1 = new Variable("hospital1");
		
		Term doctor2 = new Variable("doctor2");
		Term spec2 = new Variable("spec2");
		Term hospital2 = new Variable("hospital2");

		Atom doctor1_p = new Atom(schema.getRelation("doctor"), npi, doctor1, spec1, hospital1, conf1);
		Atom doctor2_p = new Atom(schema.getRelation("doctor"), npi, doctor2, spec2, hospital2, conf2);
		
		EGD egd2 = new EGD(Conjunction.of(doctor1_p, doctor2_p), Conjunction.<Equality>of(new Equality(doctor1, doctor2),
				new Equality(spec1, spec2), new Equality(hospital1, hospital2), new Equality(conf1, conf2)));
		
		Term doctor = new Variable("doctor");

		Atom doctor1_pp = new Atom(schema.getRelation("doctor"), npi1, doctor, spec1, hospital1, conf1);
		Atom doctor2_pp = new Atom(schema.getRelation("doctor"), npi2, doctor, spec2, hospital2, conf2);
		
		EGD egd3 = new EGD(Conjunction.of(doctor1_pp, doctor2_pp), Conjunction.<Equality>of(new Equality(npi1, npi2),
				new Equality(spec1, spec2), new Equality(hospital1, hospital2), new Equality(conf1, conf2)));
		
		Term spec = new Variable("spec");
		Term hconf1 = new Variable("hconf1");
		
		Atom target_hospital = new Atom(schema.getRelation("targethospital"), doctor, spec, hospital1, npi1, hconf1);
		Atom doctor_ppp = new Atom(schema.getRelation("doctor"), npi2, doctor, spec, hospital2, conf2);
		
		EGD egd4 = new EGD(Conjunction.of(target_hospital, doctor_ppp), Conjunction.<Equality>of(new Equality(hospital1, hospital2), 
				new Equality(npi1, npi2)));
		
		return Lists.newArrayList(egd1, egd2, egd3, egd4); 
	}
}
