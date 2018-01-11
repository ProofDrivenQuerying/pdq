package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.ParallelEGDChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the reasonUntilTermination method of the ParallelEGDChaser class
 * 
 * Mostly uses the rel1,rel2 and tgd members of PdqTest as input.
 * 
 * @author Gabor
 */
public class TestParallelEGDChaser extends PdqTest {

	public DatabaseChaseInstance state;
	private ParallelEGDChaser chaser;

	protected Schema schema;
	private DatabaseManager connection;

	private static final int NUMBER_OF_DUMMY_DATA = 100;

	@Before
	public void setup() throws Exception {
		super.setup();
		this.schema = new Schema(new Relation[] { this.rel1, this.rel2 }, new Dependency[] { this.tgd });
		this.chaser = new ParallelEGDChaser(new StatisticsCollector(true, new EventBus()));
	}

	/**
	 * Uses an two example relation a TGD and an EGD such a way that after reasoning every fact should contain the constant John such as:
	 * <pre>
	 * R1(k1,c,John), 
	 * R1(k2,c,John), 
	 * R1(k3,c,John), 
	 * R1(k4,c,John), 
	 * R1(k5,c,John), 
	 * 
	 * R2(c,John)
	 * </pre>
	 */
	@Test
	public void test_reasonUntilTermination1() {
		Atom f20 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), UntypedConstant.create("c3") });
		Atom f23 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f24 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });

		try {
			this.state = new DatabaseChaseInstance(Sets.<Atom>newHashSet(f20, f21, f22, f23, f24), createConnection(DatabaseParameters.Derby, this.schema));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		this.chaser.reasonUntilTermination(this.state, new Dependency[] { this.tgd, this.egd });
		Assert.assertEquals(false, this.state.isFailed());

		Atom n00 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom n01 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom n02 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom n03 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom n04 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom n1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });

		Set<Atom> facts = Sets.newHashSet(this.state.getFacts());
		Iterator<Atom> iterator = facts.iterator();
		while (iterator.hasNext()) {
			Atom fact = iterator.next();
			if (fact.isEquality()) {
				iterator.remove();
			}
		}

		Assert.assertEquals(Sets.newHashSet(n00, n01, n02, n03, n04, n1), facts);
	}

	/**
	 * <pre>
	 * Dependencies: 
	 * 	R(z, x) → S(x, y1) ∧ T (x, y2) 
	 * 	R(x,y1) ∧ S(x,y2)→y1 = y2
	 * 
	 * facts of the chase instance: 
	 * 	R(a_{i−1}, a_i) for 1 ≤ i ≤ 10,
	 * 
	 * The chaser should produce nine equality classes each
	 * one having representatives a_2, ... a_10 
	 * 	EQUALITY(a_2,k1), 
	 * 	EQUALITY(a_3,k3),
	 * 	EQUALITY(a_4,k5), 
	 * 	EQUALITY(a_5,k7), 
	 * 	EQUALITY(a_6,k9), 
	 * 	EQUALITY(a_7,k11),
	 * 	EQUALITY(a_8,k13), 
	 * 	EQUALITY(a_9,k15), 
	 * 	EQUALITY(a_10,k17),
	 * 
	 * and the facts 
	 * 	S(a_1,a_2), 
	 *  T(a_1,k2), 
	 *  S(a_2,a_3), 
	 *  T(a_2,k4), 
	 *  S(a_3,a_4),
	 *  T(a_3,k6), 
	 *  S(a_4,a_5), 
	 *  T(a_4,k8), 
	 *  S(a_5,a_6), 
	 *  T(a_5,k10), 
	 *  S(a_6,a_7),
	 *  T(a_6,k12), 
	 *  S(a_7,a_8), 
	 *  T(a_7,k14), 
	 *  S(a_8,a_9), 
	 *  T(a_8,k16), 
	 *  S(a_9,a_10),
	 *  T(a_9,k18), 
	 *  S(a_10,k19), 
	 *  T(a_10,k20)]
	 * </pre>
	 */
	@Test
	public void testA() {

		Relation R = Relation.create("R",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation S = Relation.create("S",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation T = Relation.create("T",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation r[] = new Relation[] { R, S, T };
		Dependency d[] = new Dependency[] {
				TGD.create(new Atom[] { Atom.create(R, Variable.create("z"), Variable.create("x")) },
						new Atom[] { Atom.create(S, Variable.create("x"), Variable.create("y1")), Atom.create(T, Variable.create("x"), Variable.create("y2")) }),
				EGD.create(new Atom[] { Atom.create(R, Variable.create("x"), Variable.create("y1")), Atom.create(S, Variable.create("x"), Variable.create("y2")) },
						new Atom[] { Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("y1"), Variable.create("y2")) }), };
		Schema s = new Schema(r, d);
		List<Atom> facts = new ArrayList<>();
		List<TypedConstant> constants = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			facts.add(Atom.create(R, new Term[] { TypedConstant.create("a_" + (i - 1)), TypedConstant.create("a_" + i) }));
			constants.add(TypedConstant.create("a_" + (i - 1)));
			constants.add(TypedConstant.create("a_" + i));
		}
		try {
			this.state = new DatabaseChaseInstance(facts, createConnection(DatabaseParameters.Derby, s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Schema:" + s);
		Set<Atom> newfacts = Sets.newLinkedHashSet(this.state.getFacts());
		Iterator<Atom> iterator = newfacts.iterator();
		System.out.println("Initial facts:");
		while (iterator.hasNext()) {
			Atom fact = iterator.next();
			System.out.println(fact);
		}
		this.chaser.reasonUntilTermination(this.state, d);
		System.out.println("\n\nAfter resoning:");

		newfacts = Sets.newHashSet(this.state.getFacts());
		iterator = newfacts.iterator();
		List<String> set = new ArrayList<>();
		int equalities = 0;
		int sCount = 0;
		int tCount = 0;
		int countOfKsInS = 0; // should be one
		int countOfKsInT = 0;// should be ten
		while (iterator.hasNext()) {
			Atom fact = iterator.next();
			set.add(fact.toString());
			if (fact.isEquality())
				equalities++;
			if ("S".equals(fact.getPredicate().getName())) {
				sCount++;
				Assert.assertTrue(fact.getTerms()[0] instanceof TypedConstant);
				if (fact.getTerms()[1] instanceof UntypedConstant) {
					if (((UntypedConstant) fact.getTerms()[1]).getSymbol().startsWith("k"))
						countOfKsInS++;
				}
			}
			if ("T".equals(fact.getPredicate().getName())) {
				tCount++;
				Assert.assertTrue(fact.getTerms()[0] instanceof TypedConstant);
				Assert.assertTrue(fact.getTerms()[1] instanceof UntypedConstant);
				if (((UntypedConstant) fact.getTerms()[1]).getSymbol().startsWith("k"))
					countOfKsInT++;
			}
		}
		Assert.assertEquals(9*2, equalities);
		Assert.assertEquals(10, sCount);
		Assert.assertEquals(10, tCount);
		Assert.assertEquals(10, countOfKsInT);
		Assert.assertEquals(1, countOfKsInS);
	}

	/**
	 * <pre>
	 * Dependencies:
	 *	 	C(x) ∧ D(x) → Q(x)
	 *		S(x, y) ∧ D(x) → D(y)
	 *		R(z, x) → S(x, y1) ∧ T (x, y2)
	 *		R(x,y1) ∧ S(x,y2)→y1 = y2
	 *		R(x,y1) ∧ T(x,y2)→y1 = y2
	 * Facts of the chase instance: 
	 *		R(a_{i−1}, a_i) for 1 ≤ i ≤ 10, 
	 *		D(a_1)
	 *		C(a_5)
	 * </pre>
	 * 
	 * The chaser should produces the facts D(a_i) for 1≤i≤10 and the fact Q(a_5).
	 */
	@Test
	public void testB() {
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute") });
		Relation D = Relation.create("D", new Attribute[] { Attribute.create(String.class, "attribute") });
		Relation Q = Relation.create("Q", new Attribute[] { Attribute.create(String.class, "attribute") });
		Relation R = Relation.create("R",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation S = Relation.create("S",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation T = Relation.create("T",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation r[] = new Relation[] { C, D, Q, R, S, T };
		Dependency d[] = new Dependency[] {
				TGD.create(new Atom[] { Atom.create(C, Variable.create("x")), Atom.create(D, Variable.create("x")) }, new Atom[] { Atom.create(Q, Variable.create("x")) }),
				TGD.create(new Atom[] { Atom.create(S, Variable.create("x"), Variable.create("y")), Atom.create(D, Variable.create("x")) },
						new Atom[] { Atom.create(D, Variable.create("y")) }),
				TGD.create(new Atom[] { Atom.create(R, Variable.create("z"), Variable.create("x")) },
						new Atom[] { Atom.create(S, Variable.create("x"), Variable.create("y1")), Atom.create(T, Variable.create("x"), Variable.create("y2")) }),
				EGD.create(new Atom[] { Atom.create(R, Variable.create("x"), Variable.create("y1")), Atom.create(S, Variable.create("x"), Variable.create("y2")) },
						new Atom[] { Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("y1"), Variable.create("y2")) }),
				EGD.create(new Atom[] { Atom.create(R, Variable.create("x"), Variable.create("y1")), Atom.create(T, Variable.create("x"), Variable.create("y2")) },
						new Atom[] { Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("y1"), Variable.create("y2")) }), };
		Schema s = new Schema(r, d);
		Collection<TypedConstant> constants = new ArrayList<>();
		for (int i = 0; i <= 10; i++)
			constants.add(TypedConstant.create("a_" + i));
		List<Atom> facts = new ArrayList<>();
		facts.add(Atom.create(D, new Term[] { TypedConstant.create("a_1") }));
		facts.add(Atom.create(C, new Term[] { TypedConstant.create("a_5") }));
		for (int i = 1; i <= 10; i++)
			facts.add(Atom.create(R, new Term[] { TypedConstant.create("a_" + (i - 1)), TypedConstant.create("a_" + i) }));
		try {
			this.state = new DatabaseChaseInstance(facts, createConnection(DatabaseParameters.Derby, s));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		System.out.println("Schema:" + s);
		Set<Atom> newfacts = Sets.newHashSet(this.state.getFacts());
		Iterator<Atom> iterator = newfacts.iterator();
		System.out.println("Initial facts:");
		while (iterator.hasNext()) {
			Atom fact = iterator.next();
			System.out.println(fact);
		}
		this.chaser.reasonUntilTermination(this.state, d);
		System.out.println("\n\nAfter resoning:");

		newfacts = Sets.newHashSet(this.state.getFacts());
		iterator = newfacts.iterator();
		List<String> set = new ArrayList<>();
		int dFacts = 0;
		int dFactHasK = 0;
		int qFacts = 0;
		while (iterator.hasNext()) {
			Atom fact = iterator.next();
			if (fact.getPredicate().getName().equals(D.getName())) {
				dFacts++;
				if (fact.getTerms()[0].toString().startsWith("k"))
					dFactHasK++;
			}
			if (fact.getPredicate().getName().equals(Q.getName())) {
				qFacts++;
				Assert.assertEquals("a_5", fact.getTerms()[0].toString());
			}
			set.add(fact.toString());
		}
		Assert.assertEquals(11, dFacts);
		Assert.assertEquals(1, dFactHasK);
		Assert.assertEquals(1, qFacts);
	}

	@Test
	public void testA1Derby() throws SQLException {
		testA1(DatabaseParameters.Derby);
	}

	@Test
	public void testA1MySql() throws SQLException {
		testA1(DatabaseParameters.MySql);
	}

	@Test
	public void testA1Postgres() throws SQLException {
		testA1(DatabaseParameters.Postgres);
	}

	/**
	 * Create the following unit tests for getMatches c. conjunctive query is
	 * 
	 * <pre>
	 * Q(x,y) = A(x,x), B(x,y), C(y,z,'TypedConstant1') D(z,z)
	 * </pre>
	 * 
	 * In this unit test we create facts in the database such that two conditions
	 * hold:
	 * <li>each relation has NUMBER_OF_DUMMY_DATA facts in the database and</li>
	 * <li>there are only five answer tuples in the answer. You could do this by
	 * adding junk tuples and manually creating tuples that participate in the
	 * match. In the last unit test the query is boolean so you should only get true
	 * </li>
	 * 
	 * We should get 5 matches.
	 * 
	 * @param dbParam
	 */
	public void testA1(DatabaseParameters dbParam) {

		Relation A = Relation.create("A",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation B = Relation.create("B",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation D = Relation.create("D",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2") });
		Relation E = Relation.create("E", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2") });
		Relation r[] = new Relation[] { A, B, C, D, E };
		Schema s = new Schema(r, new Dependency[0]);

		Collection<TypedConstant> constants = new ArrayList<>();
		for (int i = 1; i <= 5; i++)
			constants.add(TypedConstant.create("x" + i));
		for (int i = 1; i <= 5; i++)
			constants.add(TypedConstant.create("y" + i));
		for (int i = 1; i <= 5; i++)
			constants.add(TypedConstant.create("z" + i));
		constants.add(TypedConstant.create("c_constant_3"));

		List<Atom> facts = new ArrayList<>();
		// producing garbage:
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(A, new Term[] { TypedConstant.create("1g_1_" + i), TypedConstant.create("a_g_2_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("2g_1_" + i), TypedConstant.create("b_g_2_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(C, new Term[] { TypedConstant.create("3g_1_" + i), TypedConstant.create("c_g_2_" + i), TypedConstant.create("g_3a_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(D, new Term[] { TypedConstant.create("4g_1_" + i), TypedConstant.create("d_g_2_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(E, new Term[] { TypedConstant.create("5g_1_" + i), TypedConstant.create("e_g_2_" + i), TypedConstant.create("g_3b_" + i) }));
		// producing results:
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(A, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("x" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("y" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(C, new Term[] { TypedConstant.create("y" + i), TypedConstant.create("z" + i), TypedConstant.create("c_constant_3") }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(D, new Term[] { TypedConstant.create("z" + i), TypedConstant.create("z" + i) }));

		try {
			this.state = new DatabaseChaseInstance(facts, createConnection(dbParam, s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		ConjunctiveQuery query1; // Q(x,y) = A(x,x), B(x,y), C(y,z,'TypedConstant1') D(z,z)
		query1 = ConjunctiveQuery.create(new Variable[] { Variable.create("x"), Variable.create("y") },
				(Conjunction) Conjunction.of(Atom.create(A, Variable.create("x"), Variable.create("x")), Atom.create(B, Variable.create("x"), Variable.create("y")),
						Atom.create(C, Variable.create("y"), Variable.create("z"), TypedConstant.create("c_constant_3")),
						Atom.create(D, Variable.create("z"), Variable.create("z"))));

		List<Match> matches = this.state.getMatches(query1, new HashMap<Variable, Constant>());
		Assert.assertEquals(5, matches.size());
	}

	@Test
	public void testB1Derby() throws SQLException {
		testB1(DatabaseParameters.Derby);
	}

	@Test
	public void testB1MySql() throws SQLException {
		testB1(DatabaseParameters.MySql);
	}

	@Test
	public void testB1Postgres() throws SQLException {
		testB1(DatabaseParameters.Postgres);
	}

	/**
	 * Create the following unit tests for getMatches c. conjunctive query is
	 * 
	 * <pre>
	 * Q(x,y,z) = A('TypedConstant2',y,z,w), B(x,y,z,w), C(y,z,'TypedConstant1') D(x,y), E(x,y,'TypedConstant1')
	 * </pre>
	 * 
	 * In this unit test we create facts in the database such that two conditions
	 * hold:
	 * <li>each relation has NUMBER_OF_DUMMY_DATA facts in the database and</li>
	 * <li>there are only five answer tuples in the answer. You could do this by
	 * adding junk tuples and manually creating tuples that participate in the
	 * match. In the last unit test the query is boolean so you should only get true
	 * </li>
	 * 
	 * We should get 5 matches.
	 * 
	 * @param dbParam
	 */
	public void testB1(DatabaseParameters dbParam) {
		Relation A = Relation.create("A", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2"), Attribute.create(String.class, "attribute3") });
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2"), Attribute.create(String.class, "attribute3") });
		Relation D = Relation.create("D",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2") });
		Relation E = Relation.create("E", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2") });
		Relation r[] = new Relation[] { A, B, C, D, E };
		Schema s = new Schema(r, new Dependency[0]);
		List<Atom> facts = new ArrayList<>();
		// producing garbage:
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(A,
					new Term[] { TypedConstant.create("g1_1_" + i), TypedConstant.create("a_g_2_" + i), TypedConstant.create("cg_3a_" + i), TypedConstant.create("dga" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(B,
					new Term[] { TypedConstant.create("2g_1_" + i), TypedConstant.create("b_g_2_" + i), TypedConstant.create("cgb" + i), TypedConstant.create("dgb" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(C, new Term[] { TypedConstant.create("3g_1_" + i), TypedConstant.create("c_g_2_" + i), TypedConstant.create("g_3a_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(D, new Term[] { TypedConstant.create("4g_1_" + i), TypedConstant.create("d_g_2_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(E, new Term[] { TypedConstant.create("5g_1_" + i), TypedConstant.create("e_g_2_" + i), TypedConstant.create("g_3b_" + i) }));
		// producing results:
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(A, new Term[] { TypedConstant.create("TC2"), TypedConstant.create("y" + i), TypedConstant.create("z" + i), TypedConstant.create("w" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("y" + i), TypedConstant.create("z" + i), TypedConstant.create("w" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(C, new Term[] { TypedConstant.create("y" + i), TypedConstant.create("z" + i), TypedConstant.create("TC1") }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(D, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("y" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(E, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("y" + i), TypedConstant.create("TC1") }));

		try {
			this.state = new DatabaseChaseInstance(facts, createConnection(dbParam, s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		ConjunctiveQuery query1; // Q(x,y,z) = A('TypedConstant2',y,z,w), B(x,y,z,w), C(y,z,'TypedConstant1')
									// D(x,y), E(x,y,'TypedConstant1')
		query1 = ConjunctiveQuery.create(new Variable[] { Variable.create("x"), Variable.create("y"), Variable.create("z") },
				(Conjunction) Conjunction.of(Atom.create(A, TypedConstant.create("TC2"), Variable.create("y"), Variable.create("z"), Variable.create("w")),
						Atom.create(B, Variable.create("x"), Variable.create("y"), Variable.create("z"), Variable.create("w")),
						Atom.create(C, Variable.create("y"), Variable.create("z"), TypedConstant.create("TC1")), Atom.create(D, Variable.create("x"), Variable.create("y")),
						Atom.create(E, Variable.create("x"), Variable.create("y"), TypedConstant.create("TC1"))));

		List<Match> matches = this.state.getMatches(query1, new HashMap<Variable, Constant>());
		Assert.assertEquals(5, matches.size());
	}

	/**
	 * Create the following unit tests for getMatches c. conjunctive query is
	 * 
	 * <pre>
	 * Q = A('TypedConstant2',y,z,w), B(x,y,z,w), C(y,z,'TypedConstant1') D(x,y), E('TypedConstant2',y,y)
	 * </pre>
	 * 
	 * In this unit test we create facts in the database such that two conditions
	 * hold:
	 * <li>each relation has NUMBER_OF_DUMMY_DATA facts in the database and</li>
	 * <li>there are only five answer tuples in the answer. You could do this by
	 * adding junk tuples and manually creating tuples that participate in the
	 * match. In the last unit test the query is boolean so you should only get true
	 * </li>
	 * 
	 * We should get 5 matches.
	 */
	@Test
	public void testC() {
		Relation A = Relation.create("A", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2"), Attribute.create(String.class, "attribute3") });
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2"), Attribute.create(String.class, "attribute3") });
		Relation D = Relation.create("D",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2") });
		Relation E = Relation.create("E", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2") });
		Relation r[] = new Relation[] { A, B, C, D, E };
		Schema s = new Schema(r, new Dependency[0]);
		List<Atom> facts = new ArrayList<>();
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(A,
					new Term[] { TypedConstant.create("a_" + (i - 1)), TypedConstant.create("a_" + i), TypedConstant.create("a_" + i), TypedConstant.create("a_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(B,
					new Term[] { TypedConstant.create("b_" + (i - 1)), TypedConstant.create("b_" + i), TypedConstant.create("b_" + i), TypedConstant.create("b_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(D, new Term[] { TypedConstant.create("d_" + (i - 1)), TypedConstant.create("d_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(C, new Term[] { TypedConstant.create("c_1_" + (i - 1)), TypedConstant.create("c_2_" + (i - 1)), TypedConstant.create("c_3_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(E, new Term[] { TypedConstant.create("e_1_" + (i - 1)), TypedConstant.create("e_2_" + (i - 1)), TypedConstant.create("e_3_" + i) }));

		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(A, new Term[] { TypedConstant.create("TC2"), TypedConstant.create("y" + i), TypedConstant.create("z" + i), TypedConstant.create("w" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("y" + i), TypedConstant.create("z" + i), TypedConstant.create("w" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(C, new Term[] { TypedConstant.create("y" + i), TypedConstant.create("z" + i), TypedConstant.create("TC1") }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(D, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("y" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(E, new Term[] { TypedConstant.create("TC2"), TypedConstant.create("y" + i), TypedConstant.create("y" + i) }));

		try {
			this.state = new DatabaseChaseInstance(facts, createConnection(DatabaseParameters.Postgres, s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		ConjunctiveQuery query1; // Q = A('TypedConstant2',y,z,w), B(x,y,z,w), C(y,z,'TypedConstant1') D(x,y),
									// E('TypedConstant2',y,y)
		query1 = ConjunctiveQuery.create(new Variable[] {},
				(Conjunction) Conjunction.of(Atom.create(A, TypedConstant.create("TC2"), Variable.create("y"), Variable.create("z"), Variable.create("w")),
						Atom.create(B, Variable.create("x"), Variable.create("y"), Variable.create("z"), Variable.create("w")),
						Atom.create(C, Variable.create("y"), Variable.create("z"), TypedConstant.create("TC1")), Atom.create(D, Variable.create("x"), Variable.create("y")),
						Atom.create(E, TypedConstant.create("TC2"), Variable.create("y"), Variable.create("y"))));

		List<Match> matches = this.state.getMatches(query1, new HashMap<Variable, Constant>());
		Assert.assertEquals(5, matches.size());
	}

	private DatabaseManager createConnection(DatabaseParameters params, Schema s) {
		try {
			if (connection!=null) {
				connection.dropDatabase();
				connection.shutdown();
				connection = null;
			}
			ExternalDatabaseManager dm = new ExternalDatabaseManager(params);
			LogicalDatabaseInstance conn = new LogicalDatabaseInstance(new MultiInstanceFactCache(), dm, 1);
			conn.initialiseDatabaseForSchema(s);
			this.connection = conn;
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		return null;
	}

	/**
	 * Shuting this test down.
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (connection!=null) {
			connection.dropDatabase();
			connection.shutdown();
			connection = null;
		}
		if (state!=null) {
			state.close();
			state = null;
		}
	}

	public DatabaseManager getConnection() {
		if (connection == null)
			connection = createConnection(DatabaseParameters.Derby, this.schema);
		return connection;
	}

	/**
	 * Used by {@link TestRestrictedChaserMultiRun}
	 */
	public void setConnection(DatabaseManager connection) {
		this.connection = connection;
	}

	/**
	 * Used by {@link TestRestrictedChaserMultiRun}
	 */
	public void createSchema() {
		this.schema = new Schema(new Relation[] { this.rel1, this.rel2 }, new Dependency[] { this.tgd });
	}

	/**
	 * Used by {@link TestRestrictedChaserMultiRun}
	 */
	public void setup(DatabaseManager c) throws SQLException {

		this.setConnection(c);
		this.chaser = new ParallelEGDChaser(new StatisticsCollector(true, new EventBus()));
	}

}
