package uk.ac.ox.cs.pdq.test.datasources.accessRepository;

import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;

import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.simplewebservice.XmlWebService;
import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the access repository
 * 
 * @author gabor
 *
 */
public class TestAccessRepository extends PdqTest {
	Properties properties;
	boolean print = false;

	public TestAccessRepository() {
		properties = new Properties();
		properties.setProperty("url", "jdbc:postgresql://localhost:5432/");
		properties.setProperty("database", "tpch");
		properties.setProperty("username", "admin");
		properties.setProperty("password", "admin");
	}
	@Test
	public void schemaWithView() {
		try {					
			File testFolder = new File("../regression/test/runtime/DatabaseExamples/case_002");
			File schema = new File(testFolder, "schema.xml");
			Schema s = DbIOManager.importSchema(schema);
			TGD tgd = ((View)s.getRelation("region_nation")).getRelationToViewDependency();
			Assert.assertNotNull(tgd);
			System.out.println(tgd);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	// NATION relation
	//
	// Attributes in the external schema.
	Attribute[] attrs_N = new Attribute[] { Attribute.create(Integer.class, "N_NATIONKEY"),
			Attribute.create(String.class, "N_NAME"), Attribute.create(Integer.class, "N_REGIONKEY"),
			Attribute.create(String.class, "N_COMMENT") };

	// Attributes in the internal schema.
	Attribute[] attrs_nation = new Attribute[] { Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "nationKey"), Attribute.create(Integer.class, "regionKey"), };

	// Attribute mapping
	Map<Attribute, Attribute> attrMap_nation = ImmutableMap.<Attribute, Attribute>of(
			Attribute.create(Integer.class, "N_NATIONKEY"), Attribute.create(Integer.class, "nationKey"),
			Attribute.create(String.class, "N_NAME"), Attribute.create(String.class, "name"),
			Attribute.create(Integer.class, "N_REGIONKEY"), Attribute.create(Integer.class, "regionKey"));

	/**
	 * Creates an Sql access method and attempts to export it into an xml file.
	 * 
	 * @throws JAXBException
	 */
	@Test
	public void testDbAccessExport() throws JAXBException {

		SqlAccessMethod target;
		Integer[] inputs;
		Relation relation;

		relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(this.attrs_nation.clone());
		String name = "NATION";
		when(relation.getName()).thenReturn(name);

		inputs = new Integer[0];
		target = new SqlAccessMethod(name, this.attrs_N, inputs, relation, this.attrMap_nation, this.properties);
		Iterable<Tuple> data = target.access();
		Assert.assertNotNull(data);
		Iterator<Tuple> it = data.iterator();
		int counter = 0;
		while (it.hasNext()) {
			Tuple t = it.next();
			if (print)
				System.out.println(t);
			counter++;
		}
		// The NATION table in the tpch database should have 25 facts.
		Assert.assertEquals(25, counter);
		try {
			DbIOManager.exportAccessMethod(target, new File(
					"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethodOut.xml"));
		} catch (Throwable t) {
			t.printStackTrace();
			t.printStackTrace();
		}
		long goodLength = new File(
				"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethod.xml")
						.length();
		long newLength = new File(
				"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethodOut.xml")
						.length();
		
		
		//There is a file path in the exported xml file. That path
		// depends on the user who runs the test. Therefore we cannot know how large the
		// new file should be exactly, but it should be in a 30 character range to the
		// saved copy.
		Assert.assertTrue(goodLength + 30 > newLength);
		Assert.assertTrue(goodLength - 30 < newLength);

		target.close();
		new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethodOut.xml")
				.delete();
	}

	/**
	 * Creates an InMemoryAccess object and attempts to export it to xml. The access
	 * method will have 25 test facts.
	 * 
	 * @throws JAXBException
	 */
	@Test
	public void testInMemoryAccessExport() throws JAXBException {
		InMemoryAccessMethod target;
		Integer[] inputs;
		Relation relation;

		relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(this.attrs_nation.clone());
		String name = "NATION";
		when(relation.getName()).thenReturn(name);

		inputs = new Integer[0];
		target = new InMemoryAccessMethod("NATION_MEM", this.attrs_N, inputs, relation, this.attrMap_nation);
		Collection<Tuple> tuples = new ArrayList<>();
		TupleType tt = TupleType.DefaultFactory.createFromTyped(this.attrs_N);
		// Creating test facts.
		for (int index = 0; index < 25; index++) {
			tuples.add(tt.createTuple(index, "Name" + index, 1000 + index, "Comment" + index));
		}
		target.load(tuples);
		Iterable<Tuple> data = target.access();
		Assert.assertNotNull(data);
		Iterator<Tuple> it = data.iterator();
		int counter = 0;
		while (it.hasNext()) {
			Tuple t = it.next();
			if (print)
				System.out.println(t);
			counter++;
		}
		Assert.assertEquals(25, counter);
		try {
			DbIOManager.exportAccessMethod(target, new File(
					"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethodOut.xml"));
		} catch (Throwable t) {
			t.printStackTrace();
			t.printStackTrace();
		}
		long goodLength = new File(
				"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethod.xml")
						.length();
		long newLength = new File(
				"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethodOut.xml")
						.length();
		//There is a file path in the exported xml file. That path
		// depends on the user who runs the test. Therefore we cannot know how large the
		// new file should be exactly, but it should be in a 30 character range to the
		// saved copy.
		Assert.assertTrue(goodLength + 60 > newLength);
		Assert.assertTrue(goodLength - 60 < newLength);
		target.close();
		new File(
				"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethodOut.xml")
						.delete();
	}

	/**
	 * Creates an simple xml web access method object and attempts to export it to xml. 
	 * 
	 * @throws JAXBException
	 */
	@Test
	public void testXmlWebAccessExport() throws JAXBException {
		ExecutableAccessMethod target;
		Integer[] inputs;
		Relation relation;

		relation = Mockito.mock(Relation.class);
		when(relation.getAttributes()).thenReturn(this.attrs_nation.clone());
		String name = "NATION";
		when(relation.getName()).thenReturn(name);

		inputs = new Integer[0];
		target = new XmlWebService("NATION_MEM", this.attrs_N, inputs, relation, this.attrMap_nation);
		((XmlWebService)target).setUrl("http://pdq-webapp.cs.ox.ac.uk:80/webapp/servlets/servlet/NationInput");
		try {
			DbIOManager.exportAccessMethod(target, new File(
					"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/XmlWebAccessMethodOut.xml"));
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
		long goodLength = new File(
				"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/XmlWebAccessMethod.xml")
						.length();
		long newLength = new File(
				"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/XmlWebAccessMethodOut.xml")
						.length();
		
		
		//There is a file path in the exported xml file. That path
		// depends on the user who runs the test. Therefore we cannot know how large the
		// new file should be exactly, but it should be in a 30 character range to the
		// saved copy.
		Assert.assertTrue(goodLength + 60 > newLength);
		Assert.assertTrue(goodLength - 60 < newLength);
		target.close();
		
		new File(
				"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethodOut.xml")
						.delete();
		
		try {
			ExecutableAccessMethod imported = DbIOManager.importAccess(new File(
					"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/XmlWebAccessMethod.xml"));
			Assert.assertEquals("http://pdq-webapp.cs.ox.ac.uk:80/webapp/servlets/servlet/NationInput", ((XmlWebService)imported).getUrl());
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
		
		
	}

	/**
	 * Tests the import from xml function of the SqlAccessMethod.
	 * 
	 * @throws JAXBException
	 */
	@Test
	public void testAccessImport() throws JAXBException {

		SqlAccessMethod target = null;
		try {
			target = (SqlAccessMethod) DbIOManager.importAccess(new File(
					"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethod.xml"));
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
		Iterable<Tuple> data = target.access();
		Assert.assertNotNull(data);
		Iterator<Tuple> it = data.iterator();
		int counter = 0;
		while (it.hasNext()) {
			Tuple t = it.next();
			if (print)
				System.out.println(t);
			counter++;
		}
		Assert.assertEquals(25, counter);
		target.close();
	}

	/**
	 * Tests if we can import InMemoryAccessMethod from xml.
	 * 
	 * @throws JAXBException
	 */
	@Test
	public void testInMemoryAccessImport() throws JAXBException {

		InMemoryAccessMethod target = null;
		try {
			target = (InMemoryAccessMethod) DbIOManager.importAccess(new File(
					"test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethod.xml"));
		} catch (Throwable t) {
			t.printStackTrace();
			t.printStackTrace();
		}
		Iterable<Tuple> data = target.access();
		Assert.assertNotNull(data);
		Iterator<Tuple> it = data.iterator();
		int counter = 0;
		while (it.hasNext()) {
			Tuple t = it.next();
			if (print)
				System.out.println(t);
			counter++;
		}
		Assert.assertEquals(25, counter);
		target.close();
	}

	/**
	 * Full usage scenario of access repository. It will read all access methods
	 * from the test folder, and then it will read data from two of those, and
	 * finally it will attempt to close all readers.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAccessRepository() throws Exception {
		AccessRepository repo = AccessRepository
				.getRepository("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses");
		ExecutableAccessMethod accessMethod = repo.getAccess("NATION");
		testReadingData(accessMethod);
		ExecutableAccessMethod accessMethod2 = repo.getAccess("NATION_MEM");
		testReadingData(accessMethod2);
		repo.closeAllAccesses();
		Assert.assertTrue(accessMethod.isClosed());
		Assert.assertTrue(accessMethod2.isClosed());
	}

	/**
	 * Reads from the given access. Asserts if we got 25 facts.
	 * 
	 * @param accessMethod
	 */
	private void testReadingData(ExecutableAccessMethod accessMethod) {
		Iterable<Tuple> data = accessMethod.access();
		Assert.assertNotNull(data);
		Iterator<Tuple> it = data.iterator();
		int counter = 0;
		while (it.hasNext()) {
			Tuple t = it.next();
			if (print)
				System.out.println(t);
			counter++;
		}
		Assert.assertEquals(25, counter);
	}

}
