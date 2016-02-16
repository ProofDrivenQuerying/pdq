package uk.ac.ox.cs.pdq.test.algebra;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.Union;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class UnionTest.
 *
 * @author Julien Leblay
 */
public class UnionTest extends NaryOperatorTest {

	/** The operator. */
	Union operator;
	
	/** The children. */
	List<RelationalOperator> children;
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.NaryOperatorTest#setup()
	 */
	@Before public void setup() throws RelationalOperatorException {
        super.setup();
        
        children = Lists.newArrayList(child1, child2, child3);
        this.operator = new Union(children);
	}
	

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.RelationalOperatorTest#getOperator()
	 */
	@Override
	RelationalOperator getOperator() {
		return this.operator;
	}

	/**
	 * Inits the union test null argument1.
	 */
	@Test(expected=NullPointerException.class) 
	public void initUnionTestNullArgument1() {
		new Union((List) null);
	}

	/**
	 * Inits the union test null argument2.
	 */
	@Test(expected=NullPointerException.class) 
	public void initUnionTestNullArgument2() {
		new Union((RelationalOperator[]) null);
	}

	/**
	 * Inits the union test empty argument.
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void initUnionTestEmptyArgument() {
		new Union(Lists.<RelationalOperator>newArrayList());
	}

	/**
	 * Inits the union test.
	 */
	@Test public void initUnionTest() {
		Assert.assertEquals("Union children must match that of initialiazation", children, this.operator.getChildren());
		Assert.assertEquals("Union output must match the concatenation of childrens", outputTerms, this.operator.getColumns());
		Assert.assertEquals("Union output type must match the concatenation of childrens", outputType, this.operator.getType());
		Assert.assertEquals("Union input must match the concatenation of childrens", inputTerms, this.operator.getInputTerms());
		Assert.assertEquals("Union input type must match the concatenation of childrens", inputType, this.operator.getInputType());
	}

	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		Union copy = this.operator.deepCopy();
		Assert.assertEquals("Union copy's children must match that of operator", this.operator.getChildren(), copy.getChildren());
		Assert.assertEquals("Union copy's output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Union copy's output type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Union copy's input must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Union copy's input type must match that of operator", this.operator.getInputType(), copy.getInputType());
	}
}
