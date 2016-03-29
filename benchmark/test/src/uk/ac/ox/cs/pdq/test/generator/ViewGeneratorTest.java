package uk.ac.ox.cs.pdq.test.generator;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.QueryGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.SchemaGeneratorFirst;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.ViewGeneratorFirst;

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
public class ViewGeneratorTest extends ParameterizedTest {

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	@Parameters
	public static Collection<Object[]> getParameters() {
		return ParameterizedTest.getParameters(asSet(1, 2, 3, 4, 5));
	}

	/** The params. */
	private BenchmarkParameters params;
	
	/** The schema. */
	private Schema schema;
	
	/** The query. */
	private ConjunctiveQuery query;
	
	/** The views. */
	private List<View> views;

	/**
	 * Instantiates a new view generator test.
	 *
	 * @param seed the seed
	 * @throws Exception the exception
	 */
	public ViewGeneratorTest(Integer seed) throws Exception {
		this.params = new BenchmarkParameters();
		this.params.setSeed(seed);
		this.params.setQueryType(BenchmarkParameters.QueryTypes.GUARDED);
		this.schema = new SchemaGeneratorFirst(this.params).generate();
		this.query = new QueryGeneratorFirst(this.schema, this.params).generate();
		this.views =new ViewGeneratorFirst(this.schema, this.query, this.params).generateViews();
	}

	/**
	 * Test each relation in generate query.
	 */
	@Test
	public void testEachRelationInGenerateQuery() {
		List<String> relationNames = new ArrayList<>();
		for (Relation r: this.schema.getRelations()) {
			relationNames.add(r.getName());
		}
		for (Atom a : this.query.getBody()) {
			assertTrue("Relation " + a.getName() + " not present in schema.",
					relationNames.contains(a.getName()));
		}
	}

	/**
	 * Test each view contained in query.
	 */
	@Test
	public void testEachViewContainedInQuery() {
		List<Atom> queryAtoms = this.query.getBody().getAtoms();
		for (View v: this.views) {
			Formula f = v.getDependency().getRight();
			// TODO: This is not true containment, check for homomorphism instead
			assertTrue("Formula " + f + " is not contained in query " + this.query,
					queryAtoms.containsAll(v.getDependency().getRight().getAtoms()));
		}
	}
}
