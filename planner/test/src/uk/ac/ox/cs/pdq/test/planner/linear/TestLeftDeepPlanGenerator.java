package uk.ac.ox.cs.pdq.test.planner.linear;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.linear.LeftDeepPlanGenerator;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */

public class TestLeftDeepPlanGenerator {

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
		RelationalTerm plan1 = LeftDeepPlanGenerator.createLeftDeepPlan(this.R, this.method0, exposedFacts1, null);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan2 = LeftDeepPlanGenerator.createLeftDeepPlan(this.S, this.method1, exposedFacts2, plan1);
		
		//TODO add assertions 
	}
	
	@Test public void test1b() {
		//R(c1,c2,c2) S(c2,c1) R(c1,c2,c2)
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
		Atom fact3 = Atom.create(this.T, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = LeftDeepPlanGenerator.createLeftDeepPlan(this.R, this.method0, exposedFacts1, null);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan2 = LeftDeepPlanGenerator.createLeftDeepPlan(this.S, this.method1, exposedFacts2, plan1);
		
		Set<Atom> exposedFacts3 = new LinkedHashSet<>();
		exposedFacts3.add(fact3);
		RelationalTerm plan3 = LeftDeepPlanGenerator.createLeftDeepPlan(this.T, this.method2, exposedFacts3, plan2);
		
		//TODO add assertions 
	}
	
	@Test public void test2() {
		//R(c1,c2,c2) S(c2,c1) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = LeftDeepPlanGenerator.createLeftDeepPlan(this.R, this.method0, exposedFacts1, null);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan2 = LeftDeepPlanGenerator.createLeftDeepPlan(this.S, this.method0, exposedFacts2, plan1);
		
		//TODO add assertions
	}
	
	@Test public void test2b() {
		//R(c1,c2,c2) S(c2,c1) T(c1,c2,c2)
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
		Atom fact3 = Atom.create(this.T, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2")});

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = LeftDeepPlanGenerator.createLeftDeepPlan(this.R, this.method0, exposedFacts1, null);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan2 = LeftDeepPlanGenerator.createLeftDeepPlan(this.S, this.method0, exposedFacts2, plan1);
		
		Set<Atom> exposedFacts3 = new LinkedHashSet<>();
		exposedFacts3.add(fact3);
		RelationalTerm plan3 = LeftDeepPlanGenerator.createLeftDeepPlan(this.T, this.method0, exposedFacts3, plan2);
		
		//TODO add assertions
	}
	
	@Test public void test3() {
		//R(c1,c2,'Typed') S(c2,c1) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create("Typed")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), UntypedConstant.create("c1")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = LeftDeepPlanGenerator.createLeftDeepPlan(this.R, this.method0, exposedFacts1, null);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan2 = LeftDeepPlanGenerator.createLeftDeepPlan(this.S, this.method1, exposedFacts2, plan1);
		
		//TODO add assertions 
	}
	
	@Test public void test4() {
		//R(c1,c2,'Typed') S(c2,'Typed2') 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create("Typed")});
		Atom fact2 = Atom.create(this.S, new Term[]{UntypedConstant.create("c2"), TypedConstant.create("Typed2")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = LeftDeepPlanGenerator.createLeftDeepPlan(this.R, this.method0, exposedFacts1, null);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan2 = LeftDeepPlanGenerator.createLeftDeepPlan(this.S, this.method1, exposedFacts2, plan1);
		
		//TODO add assertions 
	}
	
	@Test public void test5() {
		//R(c1,c2,'Typed') S('Typed2',c2) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create("Typed")});
		Atom fact2 = Atom.create(this.S, new Term[]{TypedConstant.create("Typed2"), UntypedConstant.create("c2")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = LeftDeepPlanGenerator.createLeftDeepPlan(this.R, this.method0, exposedFacts1, null);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan2 = LeftDeepPlanGenerator.createLeftDeepPlan(this.S, this.method1, exposedFacts2, plan1);
		
		//TODO add assertions 
	}
	
	@Test public void test6() {
		//R(c1,c2,'Typed') S('Typed2',c2) 
		Atom fact1 = Atom.create(this.R, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create("Typed")});
		Atom fact2 = Atom.create(this.S, new Term[]{TypedConstant.create("Typed2"), UntypedConstant.create("c2")});
				
		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = LeftDeepPlanGenerator.createLeftDeepPlan(this.R, this.method0, exposedFacts1, null);
		
		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan2 = LeftDeepPlanGenerator.createLeftDeepPlan(this.S, this.method2, exposedFacts2, plan1);
		
		//TODO add assertions 
	}
	
}
