package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;

import com.google.common.eventbus.EventBus;

// TODO: Auto-generated Javadoc
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
			String q = queries[i];
			String f = facts[i];

			try(FileInputStream sis = new FileInputStream(PATH + s);
					FileInputStream qis = new FileInputStream(PATH + q)) {

				Schema schema = new SchemaReader().read(sis);
				ConjunctiveQuery query = new QueryReader(schema).read(qis);

				if (schema == null || query == null) {
					throw new IllegalStateException("Schema and query must be provided.");
				}
				schema.updateConstants(query.getSchemaConstants());
				RestrictedChaser reasoner = new RestrictedChaser(new StatisticsCollector(true, new EventBus()));

				SQLStatementBuilder builder = new MySQLStatementBuilder();
				HomomorphismManager detector = new DatabaseHomomorphismManager(this.driver, this.url, this.database, this.username, this.password, builder, schema);
				detector.initialize();
				DatabaseChaseState state = new DatabaseChaseState(query, (DatabaseHomomorphismManager) detector);				
				reasoner.reasonUntilTermination(state, schema.getDependencies());
				Collection<Atom> expected = loadFacts(PATH + f, schema);
				if(expected.size() != state.getFacts().size()) {
					System.out.println();
				}
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
		Predicate predicate = schema.getRelation(relationName);
		if(predicate==null) {
			return null;
		}
		String terms = (String) pred.subSequence(index1 + 1, index2);
		List<Term> variables = new ArrayList<>();
		for(String term:terms.split(",")) {
			TypedConstant<?> constant = schema.getConstant(term);
			if(constant != null) {
				variables.add(new TypedConstant<>(constant));
			}
			else {
				variables.add(new UntypedConstant(term));
			}
		}
		return new Atom(predicate, variables);
	}


}