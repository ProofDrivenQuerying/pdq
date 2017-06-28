package uk.ac.ox.cs.pdq.test.algebra;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.Join;
import uk.ac.ox.cs.pdq.algebra.Join.Variants;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.predicates.AttributeEqualityPredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.ConjunctivePredicate;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class JoinTest.
 *
 * @author Julien Leblay
 */
public class JoinTest extends NaryOperatorTest {

	/** The operator. */
	Join operator;
	
	/** The children. */
	List<RelationalOperator> children;
	
	/** The predicate12. */
	Condition predicate12 = new AttributeEqualityCondition(0, 1);
	
	/** The predicate23. */
	Condition predicate23 = new AttributeEqualityCondition(2, 4);
	
	/** The predicate. */
	Condition predicate = new ConjunctiveCondition(
			Lists.newArrayList(predicate12, predicate23));
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.NaryOperatorTest#setup()
	 */
	@Before public void setup() throws RelationalOperatorException {
        super.setup();
        
        children = Lists.newArrayList(child1, child2, child3);
        this.operator = new Join(children);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.RelationalOperatorTest#getOperator()
	 */
	@Override
	RelationalOperator getOperator() {
		return this.operator;
	}

	/**
	 * Inits the join test null argument2.
	 */
	@Test(expected=NullPointerException.class) 
	public void initJoinTestNullArgument2() {
		new Join((List) null);
	}

	/**
	 * Inits the join test null argument1.
	 */
	@Test(expected=NullPointerException.class) 
	public void initJoinTestNullArgument1() {
		new Join((RelationalOperator[]) null);
	}

	/**
	 * Inits the join test empty argument.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initJoinTestEmptyArgument() {
		new Join(Lists.<RelationalOperator>newArrayList());
	}

	/**
	 * Inits the join test empty argument2.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initJoinTestEmptyArgument2() {
		new Join(predicate, Lists.<RelationalOperator>newArrayList());
	}

	/**
	 * Inits the join test null arguments2.
	 */
	@Test(expected=NullPointerException.class) 
	public void initJoinTestNullArguments2() {
		new Join(null, (List) null);
	}

	/**
	 * Inits the join test null arguments1.
	 */
	@Test(expected=NullPointerException.class) 
	public void initJoinTestNullArguments1() {
		new Join(null, (RelationalOperator[]) null);
	}

	/**
	 * Inits the join test.
	 */
	@Test public void initJoinTest() {
		Assert.assertEquals("Join children must match that of initialiazation", children, this.operator.getChildren());
		Assert.assertEquals("Join output must match the concatenation of childrens", outputTerms, this.operator.getColumns());
		Assert.assertEquals("Join output type must match the concatenation of childrens", outputType, this.operator.getType());
		Assert.assertEquals("Join input must match the concatenation of childrens", inputTerms, this.operator.getInputTerms());
		Assert.assertEquals("Join input type must match the concatenation of childrens", inputType, this.operator.getInputType());
		Assert.assertEquals("Join predicate must match that of initialization", predicate, this.operator.getPredicate());
	}

	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		Join copy = this.operator.deepCopy();
		Assert.assertEquals("Join copy's children must match that of operator", this.operator.getChildren(), copy.getChildren());
		Assert.assertEquals("Join copy's output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Join copy's output type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Join copy's input must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Join copy's input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Join copy's predicate must match that of operator's", this.operator.getPredicate(), copy.getPredicate());
		Assert.assertEquals("Join copy's variant must match that of operator's", this.operator.getVariant(), copy.getVariant());
	}

	/**
	 * Deep copy2.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy2() throws RelationalOperatorException {
		this.operator = new Join(Lists.newArrayList(child1, child3));
		Join copy = this.operator.deepCopy();
		Assert.assertEquals("Join copy's children must match that of operator", this.operator.getChildren(), copy.getChildren());
		Assert.assertEquals("Join copy's output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Join copy's output type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Join copy's input must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Join copy's input type must match that of operator", this.operator.getInputType(), copy.getInputType());
		Assert.assertEquals("Join copy's predicate must match that of operator's", this.operator.getPredicate(), copy.getPredicate());
		Assert.assertEquals("Join copy's variant must match that of operator's", this.operator.getVariant(), copy.getVariant());
	}

	/**
	 * Default variant.
	 */
	@Test public void defaultVariant() {
		Assert.assertEquals("Join default variant is SYMMETRIC_HASH_JOIN", Variants.SYMMETRIC_HASH, this.operator.getVariant());
	}

	/**
	 * Sets the variant.
	 */
	@Test public void setVariant() {
		this.operator.setVariant(Variants.MERGE);
		Assert.assertEquals("Join default variant is SYMMETRIC_HASH_JOIN", Variants.MERGE, this.operator.getVariant());
	}

	/**
	 * Sets the variant null.
	 */
	@Test(expected=NullPointerException.class)
	public void setVariantNull() {
		this.operator.setVariant(null);
	}

	/**
	 * Checks for predicate.
	 */
	@Test public void hasPredicate() {
        this.operator = new Join(Lists.newArrayList(child1, child2, child3));
		Assert.assertTrue("Join's with attributes overlap must have a predicate", this.operator.hasPredicate());

		this.operator = new Join(Lists.newArrayList(child1, child3));
		Assert.assertFalse("Join's without attributes overlap must not have a predicate", this.operator.hasPredicate());
	}
}