// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.cost.estimators.CountNumberOfAccessedRelationsCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.FixedCostPerAccessCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TotalNumberOfOutputTuplesPerAccessCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.util.PdqTest;

import static org.mockito.Mockito.when;

/**
 * This unit test aims to demonstrate some of the OrderIndependentCostEstimators
 * and how they can be used from the most simple to the more complicated ones.
 * <br/>
 * 
 * <li>CountNumberOfAccessedRelationsCostEstimator - this will give the number
 * of AccessTerms in the plan. No input required.</li>
 * 
 * <li>FixedCostPerAccessCostEstimator - this gives weight to each access term,
 * a catalog object have to be provided as input with the cost values for each
 * access method.</li>
 * 
 * <li>TotalNumberOfOutputTuplesPerAccessCostEstimator -The cost of the plan
 * equals the total number of output tuples per access. Requires column
 * cardinalities given as a catalog.</li>
 * 
 * @author Gabor
 *
 */
public class OrderIndependentCostEstimatorTest extends PdqTest {

	/**
	 * Creates a dummy plan with 3 access terms. The
	 * CountNumberOfAccessedRelationsCostEstimator should return 3.0 as cost.
	 */
	@Test
	public void CountNumberOfAccessedRelationsCostEstimatorTest() {
		// plan
		AccessTerm at1 = AccessTerm.create(R, method0);
		AccessTerm at2 = AccessTerm.create(S, method0);
		AccessTerm at3 = AccessTerm.create(T, method0);
		JoinTerm jt1 = JoinTerm.create(at1, at2);
		JoinTerm jt2 = JoinTerm.create(jt1, at3);
		
		// cost
		CountNumberOfAccessedRelationsCostEstimator estimator = new CountNumberOfAccessedRelationsCostEstimator();
		Assert.assertEquals(3.0, estimator.cost(jt2).getCost(), 0.0001);
	}

	/**
	 * According to this implementation, the cost of a plan equals the sum of the
	 * costs of the associated accesses. ( 100 + 110 + 101 = 311 )
	 */
	@Test
	public void FixedCostPerAccessCostEstimatorTest() {
		// plan
		AccessTerm at1 = AccessTerm.create(R, method0);
		AccessTerm at2 = AccessTerm.create(S, method0);
		AccessTerm at3 = AccessTerm.create(T, method0);
		JoinTerm jt1 = JoinTerm.create(at1, at2);
		JoinTerm jt2 = JoinTerm.create(jt1, at3);
		
		// cost inputs
		SimpleCatalog catalog = Mockito.mock(SimpleCatalog.class);
		when(catalog.getCost(R, method0)).thenReturn(100.0);
		when(catalog.getCost(S, method0)).thenReturn(110.0);
		when(catalog.getCost(T, method0)).thenReturn(101.0);
		
		// cost calculation
		FixedCostPerAccessCostEstimator estimator = new FixedCostPerAccessCostEstimator(catalog);
		Assert.assertEquals(311.0, estimator.cost(jt2).getCost(), 0.0001);

	}

	/**
	 * The cost of the plan equals the total number of output tuples per access .
	 */
	@Test
	public void TotalNumberOfOutputTuplesPerAccessCostEstimatorTest() {
		// plan 
		AccessTerm at1 = AccessTerm.create(R, method0);
		AccessTerm at2 = AccessTerm.create(S, method0);
		AccessTerm at3 = AccessTerm.create(T, method0);
		JoinTerm jt1 = JoinTerm.create(at1, at2);
		JoinTerm jt2 = JoinTerm.create(jt1, at3);
		
		// cost inputs
		SimpleCatalog catalog = Mockito.mock(SimpleCatalog.class);
		when(catalog.getTotalNumberOfOutputTuplesPerInputTuple(R, method0)).thenReturn(100);
		when(catalog.getTotalNumberOfOutputTuplesPerInputTuple(S, method0)).thenReturn(110);
		when(catalog.getTotalNumberOfOutputTuplesPerInputTuple(T, method0)).thenReturn(101);

		// cost calculation
		TotalNumberOfOutputTuplesPerAccessCostEstimator estimator = new TotalNumberOfOutputTuplesPerAccessCostEstimator(catalog);
		Assert.assertEquals(311.0, estimator.cost(jt2).getCost(), 0.0001);
	}
}
