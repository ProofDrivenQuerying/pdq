package uk.ac.ox.cs.pdq.test.algebra;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.db.EntityRelation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class NaryOperatorTest.
 */
public abstract class NaryOperatorTest extends RelationalOperatorTest {
	
	/** The r. */
	EntityRelation R = new EntityRelation("R");
	
	/** The s. */
	EntityRelation S = new EntityRelation("S");

	/** The child4. */
	@Mock RelationalOperator child1, child2, child3, child4;
	
	/** The output terms1. */
	List<Term> outputTerms1 = Lists.<Term>newArrayList(new Variable("a"));
	
	/** The input terms1. */
	List<Term> inputTerms1 = Lists.<Term>newArrayList(new Variable("a"));
	
	/** The output type1. */
	TupleType outputType1 = TupleType.DefaultFactory.create(Integer.class);
	
	/** The input type1. */
	TupleType inputType1 = TupleType.DefaultFactory.create(Integer.class);
	
	/** The output terms2. */
	List<Term> outputTerms2 = Lists.<Term>newArrayList(new Variable("a"), new UntypedConstant("b"), new TypedConstant<>("c"));
	
	/** The input terms2. */
	List<Term> inputTerms2 = Lists.<Term>newArrayList();
	
	/** The output type2. */
	TupleType outputType2 = TupleType.DefaultFactory.create(Integer.class, R, String.class);
	
	/** The input type2. */
	TupleType inputType2 = TupleType.EmptyTupleType;
	
	/** The output terms3. */
	List<Term> outputTerms3 = Lists.<Term>newArrayList(new UntypedConstant("b"), new TypedConstant<>("d"));
	
	/** The input terms3. */
	List<Term> inputTerms3 = Lists.<Term>newArrayList(new UntypedConstant("b"));
	
	/** The output type3. */
	TupleType outputType3 = TupleType.DefaultFactory.create(R, String.class);
	
	/** The input type3. */
	TupleType inputType3 = TupleType.DefaultFactory.create(R);

	/** The output terms. */
	List<Term> outputTerms = Lists.<Term>newArrayList(new Variable("a"), new Variable("a"), new UntypedConstant("b"), new TypedConstant<>("c"), new UntypedConstant("b"), new TypedConstant<>("d"));
	
	/** The input terms. */
	List<Term> inputTerms = Lists.<Term>newArrayList(new Variable("a"), new UntypedConstant("b"));
	
	/** The output type. */
	TupleType outputType = TupleType.DefaultFactory.create(Integer.class, Integer.class, R, String.class, R, String.class);
	
	/** The input type. */
	TupleType inputType = TupleType.DefaultFactory.create(Integer.class, R);
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.RelationalOperatorTest#setup()
	 */
	@Before public void setup() throws RelationalOperatorException {
		super.setup();
        MockitoAnnotations.initMocks(this);
		Mockito.when(child1.getColumns()).thenReturn(outputTerms1);
		Mockito.when(child1.getType()).thenReturn(outputType1);
		Mockito.when(child1.getInputTerms()).thenReturn(inputTerms1);
		Mockito.when(child1.getInputType()).thenReturn(inputType1);
		Mockito.when(child1.deepCopy()).thenReturn(child1);

		Mockito.when(child2.getColumns()).thenReturn(outputTerms2);
		Mockito.when(child2.getType()).thenReturn(outputType2);
		Mockito.when(child2.getInputTerms()).thenReturn(inputTerms2);
		Mockito.when(child2.getInputType()).thenReturn(inputType2);
		Mockito.when(child2.deepCopy()).thenReturn(child2);

		Mockito.when(child3.getColumns()).thenReturn(outputTerms3);
		Mockito.when(child3.getType()).thenReturn(outputType3);
		Mockito.when(child3.getInputTerms()).thenReturn(inputTerms3);
		Mockito.when(child3.getInputType()).thenReturn(inputType3);
		Mockito.when(child3.deepCopy()).thenReturn(child3);

		Mockito.when(child4.getColumns()).thenReturn(Lists.<Term>newArrayList());
		Mockito.when(child4.getType()).thenReturn(TupleType.EmptyTupleType);
		Mockito.when(child4.getInputTerms()).thenReturn(Lists.<Term>newArrayList());
		Mockito.when(child4.getInputType()).thenReturn(TupleType.EmptyTupleType);
		Mockito.when(child4.deepCopy()).thenReturn(child4);
	}

	/**
	 * Gets the depth.
	 *
	 * @return the depth
	 */
	@Test public void getDepth() {
		Mockito.when(child1.getDepth()).thenReturn(10);
		Mockito.when(child2.getDepth()).thenReturn(1000);
		Mockito.when(child3.getDepth()).thenReturn(100);
		Integer depth = 1001;
		Assert.assertEquals("Operator's depth must be exactly that of deepest child + 1", depth, getOperator().getDepth());

		Mockito.when(child1.getDepth()).thenReturn(10);
		Mockito.when(child2.getDepth()).thenReturn(2);
		Mockito.when(child3.getDepth()).thenReturn(7);
		depth = 11;
		Assert.assertEquals("Operator's depth must be exactly that of deepest child + 1", depth, getOperator().getDepth());
	}

	/**
	 * Gets the depth negative.
	 *
	 * @return the depth negative
	 */
	@Test(expected=AssertionError.class) 
	public void getDepthNegative() {
		Mockito.when(child1.getDepth()).thenReturn(10);
		Mockito.when(child2.getDepth()).thenReturn(-100);
		Mockito.when(child3.getDepth()).thenReturn(1000);
		getOperator().getDepth();
	}

