package uk.ac.ox.cs.pdq.test.io.jaxb;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.AttributeEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConjunctiveCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.algebra.SimpleCondition;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Gabor
 *
 */
public class RelationalTermIOTest {
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before
	public void setup() {
		Utility.assertsEnabled();
	}

	private void testIO(RelationalTerm t) {
		try {
			File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\RelationalTermTest.xml");
			IOManager.writeRelationalTerm(t, out);
			RelationalTerm reRead = IOManager.readRelationalTerm(out, null);
			Assert.assertEquals(t, reRead);
			out.delete();
		} catch (JAXBException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
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
			testIO(projection);
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
			testIO(cartasianp);

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
			testIO(dependentJ);

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
			testIO(join);

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
			testIO(renameTerm);
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
			SelectionTerm selectionTerm = SelectionTerm.create(predicate, access);
			testIO(selectionTerm);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testSelectionTermWithCondition() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm access = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			SimpleCondition predicate = AttributeEqualityCondition.create(0, 1);
			SimpleCondition predicate2 = AttributeEqualityCondition.create(1, 0);
			ConjunctiveCondition concon = ConjunctiveCondition.create(new SimpleCondition[] { predicate, predicate2, predicate, predicate });
			SelectionTerm selectionTerm = SelectionTerm.create(concon, access);

			testIO(selectionTerm);
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
			SelectionTerm selectionTerm = SelectionTerm.create(predicate, p1);
			ProjectionTerm p2 = ProjectionTerm.create(selectionTerm.getInputAttributes(), selectionTerm);

			RelationalTerm accessX = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Condition predicateX = AttributeEqualityCondition.create(0, 1);
			ProjectionTerm pX = ProjectionTerm.create(accessX.getInputAttributes(), accessX);
			ProjectionTerm p1X = ProjectionTerm.create(pX.getInputAttributes(), pX);
			SelectionTerm selectionTermX = SelectionTerm.create(predicateX, p1X);
			ProjectionTerm p2X = ProjectionTerm.create(selectionTermX.getInputAttributes(), selectionTermX);

			JoinTerm jt = JoinTerm.create(p2, p2X);

			testIO(jt);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
