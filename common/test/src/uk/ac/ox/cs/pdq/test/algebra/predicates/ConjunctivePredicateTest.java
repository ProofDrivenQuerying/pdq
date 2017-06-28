package uk.ac.ox.cs.pdq.test.algebra.predicates;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class ConjunctivePredicateTest.
 *
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public class ConjunctivePredicateTest {

	/** The sub pred3. */
	@Mock Condition subPred1, subPred2, subPred3;
	
	/** The pred. */
	ConjunctiveCondition<Condition> pred;
	
	/** The t. */
	Tuple t = Tuple.EmptyTuple;

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);
		Mockito.when(subPred1.isSatisfied(t)).thenReturn(true);
		Mockito.when(subPred2.isSatisfied(t)).thenReturn(true);
		Mockito.when(subPred3.isSatisfied(t)).thenReturn(false);
	}
	
	/**
	 * Inits the conjunctive predicate.
	 */
	@Test public void initConjunctivePredicate() {
		pred = new ConjunctiveCondition<>();
		Assert.assertEquals("Empty constructor should yield conjunction of size 0", 0, pred.size());
		Assert.assertTrue("Empty constructor should yield empty conjunction", pred.isEmpty());
	}

	/**
	 * Inits the conjunctive predicate t.
	 */
	@Test public void initConjunctivePredicateT() {
		pred = new ConjunctiveCondition<>(subPred1);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 1", 1, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());

		pred = new ConjunctiveCondition<>(subPred2);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 1", 1, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());

		pred = new ConjunctiveCondition<>(subPred3);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 1", 1, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());
	}

	/**
	 * Inits the conjunctive predicate collection.
	 */
	@Test public void initConjunctivePredicateCollection() {
		pred = new ConjunctiveCondition<>(Lists.newArrayList(subPred1, subPred2, subPred3));
		Assert.assertEquals("Empty constructor should yield conjunction of size 3", 3, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());
	}

	/**
	 * Inits the conjunctive predicate collection no null element.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void initConjunctivePredicateCollectionNoNullElement() {
		pred = new ConjunctiveCondition<>(Lists.newArrayList(subPred1, null, subPred3));
	}

	/**
	 * Inits the conjunctive predicate not null argument.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void initConjunctivePredicateNotNullArgument() {
		pred = new ConjunctiveCondition<>((Condition) null);
	}

	/**
	 * Adds the predicate.
	 */
	@Test public void addPredicate() {
		pred = new ConjunctiveCondition<>();
		Assert.assertEquals("Empty constructor should yield conjunction of size 0", 0, pred.size());
		Assert.assertTrue("Empty constructor should yield empty conjunction", pred.isEmpty());
		
		pred.addPredicate(subPred1);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 1", 1, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());

		pred.addPredicate(subPred2);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 2", 2, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());

		pred.addPredicate(subPred3);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 3", 3, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());
	}

	/**
	 * Adds the predicate not null.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void addPredicateNotNull() {
		pred = new ConjunctiveCondition<>();
		pred.addPredicate(null);
	}

	/**
	 * Checks if is satisfied.
	 */
	@Test public void isSatisfied() {
		pred = new ConjunctiveCondition<>(subPred1);
		Assert.assertTrue("Conjunctive singleton is true when sub predicate is true", pred.isSatisfied(t));

		pred = new ConjunctiveCondition<>(Lists.newArrayList(subPred1, subPred2, subPred1));
		Assert.assertTrue("Conjunctive is true when one all sub predicates are true", pred.isSatisfied(t));

		pred = new ConjunctiveCondition<>(subPred3);
		Assert.assertFalse("Conjunctive singleton is false when sub predicate is false", pred.isSatisfied(t));

		pred = new ConjunctiveCondition<>(Lists.newArrayList(subPred1, subPred2, subPred3));
		Assert.assertFalse("Conjunctive is false when one of all sub predicate is false", pred.isSatisfied(t));

		pred = new ConjunctiveCondition<>(Lists.newArrayList(subPred3, subPred3, subPred3));
		Assert.assertFalse("Conjunctive is false when all sub predicates are false", pred.isSatisfied(t));
	}

	/**
	 * Iterator.
	 */
	@Test public void iterator() {
		// TODO
	}

	/**
	 * Equals.
	 */
	@Test public void equals() {
		// TODO
	}

	/**
	 * Test hash code.
	 */
	@Test public void testHashCode() {
		// TODO
	}
}
