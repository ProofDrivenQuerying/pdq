package uk.ac.ox.cs.pdq.test.io.jaxb;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.util.Utility;

public class IOManagerTest {
	@Before
	public void setup() {
		Utility.assertsEnabled();
	}

	@Test
	public void testReadingQuery() {
		try {
			ConjunctiveQuery q = IOManager.importQuery(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\query.xml"));
			Assert.assertNotNull(q);
			Assert.assertNotNull(q.getAtoms());
			Assert.assertTrue(q.getAtoms().length > 0);
			Assert.assertNotNull(q.getAtoms()[0]);
			Assert.assertNotNull(q.getAtoms()[0].getFreeVariables());
			Assert.assertNotNull(q.getFreeVariables());
			Assert.assertTrue(q.getFreeVariables().length > 0);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testWritingQuery() {
		try {
			File in = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\query.xml");
			File ref = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\queryRef.xml");
			ConjunctiveQuery q = IOManager.importQuery(in);
			File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\queryOut.xml");
			IOManager.exportQueryToXml(q, out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(ref.length(), out.length());
			out.delete();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testReadingSchema() {
		try {
			Schema s = IOManager.importSchema(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml"));
			Assert.assertNotNull(s);
			Assert.assertNotNull(s.getRelations());
			Assert.assertTrue(s.getRelations().length > 0);
			Assert.assertNotNull(s.getRelations()[0]);
			Assert.assertNotNull(s.getRelations()[0].getName());
			Assert.assertNotNull(s.getDependencies());
			Assert.assertTrue(s.getDependencies().length > 0);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testWritingSchema() {
		try {
			Attribute attr1 = Attribute.create(Integer.class, "r1.1");
			Attribute attr2 = Attribute.create(Integer.class, "r1.2");
			AccessMethod am1 = AccessMethod.create("m1", new Integer[] {});
			AccessMethod am2 = AccessMethod.create("m2", new Integer[] { 0, 1 });
			Relation r = Relation.create("r1", new Attribute[] { attr1, attr2 }, new AccessMethod[] { am1, am2 });
			Relation r2 = new View("myView", new Attribute[] { attr1, attr2 }, new AccessMethod[] { am1, am2 });
			Schema s = new Schema(new Relation[] { r, r2 });
			File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schemaOut.xml");
			IOManager.exportSchemaToXml(s, out);
			Assert.assertTrue(out.exists());

			Schema s2 = IOManager.importSchema(out);
			File out2 = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schemaOut2.xml");

			IOManager.exportSchemaToXml(s2, out2);

			Assert.assertEquals(out.length(), out2.length());
			out.delete();
			out2.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testReadingAccessTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			File in = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\AccessTerm.xml");
			File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\AccessTermOut.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm rt = IOManager.readRelationalTerm(in, schema);
			IOManager.writeRelationalTerm(rt, out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(in.length(), out.length());
			out.delete();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testWritingAccessTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);

			File ref = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\AccessTerm.xml");
			File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\AccessTermOut.xml");
			RelationalTerm child1 = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[0]);
			IOManager.writeRelationalTerm(child1, out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(ref.length(), out.length());
			out.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testWritingProjectionTerm() {
		try {
			File schemaFile = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);

			File ref = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\ProjectionTerm.xml");
			File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\ProjectionTermOut.xml");
			RelationalTerm child1 = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[1]);
			Attribute[] attributes = new Attribute[] { schema.getRelations()[0].getAttributes()[0], schema.getRelations()[0].getAttributes()[1] };
			RelationalTerm child2 = ProjectionTerm.create(attributes, child1);
			IOManager.writeRelationalTerm(child2, out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(ref.length(), out.length());
			out.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
