package uk.ac.ox.cs.pdq.test;

import static org.mockito.Mockito.when;

import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.cardinality.dag.DAGAnnotatedPlan;
import uk.ac.ox.cs.pdq.cardinality.estimator.CardinalityUtility;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.sqlstatement.MySQLStatementBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

/**
 * Tests the isKey method of the CardinalityUtility class 
 * @author Efthymia Tsamoura
 *
 */
public class TestCardinalityUtility {

	@Mock
	DAGAnnotatedPlan configuration;
	
	protected DatabaseHomomorphismManager manager;
	protected DatabaseChaseListState state;
	protected RestrictedChaser chaser;
	
	private Relation rel1;
	private Relation rel2;
	private Relation rel3;
	
	private TGD tgd2;
	private EGD egd1;
	private EGD egd2;
	private TGD tgd3;
	private EGD egd3;

	private Schema schema;
	
	@Before
	public void setup() throws SQLException {
		MockitoAnnotations.initMocks(this);
		
		Attribute at11 = new Attribute(String.class, "at11");
		Attribute at12 = new Attribute(String.class, "at12");
		Attribute at13 = new Attribute(String.class, "at13");
		this.rel1 = new Relation("R1", Lists.newArrayList(at11, at12, at13)) {};
		
		Attribute at21 = new Attribute(String.class, "at21");
		Attribute at22 = new Attribute(String.class, "at22");
		this.rel2 = new Relation("R2", Lists.newArrayList(at21, at22)) {};
		
		Attribute at31 = new Attribute(String.class, "at31");
		Attribute at32 = new Attribute(String.class, "at32");
		this.rel3 = new Relation("R3", Lists.newArrayList(at31, at32)) {};
		
		Atom R1 = new Atom(this.rel1, 
				Lists.newArrayList(new Variable("x"),new Variable("y"),new Variable("z")));
		
		Atom R1p = new Atom(this.rel1, 
				Lists.newArrayList(new Variable("x"),new Variable("yp"),new Variable("zp")));
		
		Atom R2 = new Atom(this.rel2, 
				Lists.newArrayList(new Variable("y"),new Variable("z")));
		Atom R2p = new Atom(this.rel2, 
				Lists.newArrayList(new Variable("y"),new Variable("w")));
		
		Atom R3 = new Atom(this.rel3, 
				Lists.newArrayList(new Variable("x"),new Variable("y")));
		Atom R3p = new Atom(this.rel3, 
				Lists.newArrayList(new Variable("x"),new Variable("z")));
		
		this.egd1 = new EGD(Conjunction.of(R1,R1p), Conjunction.of(new Equality(new Variable("y"),new Variable("yp")), new Equality(new Variable("z"),new Variable("zp"))));
		this.tgd2 = new TGD(Conjunction.of(R1),Conjunction.of(R2));
		this.egd2 = new EGD(Conjunction.of(R2,R2p), Conjunction.of(new Equality(new Variable("z"),new Variable("w"))));
		
		this.tgd3 = new TGD(Conjunction.of(R1),Conjunction.of(R3));
		this.egd3 = new EGD(Conjunction.of(R3,R3p), Conjunction.of(new Equality(new Variable("y"),new Variable("z"))));

		this.schema = new Schema(Lists.<Relation>newArrayList(this.rel1, this.rel2, this.rel3), Lists.<Dependency>newArrayList(this.tgd2,this.egd2, this.tgd3, this.egd3));
		this.schema.updateConstants(Lists.<TypedConstant<?>>newArrayList(new TypedConstant(new String("John")), new TypedConstant(new String("Michael"))));
		/** The driver. */
		String driver = null;
		/** The url. */
		String url = "jdbc:mysql://localhost/";
		/** The database. */
		String database = "pdq_chase";
		/** The username. */
		String username = "root";
		/** The password. */
		String password ="root";
		this.manager = new DatabaseHomomorphismManager(driver, url, database, username, password, new MySQLStatementBuilder(), this.schema);
		this.manager.initialize();
		
		this.chaser = new RestrictedChaser(new StatisticsCollector(true, new EventBus()));
	}
	
	@Test 
	public void test_isKey1() {		
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c1")));

		Atom f21 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c2")));

		Atom f22 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c3")));

		Atom f23 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c4")));

		Atom f24 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("John"))));
	
		when(this.configuration.getOutputFacts()).thenReturn(Sets.newHashSet(f20,f21,f22,f23,f24));
		boolean isKey = CardinalityUtility.isKey(Sets.<Constant>newHashSet(new Skolem("c")), this.configuration, this.chaser, this.manager, Sets.newHashSet(this.egd2));
		Assert.assertEquals(true, isKey);
	}
	
	@Test 
	public void test_isKey2() {		
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c1")));

		Atom f21 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c2")));

		Atom f22 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new Skolem("c3")));

		Atom f23 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("Michael"))));

		Atom f24 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("John"))));
	
		when(this.configuration.getOutputFacts()).thenReturn(Sets.newHashSet(f20,f21,f22,f23,f24));
		boolean isKey = CardinalityUtility.isKey(Sets.<Constant>newHashSet(new Skolem("c")), this.configuration, this.chaser, this.manager, Sets.newHashSet(this.egd2));
		Assert.assertEquals(false, isKey);
	}
	
	@Test 
	public void test_isKey3() {		
		Atom f20 = new Atom(this.rel3, 
				Lists.newArrayList(new Skolem("a"),new Skolem("b1")));

		Atom f21 = new Atom(this.rel3, 
				Lists.newArrayList(new Skolem("a"),new Skolem("b2")));

		Atom f22 = new Atom(this.rel3, 
				Lists.newArrayList(new Skolem("a"),new Skolem("b3")));
		
		Atom f10 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("a"),new Skolem("b1"), new Skolem("c1")));

		Atom f11 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("a"),new Skolem("b2"), new Skolem("c2")));

		Atom f12 = new Atom(this.rel1, 
				Lists.newArrayList(new Skolem("a"),new Skolem("b3"), new Skolem("c3")));

		when(this.configuration.getOutputFacts()).thenReturn(Sets.newHashSet(f20,f21,f22,f10,f11,f12));
		boolean isKey = CardinalityUtility.isKey(Sets.<Constant>newHashSet(new Skolem("a")), this.configuration, this.chaser, this.manager, Sets.newHashSet(this.tgd3, this.egd1, this.egd3));
		Assert.assertEquals(true, isKey);
	}
	
	@Test 
	public void test_isKey4() {		
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new Skolem("x0"),new Skolem("y0")));

		Atom f21 = new Atom(this.rel3, 
				Lists.newArrayList(new Skolem("x0"),new Skolem("y1")));

		when(this.configuration.getOutputFacts()).thenReturn(Sets.newHashSet(f20,f21));
		boolean isKey = CardinalityUtility.isKey(Sets.<Constant>newHashSet(new Skolem("x0")), this.configuration, this.chaser, this.manager, Sets.newHashSet(this.egd2, this.egd3));
		Assert.assertEquals(true, isKey);
	}
	
	
}
