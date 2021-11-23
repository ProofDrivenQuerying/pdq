// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.db;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

// @author Gabor
public class RelationTest extends PdqTest {

	// Create three relations and test for equality
	@Test
	public void testSimpleCreation() {
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		Relation relation2 = Relation.create("R2", new Attribute[] {Attribute.create(Integer.class, "attr2")});
		Relation relationSameAs1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		
		if (relation1 != relationSameAs1) {
			Assert.fail("Relation cache does not provide same reference");
		}
		Assert.assertEquals("R2",relation2.getName());
		Assert.assertEquals(1,relation2.getArity());
		Assert.assertEquals(0,relation2.getAttributePosition("attr2"));
		Assert.assertEquals(relation2.getAttribute("attr2"),relation2.getAttribute(0));
		Assert.assertNotNull(relation2.getAttribute(0));
		Assert.assertEquals(Integer.class,relation2.getAttribute(0).getType());
	}
	
	// Test a relation during cache restart
	@Test
	public void testRelationCacheReset() {
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		Relation relationSameAs1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		if (relation1 != relationSameAs1) {
			Assert.fail("Relation cache does not provide same reference");
		}
		uk.ac.ox.cs.pdq.algebra.Cache.reStartCaches();
		uk.ac.ox.cs.pdq.db.Cache.reStartCaches();
		uk.ac.ox.cs.pdq.fol.Cache.reStartCaches();
		Relation relationSameAs1_2 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr2")});
		if (relation1 == relationSameAs1_2) {
			Assert.fail("Relation cache did not reset.");
		}
	}
	
}
