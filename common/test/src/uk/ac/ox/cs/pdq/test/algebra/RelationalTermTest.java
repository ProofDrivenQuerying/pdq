package uk.ac.ox.cs.pdq.test.algebra;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * @author Gabor
 *
 */
public class RelationalTermTest extends PdqTest {
	
	@Test
	public void testProjectionTerm() {
		try {
			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
			RelationalTerm projection = ProjectionTerm.create(attributes, access);
			
			// There are 2 input attributes with names r1.1 and r1.2
			Attribute[] in = projection.getInputAttributes();
			Assert.assertNotNull(in);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());

			// Output attributes are the same as input
			Attribute[] out = projection.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testCartesianProductTerm() {
		try {
			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			AccessTerm access1 = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			AccessTerm access2 = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			CartesianProductTerm cartasianp = CartesianProductTerm.create(access1, access2);
			
			// There are 4 output attributes
			Attribute[] out = cartasianp.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertEquals(4,out.length);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testDependentJoinTerm() {
		try {
			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			DependentJoinTerm dependentJ = DependentJoinTerm.create(access, access);
			Attribute[] in = dependentJ.getInputAttributes();
			
			// There are 2 input attributes with names r1.1 and r1.2
			Assert.assertNotNull(in);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			
			// There are 4 output attributes
			Attribute[] out = dependentJ.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertEquals(4, out.length);
					
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testJoinTerm() {
		try {
			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			JoinTerm join = JoinTerm.create(access, access);
			Attribute[] in = join.getInputAttributes();
			
			// There are 2 input attributes named r1.1, r1.2, r1.3 and r1.4
			Assert.assertNotNull(in);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			
			// There are 4 output attributes, the same as input
			Attribute[] out = join.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertEquals(4, out.length);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testRenameTerm() {
		try {
			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
			RenameTerm renameTerm = RenameTerm.create(attributes, access);
			
			// There are 2 input attributes with names r1.1 and r1.2
			Attribute[] in = renameTerm.getInputAttributes();
			Assert.assertNotNull(in);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			
			// There are 2 output attributes, the same as input
			Attribute[] out = renameTerm.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertEquals(2, out.length);
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testSelectionTerm() {
		try {
			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicate = ConstantEqualityCondition.create(0, TypedConstant.create((Object) 1));
			SelectionTerm selectionTerm = SelectionTerm.create(predicate , access);

			// There are 2 input attributes with names r1.1 and r1.2
			Attribute[] in = selectionTerm.getInputAttributes();
			Assert.assertNotNull(in);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());

			// There are 2 output attributes, the same as input
			Attribute[] out = selectionTerm.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertEquals(2, out.length);
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testLargeRelationalTerm() {
		try {
			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicate = ConstantEqualityCondition.create(0, TypedConstant.create((Object) 1));
			ProjectionTerm p = ProjectionTerm.create(access.getInputAttributes(), access);
			ProjectionTerm p1 = ProjectionTerm.create(p.getInputAttributes(), p);
			SelectionTerm selectionTerm = SelectionTerm.create(predicate , p1);
			ProjectionTerm p2 = ProjectionTerm.create(selectionTerm.getInputAttributes(), selectionTerm);

			RelationalTerm accessX = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicateX = ConstantEqualityCondition.create(0, TypedConstant.create((Object) 1));
			ProjectionTerm pX = ProjectionTerm.create(accessX.getInputAttributes(), accessX);
			ProjectionTerm p1X = ProjectionTerm.create(pX.getInputAttributes(), pX);
			SelectionTerm selectionTermX = SelectionTerm.create(predicateX , p1X);
			ProjectionTerm p2X = ProjectionTerm.create(selectionTermX.getInputAttributes(), selectionTermX);
			
			
			JoinTerm jt = JoinTerm.create(p2, p2X);
			
			// There are 4 input attributes with names r1.1, r1.2, r1.3 and r1.4
			Attribute[] in = jt.getInputAttributes();
			Assert.assertNotNull(in);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());

			// There are 4 output attributes, the same as input
			Attribute[] out = jt.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertEquals(4, out.length);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test public void test3() {
		AccessTerm access1 = AccessTerm.create(this.R, AccessMethodDescriptor.create("am", new Integer[] {0}));
		AccessTerm access2 = AccessTerm.create(this.S, AccessMethodDescriptor.create("am", new Integer[] {1}));
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		// Output attributes are {a, b, c, d}
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		
		// Input attributes are empty
		Assert.assertArrayEquals(new Attribute[] {a}, plan1.getInputAttributes());
		
		// Join conditions are ConjunctiveConditions
		Assert.assertNotNull(plan1.getJoinConditions());
		Assert.assertTrue(plan1.getJoinConditions() instanceof ConjunctiveCondition);
		
		// There are 2 simple conditions
		SimpleCondition[] sc = ((ConjunctiveCondition)plan1.getJoinConditions()).getSimpleConditions();
		Assert.assertNotNull(sc);
		Assert.assertEquals(2,sc.length);
		Assert.assertNotNull(sc[0]);
		Assert.assertNotNull(sc[1]);
		Assert.assertTrue(sc[0] instanceof AttributeEqualityCondition);
		Assert.assertEquals(1, ((AttributeEqualityCondition)sc[0]).getPosition());
		Assert.assertTrue(sc[1] instanceof AttributeEqualityCondition);
		Assert.assertEquals(2, ((AttributeEqualityCondition)sc[1]).getPosition());
		
		// getPositionsInLeftChildThatAreInputToRightChild
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		
		// getChildren
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	
	@Test public void test4() {
		AccessTerm access1 = AccessTerm.create(this.R, AccessMethodDescriptor.create("am", new Integer[] {0}));
		AccessTerm access2 = AccessTerm.create(this.S, AccessMethodDescriptor.create("am", new Integer[] {1}));			
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		// Output attributes are {a, b, c, d}
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());

		// Join conditions are ConjunctiveConditions
		Assert.assertNotNull(plan1.getJoinConditions());
		Assert.assertTrue(plan1.getJoinConditions() instanceof ConjunctiveCondition);

		// There are 2 simple conditions
		SimpleCondition[] sc = ((ConjunctiveCondition)plan1.getJoinConditions()).getSimpleConditions();
		Assert.assertNotNull(sc);
		Assert.assertEquals(2,sc.length);
		Assert.assertNotNull(sc[0]);
		Assert.assertNotNull(sc[1]);
		Assert.assertTrue(sc[0] instanceof AttributeEqualityCondition);
		Assert.assertEquals(1, ((AttributeEqualityCondition)sc[0]).getPosition());
		Assert.assertTrue(sc[1] instanceof AttributeEqualityCondition);
		Assert.assertEquals(2, ((AttributeEqualityCondition)sc[1]).getPosition());
		
		// getPositionsInLeftChildThatAreInputToRightChild
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(2),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		// getChildren
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test5() {
		AccessTerm access1 = AccessTerm.create(this.R, AccessMethodDescriptor.create("am", new Integer[] {0}));
		AccessTerm access2 = AccessTerm.create(this.S, AccessMethodDescriptor.create("am", new Integer[] {1}));
		SelectionTerm selectionTerm = SelectionTerm.create(ConstantEqualityCondition.create(0, TypedConstant.create(new Integer(1))), access1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(selectionTerm, access2);
		
		// Output attributes are {a, b, c, d}
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());

		// Join conditions are ConjunctiveConditions
		Assert.assertNotNull(plan1.getJoinConditions());
		Assert.assertTrue(plan1.getJoinConditions() instanceof ConjunctiveCondition);
		
		// There are 2 simple conditions
		SimpleCondition[] sc = ((ConjunctiveCondition)plan1.getJoinConditions()).getSimpleConditions();
		Assert.assertNotNull(sc);
		Assert.assertEquals(2,sc.length);
		Assert.assertNotNull(sc[0]);
		Assert.assertNotNull(sc[1]);
		Assert.assertTrue(sc[0] instanceof AttributeEqualityCondition);
		Assert.assertEquals(1, ((AttributeEqualityCondition)sc[0]).getPosition());
		Assert.assertTrue(sc[1] instanceof AttributeEqualityCondition);
		Assert.assertEquals(2, ((AttributeEqualityCondition)sc[1]).getPosition());
		
		// getPositionsInLeftChildThatAreInputToRightChild
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(2),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		// getChildren
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof SelectionTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test6() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(TypedConstant.create(new Integer(1))));
		AccessTerm access1 = AccessTerm.create(this.R, AccessMethodDescriptor.create("am", new Integer[] {0}));
		AccessTerm access2 = AccessTerm.create(this.S, AccessMethodDescriptor.create("am", new Integer[] {1}));
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		
		// Join conditions are ConjunctiveConditions
		Assert.assertNotNull(plan1.getJoinConditions());
		Assert.assertTrue(plan1.getJoinConditions() instanceof ConjunctiveCondition);
	
		// There are 2 simple conditions of type AttributeEqualityCondition
		SimpleCondition[] sc = ((ConjunctiveCondition) plan1.getJoinConditions()).getSimpleConditions();
		Assert.assertNotNull(sc);
		Assert.assertEquals(2,sc.length);
		Assert.assertNotNull(sc[0]);
		Assert.assertNotNull(sc[1]);
		Assert.assertTrue(sc[0] instanceof AttributeEqualityCondition);
		Assert.assertEquals(1, ((AttributeEqualityCondition)sc[0]).getPosition());
		Assert.assertTrue(sc[1] instanceof AttributeEqualityCondition);
		Assert.assertEquals(2, ((AttributeEqualityCondition)sc[1]).getPosition());
		
		// getPositionsInLeftChildThatAreInputToRightChild
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(2),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		// getChildren are of type AccessTerm
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test7() {
		AccessTerm access1 = AccessTerm.create(this.R, AccessMethodDescriptor.create("am", new Integer[] {0}));
		AccessTerm access2 = AccessTerm.create(this.S, AccessMethodDescriptor.create("am", new Integer[] {1}));			
		JoinTerm plan1 = JoinTerm.create(access1, access2);
	
		// Output attributes are {a,b,c,b,c}
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		
		// getChildren are of type AccessTerm
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test8() {
		
		AccessMethodDescriptor am1 = AccessMethodDescriptor.create("am1", new Integer[] {0});
		AccessMethodDescriptor am2 = AccessMethodDescriptor.create("am2", new Integer[] {1});
		
		Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")});
		Relation.create("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")});
		
		// Free access on relation R1.
		AccessTerm relation1Free = AccessTerm.create(this.R, am1);
		
		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(100));
		
		// Note that it is the access method am2 that specifies that relation2 requires 
		// input(s) on the first position (i.e. position 0). The inputConstants1 map contains
		// the TypedConstant that provides that input.
		AccessTerm relation2InputonFirst = AccessTerm.create(this.S, am2);

		// A dependent join plan that takes the outputs of the first access and feeds them to the 
		// first input position (i.e. position 0) of the second accessed relation.
		try {
			DependentJoinTerm.create(relation1Free, relation2InputonFirst);
			Assert.fail("Should have thrown exception");
		} catch(AssertionError e) {
			// excpected
		}
	}


}
