package uk.ac.ox.cs.pdq.test.planner.accessible;

import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema.InferredAccessibleRelation;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class PredicateFormulaTest.
 */
public class AtomTest {

	/** The random. */
	private Random random = new Random();
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	/**
	 * Test is schema fact.
	 */
	@Test public void testIsSchemaFact() {
		Relation r = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
				TypedConstant<String> c = new TypedConstant<>("c");
				UntypedConstant s = new UntypedConstant("s");
				Assert.assertFalse("PredicateFormula must not be of type InferredAccessible",
						new Atom(r, Lists.newArrayList(s, c)).getPredicate() instanceof InferredAccessibleRelation);
				Atom p = new Atom(r, Lists.newArrayList(s, c));
	}

	/**
	 * Test is accessible fact.
	 */
	@Test public void testIsAccessibleFact() {
		Relation r = AccessibleRelation.getInstance();
		UntypedConstant s = new UntypedConstant("s");
		Assert.assertTrue("PredicateFormula must be of type Accessible",
				new Atom(r, Lists.newArrayList(s)).getPredicate() instanceof AccessibleRelation);
	}

	/**
	 * Test is inferred accessible fact.
	 */
	@Test public void testIsInferredAccessibleFact() {
		Relation r = new InferredAccessibleRelation(
				new Relation("r", Lists.newArrayList(
						new Attribute(String.class, "a1"),
						new Attribute(String.class, "a2"))) {});
		TypedConstant<String> c = new TypedConstant<>("c");
		UntypedConstant s = new UntypedConstant("s");
		Assert.assertTrue("PredicateFormula must be of type InferredAccessible",
				new Atom(r, Lists.newArrayList(s, c)).getPredicate() instanceof InferredAccessibleRelation);
	}

}
