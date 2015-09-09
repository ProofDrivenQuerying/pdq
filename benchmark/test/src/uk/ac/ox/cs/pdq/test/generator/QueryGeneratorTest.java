package uk.ac.ox.cs.pdq.test.generator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters.QueryTypes;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Signature;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.first.QueryGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.first.SchemaGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.second.QueryGeneratorSecond;
import uk.ac.ox.cs.pdq.io.xml.SchemaReader;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Generates query or views based on an input Schema, and external parameters.
 * 
 * @author Efi TSAMOURA
 * @author Julien LEBLAY
 *
 */
@Ignore
@RunWith(Parameterized.class) 
public class QueryGeneratorTest extends ParameterizedTest {

	@Parameters
	public static Collection<Object[]> getParameters() {
		return ParameterizedTest.getParameters(
				asSet(1/*, 2, 3*/),
				asSet(5/*, 10, 50*/),
				asSet(1, 2/*, 5, 10*/),
				asSet("", inputSchemas[0], inputSchemas[1])
				);
	}

	private static String[] inputSchemas = new String[] {
		"test/unit/schema-mysql-tpch.xml",
		"test/unit/schema-postgresql-tpch.xml"
	};
	

	private BenchmarkParameters params;
	private Schema schema;

	public QueryGeneratorTest(Integer seed, Integer nbRelations, Integer arity, String inputSchema) throws Exception {
		this.params = new BenchmarkParameters();
		this.params.setSeed(seed);
		this.params.setNumberOfRelations(nbRelations);
		this.params.setArity(arity);
		if (inputSchema == null || inputSchema.trim().isEmpty()) {
			this.schema = new SchemaGeneratorFirst(this.params).generate();
		} else {
			this.schema = new SchemaReader().read(new FileInputStream(inputSchema));
		}
	}

	@Test
	public void testGenerateQueryOrthogonalToUnderlyingDatabase() throws FileNotFoundException, IOException {
		try(InputStream in1 = new FileInputStream(inputSchemas[0]);
			InputStream in2 = new FileInputStream(inputSchemas[1])) {
			Schema s = new SchemaReader().read(in1);
			Variable.resetCounter();
			ConjunctiveQuery q1 = new QueryGeneratorSecond(s, this.params).generate();

			s = new SchemaReader().read(in2);
			Variable.resetCounter();
			ConjunctiveQuery q2 = new QueryGeneratorSecond(s, this.params).generate();

			assertTrue("Different queries generated from same inputs " + q1 + "\n vs. " + q2,  q1 == null ? q2 == null : q1.equals(q2));
		}
	}

	@Test
	public void testGenerateQueryFromInlusionDependencies() {
		Variable.resetCounter();
		try {
			ConjunctiveQuery q1 = 
					new QueryGeneratorSecond(this.schema, this.params).generate();
			if (q1 != null) {
				this.testEachRelationInGenerateQuery(q1);
				this.testNoRepeatedVariablesInQuery(q1);
				this.testQueryHasNoCartesianProducts(q1);
				if (!this.params.getRepeatedRelations()) {
					this.testNoRepeatedRelationQuery(q1);
				}
				
				Variable.resetCounter();
				ConjunctiveQuery q2 =
						new QueryGeneratorSecond(this.schema, this.params).generate();
				assertTrue("Different queries (from inclusion deps) generated from same inputs " + q1 + "\n vs. " + q2,  q1.equals(q2));
			}
		} catch (IllegalStateException e) {
			Assert.assertTrue("Ignoring test because schema has no fk dependencies", true);
		}

	}

	@Test
	public void testGenerateGuardedQuery() {
		Variable.resetCounter();
		this.params.setQueryType(QueryTypes.GUARDED);
		ConjunctiveQuery q1 = new QueryGeneratorFirst(this.schema, this.params).generate();

		this.testEachRelationInGenerateQuery(q1);
		this.testQueryIsGuarded(q1);
		if (!this.params.getRepeatedRelations()) {
			this.testNoRepeatedRelationQuery(q1);
		}
		
		Variable.resetCounter();
		this.params.setQueryType(QueryTypes.GUARDED);
		ConjunctiveQuery q2 = new QueryGeneratorFirst(this.schema, this.params).generate();
		assertTrue("Different queries (from inclusion deps) generated from same inputs " + q1 + "\n vs. " + q2,  q1.equals(q2));
	}

	@Test
	public void testGenerateAcyclicQuery() {
		Variable.resetCounter();
		this.params.setQueryType(QueryTypes.ACYCLIC);
		ConjunctiveQuery q1 = new QueryGeneratorFirst(this.schema, this.params).generate();

		this.testEachRelationInGenerateQuery(q1);
		this.testQueryAcyclic(q1);
		this.testQueryHasNoCartesianProducts(q1);
		if (!this.params.getRepeatedRelations()) {
			this.testNoRepeatedRelationQuery(q1);
		}
	
		Variable.resetCounter();
		this.params.setQueryType(QueryTypes.ACYCLIC);
		ConjunctiveQuery q2 = new QueryGeneratorFirst(this.schema, this.params).generate();
		assertTrue("Different queries (from inclusion deps) generated from same inputs " + q1 + "\n vs. " + q2,  q1.equals(q2));
	}
	
