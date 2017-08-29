package uk.ac.ox.cs.pdq.test.planner.dominance;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.AccessCountCostEstimator;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dominance.CostFactDominance;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * 
 * @author Efthymia Tsamoura
 */

public class ClosedDominanceTest {

	/** The cdomominance. */
	CostFactDominance cdomominance = new CostFactDominance(new AccessCountCostEstimator(null), new FastFactDominance(false), false);

	/** The r1. */
	protected Relation r1 = Relation.create("R1", 
			new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2"), Attribute.create(String.class, "a3")});

	/** The p1. */
	protected Atom p1 = Atom.create(Relation.create(AccessibleSchema.inferredAccessiblePrefix + r1.getName(), r1.getAttributes(), new AccessMethod[]{AccessMethod.create(new Integer[]{})}, r1.isEquality()),
			new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c3")});

	/** The r2. */
	protected Relation r2 = Relation.create("R2", 
			new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2"), Attribute.create(String.class, "a3"), Attribute.create(String.class, "a4")});

	/** The p2. */
	protected Atom p2 = Atom.create(Relation.create(AccessibleSchema.inferredAccessiblePrefix + r2.getName(), r2.getAttributes(), new AccessMethod[]{AccessMethod.create(new Integer[]{})}, r2.isEquality()),
			new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c3"), UntypedConstant.create("c4")}
			);

	/** The r3. */
	protected Relation r3 = Relation.create("R3", 
			new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a2")});

	/** The p3. */
	protected Atom p3 = Atom.create(Relation.create(AccessibleSchema.inferredAccessiblePrefix + r3.getName(), r3.getAttributes(), new AccessMethod[]{AccessMethod.create(new Integer[]{})}, r3.isEquality()),
			new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2")}
			);

	/** The r4. */
	protected Relation r4 = Relation.create("R4", 
			new Attribute[]{Attribute.create(String.class, "a1")});

	/** The p4. */
	protected Atom p4 = Atom.create(Relation.create(AccessibleSchema.inferredAccessiblePrefix + r4.getName(), r4.getAttributes(), new AccessMethod[]{AccessMethod.create(new Integer[]{})}, r4.isEquality()),
			new Term[]{UntypedConstant.create("c1")}
			);

	/** The r5. */
	protected Relation r5 = Relation.create("R5", 
			new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a3"), Attribute.create(String.class, "a2")});

	/** The p5. */
	protected Atom p5 = Atom.create(Relation.create(AccessibleSchema.inferredAccessiblePrefix + r5.getName(), r5.getAttributes(), new AccessMethod[]{AccessMethod.create(new Integer[]{})}, r5.isEquality()),
			new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c3"), UntypedConstant.create("c2")}
			);

	/** The r6. */
	protected Relation r6 = Relation.create("R6", 
			new Attribute[]{Attribute.create(String.class, "a1"), Attribute.create(String.class, "a3")});

	/** The p6. */
	protected Atom p6 = Atom.create(Relation.create(AccessibleSchema.inferredAccessiblePrefix + r6.getName(), r6.getAttributes(), new AccessMethod[]{AccessMethod.create(new Integer[]{})}, r6.isEquality()),
			new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c3")}
			);

	/** The r7. */
	protected Relation r7 = Relation.create("R7", 
			new Attribute[]{Attribute.create(String.class, "a1")});

	/** The p7. */
	protected Atom p7 = Atom.create(Relation.create(AccessibleSchema.inferredAccessiblePrefix + r7.getName(), r7.getAttributes(), new AccessMethod[]{AccessMethod.create(new Integer[]{})}, r7.isEquality()),
			new Term[]{UntypedConstant.create("c1")}
			);


	/** The config11. */
	@Mock protected DAGChaseConfiguration config11;

	/** The config12. */
	@Mock protected DAGChaseConfiguration config12;

	/** The config11 state. */
	@Mock protected AccessibleChaseInstance config11State;

	/** The config12 state. */
	@Mock protected AccessibleChaseInstance config12State;

	/** The plan11. */
	@Mock protected RelationalTerm plan11;

	/** The plan12. */
	@Mock protected RelationalTerm plan12;

	/** The plan11. */
	@Mock protected Cost plan11Cost;

	/** The plan12. */
	@Mock protected Cost plan12Cost;

	/** The config21. */
	@Mock protected DAGChaseConfiguration config21;

	/** The config22. */
	@Mock protected DAGChaseConfiguration config22;

	/** The config21 state. */
	@Mock protected AccessibleChaseInstance config21State;

	/** The config22 state. */
	@Mock protected AccessibleChaseInstance config22State;

	/** The plan21. */
	@Mock protected RelationalTerm plan21;

	/** The plan22. */
	@Mock protected RelationalTerm plan22;

	/** The plan11. */
	@Mock protected Cost plan21Cost;

	/** The plan12. */
	@Mock protected Cost plan22Cost;

	/** The config31. */
	@Mock protected DAGChaseConfiguration config31;

	/** The config32. */
	@Mock protected DAGChaseConfiguration config32;

	/** The config31 state. */
	@Mock protected AccessibleChaseInstance config31State;

	/** The config32 state. */
	@Mock protected AccessibleChaseInstance config32State;

	/** The plan31. */
	@Mock protected RelationalTerm plan31;

	/** The plan32. */
	@Mock protected RelationalTerm plan32;

	/** The plan11. */
	@Mock protected Cost plan31Cost;

	/** The plan12. */
	@Mock protected Cost plan32Cost;
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.planner.TestObjects1#setup()
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);
        
        when(config11.getState()).thenReturn(config11State);
		when(config11State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p3));
		when(config11.getPlan()).thenReturn(plan11);
		when(plan11Cost).thenReturn(new DoubleCost(3.0));
		when(config11.isClosed()).thenReturn(true);
		
		when(config12.getState()).thenReturn(config12State);
		when(config12State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p3,p2));
		when(config12.getPlan()).thenReturn(plan12);
		when(plan12Cost).thenReturn(new DoubleCost(3.0));
		when(config12.isClosed()).thenReturn(true);
		
		when(config21.getState()).thenReturn(config21State);
		when(config21State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p3,p4));
		when(config21.getPlan()).thenReturn(plan21);
		when(plan21Cost).thenReturn(new DoubleCost(4.0));
		when(config21.isClosed()).thenReturn(true);
		
		when(config22.getState()).thenReturn(config22State);
		when(config22State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p4));
		when(config22.getPlan()).thenReturn(plan22);
		when(plan22Cost).thenReturn(new DoubleCost(2.0));
		when(config22.isClosed()).thenReturn(false);
		
		when(config31.getState()).thenReturn(config31State);
		when(config31State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p3,p4,p5,p6,p7));
		when(config31.getPlan()).thenReturn(plan31);
		when(plan31Cost).thenReturn(new DoubleCost(2.0));
		when(config31.isClosed()).thenReturn(false);
		
		when(config32.getState()).thenReturn(config32State);
		when(config32State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p3,p4,p5,p6,p7));
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
