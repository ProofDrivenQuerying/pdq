package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;

/**
 * Tests the getMatches and the getTriggers methods of the DatabaseChaseInstance
 * class
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestGetTriggers extends PdqTest {
	private static final int NUMBER_OF_DUMMY_DATA = 100;
	private static final int PARALLEL_THREADS = 10;
	protected DatabaseChaseInstance[] chaseState = new DatabaseChaseInstance[3];
	private final int DERBY = 0;
	private final int MYSQL = 1;
	private final int POSTGRES = 2;

	@Before
	public void setup() throws Exception {
		super.setup();
		for (DatabaseChaseInstance state : chaseState) {
			if (state!=null) state.close();
		}
		DatabaseChaseInstance.resetFacts();
		this.chaseState[DERBY] = new DatabaseChaseInstance(new ArrayList<Atom>(), new DatabaseConnection(DatabaseParameters.Derby, this.testSchema1, PARALLEL_THREADS));
		this.chaseState[MYSQL] = new DatabaseChaseInstance(new ArrayList<Atom>(), new DatabaseConnection(DatabaseParameters.MySql, this.testSchema1, PARALLEL_THREADS));
		this.chaseState[POSTGRES] = new DatabaseChaseInstance(new ArrayList<Atom>(), new DatabaseConnection(DatabaseParameters.Postgres, this.testSchema1, PARALLEL_THREADS));
	}

	@After
	public void tearDown() throws Exception {
		for (DatabaseChaseInstance dci : chaseState)
			dci.close();
	}

	@Test
	public void test_getMatches1Derby() {
		test_getMatches1(chaseState[DERBY]);
	}

	@Test
	public void test_getMatches1MySql() {
		test_getMatches1(chaseState[MYSQL]);
	}

	@Test
	public void test_getMatches1Postgres() {
		test_getMatches1(chaseState[POSTGRES]);
	}

	/**
	 * Uses {@link PdqTest}.testSchema1 as input, creates a set of facts that should
	 * give 6 matches to {@link PdqTest}.tgd
	 * 
	 * @param state
	 */
	public void test_getMatches1(DatabaseChaseInstance state) {
		Atom f20 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), UntypedConstant.create("c3") });
		Atom f23 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f24 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f25 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k6"), UntypedConstant.create("c"), TypedConstant.create(new String("Michael")) });

		state.addFacts(Lists.newArrayList(f20, f21, f22, f23, f24, f25));
		List<Match> matches = state.getTriggers(new Dependency[] { this.tgd }, TriggerProperty.ACTIVE);
		Assert.assertEquals(6, matches.size());
	}

	@Test
	public void test_getMatches2Derby() {
		test_getMatches2(chaseState[DERBY]);
	}

	@Test
	public void test_getMatches2MySql() {
		test_getMatches2(chaseState[MYSQL]);
	}

	@Test
	public void test_getMatches2Postgres() {
		test_getMatches2(chaseState[POSTGRES]);
	}

	/**
	 * Uses {@link PdqTest}.testSchema1 as input, creates a set of facts that should
	 * give 8 matches to {@link PdqTest}.egd
	 * @param state
	 */
	public void test_getMatches2(DatabaseChaseInstance state) {
		Atom f20 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("k"), UntypedConstant.create("c3") });
		Atom f23 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), UntypedConstant.create("c4") });
		Atom f24 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), TypedConstant.create(new String("John")) });
		Atom f25 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), TypedConstant.create(new String("Michael")) });

		state.addFacts(Lists.newArrayList(f20, f21, f22, f23, f24, f25));
		List<Match> matches = state.getTriggers(new Dependency[] { this.egd }, TriggerProperty.ACTIVE);
		Assert.assertEquals(8, matches.size());
	}

	@Test
	public void test_getMatches3Derby() {
		test_getMatches3(chaseState[DERBY]);
	}

	@Test
	public void test_getMatches3MySql() {
		test_getMatches3(chaseState[MYSQL]);
	}

	@Test
	public void test_getMatches3Postgres() {
		test_getMatches3(chaseState[POSTGRES]);
	}

	/**
	 *  Same as test_getMatches2 but the input facts contain some equality already
	 * @param state
	 */
	public void test_getMatches3(DatabaseChaseInstance state) {
		Atom f20 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("k"), UntypedConstant.create("c3") });
		Atom f23 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), UntypedConstant.create("c4") });
		Atom f24 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), TypedConstant.create(new String("John")) });
		Atom f25 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), TypedConstant.create(new String("Michael")) });
		Atom eq1 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2), UntypedConstant.create("c1"), UntypedConstant.create("c2"));
		Atom eq2 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2), UntypedConstant.create("c1"), UntypedConstant.create("c3"));

		state.addFacts(Lists.newArrayList(f20, f21, f22, f23, f24, f25, eq1, eq2));
		List<Match> matches = state.getTriggers(new Dependency[] { this.egd }, TriggerProperty.ALL);
		Assert.assertEquals(8, matches.size());
	}

	/**
	 * Same as getMatches4 but called with all 3 database and repeated 100 times to see if there is any memory issues.
	 * @throws SQLException
	 */
	@Test
	public void test_getMatches4a100timesDerbyMysqlAndPostgres() throws SQLException {
		System.out.print("[");
		for (int i = 0; i < 100; i++) {
			System.out.print(" ");
		}
		System.out.print("]");
		System.out.println("");
		System.out.print("[");
		try {
			for (int i = 0; i < 100; i++) {
				test_getMatches4();
				if (i % 1 == 0)
					System.out.print(".");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.print("]");
		System.out.println("");
	}

	/**
	 * Same as getMatches3 but it loops over all 3 database connections
	 */
	@Test
	public void test_getMatches4() {
		Atom f20 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("k"), UntypedConstant.create("c3") });
		Atom f23 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), UntypedConstant.create("c4") });
		Atom f24 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), TypedConstant.create(new String("John")) });
		Atom f25 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("p"), TypedConstant.create(new String("Michael")) });
		Atom eq1 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2), UntypedConstant.create("c2"), UntypedConstant.create("c1"));
		Atom eq2 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2), UntypedConstant.create("c1"), UntypedConstant.create("c3"));
		for (DatabaseChaseInstance state : chaseState) {
			state.addFacts(Lists.newArrayList(f20, f21, f22, f23, f24, f25, eq1, eq2));
			List<Match> matches = state.getTriggers(new Dependency[] { this.egd }, TriggerProperty.ACTIVE);
			Assert.assertEquals(6, matches.size());
			try {
				this.setup();
				DatabaseChaseInstance.resetFacts();				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void test_getMatches5Derby() {
		test_getMatches5(chaseState[DERBY]);
	}

	@Test
	public void test_getMatches5MySql() {
		test_getMatches5(chaseState[MYSQL]);
	}

	@Test
	public void test_getMatches5Postgres() {
		test_getMatches5(chaseState[POSTGRES]);
	}

	/**
	 * Same as before, but now the database contains some untyped constants
	 * beginning with "c"
	 * 
	 * @param state
	 */
	public void test_getMatches5(DatabaseChaseInstance state) {
		Atom f20 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), UntypedConstant.create("c3") });
		Atom f23 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f24 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f25 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k6"), UntypedConstant.create("c"), TypedConstant.create(new String("Michael")) });
		Atom f26 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f27 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c2") });

		state.addFacts(Lists.newArrayList(f20, f21, f22, f23, f24, f25, f26, f27));
		List<Match> matches = state.getTriggers(new Dependency[] { this.tgd }, TriggerProperty.ACTIVE);
		Assert.assertEquals(4, matches.size());
	}

	@Test
	public void test_getMatches6Derby() {
		test_getMatches6(chaseState[DERBY]);
	}

	@Test
	public void test_getMatches6MySql() {
		test_getMatches6(chaseState[MYSQL]);
	}

	@Test
	public void test_getMatches6Postgres() {
		test_getMatches6(chaseState[POSTGRES]);
	}

	/** One last twist with the input atoms. Still uses the same test schema as the onese before.
	 * @param state
	 */
	public void test_getMatches6(DatabaseChaseInstance state) {
		Atom f20 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("r1"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("r2"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("r3"), UntypedConstant.create("c3") });
		Atom f26 = Atom.create(this.rel3, new Term[] { UntypedConstant.create("r1"), UntypedConstant.create("UntypedConstant1") });
		Atom f27 = Atom.create(this.rel3, new Term[] { UntypedConstant.create("r2"), UntypedConstant.create("UntypedConstant2") });
		state.addFacts(Lists.newArrayList(f20, f21, f22, f26, f27));
		List<Match> matches = state.getTriggers(new Dependency[] { this.tgd2 }, TriggerProperty.ACTIVE);
		Assert.assertEquals(1, matches.size());
	}

	@Test
	public void testScanario2Derby() throws SQLException {
		testScanario2(new DatabaseConnection(DatabaseParameters.Derby, createSchemaScanario2()));
	}

	@Test
	public void testScanario2MySql() throws SQLException {
		testScanario2(new DatabaseConnection(DatabaseParameters.MySql, createSchemaScanario2()));
	}

	@Test
	public void testScanario2Postgres() throws SQLException {
		testScanario2(new DatabaseConnection(DatabaseParameters.Postgres, createSchemaScanario2()));
	}

	private Schema createSchemaScanario2() {
		Relation A = Relation.create("A",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"), Attribute.create(Integer.class, "InstanceID") });
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2"), Attribute.create(Integer.class, "InstanceID") });
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(Integer.class, "InstanceID") });
		Relation D = Relation.create("D",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"), Attribute.create(Integer.class, "InstanceID") });
		Relation r[] = new Relation[] { A, B, C, D };
		Schema s = new Schema(r, new Dependency[0]);
		return s;
	}

	/**
	 * 
	 * Create unit tests for the getTriggers method
	 * 
	 * 
	 * a. The dependency is A(x,y), B(y,y,'TypedConstant1'), C(y) ->
	 * D('TypedConstant2', z) the chase instance has the facts A(c_1,c_2) A(c_2,c_2)
	 * A(c_2,c_3) A(c_3,c_3) A(c_4,c_5)
	 * 
	 * B(c_2, c_2, 'TypedConstant1') B(c_2, c_3, 'TypedConstant1') B(c_3, c_3,
	 * 'TypedConstant1') B(c_3, c_3, 'TypedConstant2') B(c_4, c_5, 'TypedConstant2')
	 * B(c_i, c_{i+1}, 'TypedConstant2') i=6,...,10000 C(c_i), i=1,...,10000
	 * 
	 * You should assert that there are returned four matches in total. General
	 * guidlines: Please try all the unit tests for all database instances. And
	 * please do not load any facts or dependencies for a csv file. Just create them
	 * in place. Maybe using a for loop.
	 * 
	 * @param dc
	 */
	public void testScanario2(DatabaseConnection dc) {
		try {
			Relation A = dc.getSchema().getRelation("A");
			Relation B = dc.getSchema().getRelation("B");
			Relation C = dc.getSchema().getRelation("C");
			Relation D = dc.getSchema().getRelation("D");
			List<Atom> facts = new ArrayList<>();
			for (int i = 2; i <= 5; i++)
				facts.add(Atom.create(A, new Term[] { TypedConstant.create("c_" + (i - 1)), TypedConstant.create("c_" + i) }));
			facts.add(Atom.create(A, new Term[] { TypedConstant.create("c_2"), TypedConstant.create("c_2") }));
			facts.add(Atom.create(A, new Term[] { TypedConstant.create("c_3"), TypedConstant.create("c_3") }));
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("c_2"), TypedConstant.create("c_2"), TypedConstant.create("TC1") }));
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("c_2"), TypedConstant.create("c_3"), TypedConstant.create("TC1") }));
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("c_3"), TypedConstant.create("c_3"), TypedConstant.create("TC1") }));
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("c_3"), TypedConstant.create("c_3"), TypedConstant.create("TC2") }));
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("c_4"), TypedConstant.create("c_5"), TypedConstant.create("TC2") }));
			for (int i = 6; i <= NUMBER_OF_DUMMY_DATA; i++)
				facts.add(Atom.create(B, new Term[] { TypedConstant.create("c_" + i), TypedConstant.create("c_" + (i + 1)), TypedConstant.create("TC2") }));
			for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
				facts.add(Atom.create(C, new Term[] { TypedConstant.create("c_" + i) }));

			DatabaseChaseInstance state = new DatabaseChaseInstance(facts, dc);
			// A(x,y), B(y,y,'TypedConstant1'), C(y) -> D('TypedConstant2', z)
			Dependency d[] = new Dependency[] { TGD.create(
					new Atom[] { Atom.create(A, Variable.create("x"), Variable.create("y")),
							Atom.create(B, Variable.create("y"), Variable.create("y"), TypedConstant.create("TC1")), Atom.create(C, Variable.create("y")) },
					new Atom[] { Atom.create(D, TypedConstant.create("TC2"), Variable.create("z")) }) };
			List<Match> matches = state.getTriggers(d, TriggerProperty.ACTIVE);
			Assert.assertFalse(matches.isEmpty());
			Assert.assertEquals(4, matches.size());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
