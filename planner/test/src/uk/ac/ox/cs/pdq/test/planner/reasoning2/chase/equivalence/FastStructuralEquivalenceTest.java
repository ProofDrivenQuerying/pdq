package uk.ac.ox.cs.pdq.test.planner.reasoning2.chase.equivalence;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.plan.DoubleCost;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.equivalence.FastStructuralEquivalence;
import uk.ac.ox.cs.pdq.test.planner.TestObjects1;

import com.google.common.collect.Lists;

/**
 * 
 * @author Efthymia Tsamoura
 *
 */
public class FastStructuralEquivalenceTest  extends TestObjects1{

	FastStructuralEquivalence cdomominance = new FastStructuralEquivalence();

	@Before public void setup() {
        MockitoAnnotations.initMocks(this);
        
		when(config11.getInferred()).thenReturn(Lists.newArrayList(p1.toString(),p2.toString(),p3.toString()));
		when(config11.getPlan()).thenReturn(plan11);
		when(plan11.getCost()).thenReturn(new DoubleCost(3.0));
		when(config11.isClosed()).thenReturn(false);
		when(config11.getInput()).thenReturn(Lists.<Constant>newArrayList(new Skolem("c1")));
		
		when(config12.getInferred()).thenReturn(Lists.newArrayList(p3.toString(),p2.toString()));
		when(config12.getPlan()).thenReturn(plan12);
		when(plan12.getCost()).thenReturn(new DoubleCost(3.0));
		when(config12.isClosed()).thenReturn(false);
		when(config12.getInput()).thenReturn(Lists.<Constant>newArrayList(new Skolem("c1")));
		
		when(config21.getInferred()).thenReturn(Lists.newArrayList(p1.toString(),p2.toString(),p3.toString(),p4.toString()));
		when(config21.getPlan()).thenReturn(plan21);
		when(plan21.getCost()).thenReturn(new DoubleCost(4.0));
		when(config21.isClosed()).thenReturn(true);
		when(config21.getInput()).thenReturn(Lists.<Constant>newArrayList());
		
		when(config22.getInferred()).thenReturn(Lists.newArrayList(p1.toString(),p2.toString(),p4.toString()));
		when(config22.getPlan()).thenReturn(plan22);
		when(plan22.getCost()).thenReturn(new DoubleCost(2.0));
		when(config22.isClosed()).thenReturn(true);
		when(config22.getInput()).thenReturn(Lists.<Constant>newArrayList());
		
		when(config31.getInferred()).thenReturn(Lists.newArrayList(p1.toString(),p2.toString(),p3.toString(),p4.toString(),p5.toString(),p6.toString(),p7.toString()));
		when(config31.getPlan()).thenReturn(plan31);
		when(plan31.getCost()).thenReturn(new DoubleCost(2.0));
		when(config31.isClosed()).thenReturn(false);
		when(config21.getInput()).thenReturn(Lists.<Constant>newArrayList(new Skolem("c1"), new Skolem("c3")));
		
		when(config32.getInferred()).thenReturn(Lists.newArrayList(p1.toString(),p2.toString(),p3.toString(),p4.toString(),p5.toString(),p6.toString(),p7.toString()));
		when(config32.getPlan()).thenReturn(plan32);
		when(plan32.getCost()).thenReturn(new DoubleCost(3.0));
		when(config32.isClosed()).thenReturn(true);
		when(config21.getInput()).thenReturn(Lists.<Constant>newArrayList(new Skolem("c1"), new Skolem("c2")));
	}

	@Test public void test1() {
		Assert.assertEquals(cdomominance.isEquivalent(config11, config12), false);
		Assert.assertEquals(cdomominance.isEquivalent(config12, config11), true);
	}

	@Test public void test2() {
		Assert.assertEquals(cdomominance.isEquivalent(config21, config22), true);
		Assert.assertEquals(cdomominance.isEquivalent(config22, config21), true);
	}

	@Test public void test3() {
		Assert.assertEquals(cdomominance.isEquivalent(config31, config32), true);
		Assert.assertEquals(cdomominance.isEquivalent(config32, config31), true);
	}
}
