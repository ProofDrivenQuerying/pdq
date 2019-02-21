package uk.ac.ox.cs.pdq.test.databasemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseParameters;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * tests the DatabaseManager with 2 different queries over a schema with 3 tables, where each table contains minimum 100 facts.
 * 
 * @author Gabor
 *
 */
public class TestLargeTables extends PdqTest {
	/**
	 * First half of the test: 
	 * <pre>
	 * We have SCHEMA with tables: R(a,b,c) S(b,c) T(b,c,d) each attribute is an
	 * integer attribute.
	 *
	 * First query: exists[x,y](R(x,y,z) & S(x,y)) 
	 * Second query: exists[x,y,z](R(x,y,z) & (S(x,y) & T(z,res1,res2)))
	 * </pre>
	 * 
	 * Second half of the test:
	 * <pre>
	 * We have SCHEMA with tables: R(a,b,c) S(b,c) T(b,c,d) each attribute is an
	 * integer attribute.
	 *
	 * First query: exists[x,y](R(x,y,z) & S(x,y)) 
	 * Second query: exists[x,y,z](R(x,y,z) & (S(x,y) & T(z,res1,res2)))
	 * </pre>
	 * For each query the example data provides one match.
	 * @throws DatabaseException
	 */
	@Test
	public void largeTableTestPostgres() throws DatabaseException {
		largeTableTest(DatabaseParameters.Postgres);
		largeTableTestWithConstantsInQuery(DatabaseParameters.Postgres);
	}

