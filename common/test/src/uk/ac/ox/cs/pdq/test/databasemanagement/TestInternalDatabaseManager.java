package uk.ac.ox.cs.pdq.test.databasemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQueryWithInequality;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the creation and basic usages of internal database manager. 
 * 
 * @author Gabor
 *
 */
public class TestInternalDatabaseManager extends PdqTest {

	/**
	 * In this test: Left query: exists[x,y](R(x,y,z) & S(x,y)) Right
	 * query:exists[x,y,z](R(x,y,z) & (S(x,y) & T(z,res1,res2)))
	 * 
	 * The result should be all facts that only satisfy the first query, but not the
	 * second one. With the test data there is only one fact satisfying this.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	@Test
	public void largeTableQueryDifferenceTGD() throws DatabaseException {
		InternalDatabaseManager manager = new InternalDatabaseManager();
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R, S, T }));
		List<Atom> facts = new ArrayList<>();

		// add some disjoint test data
		for (int i = 0; i < 100; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			facts.add(a1);
			if (i < 90) {
				Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(30000 + i), TypedConstant.create(20000 + i), TypedConstant.create(90000 + i) });
				facts.add(c1);
			}
		}

		// add test record that represents a not active dependency
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(14), TypedConstant.create(15) });
		Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(13), TypedConstant.create(14) });
		Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(15), TypedConstant.create(16), TypedConstant.create(17) });
		facts.add(a1);
		facts.add(b1);
		facts.add(c1);
		// add test record that represents an active dependency
		Atom a2 = Atom.create(this.R, new Term[] { TypedConstant.create(113), TypedConstant.create(114), TypedConstant.create(115) });
		Atom b2 = Atom.create(this.S, new Term[] { TypedConstant.create(113), TypedConstant.create(114) });
		facts.add(a2);
		facts.add(b2);
		manager.addFacts(facts);

		// form queries
		Atom q1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom q2 = Atom.create(this.S, new Term[] { Variable.create("x"), Variable.create("y") });
		Atom q3 = Atom.create(this.T, new Term[] { Variable.create("z"), Variable.create("res1"), Variable.create("res2") });
		ConjunctiveQuery left = ConjunctiveQuery.create(new Variable[] { z }, new Atom[] {q1, q2});

		ConjunctiveQuery right = ConjunctiveQuery.create(new Variable[] { Variable.create("res1"), Variable.create("res2") }, new Atom[] {q1, q2, q3});
		// check left and right queries

		List<Match> leftFacts = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { left }));
		Assert.assertEquals(2, leftFacts.size());
		List<Match> rightFacts = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { right }));
		Assert.assertEquals(1, rightFacts.size());
		// Assert.assertNull(rightFacts.get(0).getMapping().get(Variable.create("z")));

		List<Match> diffFacts = manager.answerQueryDifferences(left, right);
		Assert.assertEquals(1, diffFacts.size());
		Assert.assertTrue(diffFacts.get(0).getMapping().containsKey(Variable.create("z")));
		Assert.assertEquals(TypedConstant.create(115), diffFacts.get(0).getMapping().get(Variable.create("z")));
	}

	/**
	 * In this test: Left query: exists[x,y](R(x,y,z) & S(x,y)) Right
	 * query:exists[x,y,z,res2](R(x,y,z) & (S(x,y) & T(res1,res2,z)))
	 * 
	 * The result should be all facts that only satisfy the first query, but not the
	 * second one.
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	@Test
	public void largeTableQueryDifferenceEGD() throws DatabaseException {
		InternalDatabaseManager manager = new InternalDatabaseManager();
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R, S, T }));
		List<Atom> facts = new ArrayList<>();

		// add some disjoint test data
		for (int i = 0; i < 100; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			if (i < 90) {
				Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(30000 + i), TypedConstant.create(20000 + i), TypedConstant.create(90000 + i) });
				facts.add(c1);
			}
			facts.add(a1);
		}

		// add test record that represents a not active dependency
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(14), TypedConstant.create(15) });
		Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(13), TypedConstant.create(14) });
		Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(16), TypedConstant.create(17), TypedConstant.create(15) });
		facts.add(a1);
		facts.add(b1);
		facts.add(c1);
		// add test record that represents an active dependency
		Atom a2 = Atom.create(this.R, new Term[] { TypedConstant.create(113), TypedConstant.create(114), TypedConstant.create(115) });
		Atom b2 = Atom.create(this.S, new Term[] { TypedConstant.create(113), TypedConstant.create(114) });
		Atom c2 = Atom.create(this.T, new Term[] { TypedConstant.create(116), TypedConstant.create(117), TypedConstant.create(215) });
		facts.add(a2);
		facts.add(b2);
		facts.add(c2);
		manager.addFacts(facts);

		// form queries
		Atom q1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom q2 = Atom.create(this.S, new Term[] { Variable.create("x"), Variable.create("y") });
		Atom q3 = Atom.create(this.T, new Term[] { Variable.create("res1"), Variable.create("res2"), Variable.create("z") });
		ConjunctiveQuery left = ConjunctiveQuery.create(new Variable[] { z }, new Atom[] {q1, q2});

		ConjunctiveQuery right = ConjunctiveQuery.create(new Variable[] { Variable.create("res1") }, new Atom[] {q1, q2, q3});
		// check left and right queries
		List<Match> leftFacts = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { left }));
		Assert.assertEquals(2, leftFacts.size());
		List<Match> rightFacts = manager.answerConjunctiveQueries(Arrays.asList(new ConjunctiveQuery[] { right }));
		Assert.assertEquals(1, rightFacts.size());
		Assert.assertNull(rightFacts.get(0).getMapping().get(Variable.create("z")));

		List<Match> diffFacts = manager.answerQueryDifferences(left, right);
		System.out.println(diffFacts);
		Assert.assertEquals(1, diffFacts.size());
		Assert.assertTrue(diffFacts.get(0).getMapping().containsKey(Variable.create("z")));
		Assert.assertEquals(TypedConstant.create(115), diffFacts.get(0).getMapping().get(Variable.create("z")));
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
	@Test
	public void largeTableTest() throws DatabaseException {
		InternalDatabaseManager manager = new InternalDatabaseManager();
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
		Assert.assertEquals(TypedConstant.create(15), answer.get(0).getMapping().get(Variable.create("z")));

		// COMPLICATED QUERY
		Atom aR = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom aS = Atom.create(this.S, new Term[] { Variable.create("x"), Variable.create("y") });
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
		manager.shutdown();
	}

	/**
	 * Same as above but the tables have string attributes and the two query looks
	 * like:
	 * 
	 * <pre>
	 * CQ1: 
	 *    []R(10051,y,z)
	 * CQ2:
	 *  exists[x,y,z](R(x,y,z) & (S(13,y) & T(z,res1,res2)))
	 * </pre>
	 * 
	 * @param parameters
	 * @throws DatabaseException
	 */
	@Test
	public void largeTableTestWithConstantsInQuery() throws DatabaseException {
		InternalDatabaseManager manager = new InternalDatabaseManager();
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
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { y, z }, new Atom[] {q1});
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
		manager.shutdown();
	}
	
	@Test
	public void testQuery() throws DatabaseException {
		InternalDatabaseManager manager = new InternalDatabaseManager();
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R }));
		List<Atom> facts = new ArrayList<>();

		// add some disjoint test data
		for (int i = 0; i < 10; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			facts.add(a1);
		}
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(13), TypedConstant.create(13) });
		Atom a2 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(13), TypedConstant.create(14) });
		Atom a3 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(15), TypedConstant.create(13) });
		facts.add(a1);
		facts.add(a2);
		facts.add(a3);
		manager.addFacts(facts);
		Atom aR = Atom.create(this.R, new Term[] { x, y, z });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { x, y, z }, new Atom[] {aR});
		List<Match> res = manager.answerConjunctiveQuery(cq);
		Assert.assertEquals(13, res.size());
		
		aR = Atom.create(this.R, new Term[] { x, y, x });
		cq = ConjunctiveQuery.create(new Variable[] { x, y }, new Atom[] {aR});
		res = manager.answerConjunctiveQuery(cq);
		
		Assert.assertEquals(2, res.size());
		
	}
	
	@Test
	public void testQuerySelfEquality() throws DatabaseException {
		InternalDatabaseManager manager = new InternalDatabaseManager();
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R }));
		List<Atom> facts = new ArrayList<>();

		// add some disjoint test data
		for (int i = 0; i < 10; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			facts.add(a1);
		}
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(13), TypedConstant.create(13) });
		Atom a2 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(13), TypedConstant.create(14) });
		Atom a3 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(15), TypedConstant.create(13) });
		facts.add(a1);
		facts.add(a2);
		facts.add(a3);
		manager.addFacts(facts);
		Atom aR = Atom.create(this.R, new Term[] { x, y, x });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { x, y }, new Atom[] {aR});
		List<Match> res = manager.answerConjunctiveQuery(cq);
		Assert.assertEquals(2, res.size());
		
		aR = Atom.create(this.R, new Term[] { x, x, y });
		cq = ConjunctiveQuery.create(new Variable[] { x, y }, new Atom[] {aR});
		res = manager.answerConjunctiveQuery(cq);
		
		Assert.assertEquals(2, res.size());
		
	}
	@Test
	public void testQueryInEquality() throws DatabaseException {
		InternalDatabaseManager manager = new InternalDatabaseManager();
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R }));
		List<Atom> facts = new ArrayList<>();

		// add some disjoint test data
		for (int i = 0; i < 10; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			facts.add(a1);
		}
		Atom s1 = Atom.create(this.S, new Term[] { TypedConstant.create(10000), TypedConstant.create(20000)});
		facts.add(s1);
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(13), TypedConstant.create(13) });
		Atom a2 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(13), TypedConstant.create(14) });
		Atom a3 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(15), TypedConstant.create(13) });
		Atom a4 = Atom.create(this.R, new Term[] { TypedConstant.create(2), TypedConstant.create(2), TypedConstant.create(2) });
		facts.add(a1);
		facts.add(a2);
		facts.add(a3);
		facts.add(a4);
		manager.addFacts(facts);
		Atom s = Atom.create(this.S, new Term[] { Variable.create("s1"),Variable.create("s2")});
		Atom aR = Atom.create(this.R, new Term[] { x, x, y});
		List<Pair<Variable,Variable>> inequalities = new ArrayList<>();
		inequalities.add(Pair.of(x, y));
		ConjunctiveQuery cq = ConjunctiveQueryWithInequality.create(new Variable[] { x, y }, new Atom[] {aR,s}, inequalities );
		List<Match> res = manager.answerConjunctiveQuery(cq);
		//Assert.assertEquals(1, res.size());
		
		aR = Atom.create(this.R, new Term[] { x, x, y });
		cq = ConjunctiveQuery.create(new Variable[] { x, y }, new Atom[] {aR,s});
		res = manager.answerConjunctiveQuery(cq);
		
		Assert.assertEquals(3, res.size());
		
	}

}
