package uk.ac.ox.cs.pdq.test.planner.reasoning.chase.accessiblestate;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.LogicalDatabaseInstance;
import uk.ac.ox.cs.pdq.databasemanagement.cache.MultiInstanceFactCache;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.ExplorationSetUp;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.configuration.ChaseConfiguration;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Creates a schema, and in each test case creates a set of facts to test the
 * getUnexposedFacts method, or the groupFactsByAccessMethods method of the
 * AccessibleDatabaseChaseInstance class.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING) // makes the test executed in an abc order.
public class TestAccessibleChaseInstance extends PdqTest {
	private AccessibleDatabaseChaseInstance state;
	private DatabaseManager connection;
	protected AccessibleSchema accessibleSchema;
	protected Relation InferredAccessibleR;

	@Before
	public void setup() throws Exception {
		super.setup();
		this.InferredAccessibleR = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R2", new Attribute[] { at21, at22 }, new AccessMethodDescriptor[] {});
		Schema schema = new Schema(new Relation[] { this.rel2, this.S_s });
		this.accessibleSchema = new AccessibleSchema(schema);
	}

	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * 
	 * And the accessibility axiom requires input on the first attribute.
	 * 
	 * Checks if we got all facts in the same group.
	 * </pre>
	 */
	@Test
	public void test1_groupFactsByAccessMethods() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		AccessibilityAxiom axiom1 = new AccessibilityAxiom(this.rel2, this.method0);
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[] { axiom1 });
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.get(0).getLeft() instanceof AccessibilityAxiom);
		Assert.assertEquals(axiom1, groups.get(0).getLeft());
		Assert.assertEquals(5, groups.get(0).getRight().size());

	}

	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * 
	 * And the accessibility axiom requires input on the second attribute.
	 * 
	 * Checks if we got 2 group.
	 * </pre>
	 */
	@Test
	public void test2_groupFactsByAccessMethods() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		AccessibilityAxiom axiom1 = new AccessibilityAxiom(this.rel2, this.method1);
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

	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * 
	 * And the accessibility axiom requires input on the first and second attribute.
	 * 
	 * Checks if we got 5 group.
	 * </pre>
	 */
	@Test
	public void test3_groupFactsByAccessMethods() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		AccessibilityAxiom axiom1 = new AccessibilityAxiom(this.rel2, this.method2);
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[] { axiom1 });
		Assert.assertNotNull(groups);
		Assert.assertEquals(5, groups.size());
		for (Pair<AccessibilityAxiom, Collection<Atom>> p : groups) {
			Assert.assertEquals(1, p.getValue().size());
		}
	}

	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * 
	 * And the accessibility axiom requires input on the second attribute.
	 * 
	 * Checks if we got 3 group.
	 * </pre>
	 */
	@Test
	public void test4_groupFactsByAccessMethods() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new AccessibleDatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection, false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		AccessibilityAxiom axiom1 = new AccessibilityAxiom(this.rel2, this.method3);
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

	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * 
	 * And then gets the unexposed facts.
	 * 
	 * Checks if we got 1 group with 5 facts
	 * </pre>
	 */
	@Test
	public void test1_getUnexposedFacts() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

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
	
	
	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * InferredAccessibleR2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * InferredAccessibleR2(c2,c4)
	 * 
	 * And then gets the unexposed facts.
	 * 
	 * Checks if we got 1 group with 3 facts:
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * </pre>
	 */
	@Test
	public void test1b_getUnexposedFacts() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f0b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });
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
	
	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * 
	 * And then gets the unexposed facts.
	 * 
	 * Checks if we got 1 group with 5 facts
	 * </pre>
	 */
	@Test
	public void test2_getUnexposedFacts() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

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
	
	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * InferredAccessibleR2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * 
	 * And then gets the unexposed facts.
	 * 
	 * Checks if we got 1 group with 4 facts:
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * </pre>
	 */
	@Test
	public void test2b_getUnexposedFacts() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f0b = Atom.create(this.InferredAccessibleR, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

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

	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * InferredAccessibleR2(c2,Jhon)
	 * InferredAccessibleR2(c2,c4)
	 * 
	 * And then gets the unexposed facts.
	 * 
	 * Checks if we got 1 group with 3 facts:
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * </pre>
	 */
	@Test
	public void test3_getUnexposedFacts() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });
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


	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * 
	 * And then gets the unexposed facts.
	 * 
	 * Checks if we got 1 group with 5 facts:
	 * </pre>
	 */
	@Test
	public void test4_getUnexposedFacts() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

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


	/** <pre>
	 * Creates facts:
	 * R2(c,c1)
	 * R2(c,John)
	 * R2(c,c4)
	 * R2(c2,Jhon)
	 * R2(c2,c4)
	 * InferredAccessibleR2R2(c,c4)
	 * InferredAccessibleR2(c2,Jhon)
	 * InferredAccessibleR2(c2,c4)
	 * 
	 * And then gets the unexposed facts.
	 * 
	 * Checks if we got 1 group with 2 facts:
	 * R2(c,John)
	 * R2(c2,Jhon)
	 * </pre>
	 */
	@Test
	public void test4b_getUnexposedFacts() throws SQLException {
		this.connection = createConnection(DatabaseParameters.Postgres, this.accessibleSchema);
		Atom f0 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f1 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f2 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f3 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(new String("John")) });
		Atom f4 = Atom.create(this.rel2, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });
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

	/**
	 * Uses StandardScenario1 for schema and query. 
	 * Tests if we get 4 facts in the database after reasoning.
	 */
	@Test
	public void testChaseTheAccessibleState() {
		TestScenario ts = getStandardScenario1();
		// Create accessible schema
		AccessibleSchema accessibleSchema = new AccessibleSchema(ts.getSchema());
		ConjunctiveQuery query = ts.getQuery();
		Map<Variable, Constant> substitution = ChaseConfiguration.generateSubstitutionToCanonicalVariables(query);
		Map<Variable, Constant> substitutionFiltered = new HashMap<>(); 
		substitutionFiltered.putAll(substitution);
		for(Variable variable:query.getBoundVariables()) 
			substitutionFiltered.remove(variable);
		ExplorationSetUp.getCanonicalSubstitution().put(query,substitution);
		ExplorationSetUp.getCanonicalSubstitutionOfFreeVariables().put(query,substitutionFiltered);

		// Create database connection
		DatabaseManager connection = null;
		
		// Create the chaser
		RestrictedChaser chaser = new RestrictedChaser(null);
		
		try {
			connection = createConnection(DatabaseParameters.Postgres, accessibleSchema);
			AccessibleChaseInstance state = (uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance) 
					new AccessibleDatabaseChaseInstance(ts.getQuery(), accessibleSchema, connection, true);
			chaser.reasonUntilTermination(state, accessibleSchema.getOriginalDependencies());
			//Assert that we get four facts after chasing this thing
			Assert.assertTrue(state.getFacts().size() == 4);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Will throw assertion error if any Atom from the atoms array is contained by the list.
	 */
	private void assertNotContains(Atom[] atoms, List<Match> list) {
		assertContainment(false, atoms, list);
	}

	/**
	 * Will throw assertion error if any Atom from the atoms array is not contained by the list.
	 */
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
	
	@After
	public void tearDown() {
		if (connection!=null) {
			try {
				connection.dropDatabase();
				connection.shutdown();
			} catch (DatabaseException e) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}
	
	private DatabaseManager createConnection(DatabaseParameters params, Schema s) {
		try {
			connection = new LogicalDatabaseInstance(new MultiInstanceFactCache(), new ExternalDatabaseManager(params),1);
			connection.initialiseDatabaseForSchema(s);
			return connection;
		} catch (DatabaseException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		return null;
	}
		
}
