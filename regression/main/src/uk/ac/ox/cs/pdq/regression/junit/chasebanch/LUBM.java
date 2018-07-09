package uk.ac.ox.cs.pdq.regression.junit.chasebanch;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.regression.utils.CommonToPDQTranslator;

/**
 * The test case called "LUBM" from the chasebench project.
 * <pre>
 * Current test result (on a laptop):
 *   - case 001:  timeout
 *   - case 010:  timeout
 *   - case 100:  timeout
 *   - case 01k:  timeout
 * Old PDQ results were: 
 *   - case 001:  timeout
 *   - case 010:  timeout
 *   - case 100:  timeout
 *   - case 01k:  timeout
 * </pre>
 * @author Gabor
 *
 */
public class LUBM {
	String TEST_DATA[] = {"001","010","100", "01k"}; // test data folders;
	String testDataFolder = TEST_DATA[0];
	private Schema s = null;
	Map<String, Relation> relations = new HashMap<>();
	
	@Test
	public void testDoctorsInternalDb() throws DatabaseException, SQLException, IOException {
		s = createSchema();
		DatabaseManager dbm = new InternalDatabaseManager();
		dbm.initialiseDatabaseForSchema(s);
		DatabaseChaseInstance state = new DatabaseChaseInstance(getTestFacts(), dbm);
		Collection<Atom> res = state.getFacts();
		System.out.println("INITIAL STATE contains " + res.size() + " facts.");
		printStats(res);
		RestrictedChaser chaser = new RestrictedChaser();
		long start = System.currentTimeMillis();
		chaser.reasonUntilTermination(state, s.getAllDependencies());
		long duration = System.currentTimeMillis() - start;
		System.out.println("reasonUntilTermination took " + (duration/1000.0) + " seconds.");
		res = state.getFacts();
		System.out.println("Final state contains " + res.size() + " facts.");
		printStats(res);
		runTestQueries(state);
	}
	
	private void runTestQueries(DatabaseChaseInstance state) throws IOException, DatabaseException {
		Collection<ConjunctiveQuery> queries = getTestQueries();
		int counter = 0;
		for (ConjunctiveQuery q:queries) {
			counter++;
			System.out.println(counter + " query:\n\t" + state.getMatches(q, new HashMap<>()));
		}
	}
	
	private void printStats(Collection<Atom> res) {
		Map<String,Integer> dataMap = new HashMap<>();
		for (Atom a: res) {
			String name = a.getPredicate().getName();
			Integer i = dataMap.get(name);
			if (i!=null) {
				dataMap.put(name, i+1);
			} else {
				dataMap.put(name, 1);
			}
		}
		for (String name:dataMap.keySet()) {
			System.out.println(name + "\t\t has \t" + dataMap.get(name) + " facts");
		
		}
	}
	private Schema createSchema() {
		File schemaDir = new File("test//chaseBench//LUBM//schema");
		File dependencyDir = new File("test//chaseBench//LUBM//dependencies");
		Map<String, Relation> tables = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//LUBM.s-schema.txt");
		Map<String, Relation> tables1 = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//LUBM.t-schema.txt");
		relations.putAll(tables);
		relations.putAll(tables1);
		List<Dependency> dependencies = CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//LUBM.st-tgds.txt");
		dependencies.addAll(CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//LUBM.t-tgds.txt"));
		return new Schema(relations.values().toArray(new Relation[relations.size()]), dependencies.toArray(new Dependency[dependencies.size()]));
		
	}
	private Collection<Atom> getTestFacts() {
		File dataDir = new File("test//chaseBench//LUBM//data", testDataFolder);
		Collection<Atom> facts = new ArrayList<>();
		for (File f: dataDir.listFiles()) {
			if (f.getName().endsWith(".csv")) {
				String name = f.getName().substring(0, f.getName().indexOf("."));
				if (s.getRelation(name) == null) {
					System.out.println("Can't process file: "+ f.getAbsolutePath());
				} else {
					facts.addAll(CommonToPDQTranslator.importFacts(s, name, f.getAbsolutePath()));
				}
			}
		}
		return facts;
	}
	private Collection<ConjunctiveQuery> getTestQueries() throws IOException {
		File dataDir = new File("test//chaseBench//LUBM//queries");
		Collection<ConjunctiveQuery> facts = new ArrayList<>();
		Map<String, Relation> relations = new HashMap<>();
		for (Relation r: s.getRelations()) {
			relations.put(r.getName(), r);
		}
		for (File f: dataDir.listFiles()) {
			if (f.getName().endsWith(".txt")) {
				facts.add(CommonToPDQTranslator.parseQuery(relations, f.getAbsolutePath()));
			}
		}
		return facts;
	}

}
