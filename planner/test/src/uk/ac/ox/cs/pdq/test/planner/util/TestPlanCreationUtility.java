package uk.ac.ox.cs.pdq.test.planner.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.util.PlanCreationUtility;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Using the same schema each test creates a different set of facts and uses the
 * PlanCreationUtility to create a plan using the createSingleAccessPlan method.
 * Then assertions making sure the created plan looks as expected.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

public class TestPlanCreationUtility extends PdqTest {

	/**
	 * <pre>
	 * Creates Facts of the R and S tables from the examples:
	 * R(c1,c2,c2) S(c2,c1)
	 *  and creates a singleAccessPlan from them and joins them. 
	 * This joined plan have to have 0 inputs and 5 output attributes.
	 * </pre>
	 */
	@Test
	public void test1() {
		Atom fact1 = Atom.create(this.Ri, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2") });
		Atom fact2 = Atom.create(this.Si, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c1") });

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.Ri, this.method0, exposedFacts1);

		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.Si, this.method1, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);
		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);

	}

	/**
	 * <pre>
	 * Creates Facts of the R S and T tables from the examples: 
	 *  R(c1,c2,c2) S(c2,c1) R(c1,c2,c2)
	 *  and creates a singleAccessPlan from them and joins them. 
	 * This joined plan have to have 0 inputs and 8 output attributes, the output attributes must contain repetition. 
	 * The created plan must have one selection and one dependentJoin term child.
	 * </pre>
	 */
	@Test
	public void test1b() {

		Atom fact1 = Atom.create(this.Ri, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2") });
		Atom fact2 = Atom.create(this.Si, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c1") });
		Atom fact3 = Atom.create(this.Ti, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2") });

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.Ri, this.method0, exposedFacts1);

		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.Si, this.method1, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);

		Set<Atom> exposedFacts3 = new LinkedHashSet<>();
		exposedFacts3.add(fact3);
		RelationalTerm plan21 = PlanCreationUtility.createSingleAccessPlan(this.Ti, this.method2, exposedFacts3);
		RelationalTerm plan3 = PlanCreationUtility.createPlan(plan2, plan21);

		Assert.assertEquals(0, plan3.getInputAttributes().length);
		Assert.assertEquals(8, plan3.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan3.getOutputAttributes()).toString().contains("c1, c2, c2, c2, c1, c1, c2, c2"));
		Assert.assertEquals(2, plan3.getChildren().length);
		Assert.assertNotNull(plan3.getChild(0));
		Assert.assertTrue(plan3.getChild(0) instanceof DependentJoinTerm);
		Assert.assertTrue(plan3.getChild(1) instanceof SelectionTerm);
	}

	/**
	 * <pre>
	 * Creates Facts of the R and S tables from the examples, 
	 * R(c1,c2,c2) S(c2,c1)
	 * and creates a singleAccessPlan from them and joins them. 
	 * This joined plan have to have 0 inputs and 5 output attributes, the output attributes must contain repetition. 
	 * The created plan must have one selection- and one rename- term child.
	 * </pre>
	 */
	@Test
	public void test2() {
		Atom fact1 = Atom.create(this.Ri, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2") });
		Atom fact2 = Atom.create(this.Si, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c1") });

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.Ri, this.method0, exposedFacts1);

		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.Si, this.method0, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);

		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c1, c2, c2, c2, c1"));
		Assert.assertEquals(2, plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof SelectionTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof RenameTerm);
	}

	/**
	 * <pre>
	 * Creates Facts of the R, S, and T tables from the examples:
	 * 		 R(c1,c2,c2) S(c2,c1) T(c1,c2,c2)
	 *  and creates a singleAccessPlan from them and joins them. 
	 * This joined plan have to have 0 inputs and 8 output attributes, the output attributes must contain repetition. 
	 * The created plan must have one selection- and one join- term child.
	 * </pre>
	 */
	@Test
	public void test2b() {
		Atom fact1 = Atom.create(this.Ri, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2") });
		Atom fact2 = Atom.create(this.Si, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c1") });
		Atom fact3 = Atom.create(this.Ti, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c2") });

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.Ri, this.method0, exposedFacts1);

		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.Si, this.method0, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);

		Set<Atom> exposedFacts3 = new LinkedHashSet<>();
		exposedFacts3.add(fact3);
		RelationalTerm plan21 = PlanCreationUtility.createSingleAccessPlan(this.Ti, this.method0, exposedFacts3);
		RelationalTerm plan3 = PlanCreationUtility.createPlan(plan2, plan21);

		Assert.assertEquals(0, plan3.getInputAttributes().length);
		Assert.assertEquals(8, plan3.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan3.getOutputAttributes()).toString().contains("c1, c2, c2, c2, c1, c1, c2, c2"));
		Assert.assertEquals(2, plan3.getChildren().length);
		Assert.assertNotNull(plan3.getChild(0));
		Assert.assertTrue(plan3.getChild(0) instanceof JoinTerm);
		Assert.assertTrue(plan3.getChild(1) instanceof SelectionTerm);
	}

	/**
	 * <pre>
	 * Creates Facts of the R and S tables from the examples:
	 * R(c1,c2,'Typed') S(c2,c1)
	 *  and creates a singleAccessPlan from them and joins them. 
	 * This joined plan have to have 0 inputs and 5 output attributes, the output attributes must contain repetition. 
	 * The created plan must have one selection- and one rename- term child.
	 * </pre>
	 */
	@Test
	public void test3() {
		Atom fact1 = Atom.create(this.Ri, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create(21) });
		Atom fact2 = Atom.create(this.Si, new Term[] { UntypedConstant.create("c2"), UntypedConstant.create("c1") });

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.Ri, this.method0, exposedFacts1);

		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.Si, this.method1, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);

		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c1, c2, 21, c2, c1"));
		Assert.assertEquals(2, plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof SelectionTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof RenameTerm);
	}
	
	/**
	 * <pre>
	 * Creates Facts of the R and S tables from the examples:
	 * R(c1,c2,'Typed') S(c2,'22')
	 *  and creates a singleAccessPlan from them and joins them. 
	 * This joined plan have to have 0 inputs and 5 output attributes, the output attributes must contain repetition. 
	 * The created plan must have two selectionterm child.
	 * </pre>
	 */
	@Test
	public void test4() {
		Atom fact1 = Atom.create(this.Ri, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create(21) });
		Atom fact2 = Atom.create(this.Si, new Term[] { UntypedConstant.create("c2"), TypedConstant.create(22) });

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.Ri, this.method0, exposedFacts1);

		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.Si, this.method1, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);

		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c1, c2, 21, c2, 22"));
		Assert.assertEquals(2, plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof SelectionTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof SelectionTerm);
	}
	
	/**
	 * <pre>
	 * Creates Facts of the R and S tables from the examples:
	 *  R(c1,c2,'Typed') S('22',c2)
	 *  and creates a singleAccessPlan from them and joins them. 
	 * This joined plan have to have 0 inputs and 5 output attributes, the output attributes must contain repetition. 
	 * The created plan must have one selection- and one rename- term child.
	 * </pre>
	 */
	@Test
	public void test5() {
		Atom fact1 = Atom.create(this.Ri, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create(21) });
		Atom fact2 = Atom.create(this.Si, new Term[] { TypedConstant.create(22), UntypedConstant.create("c2") });

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.Ri, this.method0, exposedFacts1);

		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.Si, this.method1, exposedFacts2);

		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan1, plan11);
		Assert.assertEquals(0, plan2.getInputAttributes().length);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c1, c2, 21, 22, c2"));
		Assert.assertEquals(2, plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof SelectionTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof RenameTerm);
	}

	/**
	 * <pre>
	 * Creates Facts of the R and S tables from the examples:
	 *  R(c1,c2,'Typed') S('22',c2)
	 *  and creates a singleAccessPlan from them and joins them. 
	 * This joined plan have to have 0 inputs and 5 output attributes, the output attributes must contain repetition. 
	 * The created plan must have one selection- and one rename- term child.
	 * </pre>
	 */
	@Test
	public void test6() {
		// R(c1,c2,'Typed') S('22',c2)
		Atom fact1 = Atom.create(this.Ri, new Term[] { UntypedConstant.create("c1"), UntypedConstant.create("c2"), TypedConstant.create(21) });
		Atom fact2 = Atom.create(this.Si, new Term[] { TypedConstant.create(22), UntypedConstant.create("c2") });

		Set<Atom> exposedFacts1 = new LinkedHashSet<>();
		exposedFacts1.add(fact1);
		RelationalTerm plan1 = PlanCreationUtility.createSingleAccessPlan(this.Ri, this.method0, exposedFacts1);

		Set<Atom> exposedFacts2 = new LinkedHashSet<>();
		exposedFacts2.add(fact2);
		RelationalTerm plan11 = PlanCreationUtility.createSingleAccessPlan(this.Si, this.method2, exposedFacts2);
		RelationalTerm plan2 = PlanCreationUtility.createPlan(plan11, plan1);

		Assert.assertEquals(1, plan2.getInputAttributes().length);
		Assert.assertEquals(Attribute.create(Integer.class, "c2"), plan2.getInputAttributes()[0]);
		Assert.assertEquals(5, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("22, c2, c1, c2, 21"));
		Assert.assertEquals(2, plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof RenameTerm);
		Assert.assertTrue(plan2.getChild(1) instanceof SelectionTerm);
	}

	// We want to expose these two facts
	// customer(c46,c52,c53,c40,c54,c55,c56,c57),
	// customer(c31,c38,c39,c40,c41,c42,c43,c44)
	// using a single access on the 4th position.
	// We should produce an open plan that accesses relation customer once.
	// Then we have two rename terms with child the same access
	// the first term renames the attributes to c31,c38,c39,c40,c41,c42,c43,c44
	// the first term renames the attributes to c46,c52,c53,c40,c54,c55,c56,c57
	// These two terms should be joined on the attribute c40
	@Test
	public void test7() {
		AccessMethodDescriptor method0 = AccessMethodDescriptor.create(new Integer[] { 3 });
		Attribute a = Attribute.create(String.class, "a");
		Attribute b = Attribute.create(String.class, "b");
		Attribute c = Attribute.create(String.class, "c");
		Attribute d = Attribute.create(String.class, "d");
		Attribute e = Attribute.create(String.class, "e");
		Attribute f = Attribute.create(String.class, "f");
		Attribute g = Attribute.create(String.class, "g");
		Attribute h = Attribute.create(String.class, "h");

		Relation customer = Relation.create("customer", new Attribute[] { a, b, c, d, e, f, g, h }, new AccessMethodDescriptor[] { method0 });
		Atom fact1 = Atom.create(customer, new UntypedConstant[] { UntypedConstant.create("c46"), UntypedConstant.create("c52"), UntypedConstant.create("c53"),
				UntypedConstant.create("c40"), UntypedConstant.create("c54"), UntypedConstant.create("c55"), UntypedConstant.create("c56"), UntypedConstant.create("c57") });

		Atom fact2 = Atom.create(customer, new UntypedConstant[] { UntypedConstant.create("c31"), UntypedConstant.create("c38"), UntypedConstant.create("c39"),
				UntypedConstant.create("c40"), UntypedConstant.create("c41"), UntypedConstant.create("c42"), UntypedConstant.create("c43"), UntypedConstant.create("c44") });

		Set<Atom> facts = new LinkedHashSet<>();
		facts.add(fact1);
		facts.add(fact2);
		RelationalTerm plan2 = PlanCreationUtility.createSingleAccessPlan(customer, method0, facts);
		Assert.assertEquals(1, plan2.getInputAttributes().length);
		Assert.assertEquals(Attribute.create(String.class, "c40"), plan2.getInputAttributes()[0]);
		Assert.assertEquals(16, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("c31, c38, c39, c40, c41, c42, c43, c44"));
		Assert.assertEquals(2, plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof RenameTerm);
		Assert.assertEquals(1, plan2.getAccesses().size());
	}
	@Test
	public void test8() {
		AccessMethodDescriptor method0 = AccessMethodDescriptor.create(new Integer[] { 3 });
		Attribute a = Attribute.create(String.class, "a");
		Attribute b = Attribute.create(String.class, "b");
		Attribute c = Attribute.create(String.class, "c");
		Attribute d = Attribute.create(String.class, "d");
		Attribute d2 = Attribute.create(String.class, "d2");
		Attribute e = Attribute.create(String.class, "e");
		Attribute f = Attribute.create(String.class, "f");
		Attribute g = Attribute.create(String.class, "g");
		Attribute h = Attribute.create(String.class, "h");

		Relation customer = Relation.create("customer", new Attribute[] { a, b, c, d, e, f, g, h }, new AccessMethodDescriptor[] { method0 });
		Relation customer2 = Relation.create("customer2", new Attribute[] { a, b, c, d2, e, f, g, h }, new AccessMethodDescriptor[] { method0 });
		Atom fact1 = Atom.create(customer, new UntypedConstant[] { UntypedConstant.create("c46"), UntypedConstant.create("c52"), UntypedConstant.create("c53"),
				UntypedConstant.create("c40"), UntypedConstant.create("c54"), UntypedConstant.create("c55"), UntypedConstant.create("c56"), UntypedConstant.create("c57") });

		Atom fact2 = Atom.create(customer, new UntypedConstant[] { UntypedConstant.create("c31"), UntypedConstant.create("c38"), UntypedConstant.create("c39"),
				UntypedConstant.create("c40"), UntypedConstant.create("c41"), UntypedConstant.create("c42"), UntypedConstant.create("c43"), UntypedConstant.create("c44") });

		Set<Atom> facts = new LinkedHashSet<>();
		facts.add(fact1);
		facts.add(fact2);
		RelationalTerm plan2 = JoinTerm.create(AccessTerm.create(customer, method0), AccessTerm.create(customer2, method0));
		Assert.assertEquals(2, plan2.getInputAttributes().length);
		Assert.assertEquals(Attribute.create(String.class, "d"), plan2.getInputAttributes()[0]);
		Assert.assertEquals(16, plan2.getOutputAttributes().length);
		Assert.assertTrue(Arrays.asList(plan2.getOutputAttributes()).toString().contains("a, b, c, d, e, f, g, h, a, b, c, d2, e, f, g, h"));
		Assert.assertEquals(2, plan2.getChildren().length);
		Assert.assertNotNull(plan2.getChild(0));
		Assert.assertTrue(plan2.getChild(0) instanceof AccessTerm);
		Assert.assertEquals(2, plan2.getAccesses().size());
	}

}
