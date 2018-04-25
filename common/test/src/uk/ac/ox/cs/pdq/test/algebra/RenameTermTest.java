package uk.ac.ox.cs.pdq.test.algebra;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the RenameTerm class.
 * @author Mark Ridler
 *
 */
public class RenameTermTest extends PdqTest {

	public RenameTermTest() {
	}
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		PdqTest.reInitalize(this);
	}

	/*
	 * Tests the RenameTerm class by calling every method.
	 * Other classes are used as required.
	 * INPUTS: None
	 * OUTPUTS: Documented below
	 * PERFORMANCE: On 11/4/2018 this JUnit test returned in 0.5s on Mark's machine
	 */
	
	@Test
	public void testCreation() {
	
		Attribute[] renamings = new Attribute[] {
				Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2")
				};
		Relation relation = Relation.create("relation", renamings);
		RelationalTerm child = AccessTerm.create(relation, AccessMethod.create(new Integer[] {0}));
	
		// Constructor tests invariant
		RenameTerm rt = RenameTerm.create(renamings, child);
		
		// RenameTerm.equals null should be false
		boolean b = rt.equals(null);
		Assert.assertFalse(b);
		
		// RenameTerm.getClass has an expected name
		Assert.assertTrue(rt.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.RenameTerm");

		// RenameTerm.toString is #1=#2&#3=#4
		String s = rt.toString();
		Assert.assertTrue(s.equals("Rename{[attribute1,attribute2]Access{relation.mt_0[#0=attribute1]}}"));
		
		// RelationalTerm returned from RenameTerm.getAccesses is invariant
		Set<AccessTerm> sat = rt.getAccesses();
		Assert.assertNotNull(sat);
		Assert.assertFalse(sat.isEmpty());

		// RelationalTerm returned from RenameTerm.getChild is invariant
		RelationalTerm p = rt.getChild(0);
		Assert.assertNotNull(p);

        // Class returned from getClass has name RenameTerm 
		Assert.assertTrue(rt.getClass().getName() == "uk.ac.ox.cs.pdq.algebra.RenameTerm");
		
		// RelationalTerm returned from RenameTerm.getInputAttribute is invariant
		Attribute aa = rt.getInputAttribute(0);
		Assert.assertNotNull(aa);

		// array returned from RenameTerm.getInputAttributes has zero length
		Attribute[] aaa = rt.getInputAttributes();
		Assert.assertTrue(aaa.length == 1);
		
		// RenameTerm.getNumberOfInputAttributes is one
		int nia = rt.getNumberOfInputAttributes();
		Assert.assertTrue(nia == 1);

		// RenameTerm.getNumberOfOutputAttributes is two
		int noa = rt.getNumberOfOutputAttributes();
		Assert.assertTrue(noa == 2);

		// RenameTerm.getOutputAttribute is invariant
		Attribute oa = rt.getOutputAttribute(0);
		Assert.assertNotNull(oa);
		//Assert.assertTrue(oa.invariant());
		
		// RenameTerm.getOutputAttributes has length two
		Attribute[] oas = rt.getOutputAttributes();
		Assert.assertTrue(oas.length == 2);
		
		// getrenamings has length two
		Attribute[] p2 = rt.getRenamings();
		Assert.assertTrue(p2.length == 2);
		
		// isClosed is true
		boolean b2 = rt.isClosed();
		Assert.assertFalse(b2);
	}
}
