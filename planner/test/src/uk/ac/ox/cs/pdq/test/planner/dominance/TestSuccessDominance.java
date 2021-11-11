// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.planner.dominance;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator;
import uk.ac.ox.cs.pdq.planner.dominance.CostDominance;
import uk.ac.ox.cs.pdq.util.PdqTest;

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
		CostDominance sd = new CostDominance(new CountNumberOfAccessedRelationsCostEstimator());
		
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
