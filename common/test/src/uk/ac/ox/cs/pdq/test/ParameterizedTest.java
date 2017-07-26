package uk.ac.ox.cs.pdq.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class ParameterizedTest.
 *
 * @author Julien Leblay
 */
@RunWith(Parameterized.class)
public abstract class ParameterizedTest {
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Gets the parameters.
	 *
	 * @param sets the sets
	 * @return the parameters
	 */
	@Parameters
	public static Collection<Object[]> getParameters(Set<?>... sets) {
		List<Set<?>> params = new ArrayList<>();
		for (Set<?> set: sets) {
			params.add(set);
		}
		List<Object[]> result = new LinkedList<>();
		for (List<?> param : Sets.cartesianProduct(params)) {
			result.add(param.toArray());
		}
		return result;
	}
}
