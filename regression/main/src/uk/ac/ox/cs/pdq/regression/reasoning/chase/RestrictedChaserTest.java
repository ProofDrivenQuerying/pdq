package uk.ac.ox.cs.pdq.regression.reasoning.chase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

/**
 * Tests the restricted chase implementation.
 *
 * @author Efthymia Tsamoura
 */

public class RestrictedChaserTest {

	/** The path. */
	private static String PATH = "test/restricted_chaser/";

	/** The schemata1. */
	String[] schemata1 = {
			"schema_fk_view.xml",
			"schema_fk_view.xml",
			"schema_fk_view.xml",
			"schema_fk_view.xml",
			"schema_fk_view.xml",
			"schema_fk_view.xml",
			"schema_fk_view.xml",
			"schema_fk_view.xml",
			"schema_fk_view.xml",
			"schema_fk_view.xml"
	};

	/** The schemata2. */
	String[] schemata2 = {
			"schema_demo.xml",
			"schema_demo.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml",
			"schema_bio.xml"
	};

	/** The queries1. */
	String[] queries1 = {
			"query_fk_view_1.xml",
			"query_fk_view_2.xml",
			"query_fk_view_3.xml",
			"query_fk_view_4.xml",
			"query_fk_view_5.xml",
			"query_fk_view_6.xml",
			"query_fk_view_7.xml",
			"query_fk_view_8.xml",
			"query_fk_view_9.xml",
			"query_fk_view_10.xml"
	};

	/** The queries2. */
	String[] queries2 = {
			"query_demo_1.xml",
			"query_demo_2.xml",
			"query_bio_1.xml",
			"query_bio_2.xml",
			"query_bio_3.xml",
			"query_bio_4.xml",
			"query_bio_5.xml",
			"query_bio_6.xml",
			"query_bio_7.xml",
			"query_bio_8.xml",
			"query_bio_9.xml",
			"query_bio_10.xml",
			"query_bio_11.xml",
			"query_bio_12.xml",
			"query_bio_13.xml",
			"query_bio_14.xml",
			"query_bio_15.xml"
	};

	/** The facts1. */
	String[] facts1 = {
			"facts_fk_view_1.txt",
			"facts_fk_view_2.txt",
			"facts_fk_view_3.txt",
			"facts_fk_view_4.txt",
			"facts_fk_view_5.txt",
			"facts_fk_view_6.txt",
			"facts_fk_view_7.txt",
			"facts_fk_view_8.txt",
			"facts_fk_view_9.txt",
			"facts_fk_view_10.txt"
	};

	/** The facts2. */
	String[] facts2 = {
			"facts_demo_1.txt",
			"facts_demo_2.txt",
			"facts_bio_1.txt",
			"facts_bio_2.txt",
			"facts_bio_3.txt",
			"facts_bio_4.txt",
			"facts_bio_5.txt",
			"facts_bio_6.txt",
			"facts_bio_7.txt",
			"facts_bio_8.txt",
			"facts_bio_9.txt",
			"facts_bio_10.txt",
			"facts_bio_11.txt",
			"facts_bio_12.txt",
			"facts_bio_13.txt",
			"facts_bio_14.txt",
			"facts_bio_15.txt"
	};

	/** The canonical names. */
	boolean canonicalNames = true;

	/** The driver. */
	String driver = "com.mysql.jdbc.Driver";

	/** The url. */
	String url = "jdbc:mysql://localhost/";

	/** The database. */
	String database = "pdq_chase";

	/** The username. */
	String username = "root";

	/** The password. */
	String password ="root";

	private LogicalDatabaseInstance dm;

	/**
	 * Test1.
	 */
	@Test
	public void test1() {
		this.test(this.schemata1, this.queries1, this.facts1);
	}

	/**
	 * Test2.
	 */
	@Test
	public void test2() {
		this.test(this.schemata2, this.queries2, this.facts2);
	}

	/**
	 * Test.
	 *
	 * @param schemata the schemata
	 * @param queries the queries
	 * @param facts the facts
	 */
	public void test(String[] schemata, String[] queries, String[] facts) {
		for(int i = 0; i < schemata.length; ++i) {
			String s = schemata[i];
			String f = facts[i];
			try {
				Schema schema = DbIOManager.importSchema(new File(PATH + s));
				ConjunctiveQuery query = IOManager.importQuery(new File(PATH + s));
				if (schema == null || query == null) {
					throw new IllegalStateException("Schema and query must be provided.");
				}
				RestrictedChaser reasoner = new RestrictedChaser(new StatisticsCollector(true, new EventBus()));

				DatabaseManager dbcon = createConnection(DatabaseParameters.Postgres,schema);
				DatabaseChaseInstance state = new DatabaseChaseInstance(query, dbcon);				
				
				reasoner.reasonUntilTermination(state, schema.getAllDependencies());
				Collection<Atom> expected = loadFacts(PATH + f, schema);
				Assert.assertEquals(expected.size(), state.getFacts().size());
			} catch (FileNotFoundException e) {
				System.out.println("Cannot find input files");
			} catch (Exception e) {
				System.out.println("EXCEPTION: " + e.getClass().getSimpleName() + " " + e.getMessage());
				e.printStackTrace();
			} catch (Error e) {
				System.out.println("ERROR: " + e.getClass().getSimpleName() + " " + e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	/**
	 * Load facts.
	 *
	 * @param fileName the file name
	 * @param schema the schema
	 * @return the collection
	 */
	private Collection<Atom> loadFacts(String fileName, Schema schema) {
		Collection<Atom> atoms = new HashSet<>();
		String line = null;
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				Atom fact = this.loadFact(line, schema);
				if(fact != null) {
					atoms.add(fact);
				}
			}
			bufferedReader.close();    
			return atoms;
		}
		catch(FileNotFoundException ex) {      
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
		return null;
	}

	/**
	 * Load fact.
	 *
	 * @param pred the pred
	 * @param schema the schema
	 * @return the pred
	 */
	private Atom loadFact(String pred, Schema schema) {		
		int index1 = pred.indexOf("(");
		int index2 = pred.indexOf(")");
		String relationName = (String) pred.subSequence(0, index1);
		Relation predicate = schema.getRelation(relationName);
		if(predicate==null)
			return null;
		String terms = (String) pred.subSequence(index1 + 1, index2);
		String[] strings = terms.split(",");
		Term[] variables = new Term[strings.length];
		for(int stringIndex = 0; stringIndex < strings.length; ++stringIndex) {
			String term = strings[stringIndex];
			if(term.startsWith("_Typed")) 
				variables[stringIndex] = TypedConstant.deSerializeTypedConstant(term);
			else 
				variables[stringIndex] = UntypedConstant.create(term);
		}
		return Atom.create(predicate, variables);
	}
	@After
	public void tearDown() {
		if (dm!=null) {
			try {
				dm.dropDatabase();
				dm.shutdown();
			} catch (DatabaseException e) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	private DatabaseManager createConnection(DatabaseParameters params, Schema s) {
		try {
			dm = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(params),1);
			dm.initialiseDatabaseForSchema(s);
			return dm;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

}