package uk.ac.ox.cs.pdq.test.databasemanagement;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * TOCOMMENT: WHAT IS THIS??? WHO WROTE IT??
 *
 */
public class TestIndices extends PdqTest {

	@Test
	public void testAddIndexPostgres() {
		Attribute attr1 = Attribute.create(Integer.class, "r1_1");
		Attribute attr2 = Attribute.create(Integer.class, "r1_2");
		AccessMethod am1 = AccessMethod.create("m1", new Integer[] {});
		AccessMethod am2 = AccessMethod.create("m2", new Integer[] { 0, 1 });
		Relation r = Relation.create("r1", new Attribute[] { attr1, attr2 }, new AccessMethod[] { am1, am2 }, new ForeignKey[] {}, false,
				new String[] { attr1.getName(), attr2.getName() });
		Schema mySchema = new Schema(new Relation[] {r});
		ExternalDatabaseManager edm;
		try {
			edm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
			edm.initialiseDatabaseForSchema(mySchema);
			edm.dropDatabase();
		} catch (DatabaseException e) {
			e.printStackTrace();
			Assert.fail();
		}
		
	}

}
