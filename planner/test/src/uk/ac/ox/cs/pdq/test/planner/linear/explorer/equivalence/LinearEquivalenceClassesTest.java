package uk.ac.ox.cs.pdq.test.planner.linear.explorer.equivalence;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.InternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.planner.PlannerException;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.linear.LinearChaseConfiguration;
import uk.ac.ox.cs.pdq.planner.linear.explorer.LinearConfigurationNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.SearchNode;
import uk.ac.ox.cs.pdq.planner.linear.explorer.equivalence.LinearEquivalenceClasses;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleChaseInstance;
import uk.ac.ox.cs.pdq.planner.reasoning.chase.accessiblestate.AccessibleDatabaseChaseInstance;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the linear equality class. Adding nodes with different accessible facts
 * should belong to different class, but if a node has the same accessible facts
 * it should belong to the same class.
 * 
 * @author gabor
 *
 */
public class LinearEquivalenceClassesTest extends PdqTest {

	public LinearEquivalenceClassesTest() {
	}

	@Test
	public void testAddition() throws SQLException, PlannerException, DatabaseException {
		LinearEquivalenceClasses eq = new LinearEquivalenceClasses();
		Collection<Atom> facts = new ArrayList<>();

		SearchNode n1 = getNode(facts);
		eq.add(n1);
		facts.add(Atom.create(R, new Term[] { c1, c2, c3 }));
		SearchNode n2 = getNode(facts);
		eq.add(n2);
		facts.add(Atom.create(R, new Term[] { c2, c3, c4 }));
		SearchNode n3 = getNode(facts);
		eq.add(n3);
		Assert.assertEquals(1, eq.getEquivalenceClass(n1).size());
		Assert.assertEquals(1, eq.getEquivalenceClass(n2).size());
		Assert.assertEquals(1, eq.getEquivalenceClass(n3).size());
		SearchNode n4 = getNode(new ArrayList<>());
		Assert.assertEquals(n1, eq.add(n4));
	}

	private SearchNode getNode(Collection<Atom> facts) throws DatabaseException, SQLException, PlannerException {
		DatabaseManager connection = new InternalDatabaseManager();
		AccessibleChaseInstance state = new AccessibleDatabaseChaseInstance(facts, connection, false);
		AccessibilityAxiom ax = new AccessibilityAxiom(R, method0);
		state.generate(ax, facts);
		LinearChaseConfiguration config = new LinearChaseConfiguration(state);
		SearchNode node = new LinearConfigurationNode(config);
		return node;
	}
}
