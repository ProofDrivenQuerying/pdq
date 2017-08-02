package uk.ac.ox.cs.pdq.test.io.jaxb;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
			Assert.assertTrue(q.getAtoms().length>0);
			Assert.assertNotNull(q.getAtoms()[0]);
			Assert.assertNotNull(q.getAtoms()[0].getFreeVariables());
			Assert.assertNotNull(q.getFreeVariables());
			Assert.assertTrue(q.getFreeVariables().length>0);
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
			Assert.assertEquals(in.length(),out.length());
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
			Assert.assertTrue(s.getRelations().length>0);
			Assert.assertNotNull(s.getRelations()[0]);
			Assert.assertNotNull(s.getRelations()[0].getName());
			Assert.assertNotNull(s.getDependencies());
			Assert.assertTrue(s.getDependencies().length>0);
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
			Assert.assertEquals(in.length(),out.length());
			out.delete();
		} catch (JAXBException e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
	}

}
