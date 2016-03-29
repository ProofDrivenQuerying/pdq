package uk.ac.ox.cs.pdq.test.planner;

import org.junit.Before;
import org.mockito.Mock;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseState;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class TestObjects1.
 *
 * @author Efthymia Tsamoura
 */
public class TestObjects1 {
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
			/** The r1. */
			protected Relation r1 = new Relation("R1", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a2"), new Attribute(String.class, "a3"))) {};
	
			/** The p1. */
			protected Atom p1 = new Atom(new AccessibleSchema.InferredAccessibleRelation(r1),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c2"), new Skolem("c3"))
			);
	
			/** The r2. */
			protected Relation r2 = new Relation("R2", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a2"), new Attribute(String.class, "a3"), new Attribute(String.class, "a4"))) {};
	
			/** The p2. */
			protected Atom p2 = new Atom(new AccessibleSchema.InferredAccessibleRelation(r2),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c2"), new Skolem("c3"), new Skolem("c4"))
			);
	
			/** The r3. */
			protected Relation r3 = new Relation("R3", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
	
			/** The p3. */
			protected Atom p3 = new Atom(new AccessibleSchema.InferredAccessibleRelation(r3),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c2"))
			);
	
			/** The r4. */
			protected Relation r4 = new Relation("R4", 
			Lists.newArrayList(new Attribute(String.class, "a1"))) {};
			
			/** The p4. */
			protected Atom p4 = new Atom(new AccessibleSchema.InferredAccessibleRelation(r4),
			Lists.newArrayList(new Skolem("c1"))
			);
	
			/** The r5. */
			protected Relation r5 = new Relation("R5", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a3"), new Attribute(String.class, "a2"))) {};
	
			/** The p5. */
			protected Atom p5 = new Atom(new AccessibleSchema.InferredAccessibleRelation(r5),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c3"), new Skolem("c2"))
			);
	
			/** The r6. */
			protected Relation r6 = new Relation("R6", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a3"))) {};
	
			/** The p6. */
			protected Atom p6 = new Atom(new AccessibleSchema.InferredAccessibleRelation(r6),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c3"))
			);
	
			/** The r7. */
			protected Relation r7 = new Relation("R7", 
			Lists.newArrayList(new Attribute(String.class, "a1"))) {};
	
			/** The p7. */
			protected Atom p7 = new Atom(new AccessibleSchema.InferredAccessibleRelation(r7),
			Lists.newArrayList(new Skolem("c1"))
			);


	/** The config11. */
	@Mock protected DAGChaseConfiguration config11;
	
	/** The config12. */
	@Mock protected DAGChaseConfiguration config12;
	
	/** The config11 state. */
	@Mock protected AccessibleChaseState config11State;
	
	/** The config12 state. */
	@Mock protected AccessibleChaseState config12State;
	
	/** The plan11. */
	@Mock protected DAGPlan plan11;
	
	/** The plan12. */
	@Mock protected DAGPlan plan12;

	/** The config21. */
	@Mock protected DAGChaseConfiguration config21;
	
	/** The config22. */
	@Mock protected DAGChaseConfiguration config22;
	
	/** The config21 state. */
	@Mock protected AccessibleChaseState config21State;
	
	/** The config22 state. */
	@Mock protected AccessibleChaseState config22State;
	
	/** The plan21. */
	@Mock protected DAGPlan plan21;
	
	/** The plan22. */
	@Mock protected DAGPlan plan22;

	/** The config31. */
	@Mock protected DAGChaseConfiguration config31;
	
	/** The config32. */
	@Mock protected DAGChaseConfiguration config32;
	
	/** The config31 state. */
	@Mock protected AccessibleChaseState config31State;
	
	/** The config32 state. */
	@Mock protected AccessibleChaseState config32State;
	
	/** The plan31. */
	@Mock protected DAGPlan plan31;
	
	/** The plan32. */
	@Mock protected DAGPlan plan32;
}
