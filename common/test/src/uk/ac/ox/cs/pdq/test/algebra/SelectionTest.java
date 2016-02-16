package uk.ac.ox.cs.pdq.test.algebra;

import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.algebra.Selection;
import uk.ac.ox.cs.pdq.algebra.predicates.Predicate;

// TODO: Auto-generated Javadoc
/**
 * The Class SelectionTest.
 *
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public class SelectionTest extends UnaryOperatorTest {

	/** The operator. */
	Selection operator;
	
	/** The predicate. */
	@Mock Predicate predicate;
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.RelationalOperatorTest#setup()
	 */
	@Before public void setup() throws RelationalOperatorException {
		super.setup();
        MockitoAnnotations.initMocks(this);

        when(child.getColumns()).thenReturn(outputTerms);
		when(child.getType()).thenReturn(outputType);
		when(child.getInputTerms()).thenReturn(inputTerms);
		when(child.getInputType()).thenReturn(inputType);
		when(child.deepCopy()).thenReturn(child);
//        when(predicate.isSatisfied(null)).thenReturn(true, false, true, false, true);

		this.operator = new Selection(predicate, child);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.algebra.RelationalOperatorTest#getOperator()
	 */
	RelationalOperator getOperator() {
		return this.operator;
	}
	
	/**
	 * Inits the count test null arguments.
	 */
	@Test(expected=NullPointerException.class)
	public void initCountTestNullArguments() {
		new Selection(null, null);
	}
	
	/**
	 * Inits the count test null argument1.
	 */
	@Test(expected=NullPointerException.class)
	public void initCountTestNullArgument1() {
		new Selection(null, child);
	}
	
	/**
	 * Inits the count test null argument2.
	 */
	@Test(expected=NullPointerException.class)
	public void initCountTestNullArgument2() {
		new Selection(predicate, null);
	}
	
	/**
	 * Inits the selection test.
	 */
	@Test public void initSelectionTest() {
		Assert.assertEquals("Child must match that used for initialization", child, this.operator.getChild());
		Assert.assertEquals("Selection operator type must match that of child", child.getType(), this.operator.getType());
		Assert.assertEquals("Selection operator inputs must match that of child", child.getInputTerms(), this.operator.getInputTerms());
		Assert.assertEquals("Selection operator input type must match that of child", child.getInputType(), this.operator.getInputType());
		Assert.assertEquals("Selection predicate must match that of initialization", this.predicate, this.operator.getPredicate());
	}

	/**
	 * Deep copy.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Test public void deepCopy() throws RelationalOperatorException {
		Selection copy = this.operator.deepCopy();
		Assert.assertEquals("Selection operators deep copy must be equals to itself", this.operator, copy);
		Assert.assertEquals("Selection operators deep copy child must be equals to itself", this.child, copy.getChild());
		Assert.assertEquals("Selection operator type must match that of child", this.outputType, copy.getType());
		Assert.assertEquals("Selection operator inputs must match that of child", this.inputTerms, copy.getInputTerms());
		Assert.assertEquals("Selection operator input type must match that of child", this.inputType, copy.getInputType());
		Assert.assertEquals("Selection predicate must match that of initialization", this.predicate, copy.getPredicate());
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	@Test public void getColumn() {
		for (int i = 0, l = this.outputTerms.size(); i < l; i++) {
			Assert.assertEquals("Selection operator's " + i + "th column must match that of child", this.child.getColumns().get(i), this.operator.getColumn(i));
		}
	}

	/**
	 * Gets the bad column.
	 *
	 * @return the bad column
	 */
	@Test(expected=IllegalArgumentException.class) 
	public void getBadColumn() {
		this.operator.getColumn(this.outputTerms.size() + 1);
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

		s.add(new Selection(this.predicate, getOperator().deepCopy()));
		Assert.assertEquals("Operator set must have size 2, after new count is added", 2, s.size());

		// TODO: agree on semantics of adding deep copy.
		s.add(getOperator().deepCopy());
		Assert.assertEquals("Operator set must have size 2, after deep copy is added", 2, s.size());
	}
}
