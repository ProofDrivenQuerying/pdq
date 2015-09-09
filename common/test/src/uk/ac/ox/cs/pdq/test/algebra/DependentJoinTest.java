package uk.ac.ox.cs.pdq.test.algebra;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.ox.cs.pdq.algebra.DependentJoin;
import uk.ac.ox.cs.pdq.algebra.Join.Variants;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.util.TupleType;

import com.google.common.collect.Lists;


/**
 * @author Julien Leblay
 */
public class DependentJoinTest extends JoinTest{

	DependentJoin operator;
	List<RelationalOperator> children;
	Predicate predicate = new ConjunctivePredicate(Lists.newArrayList(
			new AttributeEqualityPredicate(0, 1),
			new AttributeEqualityPredicate(2, 4)));
	List<Integer> swInput = Lists.newArrayList(1);
	Predicate predicate23 = new AttributeEqualityPredicate(1, 3);

	List<Term> outputTerms12 = Lists.<Term>newArrayList(new Variable("a"), new Variable("a"), new Skolem("b"), new TypedConstant<>("c"));
	List<Term> inputTerms12 = Lists.<Term>newArrayList(new Variable("a"));
	TupleType outputType12 = TupleType.DefaultFactory.create(Integer.class, Integer.class, R, String.class);
	TupleType inputType12 = TupleType.DefaultFactory.create(Integer.class);

	List<Term> outputTerms23 = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"), new TypedConstant<>("c"), new Skolem("b"), new TypedConstant<>("d"));
	List<Term> inputTerms23 = Lists.<Term>newArrayList(new Skolem("b"));
	TupleType outputType23 = TupleType.DefaultFactory.create(Integer.class, R, String.class, R, String.class);
	TupleType inputType23 = TupleType.DefaultFactory.create(R);

	List<Term> outputTerms13 = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"), new TypedConstant<>("d"));
	List<Term> inputTerms13 = Lists.<Term>newArrayList(new Variable("a"), new Skolem("b"));
	TupleType outputType13 = TupleType.DefaultFactory.create(Integer.class, R, String.class);
	TupleType inputType13 = TupleType.DefaultFactory.create(Integer.class, R);

	@Before public void setup() throws RelationalOperatorException {
        super.setup();
        
        this.operator = new DependentJoin(child1, child2);
	}

	@Override
	RelationalOperator getOperator() {
		return this.operator;
	}

	@Test(expected=NullPointerException.class) 
	public void initDependentJoinTestNullArgument1() {
		new DependentJoin(null, child1);
	}

	@Test(expected=NullPointerException.class) 
	public void initDependentJoinTestNullArgument2() {
		new DependentJoin(child1, null);
	}

	@Test(expected=NullPointerException.class) 
	public void initDependentJoinTestNullArgument3() {
		new DependentJoin(child1, child2, null);
	}

	@Test(expected=NullPointerException.class) 
	public void initDependentJoinTestNullArguments2() {
		new DependentJoin(null, null);
	}

	@Test(expected=NullPointerException.class) 
	public void initDependentJoinTestNullArguments3() {
		new DependentJoin(null, null, null);
	}

	@Test(expected=IllegalArgumentException.class) 
	public void initDependentJoinTestInconsistent() {
		new DependentJoin(child1, child2, Lists.newArrayList(3));
	}

	@Test public void initDependentJoinTest1() {
        this.operator = new DependentJoin(child1, child2);
		Assert.assertEquals("DependentJoin children must match that of initialiazation", Lists.newArrayList(child1, child2), this.operator.getChildren());
		Assert.assertEquals("DependentJoin output must match the concatenation of childrens", outputTerms12, this.operator.getColumns());
		Assert.assertEquals("DependentJoin output type must match the concatenation of childrens", outputType12, this.operator.getType());
		Assert.assertEquals("DependentJoin input must match the concatenation of childrens", inputTerms12, this.operator.getInputTerms());
		Assert.assertEquals("DependentJoin input type must match the concatenation of childrens", inputType12, this.operator.getInputType());
		Assert.assertEquals("DependentJoin predicate must match that of initialization", new ConjunctivePredicate(predicate12), this.operator.getPredicate());
		Assert.assertEquals("DependentJoin left child must match that of initialization", child1, this.operator.getLeft());
		Assert.assertEquals("DependentJoin right child must match that of initialization", child2, this.operator.getRight());
		Assert.assertFalse("DependentJoin sideways input must match that of initialization", this.operator.hasSidewaysInputs());
	}

