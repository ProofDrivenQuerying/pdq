package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

/**
 * Tests the chaseStep method of the DatabaseChaseInstance class
 * 
 * @author Efthymia Tsamoura
 *
 */
public class TestChaseSteps {
	private DatabaseChaseInstance state;
	private DatabaseConnection connection;
	protected Schema schema;
	private Relation rel2;
	private EGD egd;

	@Before
	public void setup() throws SQLException {
		setupMocks();
		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
	}

	public void setupMocks() throws SQLException {
		MockitoAnnotations.initMocks(this);
		Attribute factId = Attribute.create(Integer.class, "InstanceID");
		Attribute at11 = Attribute.create(String.class, "at11");
		Attribute at12 = Attribute.create(String.class, "at12");
		this.rel2 = Relation.create("R2", new Attribute[] { at11, at12, factId });

		Atom R2 = Atom.create(Predicate.create("R2", 2), new Term[] { Variable.create("y"), Variable.create("z") });
		Atom R2p = Atom.create(Predicate.create("R2", 2), new Term[] { Variable.create("y"), Variable.create("w") });

		this.egd = EGD.create(new Atom[]{R2, R2p}, new Atom[]{Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { Variable.create("z"), Variable.create("w") })});

		this.schema = new Schema(new Relation[] { this.rel2 }, new Dependency[] { this.egd });
		this.schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(new String("John"))));
	}

	public void setConnection(DatabaseConnection dc) {
		this.connection = dc;
	}

	@Test
	public void test_chaseStep() {
		Atom f0 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });

		Atom f1 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });

		Atom f2 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), UntypedConstant.create("k") });

		Atom f3 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c3"), TypedConstant.create(new String("John")) });

		Atom f4 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });

		try {
			this.state = new DatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), this.connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		Map<Variable, Constant> map1 = new HashMap<>();
		map1.put(Variable.create("y"), UntypedConstant.create("c"));
		map1.put(Variable.create("z"), UntypedConstant.create("c1"));
		map1.put(Variable.create("w"), UntypedConstant.create("c2"));

		Map<Variable, Constant> map2 = new HashMap<>();
		map2.put(Variable.create("y"), UntypedConstant.create("c"));
		map2.put(Variable.create("z"), UntypedConstant.create("c1"));
		map2.put(Variable.create("w"), UntypedConstant.create("c3"));

		Map<Variable, Constant> map3 = new HashMap<>();
		map3.put(Variable.create("y"), UntypedConstant.create("c"));
		map3.put(Variable.create("z"), UntypedConstant.create("c2"));
		map3.put(Variable.create("w"), UntypedConstant.create("c3"));

		Map<Variable, Constant> map4 = new HashMap<>();
		map4.put(Variable.create("y"), UntypedConstant.create("c"));
		map4.put(Variable.create("z"), UntypedConstant.create("c3"));
		map4.put(Variable.create("w"), UntypedConstant.create("c4"));

		Map<Variable, Constant> map5 = new HashMap<>();
		map5.put(Variable.create("y"), UntypedConstant.create("c"));
		map5.put(Variable.create("z"), UntypedConstant.create("c3"));
		map5.put(Variable.create("w"), TypedConstant.create(new String("John")));

		Collection<Match> matches = new LinkedHashSet<>();
		matches.add(Match.create(this.egd, map5));
		matches.add(Match.create(this.egd, map1));
		matches.add(Match.create(this.egd, map4));
		matches.add(Match.create(this.egd, map3));
		matches.add(Match.create(this.egd, map2));

		boolean _isFailed = this.state.chaseStep(matches);
		Assert.assertEquals(false, !_isFailed);
		Assert.assertEquals(1, this.state.getConstantClasses().size());
		Assert.assertNotNull(this.state.getConstantClasses().getClass(UntypedConstant.create("c1")));
		Assert.assertNotNull(this.state.getConstantClasses().getClass(UntypedConstant.create("c2")));
		Assert.assertNotNull(this.state.getConstantClasses().getClass(UntypedConstant.create("c3")));
		Assert.assertNotNull(this.state.getConstantClasses().getClass(UntypedConstant.create("c4")));
		Assert.assertNotNull(this.state.getConstantClasses().getClass(TypedConstant.create(new String("John"))));

		Atom n0 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });

		Atom n1 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), UntypedConstant.create("k") });

		Atom n2 = Atom.create(Predicate.create("R2", 2), new Term[] { TypedConstant.create(new String("John")), TypedConstant.create(new String("John")) });

		Atom n3 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c3"), TypedConstant.create(new String("John")) });

		Atom n4 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2") });

		Atom n5 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c3"), UntypedConstant.create("c4") });

		Atom n6 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c3") });

		Atom n7 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c3") });

		Assert.assertEquals(Sets.newHashSet(n0, n1, n2, n3, n4, n5, n6, n7), this.state.getFacts());

		Map<Variable, Constant> map6 = new HashMap<>();
		map6.put(Variable.create("y"), UntypedConstant.create("c"));
		map6.put(Variable.create("z"), UntypedConstant.create("c2"));
		map6.put(Variable.create("w"), TypedConstant.create(new String("Michael")));

		matches.clear();
		matches.add(Match.create(this.egd, map6));
		_isFailed = this.state.chaseStep(matches);
		Assert.assertEquals(true, !_isFailed);
	}

	@After
	public void tearDown() throws Exception {
		try {
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	protected void test_chaseStepAddFacts() {
		Atom f0 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });

		Atom f1 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });

		Atom f2 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), UntypedConstant.create("k") });

		Atom f3 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c3"), TypedConstant.create(new String("John")) });

		Atom f4 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });
		this.state.addFacts(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4));
	}

	protected void test_chaseStepDeleteFacts() {
		Atom f0 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), UntypedConstant.create("c1") });

		Atom f1 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });

		Atom f2 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), UntypedConstant.create("k") });

		Atom f3 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c3"), TypedConstant.create(new String("John")) });

		Atom f4 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c4") });
		this.state.deleteFacts(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4));
	}

	protected void test_chaseStepInit() {
		try {
			this.state = new DatabaseChaseInstance(Sets.<Atom>newHashSet(), this.connection);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	protected void test_chaseStepMain(int repeatCounter) {
		Map<Variable, Constant> map1 = new HashMap<>();
		map1.put(Variable.create("y"), UntypedConstant.create("c"));
		map1.put(Variable.create("z"), UntypedConstant.create("c1"));
		map1.put(Variable.create("w"), UntypedConstant.create("c2"));

		Map<Variable, Constant> map2 = new HashMap<>();
		map2.put(Variable.create("y"), UntypedConstant.create("c"));
		map2.put(Variable.create("z"), UntypedConstant.create("c1"));
		map2.put(Variable.create("w"), UntypedConstant.create("c3"));

		Map<Variable, Constant> map3 = new HashMap<>();
		map3.put(Variable.create("y"), UntypedConstant.create("c"));
		map3.put(Variable.create("z"), UntypedConstant.create("c2"));
		map3.put(Variable.create("w"), UntypedConstant.create("c3"));

		Map<Variable, Constant> map4 = new HashMap<>();
		map4.put(Variable.create("y"), UntypedConstant.create("c"));
		map4.put(Variable.create("z"), UntypedConstant.create("c3"));
		map4.put(Variable.create("w"), UntypedConstant.create("c4"));

		Map<Variable, Constant> map5 = new HashMap<>();
		map5.put(Variable.create("y"), UntypedConstant.create("c"));
		map5.put(Variable.create("z"), UntypedConstant.create("c3"));
		map5.put(Variable.create("w"), TypedConstant.create(new String("John")));

		Collection<Match> matches = new LinkedHashSet<>();
		matches.add(Match.create(this.egd, map5));
		matches.add(Match.create(this.egd, map1));
		matches.add(Match.create(this.egd, map4));
		matches.add(Match.create(this.egd, map3));
		matches.add(Match.create(this.egd, map2));

		boolean _isFailed;
		_isFailed = this.state.chaseStep(matches);
		if (repeatCounter==0)		
			Assert.assertEquals(false, !_isFailed);
		Assert.assertEquals(1, this.state.getConstantClasses().size());
		Assert.assertNotNull(this.state.getConstantClasses().getClass(UntypedConstant.create("c1")));
		Assert.assertNotNull(this.state.getConstantClasses().getClass(UntypedConstant.create("c2")));
		Assert.assertNotNull(this.state.getConstantClasses().getClass(UntypedConstant.create("c3")));
		Assert.assertNotNull(this.state.getConstantClasses().getClass(UntypedConstant.create("c4")));
		Assert.assertNotNull(this.state.getConstantClasses().getClass(TypedConstant.create(new String("John"))));

		Atom n0 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), TypedConstant.create(new String("John")) });

		Atom n1 = Atom.create(Predicate.create("R2", 2), new Term[] { UntypedConstant.create("c"), UntypedConstant.create("k") });

		Atom n2 = Atom.create(Predicate.create("R2", 2), new Term[] { TypedConstant.create(new String("John")), TypedConstant.create(new String("John")) });

		Atom n3 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c3"), TypedConstant.create(new String("John")) });

		Atom n4 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2") });

		Atom n5 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c3"), UntypedConstant.create("c4") });

		Atom n6 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c3") });

		Atom n7 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c3") });

		if (repeatCounter==0)
			Assert.assertEquals(Sets.newHashSet(n0, n1, n2, n3, n4, n5, n6, n7), this.state.getFacts());

		Map<Variable, Constant> map6 = new HashMap<>();
		map6.put(Variable.create("y"), UntypedConstant.create("c"));
		map6.put(Variable.create("z"), UntypedConstant.create("c2"));
		map6.put(Variable.create("w"), TypedConstant.create(new String("Michael")));

		matches.clear();
		matches.add(Match.create(this.egd, map6));
		_isFailed = this.state.chaseStep(matches);
		Assert.assertEquals(true, !_isFailed);
	}
}
