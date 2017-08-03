package uk.ac.ox.cs.pdq.test.io.jaxb;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
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
		} catch (JAXBException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testWritingQuery() {
		try {
			File in = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\query.xml");
			ConjunctiveQuery q = IOManager.importQuery(in);
			File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\queryOut.xml");
			IOManager.exportQueryToXml(q, out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(in.length(), out.length());
			out.delete();
		} catch (JAXBException e) {
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
		} catch (JAXBException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testWritingSchema() {
		try {
			File in = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml");
			Schema s = IOManager.importSchema(in);
			File out = new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schemaOut.xml");
			IOManager.exportSchemaToXml(s, out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(in.length(), out.length());
			out.delete();
		} catch (JAXBException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
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
			IOManager.writeRelationalTerm(rt ,out);			
			Assert.assertTrue(out.exists());
			Assert.assertEquals(in.length(), out.length());
			out.delete();
		} catch (JAXBException e) {
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
			IOManager.writeRelationalTerm(child1 ,out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(ref.length(), out.length());
			out.delete();
		} catch (JAXBException e) {
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
			Attribute[] attributes = new Attribute[] {schema.getRelations()[0].getAttributes()[0],schema.getRelations()[0].getAttributes()[1]};
			RelationalTerm child2 = ProjectionTerm.create(attributes , child1);
			IOManager.writeRelationalTerm(child2 ,out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(ref.length(), out.length());
			out.delete();
		} catch (JAXBException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
