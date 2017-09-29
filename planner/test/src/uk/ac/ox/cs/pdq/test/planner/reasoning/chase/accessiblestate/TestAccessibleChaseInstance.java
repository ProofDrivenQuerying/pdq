package uk.ac.ox.cs.pdq.test.planner.reasoning.chase.accessiblestate;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Creates a schema, and in each test case creates a set of facts to test the
 * getUnexposedFacts method, and the groupFactsByAccessMethods method of the
 * AccessibleDatabaseChaseInstance class.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING) // makes the test executed in an abc order.
public class TestAccessibleChaseInstance {
	private AccessibleDatabaseChaseInstance state;
	private DatabaseConnection connection;
	protected Schema schema;
	protected AccessibleSchema accessibleSchema;

	protected AccessMethod method0 = AccessMethod.create(new Integer[] {});
	protected AccessMethod method1 = AccessMethod.create(new Integer[] { 0 });
	protected AccessMethod method2 = AccessMethod.create(new Integer[] { 0, 1 });
	protected AccessMethod method3 = AccessMethod.create(new Integer[] { 1 });

	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
	protected Attribute InstanceID = Attribute.create(Integer.class, "InstanceID");

	protected Relation R;
	protected Relation InferredAccessibleR;
	protected Relation S;

	@Before
	public void setup() throws SQLException {
		Utility.assertsEnabled();
		this.R = Relation.create("R", new Attribute[] { a, b, InstanceID }, new AccessMethod[] { this.method0, this.method2 });
		this.InferredAccessibleR = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R", new Attribute[] { a, b, InstanceID }, new AccessMethod[] {});
		this.S = Relation.create("S", new Attribute[] { b, c, InstanceID }, new AccessMethod[] { this.method0, this.method1, this.method2 });
		this.schema = new Schema(new Relation[] { this.R, this.S });
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.schema);
		this.schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(new String("John"))));
		this.accessibleSchema = new AccessibleSchema(this.schema);
	}

	@Test
	public void test1_groupFactsByAccessMethods() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		AccessibilityAxiom axiom1 = new AccessibilityAxiom(this.R, this.method0);
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[] { axiom1 });
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.get(0).getLeft() instanceof AccessibilityAxiom);
		Assert.assertEquals(axiom1, groups.get(0).getLeft());
		Assert.assertEquals(5, groups.get(0).getRight().size());

	}

	@Test
	public void test2_groupFactsByAccessMethods() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		AccessibilityAxiom axiom1 = new AccessibilityAxiom(this.R, this.method1);
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[] { axiom1 });
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size());
		Assert.assertEquals(axiom1, groups.get(0).getLeft());
		Assert.assertEquals(axiom1, groups.get(1).getLeft());
		for (Pair<AccessibilityAxiom, Collection<Atom>> group : groups) {
			Term current = null;
			for (Atom a : group.getRight()) {
				if (current == null) {
					current = a.getTerms()[0];
				}
				// all of them have to be the same
				Assert.assertEquals(current, a.getTerms()[0]);
			}
		}
	}

	@Test
	public void test3_groupFactsByAccessMethods() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		AccessibilityAxiom axiom1 = new AccessibilityAxiom(this.R, this.method2);
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[] { axiom1 });
		Assert.assertNotNull(groups);
		Assert.assertEquals(5, groups.size());
		for (Pair<AccessibilityAxiom, Collection<Atom>> p : groups) {
			Assert.assertEquals(1, p.getValue().size());
		}
	}

	@Test
	public void test4_groupFactsByAccessMethods() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		AccessibilityAxiom axiom1 = new AccessibilityAxiom(this.R, this.method3);
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[] { axiom1 });
		Assert.assertNotNull(groups);
		Assert.assertEquals(3, groups.size());
		Assert.assertEquals(axiom1, groups.get(0).getLeft());
		Assert.assertEquals(axiom1, groups.get(1).getLeft());
		for (Pair<AccessibilityAxiom, Collection<Atom>> group : groups) {
			Term current = null;
			for (Atom a : group.getRight()) {
				if (current == null) {
					current = a.getTerms()[1];
				}
				// all of them have to be the same
				Assert.assertEquals(current, a.getTerms()[1]);
			}
		}
	}

	@Test
	public void test1_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		Map<AccessibilityAxiom, List<Match>> groups = this.state.getUnexposedFacts(this.accessibleSchema);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertNotNull(groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		Assert.assertEquals(5, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]).size());
	}

	@Test
	public void test1b_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f0b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });
		Atom f4b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f0b, f1, f2, f3, f4, f4b), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		Map<AccessibilityAxiom, List<Match>> groups = this.state.getUnexposedFacts(this.accessibleSchema);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertNotNull(groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		Assert.assertEquals(3, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]).size());
		assertContains(new Atom[] { f1, f2, f3 }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		assertNotContains(new Atom[] { f0b, f4b }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
	}

	@Test
	public void test2_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		Map<AccessibilityAxiom, List<Match>> groups = this.state.getUnexposedFacts(this.accessibleSchema);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertNotNull(groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		Assert.assertEquals(5, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]).size());
		assertContains(new Atom[] { f0, f1, f2, f3, f4 }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
	}

	@Test
	public void test2b_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f0b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f0b, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		Map<AccessibilityAxiom, List<Match>> groups = this.state.getUnexposedFacts(this.accessibleSchema);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertNotNull(groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		Assert.assertEquals(4, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]).size());
		assertContains(new Atom[] { f1, f2, f3, f4 }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		assertNotContains(new Atom[] { f0b }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));

	}

	@Test
	public void test3_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });
		Atom f3b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f3b, f4, f4b), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		Map<AccessibilityAxiom, List<Match>> groups = this.state.getUnexposedFacts(this.accessibleSchema);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertNotNull(groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		Assert.assertEquals(3, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]).size());
		assertContains(new Atom[] { f0, f1, f2 }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		assertNotContains(new Atom[] { f3b, f4b }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
	}

	@Test
	public void test4_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		Map<AccessibilityAxiom, List<Match>> groups = this.state.getUnexposedFacts(this.accessibleSchema);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertNotNull(groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		Assert.assertEquals(5, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]).size());
		assertContains(new Atom[] { f0, f1, f2, f3, f4 }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
	}

	@Test
	public void test4b_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(DatabaseParameters.Derby, this.accessibleSchema);
		Atom f0 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.R, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.R, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });
		Atom f2b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f2b, f3, f3b, f4, f4b), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		Map<AccessibilityAxiom, List<Match>> groups = this.state.getUnexposedFacts(this.accessibleSchema);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertNotNull(groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		Assert.assertEquals(2, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]).size());
		assertContains(new Atom[] { f0, f1 }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));
		assertNotContains(new Atom[] { f2b, f3b, f4b }, groups.get(this.accessibleSchema.getAccessibilityAxioms()[0]));

	}

	private void assertNotContains(Atom[] atoms, List<Match> list) {
		assertContainment(false, atoms, list);
	}

	private void assertContains(Atom[] atoms, List<Match> list) {
		assertContainment(true, atoms, list);
	}

	private void assertContainment(boolean contains, Atom[] atoms, List<Match> list) {
		for (Atom a : atoms) {
			boolean found = false;
			for (Match m : list) {
				if (!contains) {
					Assert.assertFalse(Arrays.equals(a.getTerms(), m.getMapping().values().toArray(new Term[m.getMapping().values().size()])));
				}
				if (Arrays.equals(a.getTerms(), m.getMapping().values().toArray(new Term[m.getMapping().values().size()])))
					found = true;
			}
			if (contains) {
				Assert.assertTrue(found);
			}
		}
	}
	
	
	@Test
	public void testChaseTheAccessibleState() {
		Relation[] relations = new Relation[5];
		relations[0] = Relation.create("R0", new Attribute[] { this.a, this.b, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[1] = Relation.create("R1", new Attribute[] { this.a, this.b, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[2] = Relation.create("R2", new Attribute[] { this.a, this.b, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[3] = Relation.create("R3", new Attribute[] { this.a, this.b, this.InstanceID }, new AccessMethod[] { AccessMethod.create(new Integer[] {}) });
		relations[4] = Relation.create("Accessible", new Attribute[] { this.a, this.InstanceID });
		// Create query
		Atom[] atoms = new Atom[2];
		Variable x = Variable.create("x");
		Variable y = Variable.create("y");
		Variable z = Variable.create("z");
		Variable w = Variable.create("w");
		atoms[0] = Atom.create(relations[0], new Term[] { x, y });
		atoms[1] = Atom.create(relations[1], new Term[] { y, z });
		ConjunctiveQuery query = ConjunctiveQuery.create(new Variable[] { x, y }, (Conjunction) Conjunction.of(atoms));

		Dependency dependency1 = TGD.create(new Atom[] { Atom.create(relations[0], new Term[] { x, y })},
				new Atom[] { Atom.create(relations[2], new Term[] { x, y })});
		Dependency dependency2 = TGD.create(new Atom[] { Atom.create(relations[1], new Term[] { y, z })},
				new Atom[] { Atom.create(relations[3], new Term[] { y, z })});
		//R2(x,y), R3(y,z) -> R0(x,w) R1(w,z)
		Dependency dependency3 = TGD.create(new Atom[] { Atom.create(relations[2], new Term[] { y, z }), Atom.create(relations[3], new Term[] { y, z })},
				new Atom[] { Atom.create(relations[0], new Term[] { x, w }), Atom.create(relations[1], new Term[] { w, z })});
		// Create schema
		Schema schema = new Schema(relations, new Dependency[] { dependency1, dependency2, dependency3 });

		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		
		// Create database connection
		DatabaseConnection connection = null;
		
		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);
		
		try {
			connection = new DatabaseConnection(DatabaseParameters.MySql, accessibleSchema);
			AccessibleChaseInstance state = (uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance) 
					new AccessibleDatabaseChaseInstance(query, accessibleSchema, connection, true);
			chaser.reasonUntilTermination(state, accessibleSchema.getOriginalDependencies());
			//Assert that we get four facts after chasing this thing
			Assert.assertTrue(state.getFacts().size() == 4);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
}
