package uk.ac.ox.cs.pdq.test.algebra;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.Union;

import com.google.common.collect.Lists;

/**
 * @author Julien Leblay
 */
public class UnionTest extends NaryOperatorTest {

	Union operator;
	List<RelationalOperator> children;
	
	@Before public void setup() throws RelationalOperatorException {
        super.setup();
        
        children = Lists.newArrayList(child1, child2, child3);
        this.operator = new Union(children);
	}
	

	@Override
	RelationalOperator getOperator() {
		return this.operator;
	}

	@Test(expected=NullPointerException.class) 
	public void initUnionTestNullArgument1() {
		new Union((List) null);
	}

	@Test(expected=NullPointerException.class) 
	public void initUnionTestNullArgument2() {
		new Union((RelationalOperator[]) null);
	}

	@Test(expected=IllegalArgumentException.class) 
	public void initUnionTestEmptyArgument() {
		new Union(Lists.<RelationalOperator>newArrayList());
	}

	@Test public void initUnionTest() {
		Assert.assertEquals("Union children must match that of initialiazation", children, this.operator.getChildren());
		Assert.assertEquals("Union output must match the concatenation of childrens", outputTerms, this.operator.getColumns());
		Assert.assertEquals("Union output type must match the concatenation of childrens", outputType, this.operator.getType());
		Assert.assertEquals("Union input must match the concatenation of childrens", inputTerms, this.operator.getInputTerms());
		Assert.assertEquals("Union input type must match the concatenation of childrens", inputType, this.operator.getInputType());
	}

	@Test public void deepCopy() throws RelationalOperatorException {
		Union copy = this.operator.deepCopy();
		Assert.assertEquals("Union copy's children must match that of operator", this.operator.getChildren(), copy.getChildren());
		Assert.assertEquals("Union copy's output must match that of operator", this.operator.getColumns(), copy.getColumns());
		Assert.assertEquals("Union copy's output type must match that of operator", this.operator.getType(), copy.getType());
		Assert.assertEquals("Union copy's input must match that of operator", this.operator.getInputTerms(), copy.getInputTerms());
		Assert.assertEquals("Union copy's input type must match that of operator", this.operator.getInputType(), copy.getInputType());
	}
}