	public void testEachRelationInGenerateQuery(ConjunctiveQuery q) {
		List<String> relationNames = new ArrayList<>();
		for (Relation r: this.schema.getRelations()) {
			relationNames.add(r.getName());
		}
		for (Predicate a : q.getBody()) {
			assertTrue("Relation " + a.getName() + " not present in schema.",
					relationNames.contains(a.getName()));
		}
	}
	
	public void testFreeVariableRatio(ConjunctiveQuery q) {
		Set<Term> terms = new LinkedHashSet<>();
		for (Predicate a : q.getBody()) {
			terms.addAll(a.getTerms());
		}
		assertTrue("Free variable ratio not satisfied in " + q,
				terms.size() * this.params.getFreeVariable() == q.getFree().size());
	}

	public void testQueryIsGuarded(ConjunctiveQuery q) {
		List<Term> freeVars = q.getHead().getTerms();
		for (Predicate a : q.getBody().getPredicates()) {
			if (a.getTerms().containsAll(freeVars)) {
				assertTrue("Guard not found", true);
				return;
			}
		}
		fail("Guard not found in query");
	}

	/**
	 * Checks whether the given query contains a cycle.
	 * @param q
	 */
	public void testQueryAcyclic(ConjunctiveQuery q) {
		// Building variable clusters
		SetMultimap<Variable, Predicate> clusters = LinkedHashMultimap.create();
		for (Predicate p: q.getBody()) {
			for (Term t: p.getTerms()) {
				if (t instanceof Variable) {
					clusters.put((Variable) t, p);
				}
			}
		}

		// Building query graph
		SetMultimap<Predicate, Predicate> queryGraph = LinkedHashMultimap.create();
		for (Predicate p: q.getBody()) {
			for (Term t: p.getTerms()) {
				Set<Predicate> neighbours = queryGraph.get(p);
				if (neighbours.contains(p)) {
					fail("Cycle detected in " + q);
				} else {
					Collection<Predicate> allButMe = new LinkedHashSet<>(clusters.get((Variable) t));
					allButMe.remove(p);
					queryGraph.putAll(p, allButMe);
				}
			}
		}
	}

	/**
	 * Test whether the query has cartesian product by checking if there is
	 * more than 1 connected component in the query body.
	 * @param q
	 */
	public void testQueryHasNoCartesianProducts(ConjunctiveQuery q) {
		Map<Variable, Set<Predicate>> joins = new LinkedHashMap<>();
		for (Predicate p: q.getBody()) {
			for (Term t: p.getTerms()) {
				if (t instanceof Variable) {
					Set<Predicate> preds = joins.get(t);
					if (preds == null) {
						preds = new LinkedHashSet<>();
						joins.put((Variable) t, preds);
					}
					preds.add(p);
				}
			}
		}
		assertTrue("The query has a cartesian product " + q, this.connectedComponents(Lists.newArrayList(joins.values())).size() == 1);
	}

	
	/**
	 * @param clusters
	 * @return return a partition of the given clusters, such that all
	 * predicates in the each component are connected, and no predicates part
	 * of distinct component are connected.
	 */
	private List<Set<Predicate>> connectedComponents(List<Set<Predicate>> clusters) {
		List<Set<Predicate>> result = new LinkedList<>();
		if (clusters.isEmpty()) {
			return result;
		}
		Set<Predicate> first = clusters.get(0);
		if (clusters.size() > 1) {
			List<Set<Predicate>> rest = this.connectedComponents(clusters.subList(1, clusters.size()));
			for (Set<Predicate> s : rest) {
				if (!Collections.disjoint(first, s)) {
					first.addAll(s);
				} else {
					result.add(s);
				}
			}
		}
		result.add(first);
		return result;
	}

	public void testNoRepeatedRelationQuery(ConjunctiveQuery q) {
		Set<Signature> signatures = new LinkedHashSet<>();
		for (Predicate p: q.getBody()) {
			if (signatures.contains(p.getSignature())) {
				fail("Repeated relation found in " + q);
				return;
			}
			signatures.add(p.getSignature());
		}
	}

	public void testNoRepeatedVariablesInQuery(ConjunctiveQuery q) {
		List<Term> vars = new ArrayList<>();
		for (Predicate a: q.getBody()) {
			for (Term t: a.getTerms()) {
				vars.clear();
				if (t instanceof Variable) {
					if (vars.contains(t)) {
						fail("Found repeated variable in query body.");
						return;
					}
					vars.add(t);
				}
			}
		}
	}
}
