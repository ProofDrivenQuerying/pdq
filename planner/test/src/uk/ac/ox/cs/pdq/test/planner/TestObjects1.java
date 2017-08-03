package uk.ac.ox.cs.pdq.test.planner;

import org.junit.Before;
import org.mockito.Mock;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.planner.dag.DAGChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.util.Utility;

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
}
