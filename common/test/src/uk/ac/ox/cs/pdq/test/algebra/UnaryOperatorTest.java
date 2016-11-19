package uk.ac.ox.cs.pdq.test.algebra;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.db.EntityRelation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class UnaryOperatorTest.
 */
public abstract class UnaryOperatorTest extends RelationalOperatorTest {

	/** The grand child. */
	@Mock RelationalOperator child, grandChild;
	
	/** The output terms. */
	List<Term> outputTerms = Lists.<Term>newArrayList(new Variable("a"), new UntypedConstant("b"), new TypedConstant<>("c"));
	
	/** The input terms. */
	List<Term> inputTerms = Lists.<Term>newArrayList(new Variable("a"), new UntypedConstant("b"));
	
	/** The r. */
	EntityRelation R = new EntityRelation("R");
	
	/** The output type. */
	TupleType outputType = TupleType.DefaultFactory.create(Integer.class, R, String.class);
	
	/** The input type. */
	TupleType inputType = TupleType.DefaultFactory.create(Integer.class, R);

	/**
	 * Gets the depth.
	 *
	 * @return the depth
	 */
	@Test public void getDepth() {
		Mockito.when(child.getDepth()).thenReturn(10);
		Integer depth = 11;
		Assert.assertEquals("Operator's depth must be exactly that of child + 1", depth, getOperator().getDepth());

		Mockito.when(child.getDepth()).thenReturn(123124);
		depth = 123125;
		Assert.assertEquals("Operator's depth must be exactly that of child + 1", depth, getOperator().getDepth());
	}

	/**
	 * Gets the depth negative.
	 *
	 * @return the depth negative
	 */
	@Test(expected=AssertionError.class) 
	public void getDepthNegative() {
		Mockito.when(child.getDepth()).thenReturn(-1);
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
		Mockito.when(child.isClosed()).thenReturn(true);
		Assert.assertEquals("Operator's isClosed must match that of child", child.isClosed(), getOperator().isClosed());

		Mockito.when(child.isClosed()).thenReturn(false);
		Assert.assertEquals("Operator's isClosed must match that of child", child.isClosed(), getOperator().isClosed());
	}

	/**
	 * Checks if does not have any non-unary operators as subexpressions 
	 */
	@Test public void isJoinFree() {
		Mockito.when(child.isJoinFree()).thenReturn(true);
		Assert.assertEquals("Operator's isJoinFree must match that of child", child.isJoinFree(), getOperator().isJoinFree());

		Mockito.when(child.isJoinFree()).thenReturn(false);
		Assert.assertEquals("Operator's isJoinFree must match that of child", child.isJoinFree(), getOperator().isJoinFree());
	}

	/**
	 * Checks if is left deep.
	 */
	@Test public void isLeftDeep() {
		Mockito.when(child.isLeftDeep()).thenReturn(true);
		Assert.assertEquals("Operator's isLeftDeep must match that of child", child.isLeftDeep(), getOperator().isLeftDeep());

		Mockito.when(child.isLeftDeep()).thenReturn(false);
		Assert.assertEquals("Operator's isLeftDeep must match that of child", child.isLeftDeep(), getOperator().isLeftDeep());
	}

	/**
	 * Checks if is right deep.
	 */
	@Test public void isRightDeep() {
		Mockito.when(child.isRightDeep()).thenReturn(true);
		Assert.assertEquals("Operator's isRightDeep must match that of child", child.isRightDeep(), getOperator().isRightDeep());

		Mockito.when(child.isRightDeep()).thenReturn(false);
		Assert.assertEquals("Operator's isRightDeep must match that of child", child.isRightDeep(), getOperator().isRightDeep());
	}

}
