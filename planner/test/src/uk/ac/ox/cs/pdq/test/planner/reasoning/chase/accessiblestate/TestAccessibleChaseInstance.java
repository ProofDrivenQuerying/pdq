package uk.ac.ox.cs.pdq.test.planner.reasoning.chase.accessiblestate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

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
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */

public class TestAccessibleChaseInstance {
	private AccessibleDatabaseChaseInstance state;
	private DatabaseConnection connection;
	protected Schema schema;
	protected AccessibleSchema accessibleSchema;
	
	protected AccessMethod method0 = AccessMethod.create(new Integer[]{});
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected AccessMethod method3 = AccessMethod.create(new Integer[]{1});
	
	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
    
	protected Relation R;
	protected Relation InferredAccessibleR;
	protected Relation S;	
	
	@Before
	public void setup() throws SQLException {
		Utility.assertsEnabled();
		this.R = Relation.create("R", new Attribute[]{a,b,c}, new AccessMethod[]{this.method0, this.method2});
		this.InferredAccessibleR = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R", new Attribute[]{a,b,c}, new AccessMethod[]{this.method0, this.method2});
        this.S = Relation.create("S", new Attribute[]{b,c}, new AccessMethod[]{this.method0, this.method1, this.method2});
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
				this.schema = new Schema(new Relation[] { this.R, this.S });
		this.schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(new String("John"))));
		this.accessibleSchema = new AccessibleSchema(this.schema);
	}
	
	@Test
	public void test1_groupFactsByAccessMethods() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[]{axiom1});
		//TODO add assertions 
	}
	
	@Test
	public void test2_groupFactsByAccessMethods() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[]{axiom1});
		//TODO add assertions 
	}
	
	@Test
	public void test3_groupFactsByAccessMethods() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[]{axiom1});
		//TODO add assertions 
	}
	
	@Test
	public void test4_groupFactsByAccessMethods() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		List<Pair<AccessibilityAxiom, Collection<Atom>>> groups = this.state.groupFactsByAccessMethods(new AccessibilityAxiom[]{axiom1});
		//TODO add assertions 
	}
	
	@Test
	public void test1_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		//TODO add assertions 
	}
	
	@Test
	public void test1b_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		//TODO add assertions 
	}
	
	@Test
	public void test2_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		//TODO add assertions 
	}
	
	@Test
	public void test2b_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		//TODO add assertions 
	}
	
	@Test
	public void test3_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		//TODO add assertions 
	}
	
	@Test
	public void test4_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		//TODO add assertions 
	}
	
	@Test
	public void test4b_getUnexposedFacts() throws SQLException {
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
		//TODO add assertions 
	}
}
