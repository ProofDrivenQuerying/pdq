package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

/**
 * (4) Create the following unit tests for EGDchaseStep 
 * 
 * a. the facts of the chase instance are A(c_i,c_{i+1}), for i=1,...,1000 The input matches contain
 * the EGD B(x,y), B(x,y') -> y=y' and the i-th match in the input collection
 * contains the mapping {x=k1, y'=c_i, y=c_{i+1}} for i=1,...,1000 after you do
 * this operation, the database should contain only one A fact. Measure how much
 * time it takes to do this update
 * 
 * b. the facts of the chase instance are A(c_i,c_{i+1}), for i=1,...,1000 The
 * input matches contain the EGD B(x,y), B(x,y') -> y=y' and the i-th match in
 * the input collection contains the mapping {x=k1, y'=c_i, y=c_{i+1}} for
 * i=1,...,500 after you do this operation, the database should contain 501 A
 * facts. Measure how much time it takes to do this update
 * 
 * @author Gabor
 *
 */
public class TestEGDChaseStep {

	@Test
	public void testA_MySql() throws SQLException {
		testA("mysql");
	}

	@Test
	public void testA_derby() throws SQLException {
		testA("derby");
	}

	@Test
	public void testA_postgres() throws SQLException {
		testA("postgres");
	}

	@Test
	public void testB_MySql() throws SQLException {
		testB("mysql");
	}

	@Test
	public void testB_derby() throws SQLException {
		testB("derby");
	}

	@Test
	public void testB_postgres() throws SQLException {
		testB("postgres");
	}

