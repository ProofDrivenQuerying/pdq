package uk.ac.ox.cs.pdq.test.planner.accessibleschema;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class PredicateFormulaTest.
 */
public class AtomTest {
	
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
		Relation r = Relation.create("r", new Attribute[]{
				Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});
				TypedConstant c = TypedConstant.create("c");
				UntypedConstant s = UntypedConstant.create("s");
				Assert.assertFalse("PredicateFormula must not be of type InferredAccessible",
						Atom.create(r, new Term[]{s, c}).getPredicate().getName().startsWith(AccessibleSchema.inferredAccessiblePrefix));
	}

	/**
	 * Test is accessible fact.
	 */
	@Test public void testIsAccessibleFact() {
		Relation r = AccessibleSchema.accessibleRelation;
		UntypedConstant s = UntypedConstant.create("s");
		Assert.assertTrue("PredicateFormula must be of type Accessible",
				Atom.create(r, new Term[]{s}).getPredicate().getName().startsWith("Accessible"));
	}

	/**
	 * Test is inferred accessible fact.
	 */
	@Test public void testIsInferredAccessibleFact() {
		Relation r = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "r", new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")}, new AccessMethod[]{AccessMethod.create(new Integer[]{})}, false);
		TypedConstant c = TypedConstant.create("c");
		UntypedConstant s = UntypedConstant.create("s");
		Assert.assertTrue("PredicateFormula must be of type InferredAccessible",
				Atom.create(r, new Term[]{s, c}).getPredicate().getName().startsWith(AccessibleSchema.inferredAccessiblePrefix));
	}

}
