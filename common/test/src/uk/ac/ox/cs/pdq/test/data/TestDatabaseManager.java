package uk.ac.ox.cs.pdq.test.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.data.DatabaseManager;
import uk.ac.ox.cs.pdq.data.PhysicalDatabaseInstance;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/** tests the creation and basic usages of a database.
 * @author Gabor
 *
 */
public class TestDatabaseManager extends PdqTest {
	
	@Test
	public void simpleDatabaseCreationDerby() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.Derby);
	}
	@Test
	public void simpleDatabaseCreationMySql() throws DatabaseException {
		simpleDatabaseCreation(DatabaseParameters.MySql);
	}

	private void simpleDatabaseCreation(DatabaseParameters parameters) throws DatabaseException {
		DatabaseManager manager = DatabaseManager.create(parameters);
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] {R,S,T}));
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(12),TypedConstant.create(13),TypedConstant.create(14) });
		List<Atom> facts = new ArrayList<>();
	//ADD
		facts.add(a1);
		manager.addFacts(facts);
		Collection<Atom> getFacts = manager.getCachedFacts();
		Assert.assertEquals(facts,getFacts);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.size(),getFacts.size());
	//DELETE	
		manager.deleteFacts(facts);
		getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0,getFacts.size());
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0,getFacts.size());
		manager.shutdown(true);
	}
	
	@Test
	public void virtualDatabaseCreationDerby() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		virtualDatabaseCreation(DatabaseParameters.MySql);
	}
	
	private void virtualDatabaseCreation(DatabaseParameters parameters) throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// create a simple manager
		DatabaseParameters p = (DatabaseParameters) parameters.clone();
		p.setProperty("database.isvirtual", Boolean.TRUE.toString());
		DatabaseManager manager = DatabaseManager.create(p);
		String instanceID1 = manager.getDatabaseInstanceID();
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] {R,S,T}));
		Atom[] facts = new Atom[] { Atom.create(this.R, new Term[] { TypedConstant.create(1),TypedConstant.create(10),TypedConstant.create(100) })};
		manager.addFacts(Arrays.asList(facts));
		//Assert.assertEquals(/*1*/0,manager.getCachedFacts().size());
		Assert.assertEquals(1,manager.getFactsFromPhysicalDatabase().size());
		
		// create a child of the previous manager, forming a virtual database instance over the original one.
		manager.setDatabaseInstanceID(instanceID1);
		manager.setDatabaseInstanceID(instanceID1+"B");
		String instanceID2 = manager.getDatabaseInstanceID();
		Assert.assertNotEquals(instanceID1, instanceID2);
		
		Assert.assertEquals(0,manager.getCachedFacts().size());
		Atom[] facts2 = new Atom[] { Atom.create(this.R, new Term[] { TypedConstant.create(2),TypedConstant.create(20),TypedConstant.create(200) })};
		manager.addFacts(Arrays.asList(facts2));
		Assert.assertEquals(1,manager.getCachedFacts().size());
		Assert.assertEquals(1,manager.getFactsFromPhysicalDatabase().size());
		
	// Normal query
		Atom a1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] {x,y,z}, a1);
		PhysicalQuery q = PhysicalQuery.create(manager, PhysicalDatabaseInstance.createProjectionMapping(this.R, cq), cq);
		List<Match> answer = manager.answerQueries(Arrays.asList(new PhysicalQuery[] {q}));
		Assert.assertEquals(1,answer.size());
		
//delete		
		manager.deleteFacts(Arrays.asList(facts2));
		Collection<Atom> getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0,getFacts.size());
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0,getFacts.size());
		manager.shutdown(true);
		
		// SQL query
//		SQLQuery q1 = new SQLQuery("select * from R1");
//		List<Match> answer2 = manager.answerQueries(Arrays.asList(new PhysicalQuery[] {q1}));
//		Assert.assertEquals(2,answer2.size());
	}
	
}
