package uk.ac.ox.cs.pdq.test.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class SchemaTest.
 *
 * @author Julien Leblay
 */
public class SchemaTest {
	
	/** The schema. */
	Schema schema;
	
	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
		SchemaBuilder builder = new SchemaBuilder();
		builder.addRelation(Relation.create("R", new Attribute[]{Attribute.create(Integer.class, "A")}));
		this.schema = builder.build();
	}
	
	/**
	 * Test get relations.
	 */
	@Test 
	public void testGetRelations() {
		Relation[] r1 = schema.getRelations();
		Relation[] r2 = schema.getRelations();
		Assert.assertArrayEquals(r1, r2);
	}
}
