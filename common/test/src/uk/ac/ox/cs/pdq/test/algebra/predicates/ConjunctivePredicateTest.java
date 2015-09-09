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

import com.google.common.collect.Lists;

/**
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public class ConjunctivePredicateTest {

	@Mock Predicate subPred1, subPred2, subPred3;
	ConjunctivePredicate<Predicate> pred;
	Tuple t = Tuple.EmptyTuple;

	@Before
	public void setup() {
        MockitoAnnotations.initMocks(this);
		Mockito.when(subPred1.isSatisfied(t)).thenReturn(true);
		Mockito.when(subPred2.isSatisfied(t)).thenReturn(true);
		Mockito.when(subPred3.isSatisfied(t)).thenReturn(false);
	}
	
	@Test public void initConjunctivePredicate() {
		pred = new ConjunctivePredicate<>();
		Assert.assertEquals("Empty constructor should yield conjunction of size 0", 0, pred.size());
		Assert.assertTrue("Empty constructor should yield empty conjunction", pred.isEmpty());
	}

	@Test public void initConjunctivePredicateT() {
		pred = new ConjunctivePredicate<>(subPred1);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 1", 1, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());

		pred = new ConjunctivePredicate<>(subPred2);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 1", 1, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());

		pred = new ConjunctivePredicate<>(subPred3);
		Assert.assertEquals("Singleton constructor should yield conjunction of size 1", 1, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());
	}

	@Test public void initConjunctivePredicateCollection() {
		pred = new ConjunctivePredicate<>(Lists.newArrayList(subPred1, subPred2, subPred3));
		Assert.assertEquals("Empty constructor should yield conjunction of size 3", 3, pred.size());
		Assert.assertFalse("Empty constructor should yield non-empty conjunction", pred.isEmpty());
	}

	@Test(expected=IllegalArgumentException.class)
	public void initConjunctivePredicateCollectionNoNullElement() {
		pred = new ConjunctivePredicate<>(Lists.newArrayList(subPred1, null, subPred3));
	}

	@Test(expected=IllegalArgumentException.class)
	public void initConjunctivePredicateNotNullArgument() {
		pred = new ConjunctivePredicate<>((Predicate) null);
	}

	@Test public void addPredicate() {
		pred = new ConjunctivePredicate<>();
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

	@Test(expected=IllegalArgumentException.class)
	public void addPredicateNotNull() {
		pred = new ConjunctivePredicate<>();
		pred.addPredicate(null);
	}

	@Test public void isSatisfied() {
		pred = new ConjunctivePredicate<>(subPred1);
		Assert.assertTrue("Conjunctive singleton is true when sub predicate is true", pred.isSatisfied(t));

		pred = new ConjunctivePredicate<>(Lists.newArrayList(subPred1, subPred2, subPred1));
		Assert.assertTrue("Conjunctive is true when one all sub predicates are true", pred.isSatisfied(t));

		pred = new ConjunctivePredicate<>(subPred3);
		Assert.assertFalse("Conjunctive singleton is false when sub predicate is false", pred.isSatisfied(t));

		pred = new ConjunctivePredicate<>(Lists.newArrayList(subPred1, subPred2, subPred3));
		Assert.assertFalse("Conjunctive is false when one of all sub predicate is false", pred.isSatisfied(t));

		pred = new ConjunctivePredicate<>(Lists.newArrayList(subPred3, subPred3, subPred3));
		Assert.assertFalse("Conjunctive is false when all sub predicates are false", pred.isSatisfied(t));
	}

	@Test public void iterator() {
		// TODO
	}

	@Test public void equals() {
		// TODO
	}

	@Test public void testHashCode() {
		// TODO
	}
}
