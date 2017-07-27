package uk.ac.ox.cs.pdq.test.planner.dominance;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.AccessCountCostEstimator;
import uk.ac.ox.cs.pdq.planner.dominance.CostFactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.test.planner.TestObjects1;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class ClosedDominanceTest.
 *
 * @author Efthymia Tsamoura
 */

public class ClosedDominanceTest extends TestObjects1{

	/** The cdomominance. */
	CostFactDominance cdomominance = new CostFactDominance(new AccessCountCostEstimator(null), new FastFactDominance(false), false);

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.planner.TestObjects1#setup()
	 */
	@Before public void setup() {
		super.setup();
        MockitoAnnotations.initMocks(this);
        
        when(config11.getState()).thenReturn(config11State);
		when(config11State.getInferred()).thenReturn(Lists.newArrayList(p1,p2,p3));
		when(config11.getPlan()).thenReturn(plan11);
		when(plan11Cost).thenReturn(new DoubleCost(3.0));
		when(config11.isClosed()).thenReturn(true);
		
		when(config12.getState()).thenReturn(config12State);
		when(config12State.getInferred()).thenReturn(Lists.newArrayList(p3,p2));
		when(config12.getPlan()).thenReturn(plan12);
		when(plan12Cost).thenReturn(new DoubleCost(3.0));
		when(config12.isClosed()).thenReturn(true);
		
		when(config21.getState()).thenReturn(config21State);
		when(config21State.getInferred()).thenReturn(Lists.newArrayList(p1,p2,p3,p4));
		when(config21.getPlan()).thenReturn(plan21);
		when(plan21Cost).thenReturn(new DoubleCost(4.0));
		when(config21.isClosed()).thenReturn(true);
		
		when(config22.getState()).thenReturn(config22State);
		when(config22State.getInferred()).thenReturn(Lists.newArrayList(p1,p2,p4));
		when(config22.getPlan()).thenReturn(plan22);
		when(plan22Cost).thenReturn(new DoubleCost(2.0));
		when(config22.isClosed()).thenReturn(false);
		
		when(config31.getState()).thenReturn(config31State);
		when(config31State.getInferred()).thenReturn(Lists.newArrayList(p1,p2,p3,p4,p5,p6,p7));
		when(config31.getPlan()).thenReturn(plan31);
		when(plan31Cost).thenReturn(new DoubleCost(2.0));
		when(config31.isClosed()).thenReturn(false);
		
		when(config32.getState()).thenReturn(config32State);
		when(config32State.getInferred()).thenReturn(Lists.newArrayList(p1,p2,p3,p4,p5,p6,p7));
		when(config32.getPlan()).thenReturn(plan32);
		when(plan32Cost).thenReturn(new DoubleCost(3.0));
		when(config32.isClosed()).thenReturn(true);
	}

	/**
	 * Test1.
	 */
	@Test public void test1() {
		Assert.assertEquals(cdomominance.isDominated(config11, config12), false);
		Assert.assertEquals(cdomominance.isDominated(config12, config11), false);
	}

	/**
	 * Test2.
	 */
	@Test public void test2() {
		Assert.assertEquals(cdomominance.isDominated(config21, config22), false);
		Assert.assertEquals(cdomominance.isDominated(config22, config21), false);
	}

	/**
	 * Test3.
	 */
	@Test public void test3() {
		Assert.assertEquals(cdomominance.isDominated(config31, config32), false);
		Assert.assertEquals(cdomominance.isDominated(config32, config31), true);
	}

}
