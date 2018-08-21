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
import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.test.util.PdqTest;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

// Overall comments to be inserted by gabor
//
// @author: gabor

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

	//
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

	// testDbAccessExport() creates an SqlAccessMethod, inputs and relation (mocked). Then it performs an access
	// and iterates over the data returned. Then it tries to do an export and tests if the files are the same.
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
		Assert.assertEquals(25, counter);
		try {
			DbIOManager.exportAccessMethod(target, new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethodOut.xml"));
		} catch (Throwable t) {
			t.printStackTrace();
			t.printStackTrace();
		}
		long goodLengh = new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethod.xml").length();
		long newLengh = new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethodOut.xml").length();
		// the path in the xml can change from user to user, so we can only check approximation.
		Assert.assertTrue(goodLengh + 30 > newLengh);
		Assert.assertTrue(goodLengh - 30 < newLengh);
		
		
		target.close();
		new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethodOut.xml").delete();
	}

	// testInMemoryAccessExport() creates an InMemoryAccessMethod, inputs and relation (mocked). Then it performs an access
	// and iterates over the data returned. Then it tries to do an export and tests if the files are the same.
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
		for (int index = 0; index < 25; index++) {
			tuples.add(tt.createTuple(index, "Name" + index,1000 + index, "Comment" + index));
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
			DbIOManager.exportAccessMethod(target, new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethodOut.xml"));
		} catch (Throwable t) {
			t.printStackTrace();
			t.printStackTrace();
		}
		long goodLengh = new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethod.xml").length();
		long newLengh = new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethodOut.xml").length();
		// the path in the xml can change from user to user, so we can only check approximation.
		Assert.assertTrue(goodLengh + 60 > newLengh);
		Assert.assertTrue(goodLengh - 60 < newLengh);
		target.close();
		new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethodOut.xml").delete();
	}

	// testAccessImport() tries to import an SqlAccessMethod. Then it performs an access
	// and iterates over the data returned.
	@Test
	public void testAccessImport() throws JAXBException {
		
		SqlAccessMethod target = null;
		try {
			target = (SqlAccessMethod) DbIOManager
					.importAccess(new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/dbAccessMethod.xml"));
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
	
	// testInMemoryAccessImport() tries to import an InMemoryAccessMethod. Then it performs an access
	// and iterates over the data returned.
	@Test
	public void testInMemoryAccessImport() throws JAXBException {

		InMemoryAccessMethod target = null;
		try {
			target = (InMemoryAccessMethod) DbIOManager
					.importAccess(new File("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses/InMemoryAccessMethod.xml"));
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

	// testAccessRepository() gets an AccessRepository then gets accesses "NATION" and "NATION_MEM", testing the reading of data from the 
	// access method in between.
	@Test
	public void testAccessRepository() throws Exception {
		AccessRepository repo = AccessRepository.getRepository("test/src/uk/ac/ox/cs/pdq/test/datasources/accessRepository/schemas/accesses");
		ExecutableAccessMethod accessMethod = repo.getAccess("NATION");
		testReadingData(accessMethod);
		ExecutableAccessMethod accessMethod2 = repo.getAccess("NATION_MEM");
		testReadingData(accessMethod2);
		repo.closeAllAccesses();
		Assert.assertTrue(accessMethod.isClosed());
		Assert.assertTrue(accessMethod2.isClosed());
	}

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
