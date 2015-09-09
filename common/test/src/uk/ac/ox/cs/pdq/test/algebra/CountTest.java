package uk.ac.ox.cs.pdq.test.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.ox.cs.pdq.algebra.Count;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public class CountTest extends UnaryOperatorTest {
	Count operator;
	TupleType singleIntegerType = TupleType.DefaultFactory.create(Integer.class);
	
	@Before public void setup() throws RelationalOperatorException {
        MockitoAnnotations.initMocks(this);
		Mockito.when(child.getColumns()).thenReturn(outputTerms);
		Mockito.when(child.getType()).thenReturn(outputType);
		Mockito.when(child.getInputTerms()).thenReturn(inputTerms);
		Mockito.when(child.getInputType()).thenReturn(inputType);
		Mockito.when(child.deepCopy()).thenReturn(child);

		this.operator = new Count(child);
	}
	
	RelationalOperator getOperator() {
		return this.operator;
	}
	
	@Test public void initCountTest() {
		Assert.assertEquals("Child must match that used for initialization", child, this.operator.getChild());
		Assert.assertEquals("Count operator type must be a single integer", singleIntegerType, this.operator.getType());
		Assert.assertEquals("Count operator inputs must match that of child", inputTerms, this.operator.getInputTerms());
		Assert.assertEquals("Count operator input type must match that of child", inputType, this.operator.getInputType());
	}
	
	@Test(expected=NullPointerException.class)
	public void initCountTestNullArgument() {
		new Count(null);
	}

	@Test public void deepCopy() throws RelationalOperatorException {
		Count copy = this.operator.deepCopy();
		Assert.assertEquals("Count operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Count operators copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Count operator type must always single integer", singleIntegerType, copy.getType());
		Assert.assertEquals("Count operator inputs must match that of child", inputTerms, copy.getInputTerms());
		Assert.assertEquals("Count operator input type must match that of child", inputType, copy.getInputType());
	}

	@Test public void getColumn() {
		Assert.assertNotNull("Count operator's only column has type Integer", this.operator.getColumn(0));
	}

	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumn() {
		this.operator.getColumn(1);
	}

	@Test public void testHashCode() throws RelationalOperatorException {
		Set<RelationalOperator> s = new LinkedHashSet<>();

		s.add(getOperator());
		Assert.assertEquals("Operator set must have size 1", 1, s.size());

		s.add(getOperator());
		Assert.assertEquals("Operator set must have size 1, after being adding twice to set", 1, s.size());

		s.add(new Count(getOperator().deepCopy()));
		Assert.assertEquals("Operator set must have size 2, after new count is added", 2, s.size());

		// TODO: agree on semantics of adding deep copy.
		s.add(getOperator().deepCopy());
		Assert.assertEquals("Operator set must have size 2, after deep copy is added", 2, s.size());
	}
}
