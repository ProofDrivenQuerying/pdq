package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class RelationalOperatorTest.
 *
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class RelationalOperatorTest {
	
	/**
	 * Setup.
	 *
	 * @throws RelationalOperatorException the relational operator exception
	 */
	@Before 
	public void setup() throws RelationalOperatorException {
		Utility.assertsEnabled();
	}
	
	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	abstract RelationalOperator getOperator();
}
