package uk.ac.ox.cs.pdq.test.db;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.util.Utility;

public class DatabaseInstanceTest {
	
	@Before
	public void setup() {
		Utility.assertsEnabled();	
	}
	
	@Test
	public void mySqlStatementBuioderTest () {
		try {
			Schema schema = IOManager.importSchema(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml"));
			
			DatabaseConnection dc = new DatabaseConnection(new DatabaseParameters(), schema);
			Assert.assertNotNull(dc);
			Assert.assertNotNull(dc.getSQLStatementBuilder());
			Assert.assertNotNull(dc.getSynchronousConnections());
			Assert.assertTrue(dc.getSynchronousConnections().size()>0);
		}catch(Throwable t ) {
			t.printStackTrace();
			Assert.fail(t.getMessage());
		}
	}

}
