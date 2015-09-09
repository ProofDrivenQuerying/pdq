package uk.ac.ox.cs.pdq.test.planner;

import org.mockito.Mock;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.plan.DAGPlan;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.db.access.AccessibleSchema;

import com.google.common.collect.Lists;

/**
 * @author Efthymia Tsamoura
 *
 */
public class TestObjects1 {

			protected Relation r1 = new Relation("R1", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a2"), new Attribute(String.class, "a3"))) {};
	
			protected Predicate p1 = new Predicate(new AccessibleSchema.InferredAccessibleRelation(r1),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c2"), new Skolem("c3"))
			);
	
			protected Relation r2 = new Relation("R2", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a2"), new Attribute(String.class, "a3"), new Attribute(String.class, "a4"))) {};
	
			protected Predicate p2 = new Predicate(new AccessibleSchema.InferredAccessibleRelation(r2),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c2"), new Skolem("c3"), new Skolem("c4"))
			);
	
			protected Relation r3 = new Relation("R3", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a2"))) {};
	
			protected Predicate p3 = new Predicate(new AccessibleSchema.InferredAccessibleRelation(r3),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c2"))
			);
	
			protected Relation r4 = new Relation("R4", 
			Lists.newArrayList(new Attribute(String.class, "a1"))) {};
			
			protected Predicate p4 = new Predicate(new AccessibleSchema.InferredAccessibleRelation(r4),
			Lists.newArrayList(new Skolem("c1"))
			);
	
			protected Relation r5 = new Relation("R5", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a3"), new Attribute(String.class, "a2"))) {};
	
			protected Predicate p5 = new Predicate(new AccessibleSchema.InferredAccessibleRelation(r5),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c3"), new Skolem("c2"))
			);
	
			protected Relation r6 = new Relation("R6", 
			Lists.newArrayList(new Attribute(String.class, "a1"), new Attribute(String.class, "a3"))) {};
	
			protected Predicate p6 = new Predicate(new AccessibleSchema.InferredAccessibleRelation(r6),
			Lists.newArrayList(new Skolem("c1"), new Skolem("c3"))
			);
	
			protected Relation r7 = new Relation("R7", 
			Lists.newArrayList(new Attribute(String.class, "a1"))) {};
	
			protected Predicate p7 = new Predicate(new AccessibleSchema.InferredAccessibleRelation(r7),
			Lists.newArrayList(new Skolem("c1"))
			);


	@Mock protected DAGChaseConfiguration config11;
	@Mock protected DAGChaseConfiguration config12;
	@Mock protected DAGPlan plan11;
	@Mock protected DAGPlan plan12;

	@Mock protected DAGChaseConfiguration config21;
	@Mock protected DAGChaseConfiguration config22;
	@Mock protected DAGPlan plan21;
	@Mock protected DAGPlan plan22;

	@Mock protected DAGChaseConfiguration config31;
	@Mock protected DAGChaseConfiguration config32;
	@Mock protected DAGPlan plan31;
	@Mock protected DAGPlan plan32;
}
