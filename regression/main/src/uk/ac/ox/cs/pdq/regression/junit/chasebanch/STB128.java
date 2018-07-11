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
 * The test case called "STB-128" from the chasebench project.
 * <pre>
 * Current test result (on a laptop):
 *   - case : Can't parse input files.  
 * Old PDQ results were: 
 *   - case :  timeout
 * </pre>
 * @author Gabor
 *
 */
public class STB128 {
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
		File schemaDir = new File("test//chaseBench//STB-128//schema");
		File dependencyDir = new File("test//chaseBench//STB-128//dependencies");
		Map<String, Relation> tables = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//STB-128.s-schema.txt");
		Map<String, Relation> tables1 = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//STB-128.t-schema.txt");
		relations.putAll(tables);
		relations.putAll(tables1);
		List<Dependency> dependencies = CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//STB-128.st-tgds.txt");
		dependencies.addAll(CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//STB-128.t-tgds.txt"));
		dependencies.addAll(CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//STB-128.t-egds.txt"));
		return new Schema(relations.values().toArray(new Relation[relations.size()]), dependencies.toArray(new Dependency[dependencies.size()]));
		
	}
	private Collection<Atom> getTestFacts() {
		File dataDir = new File("test//chaseBench//STB-128//data");
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
		File dataDir = new File("test//chaseBench//STB-128//queries");
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
