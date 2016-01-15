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
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;

public abstract class NaryOperatorTest extends RelationalOperatorTest {
	
	EntityRelation R = new EntityRelation("R");
	EntityRelation S = new EntityRelation("S");

	@Mock RelationalOperator child1, child2, child3, child4;
	List<Term> outputTerms1 = Lists.<Term>newArrayList(new Variable("a"));
	List<Term> inputTerms1 = Lists.<Term>newArrayList(new Variable("a"));
	TupleType outputType1 = TupleType.DefaultFactory.create(Integer.class);
	TupleType inputType1 = TupleType.DefaultFactory.create(Integer.class);
	
	List<Term> outputTerms2 = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"), new TypedConstant<>("c"));
	List<Term> inputTerms2 = Lists.<Term>newArrayList();
	TupleType outputType2 = TupleType.DefaultFactory.create(Integer.class, R, String.class);
	TupleType inputType2 = TupleType.EmptyTupleType;
	
	List<Term> outputTerms3 = Lists.<Term>newArrayList(new Skolem("b"), new TypedConstant<>("d"));
	List<Term> inputTerms3 = Lists.<Term>newArrayList(new Skolem("b"));
	TupleType outputType3 = TupleType.DefaultFactory.create(R, String.class);
	TupleType inputType3 = TupleType.DefaultFactory.create(R);

	List<Term> outputTerms = Lists.<Term>newArrayList(new Variable("a"), new Variable("a"), new Skolem("b"), new TypedConstant<>("c"), new Skolem("b"), new TypedConstant<>("d"));
	List<Term> inputTerms = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"));
	TupleType outputType = TupleType.DefaultFactory.create(Integer.class, Integer.class, R, String.class, R, String.class);
	TupleType inputType = TupleType.DefaultFactory.create(Integer.class, R);
	
	
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

	@Test(expected=AssertionError.class) 
	public void getDepthNegative() {
		Mockito.when(child1.getDepth()).thenReturn(10);
		Mockito.when(child2.getDepth()).thenReturn(-100);
		Mockito.when(child3.getDepth()).thenReturn(1000);
		getOperator().getDepth();
	}

	@Test(expected=IllegalArgumentException.class) 
	public void getNegativeColumn() {
		getOperator().getColumn(-1);
	}

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

	@Test public void isQuasiLeaf() {
		Mockito.when(child1.isQuasiLeaf()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are never quasi-leaves", getOperator().isQuasiLeaf());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are never quasi-leaves", getOperator().isQuasiLeaf());

		Mockito.when(child1.isQuasiLeaf()).thenReturn(false);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false);
		Assert.assertFalse("NaryOperator's are never quasi-leaves", getOperator().isQuasiLeaf());
	}

	@Test public void isLeftDeep() {
		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isLeftDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(false); Mockito.when(child2.isLeftDeep()).thenReturn(true);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isLeftDeep()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isLeftDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true);  Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isLeftDeep()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());

		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isLeftDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true);  Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true);  Mockito.when(child3.isLeftDeep()).thenReturn(false);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());

		Mockito.when(child1.isQuasiLeaf()).thenReturn(true); Mockito.when(child1.isLeftDeep()).thenReturn(false);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true); Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true); Mockito.when(child3.isLeftDeep()).thenReturn(false);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
	}

	@Test public void isRightDeep() {
		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isRightDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(false); Mockito.when(child2.isRightDeep()).thenReturn(true);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isRightDeep()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isRightDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true);  Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isRightDeep()).thenReturn(true);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());

		Mockito.when(child1.isQuasiLeaf()).thenReturn(true); Mockito.when(child1.isRightDeep()).thenReturn(false);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true);  Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isRightDeep()).thenReturn(true);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());

		Mockito.when(child1.isQuasiLeaf()).thenReturn(true); Mockito.when(child1.isRightDeep()).thenReturn(false);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true);  Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true); Mockito.when(child3.isRightDeep()).thenReturn(false);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
	}
}
