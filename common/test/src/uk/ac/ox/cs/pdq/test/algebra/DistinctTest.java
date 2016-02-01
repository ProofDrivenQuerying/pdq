package uk.ac.ox.cs.pdq.test.algebra;

import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.ox.cs.pdq.algebra.Count;
import uk.ac.ox.cs.pdq.algebra.Distinct;
import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;


/**
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public class DistinctTest extends UnaryOperatorTest {
	Distinct operator;
	
	@Before public void setup() throws RelationalOperatorException {
		super.setup();
        MockitoAnnotations.initMocks(this);
		when(child.getColumns()).thenReturn(outputTerms);
		when(child.getType()).thenReturn(outputType);
		when(child.getInputTerms()).thenReturn(inputTerms);
		when(child.getInputType()).thenReturn(inputType);
		when(child.deepCopy()).thenReturn(child);

		this.operator = new Distinct(child);
	}
	
	RelationalOperator getOperator() {
		return this.operator;
	}
	
	@Test(expected=NullPointerException.class)
	public void initCountTestNullArgument() {
		new Distinct(null);
	}

	@Test public void initDistinctTest() {
		Assert.assertEquals("Child must match that used for initialization", child, this.operator.getChild());
		Assert.assertEquals("Distinct operator type must match that of child", child.getType(), this.operator.getType());
		Assert.assertEquals("Distinct operator inputs must match that of child", child.getInputTerms(), this.operator.getInputTerms());
		Assert.assertEquals("Distinct operator input type must match that of child", child.getInputType(), this.operator.getInputType());
	}

	@Test public void deepCopy() throws RelationalOperatorException {
		Distinct copy = this.operator.deepCopy();
		Assert.assertEquals("Distinct operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Distinct operators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Distinct operator type must match that of child", this.outputType, copy.getType());
		Assert.assertEquals("Distinct operator inputs must match that of child", this.inputTerms, copy.getInputTerms());
		Assert.assertEquals("Distinct operator input type must match that of child", this.inputType, copy.getInputType());
	}

	@Test public void getColumn() {
		for (int i = 0, l = this.outputTerms.size(); i < l; i++) {
			Assert.assertEquals("Distinct operator's " + i + "th column must match that of child", this.child.getColumns().get(i), this.operator.getColumn(i));
		}
	}

	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumn() {
		this.operator.getColumn(this.outputTerms.size() + 1);
	}

	@Test public void testHashCode() throws RelationalOperatorException {
		Set<RelationalOperator> s = new LinkedHashSet<>();

		s.add(getOperator());
		Assert.assertEquals("Operator set must have size 1", 1, s.size());

		s.add(getOperator());
		Assert.assertEquals("Operator set must have size 1, after being adding twice to set", 1, s.size());

		s.add(new Distinct(getOperator().deepCopy()));
		Assert.assertEquals("Operator set must have size 2, after new count is added", 2, s.size());

		// TODO: agree on semantics of adding deep copy.
		s.add(getOperator().deepCopy());
		Assert.assertEquals("Operator set must have size 2, after deep copy is added", 2, s.size());
	}
}
