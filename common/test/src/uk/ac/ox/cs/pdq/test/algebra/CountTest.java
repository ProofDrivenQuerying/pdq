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

// TODO: Auto-generated Javadoc
/**
 * The Class CountTest.
 *
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public class CountTest extends UnaryOperatorTest {
	
	/** The operator. */
	Count operator;
	
	/** The single integer type. */
	TupleType singleIntegerType = TupleType.DefaultFactory.create(Integer.class);
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.RelationalOperatorTest#setup()
	 */
	@Before public void setup() throws RelationalOperatorException {
		super.setup();
        MockitoAnnotations.initMocks(this);
		Mockito.when(child.getColumns()).thenReturn(outputTerms);
		Mockito.when(child.getType()).thenReturn(outputType);
		Mockito.when(child.getInputTerms()).thenReturn(inputTerms);
		Mockito.when(child.getInputType()).thenReturn(inputType);
		Mockito.when(child.deepCopy()).thenReturn(child);

		this.operator = new Count(child);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.RelationalOperatorTest#getOperator()
	 */
	RelationalOperator getOperator() {
		return this.operator;
	}
	
	/**
	 * Inits the count test.
	 */
	@Test public void initCountTest() {
		Assert.assertEquals("Child must match that used for initialization", child, this.operator.getChild());
		Assert.assertEquals("Count operator type must be a single integer", singleIntegerType, this.operator.getType());
		Assert.assertEquals("Count operator inputs must match that of child", inputTerms, this.operator.getInputTerms());
		Assert.assertEquals("Count operator input type must match that of child", inputType, this.operator.getInputType());
	}
	
	/**
	 * Inits the count test null argument.
	 */
	@Test(expected=NullPointerException.class)
	public void initCountTestNullArgument() {
		new Count(null);
	}

	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		Count copy = this.operator.deepCopy();
		Assert.assertEquals("Count operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Count operators copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Count operator type must always single integer", singleIntegerType, copy.getType());
		Assert.assertEquals("Count operator inputs must match that of child", inputTerms, copy.getInputTerms());
		Assert.assertEquals("Count operator input type must match that of child", inputType, copy.getInputType());
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	@Test public void getColumn() {
		Assert.assertNotNull("Count operator's only column has type Integer", this.operator.getColumn(0));
	}

	/**
	 * Gets the bad column.
	 *
	 * @return the bad column
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumn() {
		this.operator.getColumn(1);
	}

	/**
	 * Test hash code.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
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
