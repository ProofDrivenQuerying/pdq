package uk.ac.ox.cs.pdq.test.planner.db.access;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.AccessibleRelation;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema.InferredAccessibleRelation;

import com.google.common.collect.Lists;

public class PredicateFormulaTest {

	private Random random = new Random();

	@Test public void testIsSchemaFact() {
		Relation r = new Relation("r", Lists.newArrayList(
				new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
				TypedConstant<String> c = new TypedConstant<>("c");
				Skolem s = new Skolem("s");
				Assert.assertFalse("PredicateFormula must not be of type InferredAccessible",
						new Predicate(r, Lists.newArrayList(s, c)).getSignature() instanceof InferredAccessibleRelation);
				Predicate p = new Predicate(r, Lists.newArrayList(s, c));
	}

	@Test public void testIsAccessibleFact() {
		Relation r = AccessibleRelation.getInstance();
		Skolem s = new Skolem("s");
		Assert.assertTrue("PredicateFormula must be of type Accessible",
				new Predicate(r, Lists.newArrayList(s)).getSignature() instanceof AccessibleRelation);
	}

	@Test public void testIsInferredAccessibleFact() {
		Relation r = new InferredAccessibleRelation(
				new Relation("r", Lists.newArrayList(
						new Attribute(String.class, "a1"),
						new Attribute(String.class, "a2"))) {});
		TypedConstant<String> c = new TypedConstant<>("c");
		Skolem s = new Skolem("s");
		Assert.assertTrue("PredicateFormula must be of type InferredAccessible",
				new Predicate(r, Lists.newArrayList(s, c)).getSignature() instanceof InferredAccessibleRelation);
	}

}
