package uk.ac.ox.cs.pdq.test.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Sets;

/**
 *
 * @author Julien Leblay
 */
@RunWith(Parameterized.class)
public abstract class ParameterizedTest {

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

	protected static <T> Set<T> asSet(T... array) {
		Set<T> result = new LinkedHashSet<>();
		for (T i: array) {
			result.add(i);
		}
		return result;
	}
}
