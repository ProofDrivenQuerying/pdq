package uk.ac.ox.cs.pdq.reasoning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.reasoning.chase.Chaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.cache.FactCache;

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
	@Parameter(names = { "-ca",
	"--ca" }, required = false, description = "Certain answers only.")
	private boolean caOnly = false;
	
	@Parameter(names = { "-o", "--output" }, required = false,
			description ="Path to the output csv file.")
	private File output;

	@DynamicParameter(names = "-D", description = "Dynamic parameters. Override values defined in the configuration files.")
	protected Map<String, String> dynamicParams = new LinkedHashMap<>();
	
	/**
	 * Run with --help for options
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		try {
			new Reason(args);
		}catch(Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Initialize the Reason class by reading command line parameters, and running the
	 * resoner on them.
	 * 
	 * @param args
	 *            String[]
	 * @throws DatabaseException 
	 */
	public Reason(String... args) throws DatabaseException {
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
	 * @throws DatabaseException 
	 */
	public void run() throws DatabaseException {
		long start = System.currentTimeMillis();
		ReasoningParameters reasoningParams = this.getConfigFile() != null
				? new ReasoningParameters(this.getConfigFile())
				: new ReasoningParameters();


		for (String k : this.dynamicParams.keySet()) {
			reasoningParams.set(k, this.dynamicParams.get(k));
		}
		DatabaseManager manager = null;
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
			
			if (dbParams.getUseInternalDatabaseManager()) {
				manager = new InternalDatabaseManager();
			} else  {
				schema = convertTypesToString(schema);
				manager = new ExternalDatabaseManager(dbParams);
				if (query !=null) {
					query = convertQueryConstantsToString(query);
				}
			}
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
			System.out.println("Reasoning starts on " + this.getSchemaPath());
			reasoner.reasonUntilTermination(state, schema.getAllDependencies());
			System.out.println("Reasoning results generated in " + (System.currentTimeMillis() - start)/1000.0 + " sec.");
			Collection<Atom> results = null;
			
			if (caOnly) {
				results = new ArrayList<>();
				for (Atom a: state.getFacts()) {
					boolean hasLabelledNull = false;
					for (Term t:a.getTerms()) {
						if (t.isUntypedConstant() && ((UntypedConstant)t).isNonCannonicalConstant()) {
							hasLabelledNull = true;
						}
					}
					if (!hasLabelledNull) {
						results.add(a);
					}
				}
			} else {
				results = state.getFacts();	
			}
			if (verbose) {
				for (Atom a: results)
					System.out.println(a);
			}
			if (output!=null) {
				writeOutput(results,schema);
			}
		} catch (Throwable e) {
			log.error("Reasoning aborted: " + e.getMessage(), e);
			System.exit(-1);
		} finally {
			if (manager!=null) {
				manager.dropDatabase();
				manager.shutdown();
			}
		}
	}
	
	
	private void writeOutput(Collection<Atom> results, Schema schema) throws IOException {

		FactCache fc = new FactCache(0);
		fc.addFacts(results);
		for (Relation r: schema.getRelations()) {
			List<Atom> data = fc.getFactsOfRelation(r.getName());
			if (!data.isEmpty()) {
				if (output.isDirectory()) {
					writeOutput(new File(output, r.getName()+".csv"),data,r.getAttributes());
				} else {
					writeOutput(new File(output.getParentFile(), r.getName()+".csv"),data,r.getAttributes());
				}
			}
		}
	}
	private void writeOutput(File output, List<Atom> results, Attribute[] attributes) throws IOException {
		try (FileWriter fw = new FileWriter(output,true)) {
			for(Atom a:results) {
				StringBuilder builder = null;
				int attributeCounter = 0;
				for (Term value : a.getTerms()) {
					if (builder == null) {
						builder = new StringBuilder();
					} else {
						builder.append(",");
					}
					if (attributes[attributeCounter].getType().equals(String.class))
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
	
	public static Schema convertTypesToString(Schema schema) {
		List<Dependency> dep = new ArrayList<>();
		dep.addAll(Arrays.asList(schema.getNonEgdDependencies()));
		dep.addAll(Arrays.asList(schema.getKeyDependencies()));
		Relation[] rels = schema.getRelations();
		for (int i = 0; i < rels.length; i++) {
			rels[i] = createDatabaseRelation(rels[i]);
		}
		return new Schema(rels,dep.toArray(new Dependency[dep.size()]));
	}
	/**
	 * Creates the db relation. Currently codes in the position numbers into the
	 * names, but this should change
	 *
	 * @param relation
	 *            the relation
	 * @return a new database relation with attributes x0,x1,...,x_{N-1}, Fact where
	 *         x_i maps to the i-th relation's attribute
	 */
	private static Relation createDatabaseRelation(Relation relation) {
		Attribute[] attributes = new Attribute[relation.getArity()];
		for (int index = 0; index < relation.getArity(); index++) {
			Attribute attribute = relation.getAttribute(index);
			attributes[index] = Attribute.create(String.class, attribute.getName());
		}
		return Relation.create(relation.getName(), attributes, relation.getAccessMethods(), relation.isEquality());
	}

	/** Converts all constants of the query to strings
	 * @param query
	 * @return
	 */
	private ConjunctiveQuery convertQueryConstantsToString(ConjunctiveQuery query) {
		
		Formula newAtom = convertQueryAtomConstantToString(query.getBody());
		if (newAtom instanceof Atom) {
			return ConjunctiveQuery.create(query.getFreeVariables(), new Atom[] {(Atom)newAtom});
		} else {
			return ConjunctiveQuery.create(query.getFreeVariables(), ((Conjunction)newAtom).getAtoms());
		}
	}

	/** converts all constants to strings.
	 * @param body
	 * @return
	 */
	private Formula convertQueryAtomConstantToString(Formula body) {
		if (body instanceof Atom) { 
			Term terms[] = body.getTerms();
			for (int i = 0; i < terms.length; i++) {
				if (terms[i] instanceof TypedConstant) {
					terms[i] = TypedConstant.create("" + ((TypedConstant)terms[i]).value);
				}
			}
			return Atom.create(((Atom) body).getPredicate(), terms);
		} else {
			Formula left = ((Conjunction)body).getChildren()[0];
			Formula right = ((Conjunction)body).getChildren()[1];
			return Conjunction.create(convertQueryAtomConstantToString(left),convertQueryAtomConstantToString(right));
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

	public boolean isCaOnly() {
		return caOnly;
	}

	public void setCaOnly(boolean caOnly) {
		this.caOnly = caOnly;
	}
}
