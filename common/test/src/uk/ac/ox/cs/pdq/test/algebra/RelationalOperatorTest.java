package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;
import uk.ac.ox.cs.pdq.algebra.RelationalOperatorException;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class RelationalOperatorTest {
	
	/**
	 * @throws RelationalOperatorException 
	 */
	@Before 
	public void setup() throws RelationalOperatorException {
		Utility.assertsEnabled();
	}
	
	abstract RelationalOperator getOperator();
}