	/**
	 * Gets the negative column.
	 *
	 * @return the negative column
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void getNegativeColumn() {
		getOperator().getColumn(-1);
	}

	/**
	 * Checks if is closed.
	 */
	@Test public void isClosed() {
		Mockito.when(child1.isClosed()).thenReturn(true);
		Mockito.when(child2.isClosed()).thenReturn(true);
		Mockito.when(child3.isClosed()).thenReturn(true);
		Assert.assertTrue("Operator isClosed when all its children are", getOperator().isClosed());

		Mockito.when(child1.isClosed()).thenReturn(true);
		Mockito.when(child2.isClosed()).thenReturn(false);
		Mockito.when(child3.isClosed()).thenReturn(true);
		Assert.assertFalse("Operator isClosed when all its children are", getOperator().isClosed());

		Mockito.when(child1.isClosed()).thenReturn(false);
		Mockito.when(child2.isClosed()).thenReturn(false);
		Mockito.when(child3.isClosed()).thenReturn(false);
		Assert.assertFalse("Operator isClosed when all its children are", getOperator().isClosed());
	}

	/**
	 * Checks if does not have any non-unary operators as subexpressions.
	 */
	@Test public void isJoinFree() {
		Mockito.when(child1.isJoinFree()).thenReturn(true);
		Mockito.when(child2.isJoinFree()).thenReturn(true);
		Mockito.when(child3.isJoinFree()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are never join-free", getOperator().isJoinFree());
		
		Mockito.when(child1.isJoinFree()).thenReturn(true);
		Mockito.when(child2.isJoinFree()).thenReturn(false);
		Mockito.when(child3.isJoinFree()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are never join-free", getOperator().isJoinFree());

		Mockito.when(child1.isJoinFree()).thenReturn(false);
		Mockito.when(child2.isJoinFree()).thenReturn(false);
		Mockito.when(child3.isJoinFree()).thenReturn(false);
		Assert.assertFalse("NaryOperator's are never join-free", getOperator().isJoinFree());
	}

	/**
	 * Checks if is left deep.
	 */
	@Test public void isLeftDeep() {
		Mockito.when(child1.isJoinFree()).thenReturn(false); Mockito.when(child1.isLeftDeep()).thenReturn(true);
		Mockito.when(child2.isJoinFree()).thenReturn(false); Mockito.when(child2.isLeftDeep()).thenReturn(true);
		Mockito.when(child3.isJoinFree()).thenReturn(false); Mockito.when(child3.isLeftDeep()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are join-free", getOperator().isLeftDeep());
		
		Mockito.when(child1.isJoinFree()).thenReturn(false); Mockito.when(child1.isLeftDeep()).thenReturn(true);
		Mockito.when(child2.isJoinFree()).thenReturn(true);  Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isJoinFree()).thenReturn(false); Mockito.when(child3.isLeftDeep()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are join-free", getOperator().isLeftDeep());

		Mockito.when(child1.isJoinFree()).thenReturn(false); Mockito.when(child1.isLeftDeep()).thenReturn(true);
		Mockito.when(child2.isJoinFree()).thenReturn(true);  Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isJoinFree()).thenReturn(true);  Mockito.when(child3.isLeftDeep()).thenReturn(false);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are join-free", getOperator().isLeftDeep());

		Mockito.when(child1.isJoinFree()).thenReturn(true); Mockito.when(child1.isLeftDeep()).thenReturn(false);
		Mockito.when(child2.isJoinFree()).thenReturn(true); Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isJoinFree()).thenReturn(true); Mockito.when(child3.isLeftDeep()).thenReturn(false);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are join-free", getOperator().isLeftDeep());
	}

	/**
	 * Checks if is right deep.
	 */
	@Test public void isRightDeep() {
		Mockito.when(child1.isJoinFree()).thenReturn(false); Mockito.when(child1.isRightDeep()).thenReturn(true);
		Mockito.when(child2.isJoinFree()).thenReturn(false); Mockito.when(child2.isRightDeep()).thenReturn(true);
		Mockito.when(child3.isJoinFree()).thenReturn(false); Mockito.when(child3.isRightDeep()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are join-free", getOperator().isRightDeep());
		
		Mockito.when(child1.isJoinFree()).thenReturn(false); Mockito.when(child1.isRightDeep()).thenReturn(true);
		Mockito.when(child2.isJoinFree()).thenReturn(true);  Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isJoinFree()).thenReturn(false); Mockito.when(child3.isRightDeep()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are join-free", getOperator().isRightDeep());

		Mockito.when(child1.isJoinFree()).thenReturn(true); Mockito.when(child1.isRightDeep()).thenReturn(false);
		Mockito.when(child2.isJoinFree()).thenReturn(true);  Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isJoinFree()).thenReturn(false); Mockito.when(child3.isRightDeep()).thenReturn(true);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are join-free", getOperator().isRightDeep());

		Mockito.when(child1.isJoinFree()).thenReturn(true); Mockito.when(child1.isRightDeep()).thenReturn(false);
		Mockito.when(child2.isJoinFree()).thenReturn(true);  Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isJoinFree()).thenReturn(true); Mockito.when(child3.isRightDeep()).thenReturn(false);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are join-free", getOperator().isRightDeep());
	}
}
