package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.regression.utils.CommonToPDQTranslator;
/**
 * The test case called "Doctors" from the chasebench project, but with an extre condition that decreases the number of results and makes it run a lot faster.
 * <pre>
 * Current (2019) test result (on a laptop):
 *   - case 10k mem:  0.3 second, (reasonUntilTermination took 0.8 seconds) all queries has 1 match except the last two  that has no results.
 *   - case 10k ext:  1.6 s,   all queries has 1 match except the last two  that has no results.
 *   - case 10k logical ext:  3 s,   all queries has 1 match except the last two  that has no results.
 * Current (2018) test result (on a laptop, in memory):
 *   - case 10k :  1.1 second, (reasonUntilTermination took 0.8 seconds) all queries has 1 match except the last two  that has no results.
 *   - case 100k:  6.45 s,   all queries has 1 match except the last two  that has no results.
 *   - case 500k:  38.96s,   all queries has 1 match except the last two  that has no results.
 *   - case 1m  :  138.4s,   all queries has 2 match except the last two  that has no results.
 * Current test result (on a laptop, mysql):
 *   - case 10k :  4 second, (reasonUntilTermination took 1.245 seconds) all queries has 1 match except the last two  that has no results.
 *   - case 100k:  6.45 s,   all queries has 1 match except the last two  that has no results.
 *   - case 500k:  38.96s,   all queries has 1 match except the last two  that has no results.
 *   - case 1m  :  138.4s,   all queries has 2 match except the last two  that has no results.
 * </pre>
 * @author Gabor
 *
 */
public class DoctorsQuick {
	String TEST_DATA[] = {"10k","100k","500k","1m"}; // test data folders;
	String testDataFolder = TEST_DATA[0];
	private Schema s = createSchema();
	@Test
	public void testDoctorsInternalDb() throws DatabaseException, SQLException, IOException {
		DatabaseManager dbm = getInternalDatabaseManager();
		dbm.initialiseDatabaseForSchema(s);
		reasonTest(dbm);
	}
	
	@Test
	public void testDoctorsLogicalDb() throws DatabaseException, SQLException, IOException {
		DatabaseManager dbm = getLogicalDatabaseManager();
		Schema dbSchema = ExplorationSetUp.convertTypesToString(s);
		dbm.initialiseDatabaseForSchema(dbSchema);
		reasonTest(dbm);
	}
	
	@Test
	public void testDoctorsExternalDb() throws DatabaseException, SQLException, IOException {
		DatabaseManager dbm = getExternalDatabaseManager();
		s = convertToStringAttributeOnly(s);
		dbm.initialiseDatabaseForSchema(s);
		reasonTest(dbm);
	}
	private static Schema convertToStringAttributeOnly(Schema s) {
		Relation relations[] = new Relation[s.getRelations().length];
		for (int i = 0; i < relations.length; i++) {
			relations[i] = convertToStringAttributeOnly(s.getRelation(i));
		}
		return new Schema(relations,s.getAllDependencies());
	}

	private static Relation convertToStringAttributeOnly(Relation r) {
		Attribute[] attributes = new Attribute[r.getAttributes().length];
		for (int i = 0; i < attributes.length; i++) {
			if (r.getAttribute(i).getType().equals(String.class)) {
				attributes[i] = r.getAttribute(i);
			} else {
				attributes[i] = Attribute.create(String.class, r.getAttribute(i).getName());
			}
		}
		return Relation.create(r.getName(), attributes,r.getAccessMethods(),r.getForeignKeys(),r.isEquality());
	}

	private void reasonTest(DatabaseManager dbm) throws SQLException, IOException, DatabaseException {
		DatabaseChaseInstance state = new DatabaseChaseInstance(getTestFacts(), dbm);
		RestrictedChaser chaser = new RestrictedChaser();
		long start = System.currentTimeMillis();
		chaser.reasonUntilTermination(state, s.getAllDependencies());
		long duration = System.currentTimeMillis() - start;
		System.out.println("reasonUntilTermination took " + (duration/1000.0) + " seconds.");
		runTestQueries(state);
	}
	