	@Test public void initDependentJoinTest2() {
        this.operator = new DependentJoin(child2, child3);
		Assert.assertEquals("DependentJoin children must match that of initialiazation", Lists.newArrayList(child2, child3), this.operator.getChildren());
		Assert.assertEquals("DependentJoin output must match the concatenation of childrens", outputTerms23, this.operator.getColumns());
		Assert.assertEquals("DependentJoin output type must match the concatenation of childrens", outputType23, this.operator.getType());
		Assert.assertTrue("DependentJoin input must be empty (because of sideways input).", this.operator.getInputTerms().isEmpty());
		Assert.assertEquals("DependentJoin input type must be empty (because of sideways input)", TupleType.EmptyTupleType, this.operator.getInputType());
		Assert.assertEquals("DependentJoin predicate must match that of initialization", new ConjunctivePredicate(predicate23), this.operator.getPredicate());
		Assert.assertEquals("DependentJoin left child must match that of initialization", child2, this.operator.getLeft());
		Assert.assertEquals("DependentJoin right child must match that of initialization", child3, this.operator.getRight());
		Assert.assertEquals("DependentJoin sideways input must match that of initialization", swInput, this.operator.getSidewaysInput());
	}

	@Test public void initDependentJoinTest3() {
        this.operator = new DependentJoin(child1, child3);
		Assert.assertEquals("DependentJoin children must match that of initialiazation", Lists.newArrayList(child1, child3), this.operator.getChildren());
		Assert.assertEquals("DependentJoin output must match the concatenation of childrens", outputTerms13, this.operator.getColumns());
		Assert.assertEquals("DependentJoin output type must match the concatenation of childrens", outputType13, this.operator.getType());
		Assert.assertEquals("DependentJoin input must match the concatenation of childrens", inputTerms13, this.operator.getInputTerms());
		Assert.assertEquals("DependentJoin input type must match the concatenation of childrens", inputType13, this.operator.getInputType());
		Assert.assertFalse("DependentJoin predicate must match be empty", this.operator.hasPredicate());
		Assert.assertEquals("DependentJoin left child must match that of initialization", child1, this.operator.getLeft());
		Assert.assertEquals("DependentJoin right child must match that of initialization", child3, this.operator.getRight());
		Assert.assertFalse("DependentJoin sideways input must match be empty", this.operator.hasSidewaysInputs());
	}

