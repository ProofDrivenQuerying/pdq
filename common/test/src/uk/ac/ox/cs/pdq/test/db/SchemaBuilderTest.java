package uk.ac.ox.cs.pdq.test.db;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethod.Types;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;

import com.google.common.collect.Lists;

public class SchemaBuilderTest {
	
	private Schema schema = null;
	
	private List<String> relationNames = Lists.newArrayList(
			new String[] {"r1", "r2", "r3", "r4"}
	);
	private List<Integer> relationArities = Lists.newArrayList(
			new Integer[] {2, 1, 3, 2}
	);
	private List<Types> bindingTypes = Lists.newArrayList(
			new Types[] {
					Types.FREE, Types.BOOLEAN,
					Types.LIMITED
			}
	);
	private List<Integer[]> bindingPositions = Lists.newArrayList(
			new Integer[][] {
					{}, {1}, {2, 3}, {}
			}
	);
	private List<Attribute[]> attributes = Lists.newArrayList(
			new Attribute[][] {
					{new Attribute(String.class, "r1.1"), new Attribute(Integer.class, "r1.1")},
					{new Attribute(String.class, "r2.1")},
					{new Attribute(String.class, "r3.1"), new Attribute(Integer.class, "r3.2"), new Attribute(Integer.class, "r3.3")},
					{new Attribute(Integer.class, "r4.1"), new Attribute(Integer.class, "r4.1")}
			}
	);
	
	public SchemaBuilderTest() throws ReflectiveOperationException, IOException {
		this.schema = this.build();
	}
	
	private Schema build() {
		SchemaBuilder result = Schema.builder();
		for (int i = 0, l = this.relationNames.size(); i < l; i++) {
			result.addRelation(new Relation(
					this.relationNames.get(i),
					Lists.newArrayList(this.attributes.get(i)),
					Lists.newArrayList(new AccessMethod(this.bindingTypes.get(i%bindingTypes.size()), Lists.newArrayList(this.bindingPositions.get(i))))
					) {});
		}
		return result.build();
	}
	
	@Test
	public void testNumberOfRelations() {
		assertEquals(this.schema.getRelations().size(), 4);
	}
	
	@Test
	public void testRelationNames() {
		int i = 0;
		for (Relation r: this.schema.getRelations()) {
			assertEquals(r.getName(), this.relationNames.get(i++));
		}
	}
	
	@Test
	public void testRelationArities() {
		int i = 0;
		for (Relation r: this.schema.getRelations()) {
			assertEquals((Integer) r.getArity(), this.relationArities.get(i++));
		}
	}
	
	@Test
	public void testAttributeNames() {
		int i = 0;
		for (Relation r: this.schema.getRelations()) {
			int j = 0;
			for (Attribute a: r.getAttributes()) {
				assertEquals(a, this.attributes.get(i)[j++]);
			}
			i++;
		}
	}

	@Test
	public void testAccessMethodMethods() {
		int i = 0;
		for (Relation r: this.schema.getRelations()) {
			int j = 0;
			for (AccessMethod b: r.getAccessMethods()) {
				assertEquals(b.getType(), this.bindingTypes.get(i%bindingTypes.size()));
				if (b.getType() == Types.BOOLEAN 
						|| b.getType() == Types.LIMITED) {
					assertEquals(b.getInputs(), Lists.newArrayList(this.bindingPositions.get(i)));
				}
			}
			i++;
		}
	}
}
