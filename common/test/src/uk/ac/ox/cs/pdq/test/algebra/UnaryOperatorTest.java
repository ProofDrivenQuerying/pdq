package uk.ac.ox.cs.pdq.test.algebra;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.db.EntityRelation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

public abstract class UnaryOperatorTest extends RelationalOperatorTest {

	@Mock RelationalOperator child, grandChild;
	List<Term> outputTerms = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"), new TypedConstant<>("c"));
	List<Term> inputTerms = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"));
	EntityRelation R = new EntityRelation("R");
	TupleType outputType = TupleType.DefaultFactory.create(Integer.class, R, String.class);
	TupleType inputType = TupleType.DefaultFactory.create(Integer.class, R);

	@Test public void getDepth() {
		Mockito.when(child.getDepth()).thenReturn(10);
		Integer depth = 11;
		Assert.assertEquals("Operator's depth must be exactly that of child + 1", depth, getOperator().getDepth());

		Mockito.when(child.getDepth()).thenReturn(123124);
		depth = 123125;
		Assert.assertEquals("Operator's depth must be exactly that of child + 1", depth, getOperator().getDepth());
	}

	@Test(expected=AssertionError.class) 
	public void getDepthNegative() {
		Mockito.when(child.getDepth()).thenReturn(-1);
		getOperator().getDepth();
	}

	@Test(expected=IllegalArgumentException.class) 
	public void getNegativeColumn() {
		getOperator().getColumn(-1);
	}

	@Test public void isClosed() {
		Mockito.when(child.isClosed()).thenReturn(true);
		Assert.assertEquals("Operator's isClosed must match that of child", child.isClosed(), getOperator().isClosed());

		Mockito.when(child.isClosed()).thenReturn(false);
		Assert.assertEquals("Operator's isClosed must match that of child", child.isClosed(), getOperator().isClosed());
	}

	@Test public void isQuasiLeaf() {
		Mockito.when(child.isQuasiLeaf()).thenReturn(true);
		Assert.assertEquals("Operator's isQuasiLeaf must match that of child", child.isQuasiLeaf(), getOperator().isQuasiLeaf());

		Mockito.when(child.isQuasiLeaf()).thenReturn(false);
		Assert.assertEquals("Operator's isQuasiLeaf must match that of child", child.isQuasiLeaf(), getOperator().isQuasiLeaf());
	}

	@Test public void isLeftDeep() {
		Mockito.when(child.isLeftDeep()).thenReturn(true);
		Assert.assertEquals("Operator's isLeftDeep must match that of child", child.isLeftDeep(), getOperator().isLeftDeep());

		Mockito.when(child.isLeftDeep()).thenReturn(false);
		Assert.assertEquals("Operator's isLeftDeep must match that of child", child.isLeftDeep(), getOperator().isLeftDeep());
	}

	@Test public void isRightDeep() {
		Mockito.when(child.isRightDeep()).thenReturn(true);
		Assert.assertEquals("Operator's isRightDeep must match that of child", child.isRightDeep(), getOperator().isRightDeep());

		Mockito.when(child.isRightDeep()).thenReturn(false);
		Assert.assertEquals("Operator's isRightDeep must match that of child", child.isRightDeep(), getOperator().isRightDeep());
	}

}
