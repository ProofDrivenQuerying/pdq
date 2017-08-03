package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Gabor
 *
 */
public class RelationTest {

	public RelationTest() {
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
	public void testSimpleCreation() {
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		Relation relation2 = Relation.create("R2", new Attribute[] {Attribute.create(Integer.class, "attr2")});
		Relation relationSameAs1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		
		if (relation1 != relationSameAs1) { // ATTENTIONAL! it have to be the same reference
			Assert.fail("Relation cache does not provide same reference");
		}
		Assert.assertEquals("R2",relation2.getName());
		Assert.assertEquals(1,relation2.getArity());
		Assert.assertEquals(0,relation2.getAttributePosition("attr2"));
		Assert.assertEquals(relation2.getAttribute("attr2"),relation2.getAttribute(0));
		Assert.assertNotNull(relation2.getAttribute(0));
		Assert.assertEquals(Integer.class,relation2.getAttribute(0).getType());
	}
	
	@Test
	public void testRelationCacheReset() {
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		Relation relationSameAs1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		if (relation1 != relationSameAs1) { // ATTENTIONAL! it have to be the same reference
			Assert.fail("Relation cache does not provide same reference");
		}
		Relation.resetCache();
		Relation relationSameAs1_2 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr2")});
		if (relation1 == relationSameAs1_2) { // ATTENTIONAL! it would be the same reference if we did not reset.
			Assert.fail("Relation cache did not reset.");
		}
	}
	
}
