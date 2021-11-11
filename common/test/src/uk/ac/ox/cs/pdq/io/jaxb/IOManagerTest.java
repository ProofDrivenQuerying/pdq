// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.util.PdqTest;

import java.io.File;

/**
 * @author Gabor
 *
 */
public class IOManagerTest extends PdqTest {
	@Before
	public void setup() {
		PdqTest.assertsEnabled();
	}

	// Calls IOManager.importQuery then asserts everything expected about the query

	/**
	 * takes in a query.xml file process and return the equivalent java objects
	 */
	@Test
	public void testReadingQuery() {
		try {													 
			ConjunctiveQuery q = null;
			for (int i=1; i<30; i++)
			q = IOManager.importQuery(new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "query.xml"));
			Assert.assertNotNull(q);
			Assert.assertNotNull(q.getAtoms());
			Assert.assertTrue(q.getAtoms().length > 0);
			Assert.assertNotNull(q.getAtoms()[0]);
			Assert.assertNotNull(q.getAtoms()[0].getFreeVariables());
			Assert.assertNotNull(q.getFreeVariables());
			Assert.assertTrue(q.getFreeVariables().length > 0);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Takes a query.xml file and write it to a new xml file
	 */
	// Calls IOManager.exportQuery using importQuery as a starting point
	@Test
	public void testWritingQuery() {
		try {
			File in = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "query.xml");
			File ref = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "queryRef.xml");
			ConjunctiveQuery q = IOManager.importQuery(in);
			File out = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "queryOut.xml");
			IOManager.exportQueryToXml(q, out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(ref.length(), out.length());
			out.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Take in a schema.xml file process and return the equivalent java objects
	 */
	// Calls IOManager.importSchema and then asserts everything about the relations
	@Test
	public void testReadingSchema() {
		try {
			Schema s = IOManager.importSchema(new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "schema.xml"));
			Assert.assertNotNull(s);
			Assert.assertNotNull(s.getRelations());
			Assert.assertTrue(s.getRelations().length > 0);
			Assert.assertNotNull(s.getRelations()[0]);
			Assert.assertNotNull(s.getRelations()[0].getName());
			Assert.assertNotNull(s.getNonEgdDependencies());
			Assert.assertEquals(2, s.getRelation("r1").getAccessMethods().length);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Takes a schema.xml file and write it to a new xml file
	 */
	// Calls IOManager.exportSchemaToXML using importSchema as a starting point
	@Test
	public void testWritingSchema() {
		try {
			Attribute attr1 = Attribute.create(Integer.class, "r1.1");
			Attribute attr2 = Attribute.create(Integer.class, "r1.2");
			Relation r = Relation.create("r1", new Attribute[] { attr1, attr2 });
			Relation r2 = new View("myView", new Attribute[] { attr1, attr2 });
			
			Schema s = new Schema(new Relation[] { r, r2 },new Dependency[] { this.tgd, this.tgd2, this.egd });
			File out = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "schemaOut.xml");
			IOManager.exportSchemaToXml(s, out);
			Assert.assertTrue(out.exists());

			Schema s2 = IOManager.importSchema(out);
			File out2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "schemaOut2.xml");

			IOManager.exportSchemaToXml(s2, out2);

			Assert.assertEquals(out.length(), out2.length());
			out.delete();
			out2.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Takes a schema.xml file and a AccessTerm.xml and write it to a new xml file
	 */
	// Calls IOManager.read-and-writeRelationTerm using importSchema as a starting point
	@Test
	public void testReadingAccessTerm() {
		try {
			File schemaFile = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "schema.xml");
			File in = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "AccessTerm.xml");
			File out = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "AccessTermOut.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			RelationalTerm rt = IOManager.readRelationalTerm(in, schema);
			IOManager.writeRelationalTerm(rt, out);
			Assert.assertTrue(out.exists());
			Assert.assertEquals(in.length(), out.length());
			out.delete();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	// Calls IOManager.writeRelationalTerm using importSchema as a starting point
	@Test
	public void testWritingAccessTerm() {
		try {
			File schemaFile = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);

			File ref = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "AccessTerm.xml");
			File out = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "AccessTermOut.xml");
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

	// Calls IOManager.writeRelationalTerm using importSchema as a starting point
	@Test
	public void testWritingProjectionTerm() {
		try {
			File schemaFile = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "schema.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			File ref = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "ProjectionTerm.xml");
			File out = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" +  File.separator + "io" + File.separator + "jaxb" + File.separator + "ProjectionTermOut.xml");
			RelationalTerm child1 = AccessTerm.create(schema.getRelations()[0], schema.getRelations()[0].getAccessMethods()[0]);
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
