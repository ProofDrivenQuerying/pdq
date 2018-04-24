//package uk.ac.ox.cs.pdq.test.algebra;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import uk.ac.ox.cs.pdq.algebra.AccessTerm;
//import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
//import uk.ac.ox.cs.pdq.algebra.Condition;
//import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
//import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
//import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
//import uk.ac.ox.cs.pdq.algebra.JoinTerm;
//import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
//import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
//import uk.ac.ox.cs.pdq.algebra.RenameTerm;
//import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
//import uk.ac.ox.cs.pdq.algebra.TypeEqualityCondition;
//import uk.ac.ox.cs.pdq.db.AccessMethod;
//import uk.ac.ox.cs.pdq.db.Attribute;
//import uk.ac.ox.cs.pdq.db.Relation;
//import uk.ac.ox.cs.pdq.db.Schema;
//import uk.ac.ox.cs.pdq.db.TypedConstant;
//import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
//import uk.ac.ox.cs.pdq.test.util.PdqTest;
//
///**
// * @author Gabor
// *
// */
//public class RelationalTermTest extends PdqTest {
//
//	@Test
//	public void testAccessCreation() {
//		AccessMethod am = AccessMethod.create("test",new Integer[] {0});
//		AccessMethod am1 = AccessMethod.create("test1",new Integer[] {0});
//		Relation relation = new Relation("R0", new Attribute[] {Attribute.create(Integer.class, "attr1")});
//		Relation relation1 = new Relation("R1", new Attribute[] {Attribute.create(Integer.class, "attr2")});
//		RelationalTerm	child1 = new AccessTerm(relation,am);
//		RelationalTerm	child2 = new AccessTerm(relation1,am1);
//		RelationalTerm	child3 = new AccessTerm(relation,am);
//		
//		if (child1 != child3) { // ATTENTIONAL! it have to be the same reference
//			Assert.fail("Relation cache does not provide same reference");
//		}
//		if (child1 == child2) { // ATTENTIONAL! it have to be different reference
//			Assert.fail("Relation cache should not provide same reference");
//		}
//	}
//	
//	@Test
//	public void testProjectionTerm() {
//		try {
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
//			RelationalTerm access = new AccessTerm(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
//			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
//			RelationalTerm projection = new ProjectionTerm(attributes, access);
//			Attribute[] in = projection.getInputAttributes();
//			Attribute[] out = projection.getOutputAttributes();
//			Assert.assertNotNull(in);
//			Assert.assertNotNull(out);
//			Assert.assertEquals(2, in.length);
//			Assert.assertEquals("r1.1", in[0].getName());
//			Assert.assertEquals("r1.2", in[1].getName());
//			Assert.assertArrayEquals(in,out);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testCartesianProductTerm() {
//		try {
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
//			AccessTerm access1 = new AccessTerm(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
//			AccessTerm access2 = new AccessTerm(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
//			CartesianProductTerm cartasianp = CartesianProductTerm.create(access1, access2);
//			Attribute[] out = cartasianp.getOutputAttributes();
//			Assert.assertNotNull(out);
//			Assert.assertEquals(4,out.length);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testDependentJoinTerm() {
//		try {
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
//			RelationalTerm access = new AccessTerm(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
//			DependentJoinTerm dependentJ = new DependentJoinTerm(access, access);
//			Attribute[] in = dependentJ.getInputAttributes();
//			Attribute[] out = dependentJ.getOutputAttributes();
//			Assert.assertNotNull(in);
//			Assert.assertNotNull(out);
//			Assert.assertEquals(2, in.length);
//			Assert.assertEquals(4, out.length);
//			Assert.assertEquals("r1.1", in[0].getName());
//			Assert.assertEquals("r1.2", in[1].getName());
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testJoinTerm() {
//		try {
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
//			RelationalTerm access = new AccessTerm(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
//			JoinTerm join = JoinTerm.create(access, access);
//			Attribute[] in = join.getInputAttributes();
//			Attribute[] out = join.getOutputAttributes();
//			Assert.assertNotNull(in);
//			Assert.assertNotNull(out);
//			Assert.assertEquals(4, in.length);
//			Assert.assertEquals(4, out.length);
//			Assert.assertEquals("r1.1", in[0].getName());
//			Assert.assertEquals("r1.2", in[1].getName());
//			Assert.assertArrayEquals(in,out);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testRenameTerm() {
//		try {
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
//			RelationalTerm access = new AccessTerm(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
//			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
//			RenameTerm renameTerm = RenameTerm.create(attributes, access);
//			Attribute[] in = renameTerm.getInputAttributes();
//			Attribute[] out = renameTerm.getOutputAttributes();
//			Assert.assertNotNull(in);
//			Assert.assertNotNull(out);
//			Assert.assertEquals(2, in.length);
//			Assert.assertEquals("r1.1", in[0].getName());
//			Assert.assertEquals("r1.2", in[1].getName());
//			Assert.assertArrayEquals(in,out);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testSelectionTerm() {
//		try {
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
//			RelationalTerm access = new AccessTerm(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
//			Condition predicate = TypeEqualityCondition.create(0, 1);
//			SelectionTerm selectionTerm = new SelectionTerm(predicate , access);
//			Attribute[] in = selectionTerm.getInputAttributes();
//			Attribute[] out = selectionTerm.getOutputAttributes();
//			Assert.assertNotNull(in);
//			Assert.assertNotNull(out);
//			Assert.assertEquals(2, in.length);
//			Assert.assertEquals(2, out.length);
//			Assert.assertEquals("r1.1", in[0].getName());
//			Assert.assertEquals("r1.2", in[1].getName());
//			Assert.assertArrayEquals(in,out);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//	@Test
//	public void testLargeRelationalTerm() {
//		try {
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
//			RelationalTerm access = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
//			Condition predicate = TypeEqualityCondition.create(0, 1);
//			ProjectionTerm p = new ProjectionTerm(access.getInputAttributes(), access);
//			ProjectionTerm p1 = new ProjectionTerm(p.getInputAttributes(), p);
//			SelectionTerm selectionTerm = new SelectionTerm(predicate , p1);
//			ProjectionTerm p2 = new ProjectionTerm(selectionTerm.getInputAttributes(), selectionTerm);
//
//			RelationalTerm accessX = new AccessTerm( schema.getRelations()[0].getAccessMethods()[1]);
//			Condition predicateX = TypeEqualityCondition.create(0, 1);
//			ProjectionTerm pX = new ProjectionTerm(accessX.getInputAttributes(), accessX);
//			ProjectionTerm p1X = new ProjectionTerm(pX.getInputAttributes(), pX);
//			SelectionTerm selectionTermX = new SelectionTerm(predicateX , p1X);
//			ProjectionTerm p2X = new ProjectionTerm(selectionTermX.getInputAttributes(), selectionTermX);
//			
//			
//			JoinTerm jt = JoinTerm.create(p2, p2X);
//			
//			Attribute[] in = jt.getInputAttributes();
//			Attribute[] out = jt.getOutputAttributes();
//			Assert.assertNotNull(in);
//			Assert.assertNotNull(out);
//			Assert.assertEquals(4, in.length);
//			Assert.assertEquals(4, out.length);
//			Assert.assertEquals("r1.1", in[0].getName());
//			Assert.assertEquals("r1.2", in[1].getName());
//			Assert.assertArrayEquals(in,out);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//	
//	@Test public void test3() {
//		AccessTerm access1 = new AccessTerm(this.R, this.method0);
//		AccessTerm access2 = new AccessTerm(this.S, this.method1);
//		DependentJoinTerm plan1 = new DependentJoinTerm(access1, access2);
//		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
//		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
//		Assert.assertNotNull(plan1.getJoinCondition());
//		Assert.assertTrue(plan1.getJoinCondition() instanceof ConjunctiveCondition);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions());
//		Assert.assertEquals(2,((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions().length);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]);
//		Assert.assertTrue(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0] instanceof TypeEqualityCondition);
//		Assert.assertEquals(1, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]).getPosition());
//		Assert.assertEquals(3, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]).getOther());
//		Assert.assertTrue(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1] instanceof TypeEqualityCondition);
//		Assert.assertEquals(2, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]).getPosition());
//		Assert.assertEquals(4, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]).getOther());
//		
//		
//		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
//		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
//		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
//		
//		Assert.assertEquals(2,plan1.getChildren().length);
//		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
//		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
//	}
//	
//	
//	@Test public void test4() {
//		AccessTerm access1 = new AccessTerm(this.R, this.method0);
//		AccessTerm access2 = new AccessTerm(this.S, this.method2);			
//		DependentJoinTerm plan1 = new DependentJoinTerm(access1, access2);
//		
//		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
//		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
//		Assert.assertNotNull(plan1.getJoinCondition());
//		Assert.assertTrue(plan1.getJoinCondition() instanceof ConjunctiveCondition);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions());
//		Assert.assertEquals(2,((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions().length);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]);
//		Assert.assertTrue(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0] instanceof TypeEqualityCondition);
//		Assert.assertEquals(1, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]).getPosition());
//		Assert.assertEquals(3, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]).getOther());
//		Assert.assertTrue(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1] instanceof TypeEqualityCondition);
//		Assert.assertEquals(2, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]).getPosition());
//		Assert.assertEquals(4, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]).getOther());
//		
//		
//		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
//		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
//		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
//		
//		Assert.assertEquals(2,plan1.getChildren().length);
//		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
//		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
//	}
//	
//	@Test public void test5() {
//		AccessTerm access1 = new AccessTerm(this.method0);
//		AccessTerm access2 = new AccessTerm(this.method2);
//		SelectionTerm selectionTerm = new SelectionTerm(ConstantEqualityCondition.create(0, TypedConstant.create(new Integer(1))), access1);
//		DependentJoinTerm plan1 = new DependentJoinTerm(selectionTerm, access2);
//		
//		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
//		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
//		Assert.assertNotNull(plan1.getJoinCondition());
//		Assert.assertTrue(plan1.getJoinCondition() instanceof ConjunctiveCondition);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions());
//		Assert.assertEquals(2,((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions().length);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]);
//		Assert.assertTrue(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0] instanceof TypeEqualityCondition);
//		Assert.assertEquals(1, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]).getPosition());
//		Assert.assertEquals(3, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]).getOther());
//		Assert.assertTrue(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1] instanceof TypeEqualityCondition);
//		Assert.assertEquals(2, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]).getPosition());
//		Assert.assertEquals(4, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]).getOther());
//		
//		
//		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
//		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
//		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
//		
//		Assert.assertEquals(2,plan1.getChildren().length);
//		Assert.assertTrue(plan1.getChildren()[0] instanceof SelectionTerm);
//		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
//	}
//	
//	@Test public void test6() {
//		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
//		inputConstants1.put(0, TypedConstant.create(TypedConstant.create(new Integer(1))));
//		AccessTerm access1 = new AccessTerm(this.method1, inputConstants1);
//		AccessTerm access2 = new AccessTerm(this.method1);
//		DependentJoinTerm plan1 = new DependentJoinTerm(access1, access2);
//		
//		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
//		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
//		Assert.assertNotNull(plan1.getJoinCondition());
//		Assert.assertTrue(plan1.getJoinCondition() instanceof ConjunctiveCondition);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions());
//		Assert.assertEquals(2,((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions().length);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]);
//		Assert.assertNotNull(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]);
//		Assert.assertTrue(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0] instanceof TypeEqualityCondition);
//		Assert.assertEquals(1, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]).getPosition());
//		Assert.assertEquals(3, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[0]).getOther());
//		Assert.assertTrue(((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1] instanceof TypeEqualityCondition);
//		Assert.assertEquals(2, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]).getPosition());
//		Assert.assertEquals(4, ((TypeEqualityCondition)((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions()[1]).getOther());
//		
//		
//		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
//		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
//		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
//		
//		Assert.assertEquals(2,plan1.getChildren().length);
//		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
//		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
//	}
//	
//	@Test public void test7() {
//		AccessTerm access1 = new AccessTerm(this.R, this.method0);
//		AccessTerm access2 = new AccessTerm(this.S, this.method0);			
//		JoinTerm plan1 = JoinTerm.create(access1, access2);
//	
//		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
//		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
//		
//		Assert.assertEquals(2,plan1.getChildren().length);
//		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
//		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
//	}
//	
//	@Test public void test8() {
//		
//		AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {});
//		AccessMethod am2 = AccessMethod.create("access_method2",new Integer[] {0});
//		AccessMethod am3 = AccessMethod.create("access_method2",new Integer[] {0,1});
//		
//		Relation relation1 = new Relation("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
//				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
//				new AccessMethod[] {am1});
//		Relation relation2 = new Relation("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
//				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")},
//				new AccessMethod[] {am1, am2, am3});
//		
//		// Free access on relation R1.
//		AccessTerm relation1Free = new AccessTerm(am1);
//		
//		// Access on relation R2 that requires inputs on first position.
//		// Suppose that a user already specified the typed constant "100" to access it 
//		Map<Attribute, TypedConstant> inputConstants1 = new HashMap<>();
//		inputConstants1.put(0, TypedConstant.create(100));
//		
//		// Note that it is the access method am2 that specifies that relation2 requires 
//		// input(s) on the first position (i.e. position 0). The inputConstants1 map contains
//		// the TypedConstant that provides that input.
//		AccessTerm relation2InputonFirst = new AccessTerm(am2, inputConstants1);
//
//		// A dependent join plan that takes the outputs of the first access and feeds them to the 
//		// first input position (i.e. position 0) of the second accessed relation.
//		try {
//			new DependentJoinTerm(relation1Free, relation2InputonFirst);
//			Assert.fail("Should have thrown exception");
//		} catch(AssertionError e) {
//			// excpected
//		}
//	}
//
//
//}
package uk.ac.ox.cs.pdq.test.algebra;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
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
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * @author Gabor
 *
 */
public class RelationalTermTest extends PdqTest {

	@Test
	public void testAccessCreation() {
		Relation relation = new Relation("R0", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		Relation relation1 = new Relation("R1", new Attribute[] {Attribute.create(Integer.class, "attr2")});
		RelationalTerm	child1 = new AccessTerm(method_0.getMethod(relation));
		RelationalTerm	child2 = new AccessTerm(method_1.getMethod(relation1));
		RelationalTerm	child3 = new AccessTerm(method_0.getMethod(relation));
		
		// child1 and child3 should have the same reference because they're based on the same relation
		if (child1 != child3) {
			Assert.fail("Relation cache does not provide same reference");
		}

		// child1 and child2 should have different references because they're based on different relations
		if (child1 == child2) {
			Assert.fail("Relation cache should not provide same reference");
		}
	}
	
	@Test
	public void testProjectionTerm() {
		try {
			Schema schema = commonTestIoJaxbSchema;
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
			RelationalTerm projection = new ProjectionTerm(attributes, access);
			
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
			Schema schema = commonTestIoJaxbSchema;
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
			AccessTerm access1 = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			AccessTerm access2 = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			CartesianProductTerm cartasianp = new CartesianProductTerm(access1, access2);
			
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
			Schema schema = commonTestIoJaxbSchema;
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			DependentJoinTerm dependentJ = new DependentJoinTerm(access, access);
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
			Schema schema = commonTestIoJaxbSchema;
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			JoinTerm join = new JoinTerm(access, access);
			Attribute[] in = join.getInputAttributes();
			
			// There are 4 input attributes named r1.1, r1.2, r1.3 and r1.4
			Assert.assertNotNull(in);
			Assert.assertEquals(4, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			//Assert.assertEquals("r1.3", in[2].getName());
			//Assert.assertEquals("r1.4", in[3].getName());
			
			// There are 4 output attributes, the same as input
			Attribute[] out = join.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertEquals(4, out.length);
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testRenameTerm() {
		try {
			Schema schema = commonTestIoJaxbSchema;
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
			RenameTerm renameTerm = new RenameTerm(attributes, access);
			
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
			Schema schema = commonTestIoJaxbSchema;
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicate = ConstantEqualityCondition.create(0, TypedConstant.create((Object) 1));
			SelectionTerm selectionTerm = new SelectionTerm(predicate , access);

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
			Schema schema = commonTestIoJaxbSchema;
//			File schemaFile = new File("test/src/uk/ac/ox/cs/pdq/test/io/jaxb/schema.xml");
//			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicate = ConstantEqualityCondition.create(0, TypedConstant.create((Object) 1));
			ProjectionTerm p = new ProjectionTerm(access.getInputAttributes(), access);
			ProjectionTerm p1 = new ProjectionTerm(p.getInputAttributes(), p);
			SelectionTerm selectionTerm = new SelectionTerm(predicate , p1);
			ProjectionTerm p2 = new ProjectionTerm(selectionTerm.getInputAttributes(), selectionTerm);

			RelationalTerm accessX = new AccessTerm(schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicateX = ConstantEqualityCondition.create(0, TypedConstant.create((Object) 1));
			ProjectionTerm pX = new ProjectionTerm(accessX.getInputAttributes(), accessX);
			ProjectionTerm p1X = new ProjectionTerm(pX.getInputAttributes(), pX);
			SelectionTerm selectionTermX = new SelectionTerm(predicateX , p1X);
			ProjectionTerm p2X = new ProjectionTerm(selectionTermX.getInputAttributes(), selectionTermX);
			
			
			JoinTerm jt = new JoinTerm(p2, p2X);
			
			// There are 4 input attributes with names r1.1, r1.2, r1.3 and r1.4
			Attribute[] in = jt.getInputAttributes();
			Assert.assertNotNull(in);
			Assert.assertEquals(4, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			//Assert.assertEquals("r1.3", in[2].getName());
			//Assert.assertEquals("r1.4", in[3].getName());

			// There are 4 output attributes, the same as input
			Attribute[] out = jt.getOutputAttributes();
			Assert.assertNotNull(out);
			Assert.assertEquals(4, out.length);
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test public void test3() {
		AccessTerm access1 = new AccessTerm(method_0.getMethod(this.R));
		AccessTerm access2 = new AccessTerm(method_1.getMethod(this.S));
		DependentJoinTerm plan1 = new DependentJoinTerm(access1, access2);
		
		// Output attributes are {a, b, c, d}
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		
		// Input attributes are empty
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		
		// Join conditions are ConjunctiveConditions
		Assert.assertNotNull(plan1.getJoinCondition());
		Assert.assertTrue(plan1.getJoinCondition() instanceof ConjunctiveCondition);
		
		// There are 2 simple conditions
		SimpleCondition[] sc = ((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions();
		Assert.assertNotNull(sc);
		Assert.assertEquals(2,sc.length);
		Assert.assertNotNull(sc[0]);
		Assert.assertNotNull(sc[1]);
		Assert.assertTrue(sc[0] instanceof ConstantEqualityCondition);
		Assert.assertEquals(1, ((ConstantEqualityCondition)sc[0]).getPosition());
		Assert.assertTrue(sc[1] instanceof ConstantEqualityCondition);
		Assert.assertEquals(2, ((ConstantEqualityCondition)sc[1]).getPosition());
		
		// getPositionsInLeftChildThatAreInputToRightChild
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		// getChildren
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	
	@Test public void test4() {
		AccessTerm access1 = new AccessTerm(method_0.getMethod(this.R));
		AccessTerm access2 = new AccessTerm(method_1.getMethod(this.S));			
		DependentJoinTerm plan1 = new DependentJoinTerm(access1, access2);
		
		// Output attributes are {a, b, c, d}
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());

		// Input attributes are empty
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
	
		// Join conditions are ConjunctiveConditions
		Assert.assertNotNull(plan1.getJoinCondition());
		Assert.assertTrue(plan1.getJoinCondition() instanceof ConjunctiveCondition);

		// There are 2 simple conditions
		SimpleCondition[] sc = ((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions();
		Assert.assertNotNull(sc);
		Assert.assertEquals(2,sc.length);
		Assert.assertNotNull(sc[0]);
		Assert.assertNotNull(sc[1]);
		Assert.assertTrue(sc[0] instanceof ConstantEqualityCondition);
		Assert.assertEquals(1, ((ConstantEqualityCondition)sc[0]).getPosition());
		Assert.assertTrue(sc[1] instanceof ConstantEqualityCondition);
		Assert.assertEquals(2, ((ConstantEqualityCondition)sc[1]).getPosition());
		
		// getPositionsInLeftChildThatAreInputToRightChild
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		// getChildren
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test5() {
		AccessTerm access1 = new AccessTerm(method_0.getMethod(this.R));
		AccessTerm access2 = new AccessTerm(method_1.getMethod(this.S));
		SelectionTerm selectionTerm = new SelectionTerm(ConstantEqualityCondition.create(0, TypedConstant.create(new Integer(1))), access1);
		DependentJoinTerm plan1 = new DependentJoinTerm(selectionTerm, access2);
		
		// Output attributes are {a, b, c, d}
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());

		// Input attributes are empty
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		
		// Join conditions are ConjunctiveConditions
		Assert.assertNotNull(plan1.getJoinCondition());
		Assert.assertTrue(plan1.getJoinCondition() instanceof ConjunctiveCondition);
		
		// There are 2 simple conditions
		SimpleCondition[] sc = ((ConjunctiveCondition)plan1.getJoinCondition()).getSimpleConditions();
		Assert.assertNotNull(sc);
		Assert.assertEquals(2,sc.length);
		Assert.assertNotNull(sc[0]);
		Assert.assertNotNull(sc[1]);
		Assert.assertTrue(sc[0] instanceof ConstantEqualityCondition);
		Assert.assertEquals(1, ((ConstantEqualityCondition)sc[0]).getPosition());
		Assert.assertTrue(sc[1] instanceof ConstantEqualityCondition);
		Assert.assertEquals(2, ((ConstantEqualityCondition)sc[1]).getPosition());
		
		// getPositionsInLeftChildThatAreInputToRightChild
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		// getChildren
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof SelectionTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test6() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(TypedConstant.create(new Integer(1))));
		AccessTerm access1 = new AccessTerm(method_0.getMethod(this.R));
		AccessTerm access2 = new AccessTerm(method_1.getMethod(this.S));
		DependentJoinTerm plan1 = new DependentJoinTerm(access1, access2);
		
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		
		// Join conditions are ConjunctiveConditions
		Assert.assertNotNull(plan1.getJoinCondition());
		Assert.assertTrue(plan1.getJoinCondition() instanceof ConjunctiveCondition);
	
		// There are 2 simple conditions of type ConstantEqualityCondition
		SimpleCondition[] sc = ((ConjunctiveCondition) plan1.getJoinCondition()).getSimpleConditions();
		Assert.assertNotNull(sc);
		Assert.assertEquals(2,sc.length);
		Assert.assertNotNull(sc[0]);
		Assert.assertNotNull(sc[1]);
		Assert.assertTrue(sc[0] instanceof ConstantEqualityCondition);
		Assert.assertEquals(1, ((ConstantEqualityCondition)sc[0]).getPosition());
		Assert.assertTrue(sc[1] instanceof ConstantEqualityCondition);
		Assert.assertEquals(2, ((ConstantEqualityCondition)sc[1]).getPosition());
		
		// getPositionsInLeftChildThatAreInputToRightChild
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		// getChildren are of type AccessTerm
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test7() {
		AccessTerm access1 = new AccessTerm(method_0.getMethod(this.R));
		AccessTerm access2 = new AccessTerm(method_1.getMethod(this.S));			
		JoinTerm plan1 = new JoinTerm(access1, access2);
	
		// Output attributes are {a,b,c,b,c}
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		
		// getChildren are of type AccessTerm
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test8() {
		
		AccessMethod am1 = method_0.getMethod(this.R);
		AccessMethod am2 = method_1.getMethod(this.S);
		AccessMethod am3 = method_2.getMethod(this.T);
		
		Relation relation1 = new Relation("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")});
		relation1.addAccessMethod(am1);
		Relation relation2 = new Relation("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")});
		relation2.addAccessMethods(new AccessMethod[] {am1, am2, am3});
		
		// Free access on relation R1.
		AccessTerm relation1Free = new AccessTerm(am1);
		
		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(100));
		
		// Note that it is the access method am2 that specifies that relation2 requires 
		// input(s) on the first position (i.e. position 0). The inputConstants1 map contains
		// the TypedConstant that provides that input.
		AccessTerm relation2InputonFirst = new AccessTerm(am2);

		// A dependent join plan that takes the outputs of the first access and feeds them to the 
		// first input position (i.e. position 0) of the second accessed relation.
		try {
			new DependentJoinTerm(relation1Free, relation2InputonFirst);
			Assert.fail("Should have thrown exception");
		} catch(AssertionError e) {
			// excpected
		}
	}


}
