package uk.ac.ox.cs.pdq.test.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.data.DatabaseManager;
import uk.ac.ox.cs.pdq.data.PhysicalQuery;
import uk.ac.ox.cs.pdq.data.sql.DatabaseException;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * tests the creation and basic usages of a database.
 * 
 * @author Gabor
 *
 */
public class TestDatabaseManagerWithLargeTables extends PdqTest {

	@Test
	public void largeTableTestDerby() throws DatabaseException {
		largeTableTest(DatabaseParameters.Derby);
	}

	@Test
	public void largeTableTestMySql() throws DatabaseException {
		largeTableTest(DatabaseParameters.MySql);
	}

	@Test
	public void largeTableTestPostgres() throws DatabaseException {
		largeTableTest(DatabaseParameters.Postgres);
	}

	@Test
	public void simpleDatabaseCreatioMemory() throws DatabaseException {
		largeTableTest(DatabaseParameters.Memory);
	}

	private void largeTableTest(DatabaseParameters parameters) throws DatabaseException {
		DatabaseManager manager = DatabaseManager.create(parameters);
		manager.initialiseDatabaseForSchema(new Schema(new Relation[] { R, S, T }));
		List<Atom> facts = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(10000 + i), TypedConstant.create(20000 + i), TypedConstant.create(30000 + i) });
			Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(40000 + i), TypedConstant.create(50000 + i)});
			Atom c1 = Atom.create(this.T, new Term[] { TypedConstant.create(70000 + i), TypedConstant.create(80000 + i), TypedConstant.create(90000 + i) });
			facts.add(a1);
			facts.add(b1);
			facts.add(c1);
		}
		Atom a1 = Atom.create(this.R, new Term[] { TypedConstant.create(13), TypedConstant.create(14), TypedConstant.create(15) });
		Atom b1 = Atom.create(this.S, new Term[] { TypedConstant.create(13), TypedConstant.create(14) });
		facts.add(a1);
		facts.add(b1);
		// ADD
		manager.addFacts(facts);
		Collection<Atom> getFacts = manager.getCachedFacts();
		Set<Atom> expectedSet = new HashSet<>();
		expectedSet.addAll(facts);
		Set<Atom> readSet = new HashSet<>();
		readSet.addAll(getFacts);
		Assert.assertEquals(expectedSet, readSet);
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertEquals(facts.size(), getFacts.size());
		
		//QUERY
		Atom q1 = Atom.create(this.R, new Term[] { Variable.create("x"), Variable.create("y"),Variable.create("z") });
		Atom q2 = Atom.create(this.S, new Term[] { Variable.create("x"),Variable.create("y") });
		ConjunctiveQuery cq = ConjunctiveQuery.create(new Variable[] { z }, Conjunction.create(q1,q2));
		PhysicalQuery q = PhysicalQuery.create(manager, cq);
		List<Match> answer = manager.answerQueries(Arrays.asList(new PhysicalQuery[] { q }));
		Assert.assertEquals(1, answer.size());
		//Assert.assertEquals(1,answer.get(0).getMapping().size());
		Assert.assertTrue(answer.get(0).getMapping().containsKey(Variable.create("z")));
		if (parameters.getDatabaseDriver().contains("memory")) {
			Assert.assertEquals(TypedConstant.create(15), answer.get(0).getMapping().get(Variable.create("z")));
		} else {
			Assert.assertEquals(UntypedConstant.create("15"), answer.get(0).getMapping().get(Variable.create("z")));
		}
		
		
		// DELETE
		manager.deleteFacts(facts);
		getFacts = manager.getCachedFacts();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		getFacts = manager.getFactsFromPhysicalDatabase();
		Assert.assertNotNull(getFacts);
		Assert.assertEquals(0, getFacts.size());
		manager.shutdown(true);
	}
}