	/**
	 * <pre>
	 * We have SCHEMA with tables: R(a,b,c) S(b,c) T(b,c,d) each attribute is an
	 * integer attribute.
	 *
	 * First query: exists[x,y](R(x,y,z) & S(x,y)) 
	 * Second query: exists[x,y,z](R(x,y,z) & (S(x,y) & T(z,res1,res2)))
	 * </pre>
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	private void largeTableTest(DatabaseParameters parameters) throws DatabaseException {
		ExternalDatabaseManager manager = new ExternalDatabaseManager(parameters);
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R, S, T }));
		List<Atom> facts = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(40000 + i), TypedConstant.create(50000 + i) });
			Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(70000 + i), TypedConstant.create(80000 + i), TypedConstant.create(90000 + i) });
			facts.add(a1);
			facts.add(b1);
			facts.add(c1);
		}
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(14), TypedConstant.create(15) });
		Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(13), TypedConstant.create(14) });
		Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(15), TypedConstant.create(16), TypedConstant.create(17) });
		facts.add(a1);
		facts.add(b1);
		facts.add(c1);

		// ADD
		manager.addFacts(facts);

		// CHECK BACK
		Collection<Atom> getFacts = manager.getFactsFromPhysicalDatabase();
		Set<Atom> expectedSet = new HashSet<>();
		expectedSet.addAll(facts);
		Set<Atom> readSet = new HashSet<>();
		readSet.addAll(getFacts);
		Assert.assertTrue(expectedSet.size() == readSet.size());

		// SIMPLE QUERY
		Atom q1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom q2 = Atom.create(this.S, new Term[] { Variable.create("x"), Variable.create("y") });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { z }, new Atom[] {q1, q2});
		List<Match> answer = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
		Assert.assertEquals(1, answer.size());
		Assert.assertTrue(answer.get(0).getMapping().containsKey(Variable.create("z")));
		if (parameters.getDatabaseDriver().contains("memory")) {
			Assert.assertEquals(TypedConstant.create(15), answer.get(0).getMapping().get(Variable.create("z")));
		} else {
			Assert.assertEquals(UntypedConstant.create("15"), answer.get(0).getMapping().get(Variable.create("z")));
		}

		// COMPLICATED QUERY
		Atom aR = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom aS = Atom.create(this.S, new Term[] { Variable.create("x"), Variable.create("y") });
		Atom aT = Atom.create(this.T, new Term[] { Variable.create("z"), Variable.create("res1"), Variable.create("res2") });
		cq = ConjunctiveQuery.create(new Variable[] { Variable.create("res1"), Variable.create("res2") }, new Atom[] {aR, aS, aT});
		answer = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
		Assert.assertEquals(1, answer.size());
		Assert.assertTrue(answer.get(0).getMapping().containsKey(Variable.create("res1")));
		Assert.assertTrue(answer.get(0).getMapping().containsKey(Variable.create("res2")));
		if (parameters.getDatabaseDriver().contains("memory")) {
			Assert.assertEquals(TypedConstant.create(16), answer.get(0).getMapping().get(Variable.create("res1")));
			Assert.assertEquals(TypedConstant.create(17), answer.get(0).getMapping().get(Variable.create("res2")));
		} else {
			Assert.assertEquals(UntypedConstant.create("16"), answer.get(0).getMapping().get(Variable.create("res1")));
			Assert.assertEquals(UntypedConstant.create("17"), answer.get(0).getMapping().get(Variable.create("res2")));
		}

		// DELETE
		manager.deleteFacts(facts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		manager.dropDatabase();
		manager.shutdown();
	}

	/**
	 * Same as above but the tables have string attributes and the two query looks like:
	 * <pre>
	 * CQ1: 
	 *    []R(10051,y,z)
	 * CQ2:
	 *  exists[x,y,z](R(x,y,z) & (S(13,y) & T(z,res1,res2)))
	 * </pre>
	 * @param parameters
	 * @throws DatabaseException
	 */
	private void largeTableTestWithConstantsInQuery(DatabaseParameters parameters) throws DatabaseException {
		ExternalDatabaseManager manager = new ExternalDatabaseManager(parameters);
		Relation R = Relation.create("R", new Attribute[] { a_s, b_s, c_s }, new AccessMethodDescriptor[] { this.method0, this.method2 });
		Relation S = Relation.create("S", new Attribute[] { b_s, c_s }, new AccessMethodDescriptor[] { this.method0, this.method1, this.method2 });
		Relation T = Relation.create("T", new Attribute[] { b_s, c_s, d_s }, new AccessMethodDescriptor[] { this.method0, this.method1, this.method2 });

		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R, S, T }));
		List<Atom> facts = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(40000 + i), TypedConstant.create(50000 + i) });
			Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(70000 + i), TypedConstant.create(80000 + i), TypedConstant.create(90000 + i) });
			facts.add(a1);
			facts.add(b1);
			facts.add(c1);
		}
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(14), TypedConstant.create(15) });
		Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(13), TypedConstant.create(14) });
		Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(15), TypedConstant.create(16), TypedConstant.create(17) });
		facts.add(a1);
		facts.add(b1);
		facts.add(c1);
		// ADD
		manager.addFacts(facts);

		// CHECK BACK
		Collection<Atom> getFacts = manager.getFactsFromPhysicalDatabase();
		Set<Atom> expectedSet = new HashSet<>();
		expectedSet.addAll(facts);
		Set<Atom> readSet = new HashSet<>();
		readSet.addAll(getFacts);
		Assert.assertEquals(expectedSet, readSet);

		// SIMPLE QUERY
		Atom q1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + 51), Variable.create("y"), Variable.create("z") });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { y, z }, new Atom[] { q1 });
		List<Match> answer = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
		Assert.assertEquals(1, answer.size());
		Assert.assertTrue(answer.get(0).getMapping().containsKey(Variable.create("z")));
		Assert.assertEquals(TypedConstant.create(30051), answer.get(0).getMapping().get(Variable.create("z")));

		// COMPLICATED QUERY
		Atom aR = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom aS = Atom.create(this.S, new Term[] { TypedConstant.create(13), Variable.create("y") });
		Atom aT = Atom.create(this.T, new Term[] { Variable.create("z"), Variable.create("res1"), Variable.create("res2") });
		cq = ConjunctiveQuery.create(new Variable[] { Variable.create("res1"), Variable.create("res2") }, new Atom[] {aR, aS, aT});
		answer = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { cq }));
		Assert.assertEquals(1, answer.size());
		Assert.assertTrue(answer.get(0).getMapping().containsKey(Variable.create("res1")));
		Assert.assertTrue(answer.get(0).getMapping().containsKey(Variable.create("res2")));
		Assert.assertEquals(TypedConstant.create(16), answer.get(0).getMapping().get(Variable.create("res1")));
		Assert.assertEquals(TypedConstant.create(17), answer.get(0).getMapping().get(Variable.create("res2")));

		// DELETE
		manager.deleteFacts(facts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		manager.dropDatabase();
		manager.shutdown();
	}

}
