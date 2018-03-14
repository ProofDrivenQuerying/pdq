package uk.ac.ox.cs.pdq.test.io.jaxb;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

public class RelationPrimaryAndForeignKeyTest {
	@Test
	public void testReadingSchema() {
		try {
			Schema s = IOManager.importSchema(new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "io" + File.separator + "jaxb" + File.separator + "schemaWithKeys.xml"));
			Assert.assertNotNull(s);
			Assert.assertNotNull(s.getRelations());
			Assert.assertTrue(s.getRelations().length > 0);
			Assert.assertNotNull(s.getRelations()[0]);
			Assert.assertNotNull(s.getRelations()[0].getName());
			Assert.assertNotNull(s.getNonEgdDependencies());
			Assert.assertNotNull(s.getRelation(0).getForeignKeys());
			Assert.assertEquals(2,s.getRelation(0).getForeignKeys().length);
			Assert.assertEquals("r1_1",s.getRelation(0).getKey().getAttributes()[0].getName());
			Assert.assertTrue(s.getNonEgdDependencies().length == 0);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void testWritingSchema() {
		try {
			ForeignKey fk1 = new ForeignKey("ForeignKey1");
			ForeignKey fk2 = new ForeignKey("ForeignKey2");
			Attribute attr1 = Attribute.create(Integer.class, "r1_1");
			Attribute attr2 = Attribute.create(Integer.class, "r1_2");
			Attribute attr3 = Attribute.create(Integer.class, "r1_3");
			Attribute attr4 = Attribute.create(Integer.class, "r1_4");
			PrimaryKey pk = PrimaryKey.create(new Attribute[] {attr1,attr2});
			
			Attribute attrF = Attribute.create(Integer.class, "r1_F");
			
			Relation r2 = Relation.create("r2", new Attribute[] { attrF}, new AccessMethod[] { }, new ForeignKey[] {}, false,
					new String[] { attrF.getName()});
			fk1.addReference(new Reference(attr1,attrF));
			fk1.setForeignRelation(r2);
			AccessMethod am1 = AccessMethod.create("m1", new Integer[] {});
			AccessMethod am2 = AccessMethod.create("m2", new Integer[] { 0, 1 });
			Relation r = Relation.create("r1", new Attribute[] { attr1, attr2, attr3, attr4 }, new AccessMethod[] { am1, am2 }, new ForeignKey[] {fk1,fk2}, false,
					new String[] { attr1.getName(), attr2.getName() });
			
			r.setKey(pk);
			Schema schema = new Schema(new Relation[] {r});
			File out = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "io" + File.separator + "jaxb" + File.separator + "schemaWithKeys.xml");
			IOManager.exportSchemaToXml(schema, out);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void ImportExportTest() {
		try {
			File in = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "io" + File.separator + "jaxb" + File.separator + "schemaWithKeys.xml");
			File out = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "io" + File.separator + "jaxb" + File.separator + "schemaWithKeys2.xml");
			Schema s = IOManager.importSchema(in);
			IOManager.exportSchemaToXml(s, out);
			Assert.assertEquals(in.length(),out.length());
			out.deleteOnExit();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
