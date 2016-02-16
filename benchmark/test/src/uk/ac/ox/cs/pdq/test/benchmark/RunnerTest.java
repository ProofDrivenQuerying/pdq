package uk.ac.ox.cs.pdq.test.benchmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters.QueryTypes;
import uk.ac.ox.cs.pdq.util.Utility;
import uk.ac.ox.cs.pdq.benchmark.PlannerBenchmark;
import uk.ac.ox.cs.pdq.benchmark.Runner;

import com.google.common.collect.Sets;

// TODO: Auto-generated Javadoc
/**
 * The Class RunnerTest.
 *
 * @author Julien Leblay
 */
@Ignore
@RunWith(Parameterized.class) 
public class RunnerTest {
	
	
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
	 * @return the parameters
	 */
	@Parameters
	public static Collection<Object[]> getParameters() {
		List<Set<?>> params = new ArrayList<>();
		params.add(inputSchemas);
		params.add(queryTypes);
		params.add(nbQueryConjuncts);
		params.add(seeds);
		List<Object[]> result = new LinkedList<>();
		for (List<?> param : Sets.cartesianProduct(params)) {
			result.add(param.toArray());
		}
		return result;
	}
	
	/** The seeds. */
	private static Set<Integer> seeds = asLinkedSet(1, 2, 3, 4, 5);
	
	/** The input schemas. */
	private static Set<String> inputSchemas = asLinkedSet("test/input/schema-mysql-tpch.xml", "");
	
	/** The nb query conjuncts. */
	private static Set<Integer> nbQueryConjuncts = asLinkedSet(1, 2, 3, 4, 5);
	
	/** The query types. */
	private static Set<QueryTypes> queryTypes = asSet(QueryTypes.class);
	
	/** The seed. */
	private int seed;
	
	/** The nb query conjunct. */
	private int nbQueryConjunct;
	
	/** The input schema. */
	private String inputSchema;
	
	/** The query type. */
	private QueryTypes queryType;
	
	/**
	 * Instantiates a new runner test.
	 *
	 * @param schema the schema
	 * @param qt the qt
	 * @param q the q
	 * @param s the s
	 */
	public RunnerTest(String schema, QueryTypes qt, int q, int s) {
		this.seed = s;
		this.nbQueryConjunct = q;
		this.inputSchema = schema;
		this.queryType = qt;
	}
	
	/**
	 * Test minimal run.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testMinimalRun() throws Exception {
		List<String> params = new LinkedList<>();
		params.add("planner");
		params.add("-Ddatabase_name=pdq_chase_junit");
		params.add("-Dtimeout=90000"); // Stop search after 15 minutes
		params.add("-Dseed=" + this.seed);
		params.add("-Dinput_schema=" + this.inputSchema);
		params.add("-Dquery_type=" + this.queryType);
		params.add("-Dquery_conjuncts=" + this.nbQueryConjunct);
		Runner r = new PlannerBenchmark(params.toArray(new String[params.size()]));
	}

	/**
	 * As linked set.
	 *
	 * @param <T> the generic type
	 * @param array the array
	 * @return the sets the
	 */
	private static <T> Set<T> asLinkedSet(T... array) {
		Set<T> result = new LinkedHashSet<>();
		for (T i: array) {
			result.add(i);
		}
		return result;
	}

	/**
	 * As set.
	 *
	 * @param <T> the generic type
	 * @param e the e
	 * @return the sets the
	 */
	private static <T extends Enum<?>> Set<T> asSet(Class<T> e) {
		Set<T> result = new LinkedHashSet<>();
		for (T i: e.getEnumConstants()) {
			result.add(i);
		}
		return result;
	}
}