	private void runTestQueries(DatabaseChaseInstance state) throws IOException, DatabaseException {
		Collection<ConjunctiveQuery> queries = getTestQueries();
		int counter = 0;
		for (ConjunctiveQuery q:queries) {
			counter++;
			List<Match> matches = state.getMatches(q, new HashMap<>());
			if (counter == 6 || counter ==9)
				Assert.assertEquals(0, matches.size());
			else 
				Assert.assertEquals(1, matches.size());
			System.out.println(counter + " query:\n\t" + matches);
		}
	}
	
	protected void printStats(Collection<Atom> res) {
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
	private DatabaseManager getInternalDatabaseManager() throws DatabaseException {
		DatabaseManager dbm = new InternalDatabaseManager();
		return dbm;
	}
	private DatabaseManager getExternalDatabaseManager() throws DatabaseException {
		ExternalDatabaseManager dbm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		return dbm;
	}
	private DatabaseManager getLogicalDatabaseManager() throws DatabaseException {
		ExternalDatabaseManager dbm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		return new LogicalDatabaseInstance(new MultiInstanceFactCache(), dbm, 0);
	}

	private Schema createSchema() {

		Relation doctor = Relation.create("doctor", new Attribute[] { Attribute.create(Integer.class, "npi"), Attribute.create(String.class, "doctor"),
				Attribute.create(String.class, "spec"), Attribute.create(String.class, "hospital"), Attribute.create(Double.class, "conf") });
		Relation prescription = Relation.create("prescription", new Attribute[] { Attribute.create(Integer.class, "id"), Attribute.create(String.class, "patient"),
				Attribute.create(Integer.class, "npi"), Attribute.create(Double.class, "conf") });
		Relation targethospital = Relation.create("targethospital", new Attribute[] { Attribute.create(String.class, "doctor"), Attribute.create(String.class, "spec"),
				Attribute.create(String.class, "hospital"), Attribute.create(Integer.class, "npi"), Attribute.create(Double.class, "conf") });
		Relation hospital = Relation.create("hospital", new Attribute[] { Attribute.create(String.class, "doctor"), Attribute.create(String.class, "spec"),
				Attribute.create(String.class, "hospital"), Attribute.create(Integer.class, "npi"), Attribute.create(Double.class, "conf") });

		Relation medprescription = Relation.create("medprescription", new Attribute[] { Attribute.create(Integer.class, "id"), Attribute.create(String.class, "patient"),
				Attribute.create(Integer.class, "npi"), Attribute.create(String.class, "doctor"), Attribute.create(String.class, "spec"), Attribute.create(Double.class, "conf") });

		Relation physician = Relation.create("physician", new Attribute[] { Attribute.create(Integer.class, "npi"), Attribute.create(String.class, "name"),
				Attribute.create(String.class, "spec"), Attribute.create(Double.class, "conf") });
		Relation treatment = Relation.create("treatment", new Attribute[] { Attribute.create(Integer.class, "id"), Attribute.create(String.class, "patient"),
				Attribute.create(String.class, "hospital"), Attribute.create(Integer.class, "npi"), Attribute.create(Double.class, "conf") });

		List<Dependency> dependencies = new ArrayList<>();
		Variable id = Variable.create("id");
		Variable npi = Variable.create("id");
		Variable patient = Variable.create("patient");
		Variable doctorV = Variable.create("doctor");
		Variable hospitalV = Variable.create("hospital");
		Variable conf = Variable.create("conf");
		Variable conf1 = Variable.create("conf1");
		Variable conf2 = Variable.create("conf2");
		Variable C1 = Variable.create("C1");
		Variable C2 = Variable.create("C2");
		Variable name = Variable.create("name");
		Variable spec = Variable.create("spec");
		//TGDs
		dependencies.add(TGD.create(
				//body
				new Atom[] {Atom.create(treatment, new Variable[] {id,patient,hospitalV,npi,conf1}),
				Atom.create(physician, new Variable[] {npi,name,spec,conf2})},
				//head
				new Atom[] {Atom.create(prescription, new Variable[] {id,patient,npi,C1})}));
		
		dependencies.add(TGD.create(
					//body
					new Atom[] {Atom.create(treatment, new Variable[] {id,patient,hospitalV,npi,conf1}),
					Atom.create(physician, new Variable[] {npi,name,spec,conf2})},
					//head
					new Atom[] {Atom.create(doctor, new Variable[] {npi,name,spec,hospitalV,C2})}));

		dependencies.add(TGD.create(
				//body
				new Atom[] {Atom.create(medprescription, new Variable[] {id,patient,npi,doctorV,spec,conf})},
				//head
				new Atom[] {Atom.create(prescription, new Variable[] {id,patient,npi,C1})}));

		dependencies.add(TGD.create(
				//body
				new Atom[] {Atom.create(medprescription, new Variable[] {id,patient,npi,doctorV,spec,conf})},
				//head
				new Atom[] {Atom.create(doctor, new Variable[] {npi,doctorV,spec,hospitalV,C2})}));

			dependencies.add(TGD.create(
					//body
					new Atom[] {Atom.create(hospital, new Variable[] {doctorV,spec,hospitalV,npi,conf})},
					//head
					new Atom[] {Atom.create(targethospital, new Variable[] {doctorV,spec,hospitalV,npi,conf})}));
		
		
		
		//EGDs
		Predicate eq = Predicate.create(QNames.EQUALITY.toString(), 2, true);
		Variable patient1 = Variable.create("patient1");
		Variable patient2 = Variable.create("patient2");
		Variable npi1 = Variable.create("npi1");
		Variable npi2 = Variable.create("npi2");
		Variable hospital1 = Variable.create("hospital1");
		Variable hospital2 = Variable.create("hospital2");
		Variable doctor1 = Variable.create("doctor1");
		Variable doctor2 = Variable.create("doctor2");
		Variable spec1 = Variable.create("spec1");
		Variable spec2 = Variable.create("spec2");
	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(prescription, new Variable[] {id,patient1,npi1,conf1}),
				Atom.create(prescription, new Variable[] {id,patient2,npi2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, patient1, patient2) })
	    		);

	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(prescription, new Variable[] {id,patient1,npi1,conf1}),
				Atom.create(prescription, new Variable[] {id,patient2,npi2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, npi1, npi2) })
	    		);
	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(prescription, new Variable[] {id,patient1,npi1,conf1}),
				Atom.create(prescription, new Variable[] {id,patient2,npi2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, conf1, conf2) })
	    		);
	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(doctor, new Variable[] {npi,doctor1,spec1,hospital1,conf1}),
				Atom.create(doctor, new Variable[] {npi,doctor2,spec2,hospital2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, doctor1, doctor2) })
	    		);
	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(doctor, new Variable[] {npi,doctor1,spec1,hospital1,conf1}),
				Atom.create(doctor, new Variable[] {npi,doctor2,spec2,hospital2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, spec1, spec2) })
	    		);
	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(doctor, new Variable[] {npi,doctor1,spec1,hospital1,conf1}),
				Atom.create(doctor, new Variable[] {npi,doctor2,spec2,hospital2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, hospital1, hospital2) })
	    		);
	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(doctor, new Variable[] {npi1,doctor1,spec1,hospital1,conf1}),
				Atom.create(doctor, new Variable[] {npi2,doctor1,spec2,hospital2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, npi1, npi2) })
	    		);
	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(doctor, new Variable[] {npi,doctor1,spec1,hospital1,conf1}),
				Atom.create(doctor, new Variable[] {npi,doctor2,spec2,hospital2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, conf1, conf2) })
	    		);

	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(targethospital, new Variable[] {doctorV,spec,hospital1,npi1,conf1}),
				Atom.create(doctor, new Variable[] {npi2,doctorV,spec,hospital2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, hospital1, hospital2) })
	    		);
	    dependencies.add(EGD.create(
		    	//body,
				new Atom[] {Atom.create(targethospital, new Variable[] {doctorV,spec,hospital1,npi1,conf1}),
				Atom.create(doctor, new Variable[] {npi2,doctorV,spec,hospital2,conf2})},
	    		//head
				new Atom[] { Atom.create(eq, npi1, npi2) })
	    		);

		return new Schema(new Relation[] {
			doctor,prescription,targethospital,hospital,medprescription,physician,treatment
		},dependencies.toArray(new Dependency[dependencies.size()]));
	}
	private Collection<Atom> getTestFacts() {
		File dataDir = new File("test//chaseBench//doctors//data",testDataFolder);
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
		File dataDir = new File("test//chaseBench//doctors//queries",testDataFolder);
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
