package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import org.junit.Assert;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitToThisOrAllInstances;
import uk.ac.ox.cs.pdq.reasoning.chase.state.TriggerProperty;

/**
 * Create unit tests for the getTriggers method
 * 
 * 
 *  a. The dependency is A(x,y), B(y,y,'TypedConstant1'), C(y) -> D('TypedConstant2', z) 
 *  the chase instance has the facts 
 *  A(c_1,c_2) 
 *  A(c_2,c_2) 
 *  A(c_2,c_3) 
 *  A(c_3,c_3) 
 *  A(c_4,c_5) 
 *  
 *  B(c_2, c_2, 'TypedConstant1') 
 *  B(c_2, c_3, 'TypedConstant1')
 *  B(c_3, c_3, 'TypedConstant1')
 *  B(c_3, c_3, 'TypedConstant2')
 *  B(c_4, c_5, 'TypedConstant2')
 *  B(c_i, c_{i+1}, 'TypedConstant2') i=6,...,10000 
 *  C(c_i), i=1,...,10000
 *  
 *  You should assert that there are returned four matches in total.
 *  General guidlines:
 *  Please try all the unit tests for all database instances.
 *  And please do not load any facts or dependencies for a csv file.
 *  Just create them in place. Maybe using a for loop.
 * 
 * @author Gabor
 *
 */
public class TestGetTriggers {

	
	@Test
	public void testDerby() throws SQLException {
		test(new DatabaseConnection(new DatabaseParameters(), createSchema()));
	}
	@Test
	public void testMySql() throws SQLException {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:mysql://localhost/");
		dbParam.setDatabaseDriver("com.mysql.jdbc.Driver");
		dbParam.setDatabaseName("test_get_triggers");
		dbParam.setDatabaseUser("root");
		dbParam.setDatabasePassword("root");
		test(new DatabaseConnection(dbParam , createSchema()));
	}
	@Test
	public void testPostgres() throws SQLException {
		DatabaseParameters dbParam = new DatabaseParameters();
		dbParam.setConnectionUrl("jdbc:postgresql://localhost/");
		dbParam.setDatabaseDriver("org.postgresql.Driver");
		dbParam.setDatabaseName("test_get_triggers");
		dbParam.setDatabaseUser("postgres");
		dbParam.setDatabasePassword("root");
		test(new DatabaseConnection(dbParam , createSchema()));
	}
	private Schema createSchema() {
		Relation A = Relation.create("A", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(String.class, "attribute2"), Attribute.create(Integer.class, "InstanceID")});
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(Integer.class, "InstanceID")});
		Relation D = Relation.create("D", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID") });
		Relation r[] = new Relation[] { A,B,C,D };
		Schema s = new Schema(r,new Dependency[0]);
		return s;
	}
	
	public void test(DatabaseConnection dc) {
		try {
			Relation A = dc.getSchema().getRelation("A");
			Relation B = dc.getSchema().getRelation("B");
			Relation C = dc.getSchema().getRelation("C");
			Relation D = dc.getSchema().getRelation("D");
			List<Atom> facts = new ArrayList<>();
			for (int i=2; i <= 5; i++) facts.add(Atom.create(A, new Term[]{TypedConstant.create("a_"+(i-1)),TypedConstant.create("a_"+i)}));
			facts.add(Atom.create(B, new Term[]{TypedConstant.create("c_2"),TypedConstant.create("c_2"),TypedConstant.create("TC1")}));
			facts.add(Atom.create(B, new Term[]{TypedConstant.create("c_2"),TypedConstant.create("c_3"),TypedConstant.create("TC1")}));
			facts.add(Atom.create(B, new Term[]{TypedConstant.create("c_3"),TypedConstant.create("c_3"),TypedConstant.create("TC1")}));
			facts.add(Atom.create(B, new Term[]{TypedConstant.create("c_3"),TypedConstant.create("c_3"),TypedConstant.create("TC2")}));
			facts.add(Atom.create(B, new Term[]{TypedConstant.create("c_4"),TypedConstant.create("c_5"),TypedConstant.create("TC2")}));
			for (int i=6; i <= 100; i++) facts.add(Atom.create(B, new Term[]{TypedConstant.create("b_"+i),TypedConstant.create("b_"+(i+1)),TypedConstant.create("TC2")}));
			for (int i=1; i <= 100; i++) facts.add(Atom.create(C, new Term[]{TypedConstant.create("c_"+i)}));
			
			DatabaseChaseInstance state = new DatabaseChaseInstance(facts, dc);
			 // A(x,y), B(y,y,'TypedConstant1'), C(y) -> D('TypedConstant2', z) 
			Dependency d[] = new Dependency[] {
					TGD.create(new Atom[]{ Atom.create(A, Variable.create("x"),Variable.create("y")),
											Atom.create(B, Variable.create("y"), Variable.create("y"), TypedConstant.create("TC1")),
											Atom.create(C, Variable.create("y")) }, 
							new Atom[] {Atom.create(D, TypedConstant.create("TC2"), Variable.create("z"))})
					};
			System.out.println("Initial facts:");
			Set<Atom> newfacts = Sets.newHashSet(state.getFacts());
			Iterator<Atom> iterator = newfacts.iterator();
			while(iterator.hasNext()) {
				Atom fact = iterator.next();
				System.out.println(fact);
			}
			
			System.out.println("\n\nmatches for dependency: " + d[0]);
			
			List<Match> matches = state.getTriggers(d, TriggerProperty.ACTIVE, LimitToThisOrAllInstances.THIS);
			Assert.assertFalse(matches.isEmpty());
			iterator = newfacts.iterator();
			List<String> set = new ArrayList<>();
			for(Match m:matches) {
				set.add(m.toString());
			}
			Collections.sort(set, String.CASE_INSENSITIVE_ORDER);
			for(String line:set) System.out.println(line);
			System.out.println("TestGetTriggers finished.");
			 
			 
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
