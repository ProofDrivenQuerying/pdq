package uk.ac.ox.cs.pdq.test.algebra;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Utility;

public class RelationTermTest {
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	@Test
	public void testSimpleCreation() {
		AccessMethod am = AccessMethod.create("test",new Integer[] {0});
		AccessMethod am1 = AccessMethod.create("test1",new Integer[] {0});
		Relation relation = Relation.create("R0", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr2")});
		RelationalTerm	child1 = AccessTerm.create(relation,am);
		RelationalTerm	child2 = AccessTerm.create(relation1,am1);
		RelationalTerm	child3 = AccessTerm.create(relation,am);
		
		if (child1 != child3) { // ATTENTIONAL! it have to be the same reference
			Assert.fail("Relation cache does not provide same reference");
		}
		if (child1 == child2) { // ATTENTIONAL! it have to be different reference
			Assert.fail("Relation cache should not provide same reference");
		}
	}

}
