package uk.ac.ox.cs.pdq.test.datasources.io.jaxb;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Gabor
 *
 */
public class DbIOManagerTest {
	@Before
	public void setup() {
		Utility.assertsEnabled();
	}

	@Test
	public void testReadingSchema() {
		Assert.assertTrue(false);
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\case.properties")));
			Schema s = DbIOManager.importSchema(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml"),props);
			Assert.assertNotNull(s);
			Assert.assertNotNull(s.getRelations());
			Assert.assertTrue(s.getRelations().length > 0);
			Assert.assertNotNull(s.getRelations()[0]);
			Assert.assertNotNull(s.getRelations()[0].getName());
			Assert.assertNotNull(s.getDependencies());
			Assert.assertTrue(s.getDependencies().length > 0);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
