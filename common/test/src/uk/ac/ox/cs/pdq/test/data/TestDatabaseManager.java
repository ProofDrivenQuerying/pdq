package uk.ac.ox.cs.pdq.test.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import org.junit.Assert;
import uk.ac.ox.cs.pdq.data.DatabaseManager;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.data.sql.SQLQuery;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
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
		DatabaseManager manager = new DatabaseManager(parameters);
		manager.initialiseDatabaseForSchema(testSchema1);
		Atom a1 = Atom.create(this.rel1, new Term[] { TypedConstant.create("test1"),TypedConstant.create("test2"),TypedConstant.create("test3") });
		List<Atom> facts = new ArrayList<>();
		facts.add(a1);
		manager.addFacts(facts);
		Collection<Atom> getFacts = manager.getFacts();
		Assert.assertEquals(facts,getFacts);
		manager.deleteFacts(facts);
		getFacts = manager.getFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0,getFacts.size());
		manager.closeConnections(true);
	}
	
	@Test
	public void virtualDatabaseCreationDerby() throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		virtualDatabaseCreation(DatabaseParameters.Derby);
	}
	
	private void virtualDatabaseCreation(DatabaseParameters parameters) throws DatabaseException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// create a simple manager
		DatabaseManager rootManager = new DatabaseManager(parameters);
		rootManager.initialiseDatabaseForSchema(testSchema1);
		Atom[] facts = new Atom[] { Atom.create(this.rel1, new Term[] { TypedConstant.create("test1"),TypedConstant.create("test2"),TypedConstant.create("test3") })};
		rootManager.addFacts(Arrays.asList(facts));
		Assert.assertEquals(/*1*/0,rootManager.getCachedFacts().size());
		Assert.assertEquals(/*1*/0,rootManager.getFactsFromPhysicalDatabase().size());
		
		// create a child of the previous manager, forming a virtual database instance over the original one.
		DatabaseManager manager2 = new DatabaseManager(rootManager,"ChaseStep2");
		Assert.assertEquals(0,manager2.getFacts().size());
		Atom[] facts2 = new Atom[] { Atom.create(this.rel1, new Term[] { TypedConstant.create("test1B"),TypedConstant.create("test2B"),TypedConstant.create("test3B") })};
		manager2.addFacts(Arrays.asList(facts2));
		Assert.assertEquals(/*1*/0,manager2.getCachedFacts().size());
		Assert.assertEquals(/*1*/0,manager2.getFactsFromPhysicalDatabase().size());
		
		// Normal query
		PhysicalQuery q = PhysicalQuery.create(manager2, ConjunctiveQuery.create(new Variable[] {x,y,z}, a1));
		List<Match> answer = manager2.answerQueries(Arrays.asList(new PhysicalQuery[] {q}));
		Assert.assertEquals(1,answer.size());
		
		// SQL query
		SQLQuery q1 = new SQLQuery("select * from R1");
		List<Match> answer2 = rootManager.answerQueries(Arrays.asList(new PhysicalQuery[] {q1}));
		Assert.assertEquals(2,answer2.size());
	}
	
}
