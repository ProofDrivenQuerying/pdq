package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

/**
 * Tests the reasonUntilTermination method of the RestrictedChaser class
 * 
 * @author Gabor
 *
 */
public class TestAddingEqualities {

	public DatabaseChaseInstance state;
	private RestrictedChaser chaser;

	private Relation rel1;
	private Relation rel2;

	private TGD tgd;
	private EGD egd;

	protected Schema schema;
	private DatabaseConnection connection;

	@Before
	public void setup() throws SQLException {
		createSchema();
		setup(new DatabaseConnection(new DatabaseParameters(), schema));
	}

	public void createSchema() {
		Attribute fact = Attribute.create(Integer.class, "InstanceID");

		Attribute at11 = Attribute.create(String.class, "at11");
		Attribute at12 = Attribute.create(String.class, "at12");
		Attribute at13 = Attribute.create(String.class, "at13");

		this.rel1 = Relation.create("R1", new Attribute[] { at11, at12, at13, fact });

		Attribute at21 = Attribute.create(String.class, "at21");
		Attribute at22 = Attribute.create(String.class, "at22");
		this.rel2 = Relation.create("R2", new Attribute[] { at21, at22, fact });

		Atom R1 = Atom.create(this.rel1, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom R2 = Atom.create(this.rel2, new Term[] { Variable.create("y"), Variable.create("z") });
		Atom R2p = Atom.create(this.rel2, new Term[] { Variable.create("y"), Variable.create("w") });

		this.tgd = TGD.create(new Atom[] { R1 }, new Atom[] { R2 });
		this.egd = EGD.create(new Atom[]{R2, R2p},
				new Atom[]{Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("z"), Variable.create("w"))});

		this.schema = new Schema(new Relation[] { this.rel1, this.rel2 }, new Dependency[] { this.tgd });
		this.schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(new String("John"))));
	}

	public void setup(DatabaseConnection c) throws SQLException {

		this.setConnection(c);
		this.chaser = new RestrictedChaser(new StatisticsCollector(true, new EventBus()));
		Atom f20 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), UntypedConstant.create("c1") });

		Atom f21 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), UntypedConstant.create("c2") });

		Atom f22 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), UntypedConstant.create("c3") });

		Atom f23 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), UntypedConstant.create("c4") });

		Atom f24 = Atom.create(this.rel1, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });

		try {
			this.state = new DatabaseChaseInstance(Sets.<Atom>newHashSet(f20, f21, f22, f23, f24), this.getConnection());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	public void test_scenario1() {
		Predicate equality = Predicate.create("name", 2, true);
		Map<Constant, Constant> ret1 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("t1"), UntypedConstant.create("t2")));
		Constant representative1 = ret1.get(ret1.keySet().iterator().next());
		checkResults(ret1, 1, representative1);
		Map<Constant, Constant> ret2 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("t2"), UntypedConstant.create("t3")));
		checkResults(ret2, 1, representative1);
		Map<Constant, Constant> ret3 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("t3"), UntypedConstant.create("t4")));
		checkResults(ret3, 1, representative1);
		Map<Constant, Constant> ret4 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("t4"), UntypedConstant.create("t6")));
		checkResults(ret4, 1, representative1);
		Map<Constant, Constant> ret5 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("t10"), UntypedConstant.create("t11")));
		Constant representative2 = ret5.get(ret5.keySet().iterator().next());
		checkResults(ret5, 1, representative2);
		Map<Constant, Constant> ret6 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("t11"), UntypedConstant.create("t13")));
		checkResults(ret6, 1, representative2);
		Map<Constant, Constant> ret6b = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("t3"), UntypedConstant.create("t13")));
		checkResults(ret6b, 3, representative1);
		TypedConstant representative3 = TypedConstant.create(new String("John"));
		Map<Constant, Constant> ret7 = this.state.updateEqualConstantClasses(Atom.create(equality, representative3, UntypedConstant.create("t1")));
		checkResults(ret7, 8, representative3);
		Map<Constant, Constant> ret8 = this.state.updateEqualConstantClasses(Atom.create(equality, TypedConstant.create(new String("John")), UntypedConstant.create("t11")));
		checkResults(ret8, 0, null);
		Assert.assertTrue(!this.state.isFailed());
	}

	@Test
	public void test_scenario2() {
		Predicate equality = Predicate.create("name", 2, true);
		TypedConstant representative3 = TypedConstant.create(new String("John"));
		this.state.updateEqualConstantClasses(Atom.create(equality, representative3, UntypedConstant.create("t1")));
		this.state.updateEqualConstantClasses(Atom.create(equality, TypedConstant.create(new String("John1")), UntypedConstant.create("t1")));
		Assert.assertTrue(this.state.isFailed());
		System.out.println();
	}

	// @Test(expected =IllegalArgumentException.class)
	@Test
	public void test_scenario3() {
		Predicate equality = Predicate.create("name", 2, true);
		TypedConstant representative3 = TypedConstant.create(new String("John"));
		this.state.updateEqualConstantClasses(Atom.create(equality, representative3, TypedConstant.create(new String("John1"))));
		Assert.assertTrue(this.state.isFailed());
	}

	@Test
	public void test_scenario4() {
		Predicate equality = Predicate.create("name", 2, true);
		TypedConstant representative3 = TypedConstant.create(new String("John"));
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a" + i), UntypedConstant.create("a" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("a0"));
		}
		Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a20"), representative3));
		Assert.assertTrue(!this.state.isFailed());
		checkResults(ret, 21, representative3);
	}

	@Test
	public void test_scenario5() {
		Predicate equality = Predicate.create("name", 2, true);
		TypedConstant representative3 = TypedConstant.create(new String("John"));
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a" + i), UntypedConstant.create("a" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("a0"));
		}
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("b" + i), UntypedConstant.create("b" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("b0"));
		}
		Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a0"), representative3));
		checkResults(ret, 21, representative3);
		Map<Constant, Constant> ret2 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a0"), UntypedConstant.create("b0")));
		checkResults(ret2, 21, representative3);
		Assert.assertTrue(!this.state.isFailed());
	}

	@Test
	public void test_scenario6() {
		Predicate equality = Predicate.create("name", 2, true);
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a" + i), UntypedConstant.create("a" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("a0"));
		}
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("b" + i), UntypedConstant.create("b" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("b0"));
		}
		Map<Constant, Constant> ret2 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a0"), UntypedConstant.create("b0")));
		checkResults(ret2, 21, UntypedConstant.create("a0"));
		TypedConstant representative3 = TypedConstant.create(new String("John"));
		Map<Constant, Constant> ret3 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a0"), representative3));
		checkResults(ret3, 42, representative3);
		Assert.assertTrue(!this.state.isFailed());
	}

	@Test
	public void test_scenario7() {
		Predicate equality = Predicate.create("name", 2, true);
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a" + i), UntypedConstant.create("a" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("a0"));
		}
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("b" + i), UntypedConstant.create("b" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("b0"));
		}
		Map<Constant, Constant> ret2 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a10"), UntypedConstant.create("b10")));
		checkResults(ret2, 21, UntypedConstant.create("a0"));
		Assert.assertTrue(!this.state.isFailed());
	}

	@Test
	public void test_scenario8() {
		Predicate equality = Predicate.create("name", 2, true);
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a" + i), UntypedConstant.create("a" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("a0"));
		}
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("b" + i), UntypedConstant.create("b" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("b0"));
		}
		Map<Constant, Constant> ret2 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a0"), UntypedConstant.create("b10")));
		checkResults(ret2, 21, UntypedConstant.create("a0"));
		Assert.assertTrue(!this.state.isFailed());
	}

	@Test
	public void test_scenario9() {
		Predicate equality = Predicate.create("name", 2, true);
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a" + i), UntypedConstant.create("a" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("a0"));
		}
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("b" + i), UntypedConstant.create("b" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("b0"));
		}
		Map<Constant, Constant> ret2 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a10"), UntypedConstant.create("b0")));
		checkResults(ret2, 21, UntypedConstant.create("a0"));
		Assert.assertTrue(!this.state.isFailed());
	}
	
	@Test
	public void test_scenario10() {
		Predicate equality = Predicate.create("name", 2, true);
		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a" + i), UntypedConstant.create("a" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("a0"));
		}
		TypedConstant representative3 = TypedConstant.create(new String("John"));		
		Map<Constant, Constant> ret3 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a0"), representative3));
		checkResults(ret3, 21, representative3);

		for (int i = 0; i < 20; i++) {
			Map<Constant, Constant> ret = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("b" + i), UntypedConstant.create("b" + (i + 1))));
			checkResults(ret, 1, UntypedConstant.create("b0"));
		}
		
		TypedConstant representative = TypedConstant.create(new String("John2"));		
		Map<Constant, Constant> ret4 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("b0"), representative));
		checkResults(ret4, 21, representative);
		
		
		Map<Constant, Constant> ret2 = this.state.updateEqualConstantClasses(Atom.create(equality, UntypedConstant.create("a10"), UntypedConstant.create("b0")));
		checkResults(ret2, 0, null);
		Assert.assertTrue(this.state.isFailed());
	}

	private void checkResults(Map<Constant, Constant> ret1, int numberOfelements, Constant representative) {
		Assert.assertNotNull(ret1);
		Assert.assertEquals(numberOfelements, ret1.size());
		for (Constant c : ret1.keySet()) {
			Assert.assertEquals(representative, ret1.get(c));
		}
	}

	public void tearDown() throws Exception {
		getConnection().close();
		state.close();
	}

	public DatabaseConnection getConnection() {
		return connection;
	}

	public void setConnection(DatabaseConnection connection) {
		this.connection = connection;
	}
}
