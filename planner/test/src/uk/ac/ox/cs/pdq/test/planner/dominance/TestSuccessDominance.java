package uk.ac.ox.cs.pdq.test.planner.dominance;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominance;
import uk.ac.ox.cs.pdq.planner.dominance.SuccessDominanceFactory;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/** 
 * Tests the isDominated function of the SuccessDominance class.
 *  
 * @author Gabor
 *
 */
public class TestSuccessDominance extends PdqTest {

	/**
	 * Uses test relations to create access methods and dummy costs for them. Checks open and closed plan dominance tests. 
	 */
	@Test
	public void testSuccessDominance() {
		SuccessDominanceFactory factory = new SuccessDominanceFactory();
		SuccessDominance sd = factory.getInstance();
		
		AccessTerm at1 = AccessTerm.create(R, method0);
		AccessTerm at2 = AccessTerm.create(S, method0);
		AccessTerm at3 = AccessTerm.create(S, method1);
		JoinTerm join = JoinTerm.create(at1, at3);
		Cost c1 = new DoubleCost();
		Cost c2 = new DoubleCost(1.0);
		
		// checking closed plans
		Assert.assertFalse(sd.isDominated(at1, c1, at2, c2));
		
		Assert.assertTrue(sd.isDominated(at1, c2, at2, c1));
		
		// checking open plans
		Assert.assertFalse(sd.isDominated(at1, c2, at3, c1));
		
		Assert.assertTrue(sd.isDominated(join, c2, at3, c1));
	}
	

}
