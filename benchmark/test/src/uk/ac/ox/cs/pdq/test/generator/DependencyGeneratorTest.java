package uk.ac.ox.cs.pdq.test.generator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters.QueryTypes;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.QueryGenerator;
import uk.ac.ox.cs.pdq.generator.SchemaGenerator;
import uk.ac.ox.cs.pdq.generator.first.DependencyGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.first.QueryGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.first.SchemaGeneratorFirst;

/**
 * Generates query or views based on an input Schema, and external parameters.
 * 
 * @author Efi TSAMOURA
 * @author Julien LEBLAY
 *
 */
@Ignore
@RunWith(Parameterized.class) 
public class DependencyGeneratorTest extends ParameterizedTest {

	@Parameters
	public static Collection<Object[]> getParameters() {
		return ParameterizedTest.getParameters(seeds, numberOfTGDs, queryTypes);
	}
	
	private static Set<Integer> seeds = ParameterizedTest.asSet(1);
	private static Set<Integer> numberOfTGDs = ParameterizedTest.asSet(5, 10, 25);
	private static Set<QueryTypes> queryTypes = ParameterizedTest.asSet(QueryTypes.ACYCLIC, QueryTypes.GUARDED, QueryTypes.CHAINGUARDED);

	private BenchmarkParameters params;
	private Schema schema;
	private ConjunctiveQuery query;
	private List<Constraint> dependencies;

	public DependencyGeneratorTest(Integer seed, Integer nbTGDs, QueryTypes queryType) throws Exception {
		this.params = new BenchmarkParameters();
		this.params.setSeed(seed);
		this.params.setQueryType(queryType);
		this.params.setNumberOfConstraints(nbTGDs);
		
		SchemaGenerator sgen = new SchemaGeneratorFirst(this.params);
		this.schema = sgen.generate();
		
		QueryGenerator queryGen = new QueryGeneratorFirst(this.schema, this.params);
		this.query = queryGen.generate();
		this.schema = new DependencyGeneratorFirst(this.schema, this.query, this.params).generate();
		this.dependencies = this.schema.getDependencies(); 
	}
	
	@Test
	public void testEachRelationInGenerateDependencies() {
		List<String> relationNames = new ArrayList<>();
		for (Relation r: this.schema.getRelations()) {
			relationNames.add(r.getName());
		}
		for (Constraint ic : this.dependencies) {
			for (Predicate a : ((TGD) ic).getPredicates()) {
				assertTrue("Relation " + a.getName() + " not present in schema.",
						relationNames.contains(a.getName()));
			}
		}
	}

	@Test
	public void testNoRepeatedVariablesInDependencies() {
		List<Term> vars = new ArrayList<>();
		for (Constraint ic: this.dependencies) {
			for (Predicate a: ((TGD) ic).getPredicates()) {
				vars.clear();
				for (Term t: a.getTerms()) {
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
}
