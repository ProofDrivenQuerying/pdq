package uk.ac.ox.cs.pdq.test.reasoning.homomorphism;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitToThisOrAllInstances;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;

import com.google.common.collect.Lists;

/**
 * Tests the getMatches method of the DatabaseChaseInstance class 
 * 
 * @author Efthymia Tsamoura
 *
 */
public class TestDatabaseChaseInstance {	
	protected DatabaseChaseInstance chaseState;
	
	private Relation rel1;
	private Relation rel2;
	private Relation rel3;
	
	private TGD tgd;
	private TGD tgd2;
	private EGD egd;

	private Schema schema;
				
	@Before
	public void setup() throws SQLException {
		Attribute factId = Attribute.create(Integer.class, "InstanceID");
		
		Attribute at11 = Attribute.create(String.class, "at11");
		Attribute at12 = Attribute.create(String.class, "at12");
		Attribute at13 = Attribute.create(String.class, "at13");
		this.rel1 = Relation.create("R1", new Attribute[]{at11, at12, at13,factId});
		
		Attribute at21 = Attribute.create(String.class, "at21");
		Attribute at22 = Attribute.create(String.class, "at22");
		this.rel2 = Relation.create("R2", new Attribute[]{at21, at22,factId});
		
		Attribute at31 = Attribute.create(String.class, "at31");
		Attribute at32 = Attribute.create(String.class, "at32");
		this.rel3 = Relation.create("R3", new Attribute[]{at31, at32,factId});
		
		Atom R1 = Atom.create(this.rel1, new Term[]{Variable.create("x"),Variable.create("y"),Variable.create("z")});
		Atom R2 = Atom.create(this.rel2, new Term[]{Variable.create("y"),Variable.create("z")});
		Atom R2p = Atom.create(this.rel2, new Term[]{Variable.create("y"),Variable.create("w")});
		
		Atom R3 = Atom.create(this.rel3, new Term[]{Variable.create("y"),Variable.create("w")});
		
		this.tgd = TGD.create(new Atom[]{R1},new Atom[]{R2});
		this.tgd2 = TGD.create(new Atom[]{R1,R2p},new Atom[]{R3});
		this.egd = EGD.create(Conjunction.of(R2,R2p), Conjunction.of(Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), 
				Variable.create("z"),Variable.create("w"))));

		this.schema = new Schema(new Relation[]{this.rel1, this.rel2, this.rel3}, new Dependency[]{this.tgd,this.tgd2, this.egd});
		this.chaseState = new DatabaseChaseInstance(new ArrayList<Atom>(),new DatabaseConnection(new DatabaseParameters(), this.schema));
	}
	
	@After
	public void tearDown() throws Exception {
		this.chaseState.close();
	}
	
	@Test 
	public void test_getMatches1() {	
		Atom f20 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k1"), UntypedConstant.create("c"),UntypedConstant.create("c1")});

		Atom f21 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k2"), UntypedConstant.create("c"),UntypedConstant.create("c2")});

		Atom f22 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k3"), UntypedConstant.create("c"),UntypedConstant.create("c3")});

		Atom f23 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k4"), UntypedConstant.create("c"),UntypedConstant.create("c4")});

		Atom f24 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k5"), UntypedConstant.create("c"),TypedConstant.create(new String("John"))});

		Atom f25 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k6"), UntypedConstant.create("c"),TypedConstant.create(new String("Michael"))});
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25));
		List<Match> matches = this.chaseState.getTriggers(new Dependency[]{this.tgd},TriggerProperty.ACTIVE,LimitToThisOrAllInstances.THIS);
		Assert.assertEquals(6, matches.size());
	}
	
	@Test 
	public void test_getMatches2() {
		Atom f20 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"),UntypedConstant.create("c1")});

		Atom f21 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"),UntypedConstant.create("c2")});

		Atom f22 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("k"),UntypedConstant.create("c3")});

		Atom f23 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),UntypedConstant.create("c4")});

		Atom f24 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),TypedConstant.create(new String("John"))});

		Atom f25 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),TypedConstant.create(new String("Michael"))});
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25));
		List<Match> matches = this.chaseState.getTriggers(new Dependency[]{this.egd},TriggerProperty.ACTIVE,LimitToThisOrAllInstances.THIS);
		Assert.assertEquals(4, matches.size());
	}
	
	@Test 
	public void test_getMatches3() {
		Atom f20 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"),UntypedConstant.create("c1")});

		Atom f21 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"),UntypedConstant.create("c2")});

		Atom f22 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("k"),UntypedConstant.create("c3")});

		Atom f23 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),UntypedConstant.create("c4")});

		Atom f24 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),TypedConstant.create(new String("John"))});

		Atom f25 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),TypedConstant.create(new String("Michael"))});
		
		Atom eq1 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2), UntypedConstant.create("c1"), UntypedConstant.create("c2"));
		Atom eq2 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2), UntypedConstant.create("c1"), UntypedConstant.create("c3"));
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, eq1,eq2));
		List<Match> matches = this.chaseState.getTriggers(new Dependency[]{this.egd},TriggerProperty.ALL,LimitToThisOrAllInstances.THIS);
		Assert.assertEquals(4, matches.size());
	}	
	
	@Test 
	public void test_getMatches4() {
		Atom f20 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"),UntypedConstant.create("c1")});

		Atom f21 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"),UntypedConstant.create("c2")});

		Atom f22 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("k"),UntypedConstant.create("c3")});

		Atom f23 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),UntypedConstant.create("c4")});

		Atom f24 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),TypedConstant.create(new String("John"))});

		Atom f25 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("p"),TypedConstant.create(new String("Michael"))});
		
		Atom eq1 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2), UntypedConstant.create("c1"), UntypedConstant.create("c2"));
		Atom eq2 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2), UntypedConstant.create("c1"), UntypedConstant.create("c3"));
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, eq1,eq2));
		List<Match> matches = this.chaseState.getTriggers(new Dependency[]{this.egd},TriggerProperty.ACTIVE,LimitToThisOrAllInstances.THIS);
		Assert.assertEquals(3, matches.size());
	}	
	
	@Test 
	public void test_getMatches5() {
		Atom f20 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k1"), UntypedConstant.create("c"),UntypedConstant.create("c1")});

		Atom f21 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k2"), UntypedConstant.create("c"),UntypedConstant.create("c2")});

		Atom f22 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k3"), UntypedConstant.create("c"),UntypedConstant.create("c3")});

		Atom f23 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k4"), UntypedConstant.create("c"),UntypedConstant.create("c4")});

		Atom f24 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k5"), UntypedConstant.create("c"),TypedConstant.create(new String("John"))});

		Atom f25 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k6"), UntypedConstant.create("c"),TypedConstant.create(new String("Michael"))});
		
		Atom f26 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"),UntypedConstant.create("c1")});

		Atom f27 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"),UntypedConstant.create("c2")});
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, f26, f27));
		List<Match> matches = this.chaseState.getTriggers(new Dependency[]{this.tgd}, TriggerProperty.ACTIVE,LimitToThisOrAllInstances.THIS);
		Assert.assertEquals(4, matches.size());
	}
	
	@Test 
	public void test_getMatches6() {
		Atom f20 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k1"), UntypedConstant.create("r1"),UntypedConstant.create("c1")});

		Atom f21 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k2"), UntypedConstant.create("r2"),UntypedConstant.create("c2")});

		Atom f22 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k3"), UntypedConstant.create("r3"),UntypedConstant.create("c3")});
		
		Atom f26 = Atom.create(this.rel3, 
				new Term[]{UntypedConstant.create("r1"),UntypedConstant.create("UntypedConstant1")});

		Atom f27 = Atom.create(this.rel3, 
				new Term[]{UntypedConstant.create("r2"),UntypedConstant.create("UntypedConstant2")});
		
		this.chaseState.addFacts(Lists.newArrayList(f20,f21,f22,f26,f27));
		List<Match> matches = this.chaseState.getTriggers(new Dependency[]{this.tgd2}, TriggerProperty.ACTIVE,LimitToThisOrAllInstances.THIS);
		Assert.assertEquals(1, matches.size());		
	}
}