	/**
	 * 
	 * a. the facts of the chase instance are A(c_i,c_{i+1}), for i=1,...,1000 The
	 * input matches contain the EGD B(x,y), B(x,y') -> y=y' and the i-th match in
	 * the input collection contains the mapping {x=k1, y'=c_i, y=c_{i+1}} for
	 * i=1,...,1000 after you do this operation, the database should contain only
	 * one A fact. Measure how much time it takes to do this update
	 * 
	 * @param sqlType
	 * @throws SQLException 
	 */
	public void testA(String sqlType) throws SQLException {
		Relation A = Relation.create("A", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation r[] = new Relation[] { A,B };
		Schema s = new Schema(r,new Dependency[0]);
		List<Atom> facts = new ArrayList<>();
		for (int i=2; i <= 1000; i++) facts.add(Atom.create(A, new Term[]{UntypedConstant.create("c_"+(i-1)),UntypedConstant.create("c_"+i)}));
		
		DatabaseChaseInstance state = new DatabaseChaseInstance(facts, getDatabaseConnection(sqlType,s));
		Dependency d[] = new Dependency[] {
				EGD.create(new Atom[]{ Atom.create(B, Variable.create("x"),Variable.create("y1")),
										Atom.create(B, Variable.create("x"), Variable.create("y2"))}, 
						new Atom[] {Atom.create(Predicate.create("EQUALITY", 2,true), Variable.create("y1"), Variable.create("y2"))})
				};
		long start = System.currentTimeMillis();
		Collection<Match> matches = new HashSet<>();
		for (int i = 1; i < 1000; i++)  {
			Map<Variable, Constant> mapping = new HashMap<>();
			//mapping.put(Variable.create("x"),UntypedConstant.create("k1"));
			mapping.put(Variable.create("y1"),UntypedConstant.create("c_"+i));
			mapping.put(Variable.create("y2"),UntypedConstant.create("c_"+(i+1)));
			matches.add(Match.create(d[0], mapping));
		}
		state.EGDchaseStep(matches );
		System.out.println("1000 equalities processed in : "+ (System.currentTimeMillis()-start)/1000.0 +" sec. Using: "+ sqlType);
		Set<Atom> facts2 = Sets.newHashSet(state.getFacts());
		Iterator<Atom> iterator2 = facts2.iterator();
		while(iterator2.hasNext()) {
			Atom fact = iterator2.next();
			if(fact.isEquality()) {
				iterator2.remove();
			}
		}
		Assert.assertEquals(1, facts2.size());
	}

	private DatabaseConnection getDatabaseConnection(String sqlType, Schema s) throws SQLException {
		if ("derby".equals(sqlType))
			return new DatabaseConnection(new DatabaseParameters(), s);
		if ("mysql".equals(sqlType)) {
			DatabaseParameters dbParam = new DatabaseParameters();
			dbParam.setConnectionUrl("jdbc:mysql://localhost/");
			dbParam.setDatabaseDriver("com.mysql.jdbc.Driver");
			dbParam.setDatabaseName("test_get_triggers");
			dbParam.setDatabaseUser("root");
			dbParam.setDatabasePassword("root");
			return new DatabaseConnection(dbParam , s);
		}
		if ("postgres".equals(sqlType)) {
			DatabaseParameters dbParam = new DatabaseParameters();
			dbParam.setConnectionUrl("jdbc:postgresql://localhost/");
			dbParam.setDatabaseDriver("org.postgresql.Driver");
			dbParam.setDatabaseName("test_get_triggers");
			dbParam.setDatabaseUser("postgres");
			dbParam.setDatabasePassword("root");
			return new DatabaseConnection(dbParam , s);
		}
		throw new IllegalArgumentException("SqlType " + sqlType + " is not valid.");
	}

	/**
	 * b. the facts of the chase instance are A(c_i,c_{i+1}), for i=1,...,1000 The
	 * input matches contain the EGD B(x,y), B(x,y') -> y=y' and the i-th match in
	 * the input collection contains the mapping {x=k1, y'=c_i, y=c_{i+1}} for
	 * i=1,...,500 after you do this operation, the database should contain 501 A
	 * facts. Measure how much time it takes to do this update
	 * 
	 * @param sqlType
	 * @throws SQLException 
	 */
	public void testB(String sqlType) throws SQLException {
		Relation A = Relation.create("A", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation r[] = new Relation[] { A,B };
		Schema s = new Schema(r,new Dependency[0]);
		List<Atom> facts = new ArrayList<>();
		for (int i=2; i <= 1000; i++) facts.add(Atom.create(A, new Term[]{UntypedConstant.create("c_"+(i-1)),UntypedConstant.create("c_"+i)}));
		
		DatabaseChaseInstance state = new DatabaseChaseInstance(facts, getDatabaseConnection(sqlType,s));
		Dependency d[] = new Dependency[] {
				EGD.create(new Atom[]{ Atom.create(B, Variable.create("x"),Variable.create("y1")),
										Atom.create(B, Variable.create("x"), Variable.create("y2"))}, 
						new Atom[] {Atom.create(Predicate.create("EQUALITY", 2,true), Variable.create("y1"), Variable.create("y2"))})
				};
		long start = System.currentTimeMillis();
		Collection<Match> matches = new HashSet<>();
		for (int i = 1; i < 500; i++)  {
			Map<Variable, Constant> mapping = new HashMap<>();
			mapping.put(Variable.create("x"),UntypedConstant.create("k1"));
			mapping.put(Variable.create("y1"),UntypedConstant.create("c_"+i));
			mapping.put(Variable.create("y2"),UntypedConstant.create("c_"+(i+1)));
			matches.add(Match.create(d[0], mapping));
		}
		state.EGDchaseStep(matches );
		System.out.println("500 equalities processed in : "+ (System.currentTimeMillis()-start)/1000.0 +" sec. Using: "+ sqlType);
		Set<Atom> facts2 = Sets.newHashSet(state.getFacts());
		Iterator<Atom> iterator2 = facts2.iterator();
		while(iterator2.hasNext()) {
			Atom fact = iterator2.next();
			if(fact.isEquality()) {
				iterator2.remove();
			}
		}
		Assert.assertEquals(501, facts2.size());
	}
}
