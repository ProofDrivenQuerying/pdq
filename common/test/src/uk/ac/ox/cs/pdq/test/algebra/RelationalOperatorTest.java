package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import uk.ac.ox.cs.pdq.algebra.RelationalOperator;

/**
 * @author Julien Leblay
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class RelationalOperatorTest {
	
	abstract RelationalOperator getOperator();
}
