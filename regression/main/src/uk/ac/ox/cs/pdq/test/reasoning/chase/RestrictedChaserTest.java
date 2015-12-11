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

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.xml.QueryReader;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.HomomorphismDetectorTypes;
import uk.ac.ox.cs.pdq.reasoning.ReasoningParameters.ReasoningTypes;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseListState;
import uk.ac.ox.cs.pdq.reasoning.chase.state.ListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DBHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismDetector;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.HomomorphismManagerFactory;

import com.google.common.eventbus.EventBus;

/**
 * Tests the restricted chase implementation
 * @author Efthymia Tsamoura
 *
 */

public class RestrictedChaserTest {

	private EventBus eventBus = new EventBus();

	private static String PATH = "test/restricted_chaser/";
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

	boolean canonicalNames = true;
	String driver = null;
	String url = "jdbc:mysql://localhost/";
	String database = "pdq_chase";
	String username = "root";
	String password ="root";

	@Before
	public void prepare() {
	}

	@Test
	public void test1() {
		this.test(this.schemata1, this.queries1, this.facts1);
	}

//	@Test
//	public void test2() {
//		this.test(this.schemata2, this.queries2, this.facts2);
//	}

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
				RestrictedChaser reasoner = new RestrictedChaser(
						new StatisticsCollector(true, this.eventBus));
				HomomorphismManager detector =
						new HomomorphismManagerFactory().getInstance(
								schema, 
								HomomorphismDetectorTypes.DATABASE,
								this.driver,
								this.url,
								this.database,
								this.username,			
								this.password);
			    detector.addQuery(query);
				ReasoningParameters reasoningParameters = new ReasoningParameters();
				reasoningParameters.setReasoningType(ReasoningTypes.RESTRICTED_CHASE);
				ListState state = new DatabaseListState(null, (DBHomomorphismManager) detector);				
				reasoner.reasonUntilTermination(state, query, schema.getDependencies());
				detector.clearQuery();

				Collection<Predicate> expected = loadFacts(PATH + f, schema);
				
				System.out.println("EXPECTED " + expected);
				System.out.println("ACTUAL " + state.getFacts());
//				Assert.assertEquals(expected, state.getFacts());

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

	private Collection<Predicate> loadFacts(String fileName, Schema schema) {
		Collection<Predicate> predicates = new HashSet<>();
		String line = null;
		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null) {
				Predicate fact = this.loadFact(line, schema);
				if(fact != null) {
					predicates.add(fact);
				}
			}
			bufferedReader.close();    
			return predicates;
		}
		catch(FileNotFoundException ex) {      
			ex.printStackTrace(System.out);
		}
		catch(IOException ex) {
			ex.printStackTrace(System.out);
		}
		return null;
	}

	private Predicate loadFact(String predicate, Schema schema) {		
		int index1 = predicate.indexOf("(");
		int index2 = predicate.indexOf(")");
		String relationName = (String) predicate.subSequence(0, index1);
		Signature signature = schema.getRelation(relationName);
		if(signature==null) {
			return null;
		}
		String terms = (String) predicate.subSequence(index1 + 1, index2);
		List<Term> variables = new ArrayList<>();
		for(String term:terms.split(",")) {
			TypedConstant<?> constant = schema.getConstant(term);
			if(constant != null) {
				variables.add(new TypedConstant<>(constant));
			}
			else {
				variables.add(new Skolem(term));
			}
		}
		return new Predicate(signature, variables);
	}


}