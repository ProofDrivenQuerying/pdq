package uk.ac.ox.cs.pdq.test.db;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

/**
 * @author Julien Leblay
 */
public class SchemaTest {
	
	Schema schema;
	
	@Before public void setup() {
		Utility.assertsEnabled();
		SchemaBuilder builder = new SchemaBuilder();
		builder.addRelation(
				new Relation("R", Lists.newArrayList(new Attribute(Integer.class, "A"))) {
		});
		this.schema = builder.build();
	}
	
	@Test public void testGetRelations() {
		List<Relation> r1 = schema.getRelations();
		List<Relation> r2 = schema.getRelations();
		Assert.assertSame(r1, r2);
	}
}
