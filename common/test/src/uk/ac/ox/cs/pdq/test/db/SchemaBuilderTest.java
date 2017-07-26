package uk.ac.ox.cs.pdq.test.db;


import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class SchemaBuilderTest.
 */
public class SchemaBuilderTest {
	
	/** The schema. */
	private Schema schema = null;
	
	/** The relation names. */
	private List<String> relationNames = Lists.newArrayList(
			new String[] {"r1", "r2", "r3", "r4"}
	);
	
	/** The relation arities. */
	private List<Integer> relationArities = Lists.newArrayList(
			new Integer[] {2, 1, 3, 2}
	);
	
	/** The binding positions. */
	private Integer[][] bindingPositions = 
			new Integer[][] {
					{}, {1}, {2, 3}, {}
			};
	
	/** The attributes. */
	private Attribute[][] attributes = 
			new Attribute[][] {
					{Attribute.create(String.class, "r1.1"), Attribute.create(Integer.class, "r1.1")},
					{Attribute.create(String.class, "r2.1")},
					{Attribute.create(String.class, "r3.1"), Attribute.create(Integer.class, "r3.2"), Attribute.create(Integer.class, "r3.3")},
					{Attribute.create(Integer.class, "r4.1"), Attribute.create(Integer.class, "r4.1")}
			};
	
	/**
	 * Instantiates a new schema builder test.
	 *
	 * @throws ReflectiveOperationException the reflective operation exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SchemaBuilderTest() throws ReflectiveOperationException, IOException {
		this.schema = this.build();
	}
	
	/**
	 * Builds the.
	 *
	 * @return the schema
	 */
	private Schema build() {
		SchemaBuilder result = new SchemaBuilder();
		for (int i = 0, l = this.relationNames.size(); i < l; i++) {
			result.addRelation(Relation.create(
					this.relationNames.get(i),
					this.attributes[i],
					new AccessMethod[]{AccessMethod.create(this.bindingPositions[i])}
					));
		}
		return result.build();
	}
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	/**
	 * Test number of relations.
	 */
	@Test
	public void testNumberOfRelations() {
		Assert.assertEquals(this.schema.getRelations().length, 4);
	}
	
	/**
	 * Test relation names.
	 */
	@Test
	public void testRelationNames() {
		int i = 0;
		for (Relation r: this.schema.getRelations()) {
			Assert.assertEquals(r.getName(), this.relationNames.get(i++));
		}
	}
	
	/**
	 * Test relation arities.
	 */
	@Test
	public void testRelationArities() {
		int i = 0;
		for (Relation r: this.schema.getRelations()) {
			Assert.assertEquals((Integer) r.getArity(), this.relationArities.get(i++));
		}
	}
	
	/**
	 * Test attribute names.
	 */
	@Test
	public void testAttributeNames() {
		int i = 0;
		for (Relation r: this.schema.getRelations()) {
			int j = 0;
			for (Attribute a: r.getAttributes()) {
				Assert.assertEquals(a, this.attributes[i][j++]);
			}
			i++;
		}
	}

	/**
	 * Test access method methods.
	 */
	@Test
	public void testAccessMethodMethods() {
		int i = 0;
		for (Relation r: this.schema.getRelations()) {
			for (AccessMethod b: r.getAccessMethods()) 
				Assert.assertArrayEquals(b.getInputs(), this.bindingPositions[i]);
			i++;
		}
	}
}
