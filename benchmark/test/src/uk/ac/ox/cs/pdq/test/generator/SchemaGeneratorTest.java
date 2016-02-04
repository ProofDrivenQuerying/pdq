package uk.ac.ox.cs.pdq.test.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.ox.cs.pdq.benchmark.BenchmarkParameters;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.generator.tgdsfromquery.SchemaGeneratorFirst;

/**
 * Generates query or views based on an input Schema, and external parameters.
 * 
 * @author Julien LEBLAY
 *
 */
@Ignore
@RunWith(Parameterized.class) 
public class SchemaGeneratorTest extends ParameterizedTest {

	@Parameters
	public static Collection<Object[]> getParameters() {
		return ParameterizedTest.getParameters(
				asSet(1, 2, 3, 4, 5),
				asSet(5, 10, 50),
				asSet(1, 2, 5, 10),
				asSet(1, 3, 5)
				);
	}

	private BenchmarkParameters params;
	private Schema schema;

	public SchemaGeneratorTest(Integer seed, Integer relations, Integer arity, Integer bindings) throws Exception {
		this.params = new BenchmarkParameters();
		this.params.setSeed(seed);
		this.params.setNumberOfRelations(relations);
		this.params.setArity(arity);
		this.params.setNumberOfAccessMethods(bindings);
		this.schema = new SchemaGeneratorFirst(this.params).generate();
	}

	@Test
	public void testNumberOfRelations() {
		assertEquals((int) this.params.getNumberOfRelations(), this.schema.getRelations().size());
	}

	@Test
	public void testRelationsArities() {
		assertEquals((int) this.params.getArity(),
				this.schema.getMaxArity());
	}
	
	@Test
	public void testNumberOfAccessMethods() {
		int nbAccessMethods = this.params.getNumberOfAccessMethods();
		for (Relation r: this.schema.getRelations()) {
			if (r.getAccessMethods() != null) {
				assertTrue(
						"Relation " + r + " has " + r.getAccessMethods().size() + " binding method (> " + this.params.getNumberOfAccessMethods() + ")",
						r.getAccessMethods().size() <= this.params.getNumberOfAccessMethods());
			} else {
				assertTrue("Relation " + r + " has binding methods, while it should not.", nbAccessMethods == 0);
			}
		}
	}
	
	@Test
	public void testEachRelationAccessMethodsIsConsistent() {
		for (Relation r: this.schema.getRelations()) {
			for (AccessMethod b: r.getAccessMethods()) {
				switch(b.getType()) {
				case FREE:
					assertNotNull(b.getInputs());
					assertTrue(b.getInputs().isEmpty());
					break;
				case BOOLEAN:
				case LIMITED:
					assertNotNull(b.getInputs());
					for (int pos: b.getInputs()) {
						assertTrue(
								"AccessMethod method out of bounds '" + pos + " vs. " + r.getArity() + " in " + r,
								0 < pos && pos <= r.getArity());
					}
					break;
				default:
					assertNotNull(b.getInputs());
					assertEquals(
								"Inaccessible binding methods must have the same number of inputs as the relation arity",
								b.getInputs().size(), r.getArity());
					break;
				}
			}
		}
	}

	@Test
	public void testFreeAccessProbabilityIsCorrect() {
		double total = 0.0;
		double count = 0.0;
		for (Relation r: this.schema.getRelations()) {
			total++;
			for (AccessMethod b: r.getAccessMethods()) {
				if (b.getType() == Types.FREE) {
					count++;
				}
			}
		}
		assertEquals((Object) this.params.getFreeAccess(), (Object) (count / Math.max(1, total)));
	}
}