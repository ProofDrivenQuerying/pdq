// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.databasemanagement;

import org.junit.*;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.*;
import uk.ac.ox.cs.pdq.reasoningdatabase.sqlcommands.Command;
import uk.ac.ox.cs.pdq.util.PdqTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author gabor
 *
 */
public class StateExporterTest extends PdqTest {

	protected Schema schema;
	private DatabaseManager connection;

	private static final int NUMBER_OF_DUMMY_DATA = 100;

	@Before
	public void setup() throws Exception {
		super.setup();
		this.schema = new Schema(new Relation[] { this.rel1, this.rel2 }, new Dependency[] { this.tgd });
	}
	/**
	 * Uses the following example run of the chase to generate a state, and then attempts to export and import it to assess differences.
	 * 
	 * Create the following unit tests for getMatches c. conjunctive query is
	 * 
	 * <pre>
	 * Q(x,y) = A(x,x), B(x,y), C(y,z,'TypedConstant1') D(z,z)
	 * </pre>
	 * 
	 * In this unit test we create facts in the database such that two conditions
	 * hold:
	 * <li>each relation has NUMBER_OF_DUMMY_DATA facts in the database and</li>
	 * <li>there are only five answer tuples in the answer. You could do this by
	 * adding junk tuples and manually creating tuples that participate in the
	 * match. In the last unit test the query is boolean so you should only get true
	 * </li>
	 * 
	 * We should get 5 matches.
	 *
	 * @throws IOException 
	 * @throws DatabaseException 
	 */
	@Test
	public void test() throws IOException, DatabaseException {

		Relation A = Relation.create("A",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation B = Relation.create("B",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation D = Relation.create("D",
				new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1") });
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2") });
		Relation E = Relation.create("E", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),
				Attribute.create(String.class, "attribute2") });
		Relation r[] = new Relation[] { A, B, C, D, E };
		Schema s = new Schema(r, new Dependency[0]);

		Collection<TypedConstant> constants = new ArrayList<>();
		for (int i = 1; i <= 5; i++)
			constants.add(TypedConstant.create("x" + i));
		for (int i = 1; i <= 5; i++)
			constants.add(TypedConstant.create("y" + i));
		for (int i = 1; i <= 5; i++)
			constants.add(TypedConstant.create("z" + i));
		constants.add(TypedConstant.create("c_constant_3"));

		List<Atom> facts = new ArrayList<>();
		// producing garbage:
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(A, new Term[] { TypedConstant.create("1g_1_" + i), TypedConstant.create("a_g_2_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("2g_1_" + i), TypedConstant.create("b_g_2_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(C, new Term[] { TypedConstant.create("3g_1_" + i), TypedConstant.create("c_g_2_" + i), TypedConstant.create("g_3a_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(D, new Term[] { TypedConstant.create("4g_1_" + i), TypedConstant.create("d_g_2_" + i) }));
		for (int i = 1; i <= NUMBER_OF_DUMMY_DATA; i++)
			facts.add(Atom.create(E, new Term[] { TypedConstant.create("5g_1_" + i), TypedConstant.create("e_g_2_" + i), TypedConstant.create("g_3b_" + i) }));
		// producing results:
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(A, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("x" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(B, new Term[] { TypedConstant.create("x" + i), TypedConstant.create("y" + i) }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(C, new Term[] { TypedConstant.create("y" + i), TypedConstant.create("z" + i), TypedConstant.create("c_constant_3") }));
		for (int i = 1; i <= 5; i++)
			facts.add(Atom.create(D, new Term[] { TypedConstant.create("z" + i), TypedConstant.create("z" + i) }));

		DatabaseManager dbm = createConnection(s);
		dbm.addFacts(facts);
		Collection<Atom> originalFacts = dbm.getCachedFacts();
		File dir = new File("test/src/uk/ac/ox/cs/pdq/databasemanagement/tmp");
		if (dir.exists()) deleteDir(dir);
		dir.mkdir();
		StateExporter se = new StateExporter(dbm);
		se.exportTo(dir);
		Assert.assertTrue(dir.listFiles().length == 5);
		DatabaseManager newDbm = createConnection(s);
		se = new StateExporter(newDbm);
		se.importFrom(dir, s);
		
		Assert.assertEquals(newDbm.getCachedFacts().size(),originalFacts.size());
		for (Atom f: newDbm.getCachedFacts()) {
			Assert.assertTrue(originalFacts.contains(f));
		}
		deleteDir(dir);
	}
	
	/** This test will attempt to export the whole 1Gb tpch database into csv files.
	 * @throws IOException
	 * @throws DatabaseException
	 */
	@Ignore //this test is too slow
	@Test
	public void exportTpcHDatabaseToFolderAndImportItToChaser() throws IOException, DatabaseException {
		try {
			File dir = new File("test/src/uk/ac/ox/cs/pdq/test/databasemanagement/tmp");
			if (dir.exists()) deleteDir(dir);
			dir.mkdir();
			File schmeXml = new File(dir,"../schema/tpchSchema.xml");
			//tpch schema
			Schema tpch = IOManager.importSchema(schmeXml);
			
			// read the tpch database, and save everything into the tmp folder.
			StateExporter.exportFromDatabaseToDirectory(dir, getTpcHParameters(), tpch);
			
			// create string only schema
			Schema tpchStrings = IOManager.convertTypesToString(tpch);
			
			// write csv files from tmp folder to chase
			DatabaseParameters dbParam = (DatabaseParameters) DatabaseParameters.Postgres.clone();
			dbParam.setNumberOfThreads(1);
			ExternalDatabaseManager dm = new ExternalDatabaseManager(dbParam);
			dm.initialiseDatabaseForSchema(tpchStrings);
			
			// validate the data exists in the chase database.
			List<String> ret = dm.execute(new Command("Select count(*) from lineitem"));
			Assert.assertTrue(ret!=null);
			Assert.assertTrue(!ret.isEmpty());
			Assert.assertEquals(ret.size(),1);
			Assert.assertTrue("6001215".equals(ret.get(0)));
		}catch(Throwable t) {
			t.printStackTrace();
			Assert.fail();
		}
	}
	
	public static DatabaseParameters getTpcHParameters() {
		DatabaseParameters dbParam = DatabaseParameters.Empty;
		dbParam.setConnectionUrl("jdbc:postgresql://localhost:5432/");
		dbParam.setDatabaseDriver("org.postgresql.Driver");
		dbParam.setDatabaseName("tpch");
		dbParam.setDatabaseUser("postgres");
		dbParam.setDatabasePassword("root");
		dbParam.setCreateNewDatabase(false);
		dbParam.setNumberOfThreads(10);
		return dbParam;
	}

	public static Properties getTpchProperties() {
		Properties properties = new Properties();
		properties.setProperty("url", "jdbc:postgresql://localhost:5432/");
		properties.setProperty("driver","org.postgresql.Driver");
		properties.setProperty("database", "tpch");
		properties.setProperty("username", "postgres");
		properties.setProperty("password", "root");
		return(properties);
	}

	
	private void deleteDir(File dir) {
		for (File f: dir.listFiles())
			f.delete();
		dir.delete();
	}

	private DatabaseManager createConnection(Schema s) {
		try {
			if (connection!=null) {
				connection.dropDatabase();
				connection.shutdown();
				connection = null;
			}
			InternalDatabaseManager dm = new InternalDatabaseManager();
			dm.initialiseDatabaseForSchema(s);
			this.connection = dm;
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}

	/**
	 * Shuting this test down.
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		if (connection!=null) {
			connection.dropDatabase();
			connection.shutdown();
			connection = null;
		}
	}

}
