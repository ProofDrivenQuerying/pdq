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
import uk.ac.ox.cs.pdq.db.AccessMethod;
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
	public void testAccessCreation() {
		AccessMethod am = AccessMethod.create("test",new Integer[] {0});
		AccessMethod am1 = AccessMethod.create("test1",new Integer[] {0});
		Relation relation = Relation.create("R0", new Attribute[] {Attribute.create(Integer.class, "attr1")});
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "attr2")});
		RelationalTerm	child1 = AccessTerm.create(relation,am);
		RelationalTerm	child2 = AccessTerm.create(relation1,am1);
		RelationalTerm	child3 = AccessTerm.create(relation,am);
		
		if (child1 != child3) { // ATTENTIONAL! it have to be the same reference
			Assert.fail("Relation cache does not provide same reference");
		}
		if (child1 == child2) { // ATTENTIONAL! it have to be different reference
			Assert.fail("Relation cache should not provide same reference");
		}
	}
	
	@Test
	public void testProjectionTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
			RelationalTerm projection = ProjectionTerm.create(attributes, access);
			Attribute[] in = projection.getInputAttributes();
			Attribute[] out = projection.getOutputAttributes();
			Assert.assertNotNull(in);
			Assert.assertNotNull(out);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testCartesianProductTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			AccessTerm access1 = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			AccessTerm access2 = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			CartesianProductTerm cartasianp = CartesianProductTerm.create(access1, access2);
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
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			DependentJoinTerm dependentJ = DependentJoinTerm.create(access, access);
			Attribute[] in = dependentJ.getInputAttributes();
			Attribute[] out = dependentJ.getOutputAttributes();
			Assert.assertNotNull(in);
			Assert.assertNotNull(out);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals(4, out.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testJoinTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			JoinTerm join = JoinTerm.create(access, access);
			Attribute[] in = join.getInputAttributes();
			Attribute[] out = join.getOutputAttributes();
			Assert.assertNotNull(in);
			Assert.assertNotNull(out);
			Assert.assertEquals(4, in.length);
			Assert.assertEquals(4, out.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testRenameTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
			RenameTerm renameTerm = RenameTerm.create(attributes, access);
			Attribute[] in = renameTerm.getInputAttributes();
			Attribute[] out = renameTerm.getOutputAttributes();
			Assert.assertNotNull(in);
			Assert.assertNotNull(out);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testSelectionTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicate = AttributeEqualityCondition.create(0, 1);
			SelectionTerm selectionTerm = SelectionTerm.create(predicate , access);
			Attribute[] in = selectionTerm.getInputAttributes();
			Attribute[] out = selectionTerm.getOutputAttributes();
			Assert.assertNotNull(in);
			Assert.assertNotNull(out);
			Assert.assertEquals(2, in.length);
			Assert.assertEquals(2, out.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testLargeRelationalTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicate = AttributeEqualityCondition.create(0, 1);
			ProjectionTerm p = ProjectionTerm.create(access.getInputAttributes(), access);
			ProjectionTerm p1 = ProjectionTerm.create(p.getInputAttributes(), p);
			SelectionTerm selectionTerm = SelectionTerm.create(predicate , p1);
			ProjectionTerm p2 = ProjectionTerm.create(selectionTerm.getInputAttributes(), selectionTerm);

			RelationalTerm accessX = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicateX = AttributeEqualityCondition.create(0, 1);
			ProjectionTerm pX = ProjectionTerm.create(accessX.getInputAttributes(), accessX);
			ProjectionTerm p1X = ProjectionTerm.create(pX.getInputAttributes(), pX);
			SelectionTerm selectionTermX = SelectionTerm.create(predicateX , p1X);
			ProjectionTerm p2X = ProjectionTerm.create(selectionTermX.getInputAttributes(), selectionTermX);
			
			
			JoinTerm jt = JoinTerm.create(p2, p2X);
			
			Attribute[] in = jt.getInputAttributes();
			Attribute[] out = jt.getOutputAttributes();
			Assert.assertNotNull(in);
			Assert.assertNotNull(out);
			Assert.assertEquals(4, in.length);
			Assert.assertEquals(4, out.length);
			Assert.assertEquals("r1.1", in[0].getName());
			Assert.assertEquals("r1.2", in[1].getName());
			Assert.assertArrayEquals(in,out);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test public void test3() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		Assert.assertNotNull(plan1.getFollowupJoinConditions());
		Assert.assertTrue(plan1.getFollowupJoinConditions() instanceof ConjunctiveCondition);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions());
		Assert.assertEquals(2,((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions().length);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]);
		Assert.assertTrue(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0] instanceof AttributeEqualityCondition);
		Assert.assertEquals(1, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]).getPosition());
		Assert.assertEquals(3, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]).getOther());
		Assert.assertTrue(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1] instanceof AttributeEqualityCondition);
		Assert.assertEquals(2, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]).getPosition());
		Assert.assertEquals(4, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]).getOther());
		
		
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	
	@Test public void test4() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);			
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		Assert.assertNotNull(plan1.getFollowupJoinConditions());
		Assert.assertTrue(plan1.getFollowupJoinConditions() instanceof ConjunctiveCondition);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions());
		Assert.assertEquals(2,((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions().length);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]);
		Assert.assertTrue(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0] instanceof AttributeEqualityCondition);
		Assert.assertEquals(1, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]).getPosition());
		Assert.assertEquals(3, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]).getOther());
		Assert.assertTrue(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1] instanceof AttributeEqualityCondition);
		Assert.assertEquals(2, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]).getPosition());
		Assert.assertEquals(4, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]).getOther());
		
		
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test5() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);
		SelectionTerm selectionTerm = SelectionTerm.create(ConstantEqualityCondition.create(0, TypedConstant.create(new Integer(1))), access1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(selectionTerm, access2);
		
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		Assert.assertNotNull(plan1.getFollowupJoinConditions());
		Assert.assertTrue(plan1.getFollowupJoinConditions() instanceof ConjunctiveCondition);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions());
		Assert.assertEquals(2,((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions().length);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]);
		Assert.assertTrue(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0] instanceof AttributeEqualityCondition);
		Assert.assertEquals(1, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]).getPosition());
		Assert.assertEquals(3, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]).getOther());
		Assert.assertTrue(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1] instanceof AttributeEqualityCondition);
		Assert.assertEquals(2, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]).getPosition());
		Assert.assertEquals(4, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]).getOther());
		
		
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof SelectionTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test6() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(TypedConstant.create(new Integer(1))));
		AccessTerm access1 = AccessTerm.create(this.R, this.method1, inputConstants1);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		Assert.assertNotNull(plan1.getFollowupJoinConditions());
		Assert.assertTrue(plan1.getFollowupJoinConditions() instanceof ConjunctiveCondition);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions());
		Assert.assertEquals(2,((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions().length);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]);
		Assert.assertNotNull(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]);
		Assert.assertTrue(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0] instanceof AttributeEqualityCondition);
		Assert.assertEquals(1, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]).getPosition());
		Assert.assertEquals(3, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[0]).getOther());
		Assert.assertTrue(((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1] instanceof AttributeEqualityCondition);
		Assert.assertEquals(2, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]).getPosition());
		Assert.assertEquals(4, ((AttributeEqualityCondition)((ConjunctiveCondition)plan1.getFollowupJoinConditions()).getSimpleConditions()[1]).getOther());
		
		
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild());
		Assert.assertNotNull(plan1.getPositionsInLeftChildThatAreInputToRightChild().get(new Integer(0)));
		Assert.assertEquals(new Integer(1),(Integer)plan1.getPositionsInLeftChildThatAreInputToRightChild().get(0));
		
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test7() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method0);			
		JoinTerm plan1 = JoinTerm.create(access1, access2);
	
		Assert.assertArrayEquals(new Attribute[] {a,b,c,b,c}, plan1.getOutputAttributes());
		Assert.assertArrayEquals(new Attribute[] {}, plan1.getInputAttributes());
		
		Assert.assertEquals(2,plan1.getChildren().length);
		Assert.assertTrue(plan1.getChildren()[0] instanceof AccessTerm);
		Assert.assertTrue(plan1.getChildren()[1] instanceof AccessTerm);
	}
	
	@Test public void test8() {
		
		AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {});
		AccessMethod am2 = AccessMethod.create("access_method2",new Integer[] {0});
		AccessMethod am3 = AccessMethod.create("access_method2",new Integer[] {0,1});
		
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "a"),
				Attribute.create(Integer.class, "b"), Attribute.create(Integer.class, "c")},
				new AccessMethod[] {am1});
		Relation relation2 = Relation.create("R2", new Attribute[] {Attribute.create(Integer.class, "c"),
				Attribute.create(Integer.class, "d"), Attribute.create(Integer.class, "e")},
				new AccessMethod[] {am1, am2, am3});
		
		// Free access on relation R1.
		AccessTerm relation1Free = AccessTerm.create(relation1, am1);
		
		// Access on relation R2 that requires inputs on first position.
		// Suppose that a user already specified the typed constant "100" to access it 
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(100));
		
		// Note that it is the access method am2 that specifies that relation2 requires 
		// input(s) on the first position (i.e. position 0). The inputConstants1 map contains
		// the TypedConstant that provides that input.
		AccessTerm relation2InputonFirst = AccessTerm.create(relation2, am2, inputConstants1);

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