	@Test public void deepCopy() throws RelationalOperatorException {
		this.operator = new DependentJoin(child1, child2);
		DependentJoin copy = this.operator.deepCopy();
		Assert.assertEquals("DependentJoin copy's children must match that of operator", this.operator.getChildren(), copy.getChildren());
		Assert.assertEquals("DependentJoin copy's output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("DependentJoin copy's output type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("DependentJoin copy's input must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("DependentJoin copy's input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("DependentJoin copy's predicate must match that of operator's", this.operator.getPredicate(), copy.getPredicate());
		Assert.assertEquals("DependentJoin copy's variant must match that of operator's", this.operator.getVariant(), copy.getVariant());
	}

	@Test public void deepCopy2() throws RelationalOperatorException {
		this.operator = new DependentJoin(child2, child3);
		DependentJoin copy = this.operator.deepCopy();
		Assert.assertEquals("DependentJoin copy's children must match that of operator", this.operator.getChildren(), copy.getChildren());
		Assert.assertEquals("DependentJoin copy's output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("DependentJoin copy's output type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("DependentJoin copy's input must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("DependentJoin copy's input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("DependentJoin copy's predicate must match that of operator's", this.operator.getPredicate(), copy.getPredicate());
		Assert.assertEquals("DependentJoin copy's variant must match that of operator's", this.operator.getVariant(), copy.getVariant());
	}

	@Test public void defaultVariant() {
		Assert.assertEquals("DependentJoin default variant is SYMMETRIC_HASH_JOIN", Variants.SYMMETRIC_HASH, this.operator.getVariant());
	}

	@Test public void setVariant() {
		this.operator.setVariant(Variants.MERGE);
		Assert.assertEquals("DependentJoin default variant is SYMMETRIC_HASH_JOIN", Variants.MERGE, this.operator.getVariant());
	}

	@Test(expected=NullPointerException.class)
	public void setVariantNull() {
		this.operator.setVariant(null);
	}

	@Test public void hasPredicate() {
        this.operator = new DependentJoin(child1, child2);
		Assert.assertTrue("DependentJoin's with attributes overlap must have a predicate", this.operator.hasPredicate());
        this.operator = new DependentJoin(child1, child3);
		Assert.assertFalse("DependentJoin's without attributes overlap must nothave a predicate", this.operator.hasPredicate());
	}

	@Test public void isClosed() {
        this.operator = new DependentJoin(child1, child2);
        super.isClosed();
        this.operator = new DependentJoin(child2, child3);
        super.isClosed();
	}

	@Test public void isLeftDeep() {
		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isLeftDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(false); Mockito.when(child2.isLeftDeep()).thenReturn(true);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isLeftDeep()).thenReturn(true);

		this.operator = new DependentJoin(child1, child2);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		this.operator = new DependentJoin(child2, child3);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		this.operator = new DependentJoin(child1, child3);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isLeftDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true); Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true); Mockito.when(child3.isLeftDeep()).thenReturn(true);

		this.operator = new DependentJoin(child1, child2);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		this.operator = new DependentJoin(child2, child3);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		this.operator = new DependentJoin(child1, child3);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(true); Mockito.when(child1.isLeftDeep()).thenReturn(false);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true); Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true); Mockito.when(child3.isLeftDeep()).thenReturn(false);

		this.operator = new DependentJoin(child1, child2);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		this.operator = new DependentJoin(child2, child3);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		this.operator = new DependentJoin(child1, child3);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(true); Mockito.when(child1.isLeftDeep()).thenReturn(false);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true); Mockito.when(child2.isLeftDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isLeftDeep()).thenReturn(true);

		this.operator = new DependentJoin(child1, child2);
		Assert.assertTrue("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		this.operator = new DependentJoin(child2, child3);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
		this.operator = new DependentJoin(child1, child3);
		Assert.assertFalse("NaryOperator's are left-deep if only the left-most child is, or all are quasi-leaves", getOperator().isLeftDeep());
	}

	@Test public void isRightDeep() {
		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isRightDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(false); Mockito.when(child2.isRightDeep()).thenReturn(true);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isRightDeep()).thenReturn(true);

		this.operator = new DependentJoin(child1, child2);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		this.operator = new DependentJoin(child2, child3);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		this.operator = new DependentJoin(child1, child3);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(false); Mockito.when(child1.isRightDeep()).thenReturn(true);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true); Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true); Mockito.when(child3.isRightDeep()).thenReturn(true);

		this.operator = new DependentJoin(child1, child2);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		this.operator = new DependentJoin(child2, child3);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		this.operator = new DependentJoin(child1, child3);
		Assert.assertFalse("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(true); Mockito.when(child1.isRightDeep()).thenReturn(false);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true); Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(true); Mockito.when(child3.isRightDeep()).thenReturn(false);

		this.operator = new DependentJoin(child1, child2);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		this.operator = new DependentJoin(child2, child3);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		this.operator = new DependentJoin(child1, child3);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		
		Mockito.when(child1.isQuasiLeaf()).thenReturn(true); Mockito.when(child1.isRightDeep()).thenReturn(false);
		Mockito.when(child2.isQuasiLeaf()).thenReturn(true); Mockito.when(child2.isRightDeep()).thenReturn(false);
		Mockito.when(child3.isQuasiLeaf()).thenReturn(false); Mockito.when(child3.isRightDeep()).thenReturn(true);

		this.operator = new DependentJoin(child1, child2);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		this.operator = new DependentJoin(child2, child3);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
		this.operator = new DependentJoin(child1, child3);
		Assert.assertTrue("NaryOperator's are right-deep if only the right-most child is, or all are quasi-leaves", getOperator().isRightDeep());
	}
}
