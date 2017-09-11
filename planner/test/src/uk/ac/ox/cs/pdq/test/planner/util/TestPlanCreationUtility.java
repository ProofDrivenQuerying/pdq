package uk.ac.ox.cs.pdq.test.planner.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * Using the same schema each test creates a different set of facts and uses the
 * PlanCreationUtility to create a plan using the createSingleAccessPlan method.
 * Then assertions making sure the created plan looks as expected.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

public class TestPlanCreationUtility {

	protected AccessMethod method0 = AccessMethod.create(new Integer[]{});
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected AccessMethod method3 = AccessMethod.create(new Integer[]{1});
	
	protected Attribute a = Attribute.create(String.class, "a");
	protected Attribute b = Attribute.create(String.class, "b");
	protected Attribute c = Attribute.create(String.class, "c");
	protected Attribute d = Attribute.create(String.class, "d");
	protected Attribute InstanceID = Attribute.create(String.class, "InstanceID");
	protected Attribute i = Attribute.create(String.class, "i");
    
	protected Relation R;
	protected Relation S;
	protected Relation T;
	protected Relation access;
    
	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);                
        this.R = Relation.create("R", new Attribute[]{a,b,c,InstanceID}, new AccessMethod[]{this.method0, this.method2});
        this.S = Relation.create("S", new Attribute[]{b,c,InstanceID}, new AccessMethod[]{this.method0, this.method1, this.method2});
        this.T = Relation.create("T", new Attribute[]{b,c,d,InstanceID}, new AccessMethod[]{this.method0, this.method1, this.method2});
        this.access = Relation.create("Accessible", new Attribute[]{i,InstanceID});
	}
	
	@Test public void test1() {
		//R(c1,c2,c2) S(c2,c1) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.R, this.method0, exposedFacts1);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.S, this.method1, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);
		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		
	}
	
	@Test public void test1b() {
		//R(c1,c2,c2) S(c2,c1) R(c1,c2,c2)
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
		Atom fact3 = Atom.create(this.T, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.R, this.method0, exposedFacts1);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.S, this.method1, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);
		
		Set<Atom> exposedFacts3 = new LinkedHashSet<>();
		exposedFacts3.add(fact3);
		RelationalTerm plan21 = PlanCreationUtility.createSingleAccessPlan(this.T, this.method2, exposedFacts3);
		RelationalTerm plan3 = PlanCreationUtility.createPlan(plan2, plan21);
		
		Assert.assertEquals(0, plan3.getInputAttributes().length);
		Assert.assertEquals(8, plan3.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan3.getOutputAttributes()).toString().contains("c1, c2, c2, c2, c1, c1, c2, c2"));
		Assert.assertEquals(2,plan3.getChildren().length);
		Assert.assertNotNull(plan3.getChild(0));
		Assert.assertTrue(plan3.getChild(0) instanceof DependentJoinTerm);
		Assert.assertTrue(plan3.getChild(1) instanceof SelectionTerm);
	}
	
	@Test public void test2() {
		//R(c1,c2,c2) S(c2,c1) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.R, this.method0, exposedFacts1);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.S, this.method0, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);
		
		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c1, c2, c2, c2, c1"));
		Assert.assertEquals(2,plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof SelectionTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof RenameTerm);
		
	}
	
	@Test public void test2b() {
		//R(c1,c2,c2) S(c2,c1) T(c1,c2,c2)
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
		Atom fact3 = Atom.create(this.T, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.R, this.method0, exposedFacts1);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.S, this.method0, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);
		
		Set<Atom> exposedFacts3 = new LinkedHashSet<>();
		exposedFacts3.add(fact3);
		RelationalTerm plan21 = PlanCreationUtility.createSingleAccessPlan(this.T, this.method0, exposedFacts3);
		RelationalTerm plan3 = PlanCreationUtility.createPlan(plan2, plan21);
		
		Assert.assertEquals(0, plan3.getInputAttributes().length);
		Assert.assertEquals(8, plan3.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan3.getOutputAttributes()).toString().contains("c1, c2, c2, c2, c1, c1, c2, c2"));
		Assert.assertEquals(2,plan3.getChildren().length);
		Assert.assertNotNull(plan3.getChild(0));
		Assert.assertTrue(plan3.getChild(0) instanceof JoinTerm);
		Assert.assertTrue(plan3.getChild(1) instanceof SelectionTerm);
	}
	
	@Test public void test3() {
		//R(c1,c2,'Typed') S(c2,c1) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create("Typed")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.R, this.method0, exposedFacts1);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.S, this.method1, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);
		
		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c1, c2, Typed, c2, c1"));
		Assert.assertEquals(2,plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof SelectionTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof RenameTerm);
		
	}
	
	@Test public void test4() {
		//R(c1,c2,'Typed') S(c2,'Typed2') 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create("Typed")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), TypedConstant.create("Typed2")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.R, this.method0, exposedFacts1);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.S, this.method1, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1,plan11);
		
		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c1, c2, Typed, c2, Typed2"));
		Assert.assertEquals(2,plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof SelectionTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof SelectionTerm);
		
	}
	
	@Test public void test5() {
		//R(c1,c2,'Typed') S('Typed2',c2) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create("Typed")});
		Atom fact2 = Atom.create(this.S, new Term[]{TypedConstant.create("Typed2"), UntypedConstant.create("c2")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.R, this.method0, exposedFacts1);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.S, this.method1, exposedFacts2);

		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1,plan11);
		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c1, c2, Typed, Typed2, c2"));
		Assert.assertEquals(2,plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof SelectionTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof RenameTerm);
		
	}
	
	@Test public void test6() {
		//R(c1,c2,'Typed') S('Typed2',c2) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create("Typed")});
		Atom fact2 = Atom.create(this.S, new Term[]{TypedConstant.create("Typed2"), UntypedConstant.create("c2")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.R, this.method0, exposedFacts1);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.S, this.method2, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan11, plan1);
		
		Assert.assertEquals(1, plan2.getInputAttributes().length);
		Assert.assertEquals(Attribute.create(String.class, "c2"), plan2.getInputAttributes()[0]);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("Typed2, c2, c1, c2, Typed"));
		Assert.assertEquals(2,plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof RenameTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof SelectionTerm);
		
	}
	
}
