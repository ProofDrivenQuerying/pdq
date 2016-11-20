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
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.generator.QueryGenerator;
import uk.ac.ox.cs.pdq.generator.SchemaGenerator;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.DependencyGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.QueryGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.SchemaGeneratorFirst;

// TODO: Auto-generated Javadoc
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

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	@Parameters
	public static Collection<Object[]> getParameters() {
		return ParameterizedTest.getParameters(seeds, numberOfTGDs, queryTypes);
	}
	
	/** The seeds. */
	private static Set<Integer> seeds = ParameterizedTest.asSet(1);
	
	/** The number of tg ds. */
	private static Set<Integer> numberOfTGDs = ParameterizedTest.asSet(5, 10, 25);
	
	/** The query types. */
	private static Set<QueryTypes> queryTypes = ParameterizedTest.asSet(QueryTypes.ACYCLIC, QueryTypes.GUARDED, QueryTypes.CHAINGUARDED);

	/** The params. */
	private BenchmarkParameters params;
	
	/** The schema. */
	private Schema schema;
	
	/** The query. */
	private ConjunctiveQuery query;
	
	/** The dependencies. */
	private List<Dependency> dependencies;

	/**
	 * Instantiates a new dependency generator test.
	 *
	 * @param seed the seed
	 * @param nbTGDs the nb tg ds
	 * @param queryType the query type
	 * @throws Exception the exception
	 */
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
	
	/**
	 * Test each relation in generate dependencies.
	 */
	@Test
	public void testEachRelationInGenerateDependencies() {
		List<String> relationNames = new ArrayList<>();
		for (Relation r: this.schema.getRelations()) {
			relationNames.add(r.getName());
		}
		for (Dependency ic : this.dependencies) {
			for (Atom a : ((TGD) ic).getAtoms()) {
				assertTrue("Relation " + a.getPredicate().getName() + " not present in schema.",
						relationNames.contains(a.getPredicate().getName()));
			}
		}
	}

	/**
	 * Test no repeated variables in dependencies.
	 */
	@Test
	public void testNoRepeatedVariablesInDependencies() {
		List<Term> vars = new ArrayList<>();
		for (Dependency ic: this.dependencies) {
			for (Atom a: ((TGD) ic).getAtoms()) {
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
