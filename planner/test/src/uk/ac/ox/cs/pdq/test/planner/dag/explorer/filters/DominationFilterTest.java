package uk.ac.ox.cs.pdq.test.planner.dag.explorer.filters;

import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.dag.explorer.filters.DominationFilter;
import uk.ac.ox.cs.pdq.planner.dominance.FastFactDominance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 *
 * @author Efthymia Tsamoura
 */
public class DominationFilterTest {

	protected Attribute a1 = Attribute.create(String.class, "a1");
	protected Attribute a2 = Attribute.create(String.class, "a2"); 
	protected Attribute a3 = Attribute.create(String.class, "a3");
	protected Attribute a4 = Attribute.create(String.class, "a4");
	
	protected Relation r1 = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R1", new Attribute[]{a1,a2,a3}, new AccessMethod[]{});
	protected Relation r2 = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R2", new Attribute[]{a1,a2,a3,a4}, new AccessMethod[]{});
	protected Relation r3 = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R3", new Attribute[]{a1,a2}, new AccessMethod[]{});
	protected Relation r4 = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R4", new Attribute[]{a1}, new AccessMethod[]{});
	protected Relation r5 = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R5", new Attribute[]{a1,a2,a3}, new AccessMethod[]{});
	protected Relation r6 = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R6", new Attribute[]{a1,a2}, new AccessMethod[]{});
	protected Relation r7 = Relation.create(AccessibleSchema.inferredAccessiblePrefix + "R7", new Attribute[]{a1}, new AccessMethod[]{});

	protected Atom p1 = Atom.create(r1, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c3")});
	protected Atom p2 = Atom.create(r2, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2"), UntypedConstant.create("c3"), UntypedConstant.create("c4")});
	protected Atom p3 = Atom.create(r3, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c2")});
	protected Atom p4 = Atom.create(r4, new Term[]{UntypedConstant.create("c1")});
	protected Atom p5 = Atom.create(r5, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c3"), UntypedConstant.create("c2")});
	protected Atom p6 = Atom.create(r6, new Term[]{UntypedConstant.create("c1"), UntypedConstant.create("c3")});
	protected Atom p7 = Atom.create(r7, new Term[]{UntypedConstant.create("c1")});

	@Mock protected DAGChaseConfiguration config11;
	@Mock protected DAGChaseConfiguration config12;
	@Mock protected AccessibleChaseInstance config11State;
	@Mock protected AccessibleChaseInstance config12State;
	@Mock protected RelationalTerm plan11;
	@Mock protected RelationalTerm plan12;
	@Mock protected Cost plan11Cost;
	@Mock protected Cost plan12Cost;
	@Mock protected DAGChaseConfiguration config21;
	@Mock protected DAGChaseConfiguration config22;
	@Mock protected AccessibleChaseInstance config21State;
	@Mock protected AccessibleChaseInstance config22State;
	@Mock protected RelationalTerm plan21;
	@Mock protected RelationalTerm plan22;
	@Mock protected Cost plan21Cost;
	@Mock protected Cost plan22Cost;
	@Mock protected DAGChaseConfiguration config31;
	@Mock protected DAGChaseConfiguration config32;
	@Mock protected AccessibleChaseInstance config31State;
	@Mock protected AccessibleChaseInstance config32State;
	@Mock protected RelationalTerm plan31;
	@Mock protected RelationalTerm plan32;
	@Mock protected Cost plan31Cost;
	@Mock protected Cost plan32Cost;

	
	DominationFilter filter = new DominationFilter(new FastFactDominance(false));

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.test.planner.TestObjects1#setup()
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
		MockitoAnnotations.initMocks(this);
	}
	
	//Configuration config11 has facts p1,p2,p3 is not closed and requires input c1
	//Configuration config12 has facts p2,p3 is not closed and requires input c1
	//Configuration config21 has facts p1,p2,p3,p4 and is closed 
	//Configuration config22 has facts p1,p2,p4 is closed
	//Configuration config31 has facts p1,p2,p3,p4,p5,p6,p7 and requires inputs c1,c3	
	//Configuration config32 has facts p1,p2,p3,p4,p5,p6,p7 and is closed
	//config11 dominates config12
	//config21 dominates config22
	//config32 dominates config31
	//config21 dominates config11, config12
	//config32 dominates all configurations
	@Test public void test1() {
		when(config11.getState()).thenReturn(config11State);
		when(config11State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p3));
		when(config11.getPlan()).thenReturn(plan11);
		when(config11.getCost()).thenReturn(new DoubleCost(3.0));
		when(config11.isClosed()).thenReturn(false);
		when(config11.getInput()).thenReturn(Lists.<Constant>newArrayList(UntypedConstant.create("c1")));

		when(config12.getState()).thenReturn(config12State);
		when(config12State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p3,p2));
		when(config12.getPlan()).thenReturn(plan12);
		when(config12.getCost()).thenReturn(new DoubleCost(3.0));
		when(config12.isClosed()).thenReturn(false);
		when(config12.getInput()).thenReturn(Lists.<Constant>newArrayList(UntypedConstant.create("c1")));

		when(config21.getState()).thenReturn(config21State);
		when(config21State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p3,p4));
		when(config21.getPlan()).thenReturn(plan21);
		when(config21.getCost()).thenReturn(new DoubleCost(4.0));
		when(config21.isClosed()).thenReturn(true);
		when(config21.getInput()).thenReturn(Lists.<Constant>newArrayList());
		
		when(config22.getState()).thenReturn(config22State);
		when(config22State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p4));
		when(config22.getPlan()).thenReturn(plan22);
		when(config22.getCost()).thenReturn(new DoubleCost(2.0));
		when(config22.isClosed()).thenReturn(true);
		when(config22.getInput()).thenReturn(Lists.<Constant>newArrayList());

		when(config31.getState()).thenReturn(config31State);
		when(config31State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p3,p4,p5,p6,p7));
		when(config31.getPlan()).thenReturn(plan31);
		when(config31.getCost()).thenReturn(new DoubleCost(2.0));
		when(config31.isClosed()).thenReturn(false);
		when(config31.getInput()).thenReturn(Lists.<Constant>newArrayList(UntypedConstant.create("c1"), UntypedConstant.create("c3")));

		when(config32.getState()).thenReturn(config32State);
		when(config32State.getInferredAccessibleFacts()).thenReturn(Lists.newArrayList(p1,p2,p3,p4,p5,p6,p7));
		when(config32.getPlan()).thenReturn(plan32);
		when(config32.getCost()).thenReturn(new DoubleCost(3.0));
		when(config32.isClosed()).thenReturn(true);
		when(config32.getInput()).thenReturn(Lists.<Constant>newArrayList());

		Set<DAGChaseConfiguration> expected = new LinkedHashSet<>();
		expected.add(config12);
		expected.add(config22);
		expected.add(config31);
		
		Set<DAGChaseConfiguration> input = new LinkedHashSet<>();
		input.add(config11);
		input.add(config12);
		input.add(config21);
		input.add(config22);
		input.add(config31);
		input.add(config32);
		
		Assert.assertEquals(expected, this.filter.filter(input));
	}
}
