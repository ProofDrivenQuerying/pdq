package uk.ac.ox.cs.pdq.test.algebra;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
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
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Gabor
 *
 */
public class RelationalTermTest {
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

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

}
