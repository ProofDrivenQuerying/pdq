package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.regression.utils.CommonToPDQTranslator;

/**
 * The test case called "DoctorsFD" from the chasebench project.
 * @author Gabor
 *
 */
public class DoctorsFD {
	String TEST_DATA[] = {"10k","100k","500k","1m"}; // test data folders;
	String testDataFolder = TEST_DATA[0];
	private Schema s = null;
	Map<String, Relation> relations = new HashMap<>();
	@Test 
	public void testDoctorsInternalDb() throws DatabaseException, SQLException, IOException {
		s = ExplorationSetUp.convertTypesToString(createSchema());
		
		System.out.println("MEMORY");
		DatabaseManager dbm = new InternalDatabaseManager();
		dbm.initialiseDatabaseForSchema(s);
		DatabaseChaseInstance state = new DatabaseChaseInstance(getTestFacts(), dbm);
		Collection<Atom> res = state.getFacts();
		System.out.println("INITIAL STATE contains " + res.size() + " facts.");
		RestrictedChaser chaser = new RestrictedChaser();
		chaser.reasonUntilTermination(state, s.getNonEgdDependencies());
		res = state.getFacts();
		System.out.println("Final state contains " + res.size() + " facts." + new HashSet<>(res).size() + " unique.");
		
		System.out.println("EXTERNAL");
		DatabaseManager dbmExt = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		dbmExt.initialiseDatabaseForSchema(s);
		DatabaseChaseInstance stateExt = new DatabaseChaseInstance(getTestFacts(), dbmExt);
		Collection<Atom> resExt = stateExt.getFacts();
		System.out.println("INITIAL STATE contains " + resExt.size() + " facts.");
		RestrictedChaser chaserExt = new RestrictedChaser();
		chaserExt.reasonUntilTermination(stateExt, s.getNonEgdDependencies());
		resExt = stateExt.getFacts();
		System.out.println("Final state contains " + resExt.size() + " facts." + new HashSet<>(resExt).size() + " unique.");

		if (res.size() > resExt.size()) {
			System.out.println("There are more facts in mem.");
			Assert.fail("There are more facts in mem.");
		} else {
			if (res.size() < resExt.size()) {
				System.out.println("There are more facts in Ext.");
				Assert.fail("There are more facts in Ext.");
			} else {				
				System.out.println("There are no difference in the number of facts.");
			}
		}
		Assert.assertTrue(res.size()>1000);
	}
	
	private Schema createSchema() {
		File schemaDir = new File("test//chaseBench//doctors//schema//");
		File dependencyDir = new File("test//chaseBench//doctors//dependencies");
		Map<String, Relation> tables = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//doctors-fd.s-schema.txt");
		Map<String, Relation> tables1 = CommonToPDQTranslator.parseTables(schemaDir.getAbsolutePath() + "//doctors-fd.t-schema.txt");
		relations.putAll(tables);
		relations.putAll(tables1);
		List<Dependency> dependencies = CommonToPDQTranslator.parseDependencies(relations, dependencyDir .getAbsolutePath() + "//doctors-fd.st-tgds.txt");
		return new Schema(relations.values().toArray(new Relation[relations.size()]), dependencies.toArray(new Dependency[dependencies.size()]));
		
	}
	private Collection<Atom> getTestFacts() {
		File dataDir = new File("test//chaseBench//doctors//data//" + testDataFolder + "//");
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

}
