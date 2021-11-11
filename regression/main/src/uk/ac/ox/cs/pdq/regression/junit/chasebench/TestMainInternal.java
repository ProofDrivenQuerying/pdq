// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.regression.junit.chasebench;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.io.CommonToPDQTranslator;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.reasoning.chase.ParallelChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.LogicalDatabaseInstance;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Gabor
 * @contributor Brandon Moore
 * Runs the chaseBanch test cases using the internal memory database.
 */
public class TestMainInternal {

	//filters what file separator to use unix / or windows \\
	private String fileSeparator = System.getProperty("file.separator");
	private LogicalDatabaseInstance dm;

	@Test
	public void testChaseBench_tgds() { 
		System.out.println("TestMain.testChaseBench_tgds()");
		Schema schema = null;
		try {
			schema = IOManager.importSchema(new File("test"+fileSeparator+"chaseBench"+fileSeparator+"tgds"+fileSeparator+"schema.xml"));
		} catch (JAXBException | FileNotFoundException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		Collection<Atom> facts = CommonToPDQTranslator.importFacts(schema, "s", "test"+fileSeparator+"chaseBench"+fileSeparator+"tgds"+fileSeparator+"data"+fileSeparator+"s.csv");

		try {
			DatabaseManager dc = createConnection(DatabaseParameters.Empty, schema, 10);
			DatabaseChaseInstance state = new DatabaseChaseInstance(facts, dc);
			Collection<Atom> res = state.getFacts();
			System.out.println("INITIAL STATE: " + res);

			ParallelChaser chaser = new ParallelChaser();
			chaser.reasonUntilTermination(state, schema.getAllDependencies());
			res = state.getFacts();
			System.out.println("REASONING FAILED:" + state.isFailed());
			System.out.println("WITH FINAL STATE: " + res);

			Assert.assertTrue(!state.isFailed());
			List<Atom> t3Atoms = getAtomsOfTable(res, "t3");
			List<Atom> w2Atoms = getAtomsOfTable(res, "w2");
			Assert.assertEquals(2, t3Atoms.size());
			Assert.assertEquals(2, w2Atoms.size());
			Assert.assertEquals("beta", w2Atoms.get(0).getTerms()[1].toString());
			Assert.assertEquals("beta", w2Atoms.get(1).getTerms()[1].toString());

			for (Atom a : t3Atoms) {
				Assert.assertTrue(a.getTerms()[2].toString().startsWith("k"));
			}
			state.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	private List<Atom> getAtomsOfTable(Collection<Atom> res, String relation) {
		List<Atom> ret = new ArrayList<>();
		for (Atom a : res) {
			if (a.getPredicate().getName().equals(relation)) {
				ret.add(a);
			}
		}
		return ret;
	}

	@Test
	public void testChaseBench_tgds5() {
		System.out.println("TestMain.testChaseBench_tgds5()");
		Schema schema = null;
		try {
			schema = IOManager.importSchema(new File("test"+fileSeparator+"chaseBench"+fileSeparator+"tgds5"+fileSeparator+"schema.xml"));
		} catch (JAXBException | FileNotFoundException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		Collection<Atom> facts0 = CommonToPDQTranslator.importFacts(schema, "s0", "test"+fileSeparator+"chaseBench"+fileSeparator+"tgds5"+fileSeparator+"data"+fileSeparator+"s0.csv");
		Collection<Atom> facts1 = CommonToPDQTranslator.importFacts(schema, "s1", "test"+fileSeparator+"chaseBench"+fileSeparator+"tgds5"+fileSeparator+"data"+fileSeparator+"s1.csv");
		facts0.addAll(facts1);
		try {
			DatabaseManager dc = createConnection(DatabaseParameters.Empty, schema, 10);
			DatabaseChaseInstance state = new DatabaseChaseInstance(facts0, dc);
			Collection<Atom> res = state.getFacts();
			System.out.println("INITIAL STATE: " + res);

			ParallelChaser chaser = new ParallelChaser();
			chaser.reasonUntilTermination(state, schema.getAllDependencies());
			res = state.getFacts();
			System.out.println("REASONING FAILED:" + state.isFailed());
			System.out.println("WITH FINAL STATE: " + res);

			Assert.assertTrue(!state.isFailed());
			List<Atom> t1Atoms = getAtomsOfTable(res, "t1");
			List<Atom> t2Atoms = getAtomsOfTable(res, "t2");
			Assert.assertEquals(16, t1Atoms.size());
			Assert.assertEquals(18, t2Atoms.size());
			int notK = 0;
			for (Atom a : t1Atoms) {
				if (!a.getTerms()[0].toString().startsWith("k") && !a.getTerms()[1].toString().startsWith("k") && !a.getTerms()[2].toString().startsWith("k"))
					notK++;
			}
			Assert.assertEquals(4, notK);
			state.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testChaseBench_tgdsEgds() {
		System.out.println("TestMain.testChaseBench_tgdsEgds()");
		Schema schema = null;
		try {
			schema = IOManager.importSchema(new File("test"+fileSeparator+"chaseBench"+fileSeparator+"tgdsEgds"+fileSeparator+"schema.xml"));
		} catch (JAXBException | FileNotFoundException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		Collection<Atom> facts0 = CommonToPDQTranslator.importFacts(schema, "s", "test"+fileSeparator+"chaseBench"+fileSeparator+"tgdsEgds"+fileSeparator+"data"+fileSeparator+"s.csv");
		try {
			DatabaseManager dc = createConnection(DatabaseParameters.Empty, schema, 10);
			DatabaseChaseInstance state = new DatabaseChaseInstance(facts0, dc);
			Collection<Atom> res = state.getFacts();
			System.out.println("INITIAL STATE: " + res);

			ParallelChaser chaser = new ParallelChaser();
			chaser.reasonUntilTermination(state, schema.getAllDependencies());
			res = state.getFacts();
			System.out.println("REASONING FAILED:" + state.isFailed());
			System.out.println("WITH FINAL STATE: " + res);
			Assert.assertTrue(!state.isFailed());

			List<Atom> w2Atoms = getAtomsOfTable(res, "w2");
			Assert.assertEquals(4, w2Atoms.size());
			int notK = 0;
			for (Atom a : w2Atoms) {
				if (!a.getTerms()[0].toString().startsWith("k") && !a.getTerms()[1].toString().startsWith("k"))
					notK++;
			}
			Assert.assertEquals(0, notK);

			List<Atom> t1Atoms = getAtomsOfTable(res, "t1");
			Assert.assertEquals(6, t1Atoms.size());

			List<Atom> t2Atoms = getAtomsOfTable(res, "t2");
			Assert.assertEquals(6, t2Atoms.size());

			List<Atom> w1Atoms = getAtomsOfTable(res, "w1");
			Assert.assertEquals(4, w1Atoms.size());

			int isK = 0;
			for (Atom a : getAtomsOfTable(res, "t3")) {
				if (a.getTerms()[0].toString().startsWith("k") || a.getTerms()[1].toString().startsWith("k") || a.getTerms()[2].toString().startsWith("k"))
					isK++;
				if (a.getTerms()[0].toString().startsWith("k") || a.getTerms()[1].toString().startsWith("k")) {
					Assert.fail();
				}

			}
			Assert.assertEquals(6, isK);
			state.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testChaseBench_tgdsEgdsLarge() {
		System.out.println("TestMain.testChaseBench_tgdsEgdsLarge()");
		Schema schema = null;
		try {
			schema = IOManager.importSchema(new File("test"+fileSeparator+"chaseBench"+fileSeparator+"tgdsEgdsLarge"+fileSeparator+"schema.xml"));
		} catch (JAXBException | FileNotFoundException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		Collection<Atom> facts0 = CommonToPDQTranslator.importFacts(schema, "s", "test"+fileSeparator+"chaseBench"+fileSeparator+"tgdsEgdsLarge"+fileSeparator+"data"+fileSeparator+"s.csv");
		try {
			DatabaseManager dc = createConnection(DatabaseParameters.Empty, schema, 10);
			DatabaseChaseInstance state = new DatabaseChaseInstance(facts0, dc);
			Collection<Atom> res = state.getFacts();
			System.out.println("INITIAL STATE: " + res);

			ParallelChaser chaser = new ParallelChaser();
			chaser.reasonUntilTermination(state, schema.getAllDependencies());
			res = state.getFacts();
			System.out.println("REASONING FAILED:" + state.isFailed());
			System.out.println("WITH FINAL STATE: " + res);

			List<Atom> w2Atoms = getAtomsOfTable(res, "w2");
			Assert.assertEquals(84, w2Atoms.size());
			int notK = 0;
			for (Atom a : w2Atoms) {
				if (!a.getTerms()[0].toString().startsWith("k") && !a.getTerms()[1].toString().startsWith("k"))
					notK++;
			}
			Assert.assertEquals(0, notK);

			List<Atom> t1Atoms = getAtomsOfTable(res, "t1");
			Assert.assertEquals(166, t1Atoms.size());

			List<Atom> t2Atoms = getAtomsOfTable(res, "t2");
			Assert.assertEquals(1, t2Atoms.size());

			List<Atom> w1Atoms = getAtomsOfTable(res, "w1");
			Assert.assertEquals(164, w1Atoms.size());

			Assert.assertEquals(1, getAtomsOfTable(res, "t3").size());
			int isK = 0;
			for (Atom a : getAtomsOfTable(res, "t3")) {
				if (a.getTerms()[0].toString().startsWith("k") || a.getTerms()[1].toString().startsWith("k") || a.getTerms()[2].toString().startsWith("k"))
					isK++;
				if (a.getTerms()[0].toString().startsWith("k")) {
					Assert.fail();
				}

			}
			Assert.assertEquals(1, isK);
			state.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testChaseBench_vldb2010() {
		System.out.println("TestMain.testChaseBench_vldb2010()");
		Schema schema = null;
		try {
			schema = IOManager.importSchema(new File("test"+fileSeparator+"chaseBench"+fileSeparator+"vldb2010"+fileSeparator+"schema.xml"));
		} catch (JAXBException | FileNotFoundException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		Collection<Atom> facts0 = CommonToPDQTranslator.importFacts(schema, "A", "test"+fileSeparator+"chaseBench"+fileSeparator+"vldb2010"+fileSeparator+"data"+fileSeparator+"A.csv");
		try {
			DatabaseManager dc = createConnection(DatabaseParameters.Empty, schema, 10);
			DatabaseChaseInstance state = new DatabaseChaseInstance(facts0, dc);
			Collection<Atom> res = state.getFacts();
			System.out.println("INITIAL STATE: " + res);

			ParallelChaser chaser = new ParallelChaser();
			chaser.reasonUntilTermination(state, schema.getAllDependencies());
			res = state.getFacts();
			System.out.println("REASONING FAILED:" + state.isFailed());
			System.out.println("WITH FINAL STATE: " + res);
			Assert.assertTrue(!state.isFailed());
			List<Atom> rAtoms = getAtomsOfTable(res, "R");
			Assert.assertEquals(5, rAtoms.size());
			HashSet<String> unique = new HashSet<>();
			for (Atom a : rAtoms) {
				if (a.getTerms()[0].toString().startsWith("k"))
					Assert.fail();
				if (!a.getTerms()[1].toString().startsWith("k"))
					Assert.fail();
				if (!"b".equals(a.getTerms()[0].toString()))
					Assert.assertTrue(unique.add(a.getTerms()[0].toString()));
			}
			state.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testChaseBench_weak() {
		System.out.println("TestMain.testChaseBench_weak()");
		Schema schema = null;
		try {
			schema = IOManager.importSchema(new File("test"+fileSeparator+"chaseBench"+fileSeparator+"weak"+fileSeparator+"schema.xml"));
		} catch (JAXBException | FileNotFoundException e1) {
			e1.printStackTrace();
			Assert.fail();
		}

		Collection<Atom> facts0 = CommonToPDQTranslator.importFacts(schema, "deptemp", "test"+fileSeparator+"chaseBench"+fileSeparator+"weak"+fileSeparator+"data"+fileSeparator+"deptemp.csv");
		try {
			DatabaseManager dc = createConnection(DatabaseParameters.Empty, schema, 10);
			DatabaseChaseInstance state = new DatabaseChaseInstance(facts0, dc);
			Collection<Atom> res = state.getFacts();
			System.out.println("INITIAL STATE: " + res);

			ParallelChaser chaser = new ParallelChaser();
			chaser.reasonUntilTermination(state, schema.getAllDependencies());
			res = state.getFacts();
			System.out.println("REASONING FAILED:" + state.isFailed());
			System.out.println("WITH FINAL STATE: " + res);
			Assert.assertTrue(!state.isFailed());
			List<Atom> deptAtoms = getAtomsOfTable(res, "dept");
			Assert.assertEquals(1, deptAtoms.size());
			List<Atom> empAtoms = getAtomsOfTable(res, "emp");
			Assert.assertEquals(2, empAtoms.size());
			for (Atom a : deptAtoms) {
				if (a.getTerms()[0].toString().startsWith("k"))
					Assert.fail();
				if (!a.getTerms()[1].toString().startsWith("k"))
					Assert.fail();
				if (a.getTerms()[2].toString().startsWith("k"))
					Assert.fail();
			}
			int sumK = 0;
			for (Atom a : empAtoms) {
				if (a.getTerms()[0].toString().startsWith("k") || a.getTerms()[1].toString().startsWith("k"))
					sumK++;
			}
			Assert.assertEquals(1, sumK);
			state.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
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

	private DatabaseManager createConnection(DatabaseParameters params, Schema s, int i) {
		try {
			
			dm = new InternalDatabaseManager();
			dm.initialiseDatabaseForSchema(s);
			return dm;
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Schema convertShemaFiles(String schemaSrc, String schemaTarget, List<String> dependencies) {
		Map<String, Relation> s = CommonToPDQTranslator.parseTables(schemaSrc);
		Map<String, Relation> t = CommonToPDQTranslator.parseTables(schemaTarget);
		s.putAll(t);
		List<Dependency> dependenciesObjects = new ArrayList<>();
		for (String d : dependencies) {
			List<Dependency> deps = CommonToPDQTranslator.parseDependencies(s, d);
			dependenciesObjects.addAll(deps);
		}
		Schema schema = new Schema(s.values().toArray(new Relation[s.values().size()]), dependenciesObjects.toArray(new Dependency[dependenciesObjects.size()]));
		return schema;
	}

}
