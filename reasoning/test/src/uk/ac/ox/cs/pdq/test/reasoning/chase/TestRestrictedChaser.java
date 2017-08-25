package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance.LimitToThisOrAllInstances;

/**
 * Tests the reasonUntilTermination method of the RestrictedChaser class 
 * @author Efthymia Tsamoura
 *
 */
public class TestRestrictedChaser {

	public DatabaseChaseInstance state;
	private RestrictedChaser chaser;

	private Relation rel1;
	private Relation rel2;

	private TGD tgd;
	private EGD egd;

	protected Schema schema;
	private DatabaseConnection connection;

	@Before
	public void setup() throws SQLException {
		Attribute fact= Attribute.create(Integer.class, "InstanceID");
		
		Attribute at11 = Attribute.create(String.class, "at11");
		Attribute at12 = Attribute.create(String.class, "at12");
		Attribute at13 = Attribute.create(String.class, "at13");
		
		this.rel1 = Relation.create("R1", new Attribute[]{at11, at12, at13, fact});

		Attribute at21 = Attribute.create(String.class, "at21");
		Attribute at22 = Attribute.create(String.class, "at22");
		this.rel2 = Relation.create("R2", new Attribute[]{at21, at22, fact});

		Atom R1 = Atom.create(this.rel1, new Term[]{Variable.create("x"),Variable.create("y"),Variable.create("z")});
		Atom R2 = Atom.create(this.rel2, new Term[]{Variable.create("y"),Variable.create("z")});
		Atom R2p = Atom.create(this.rel2, new Term[]{Variable.create("y"),Variable.create("w")});

		this.tgd = TGD.create(new Atom[]{R1},new Atom[]{R2});
		this.egd = EGD.create(new Atom[]{R2,R2p}, new Atom[]{Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), 
				Variable.create("z"),Variable.create("w"))});

		this.schema = new Schema(new Relation[]{this.rel1, this.rel2}, new Dependency[]{this.tgd});
		this.schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(new String("John"))));

		this.setConnection(new DatabaseConnection(new DatabaseParameters(), this.schema));
		this.chaser = new RestrictedChaser(new StatisticsCollector(true, new EventBus()));
	}
	public void createSchema() {
		Attribute fact= Attribute.create(Integer.class, "InstanceID");
		
		Attribute at11 = Attribute.create(String.class, "at11");
		Attribute at12 = Attribute.create(String.class, "at12");
		Attribute at13 = Attribute.create(String.class, "at13");
		
		this.rel1 = Relation.create("R1", new Attribute[]{at11, at12, at13, fact});

		Attribute at21 = Attribute.create(String.class, "at21");
		Attribute at22 = Attribute.create(String.class, "at22");
		this.rel2 = Relation.create("R2", new Attribute[]{at21, at22, fact});

		Atom R1 = Atom.create(this.rel1, new Term[]{Variable.create("x"),Variable.create("y"),Variable.create("z")});
		Atom R2 = Atom.create(this.rel2, new Term[]{Variable.create("y"),Variable.create("z")});
		Atom R2p = Atom.create(this.rel2, new Term[]{Variable.create("y"),Variable.create("w")});

		this.tgd = TGD.create(new Atom[]{R1},new Atom[]{R2});
		this.egd = EGD.create(new Atom[]{R2,R2p}, new Atom[]{Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), 
				Variable.create("z"),Variable.create("w"))});

		this.schema = new Schema(new Relation[]{this.rel1, this.rel2}, new Dependency[]{this.tgd});
		this.schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(new String("John"))));
	}
	
	public void setup(DatabaseConnection c) throws SQLException {

		this.setConnection(c);
		this.chaser = new RestrictedChaser(new StatisticsCollector(true, new EventBus()));
	}
	

	@Test 
	public void test_reasonUntilTermination1() {
		Atom f20 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k1"), UntypedConstant.create("c"),UntypedConstant.create("c1")});

		Atom f21 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k2"), UntypedConstant.create("c"),UntypedConstant.create("c2")});

		Atom f22 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k3"), UntypedConstant.create("c"),UntypedConstant.create("c3")});

		Atom f23 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k4"), UntypedConstant.create("c"),UntypedConstant.create("c4")});

		Atom f24 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k5"), UntypedConstant.create("c"),TypedConstant.create(new String("John"))});

		try {
			this.state = new DatabaseChaseInstance(Sets.<Atom>newHashSet(f20,f21,f22,f23,f24), this.getConnection());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		this.chaser.reasonUntilTermination(this.state, new Dependency[]{this.tgd,this.egd});
		Assert.assertEquals(false, this.state.isFailed());

		Atom n00 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k5"), UntypedConstant.create("c"),TypedConstant.create(new String("John"))});

		Atom n01 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k4"), UntypedConstant.create("c"),TypedConstant.create(new String("John"))});

		Atom n02 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k3"), UntypedConstant.create("c"),TypedConstant.create(new String("John"))});

		Atom n03 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k1"), UntypedConstant.create("c"),TypedConstant.create(new String("John"))});

		Atom n04 = Atom.create(this.rel1, 
				new Term[]{UntypedConstant.create("k2"), UntypedConstant.create("c"),TypedConstant.create(new String("John"))});

		Atom n1 = Atom.create(this.rel2, 
				new Term[]{UntypedConstant.create("c"), TypedConstant.create(new String("John"))});

		Set<Atom> facts = Sets.newHashSet(this.state.getFacts());
		Iterator<Atom> iterator = facts.iterator();
		while(iterator.hasNext()) {
			Atom fact = iterator.next();
			if(fact.isEquality()) {
				iterator.remove();
			}
		}

		Assert.assertEquals(Sets.newHashSet(n00,n01,n02,n03,n04,n1), facts);
	}
	
	@Test
	public void efiTests0() {
// Dependencies:		
//		R(z, x) → S(x, y1) ∧ T (x, y2)
//		R(x,y1) ∧ S(x,y2)→y1 = y2
//
//		facts of the chase instance:
//		R(a_{i−1}, a_i) for 1 ≤ i ≤ 10,
//
//		You should assert that the chaser should produce
//		nine equality classes each one having representatives a_2, ... a_10
//		EQUALITY(a_2,k1), 
//		EQUALITY(a_3,k3), 
//		EQUALITY(a_4,k5), 
//		EQUALITY(a_5,k7), 
//		EQUALITY(a_6,k9), 
//		EQUALITY(a_7,k11), 
//		EQUALITY(a_8,k13), 
//		EQUALITY(a_9,k15), 
//		EQUALITY(a_10,k17), 
//
//		and the facts 
//		S(a_1,a_2), T(a_1,k2), 
//		S(a_2,a_3), T(a_2,k4), 
//		S(a_3,a_4), T(a_3,k6), 
//		S(a_4,a_5), T(a_4,k8), 
//		S(a_5,a_6), T(a_5,k10), 
//		S(a_6,a_7), T(a_6,k12), 
//		S(a_7,a_8), T(a_7,k14), 
//		S(a_8,a_9), T(a_8,k16), 
//		S(a_9,a_10), T(a_9,k18), 
//		S(a_10,k19), T(a_10,k20)]
		
//		Please define the dependencies and the input facts inside the test class and do
//		not load them from external files.		
		Relation R = Relation.create("R", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID") });
		Relation S = Relation.create("S", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID") });
		Relation T = Relation.create("T", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID") });
		Relation r[] = new Relation[] { R, S, T };
		Dependency d[] = new Dependency[] {
				TGD.create(new Atom[] {Atom.create(R, Variable.create("z"),Variable.create("x"))}, new Atom[] {Atom.create(S, Variable.create("x"),Variable.create("y1")),Atom.create(T, Variable.create("x"),Variable.create("y2"))}),
				EGD.create(new Atom[] {Atom.create(R, Variable.create("x"),Variable.create("y1")),Atom.create(S, Variable.create("x"),Variable.create("y2"))}, new Atom[]{Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("y1"),Variable.create("y2"))}),
		};
		Schema s = new Schema(r,d);
		List<Atom> facts = new ArrayList<>();
		for (int i=1; i <=10; i++) facts.add(Atom.create(R, new Term[]{TypedConstant.create("a_"+(i-1)),TypedConstant.create("a_"+i)}));
		try {
			this.state = new DatabaseChaseInstance(facts, new DatabaseConnection(new DatabaseParameters(), s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Schema:" + s);
		Set<Atom> newfacts = Sets.newLinkedHashSet(this.state.getFacts());
		Iterator<Atom> iterator = newfacts.iterator();
		System.out.println("Initial facts:");
		while(iterator.hasNext()) {
			Atom fact = iterator.next();
			System.out.println(fact);
		}
		this.chaser.reasonUntilTermination(this.state, d);
		System.out.println("\n\nAfter resoning:");
		
		newfacts = Sets.newHashSet(this.state.getFacts());
		iterator = newfacts.iterator();
		List<String> set = new ArrayList<>();
		while(iterator.hasNext()) {
			Atom fact = iterator.next();
			set.add(fact.toString());
		}
		Collections.sort(set, String.CASE_INSENSITIVE_ORDER);
		for(String line:set) System.out.println(line);
		System.out.println("TestRestrictedChaser.efiTests1() finished.");
	}
	
	@Test
	public void efiTests1() {
// Dependencies:		
//		C(x) ∧ D(x) → Q(x)
//		S(x, y) ∧ D(x) → D(y)
//		R(z, x) → S(x, y1) ∧ T (x, y2)
//		R(x,y1) ∧ S(x,y2)→y1 = y2
//		R(x,y1) ∧ T(x,y2)→y1 = y2
//
//		facts of the chase instance:
//		R(a_{i−1}, a_i) for 1 ≤ i ≤ 10,
//		D(a_1)
//		C(a_5)
//
//		You should assert that the chaser should produces
//		the facts D(a_i) for 1≤i≤10
//		and the fact Q(a_5).
//
//		Please define the dependencies and the input facts inside the test class and do
//		not load them from external files.		
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute"),Attribute.create(Integer.class, "InstanceID")});
		Relation D = Relation.create("D", new Attribute[] { Attribute.create(String.class, "attribute"),Attribute.create(Integer.class, "InstanceID") });
		Relation Q = Relation.create("Q", new Attribute[] { Attribute.create(String.class, "attribute"),Attribute.create(Integer.class, "InstanceID") });
		Relation R = Relation.create("R", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID") });
		Relation S = Relation.create("S", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID") });
		Relation T = Relation.create("T", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID") });
		Relation r[] = new Relation[] { C, D, Q, R, S, T };
		Dependency d[] = new Dependency[] {
				TGD.create(new Atom[] {Atom.create(C, Variable.create("x")),Atom.create(D, Variable.create("x"))}, new Atom[] {Atom.create(Q, Variable.create("x"))}),
				TGD.create(new Atom[] {Atom.create(S, Variable.create("x"),Variable.create("y")),Atom.create(D, Variable.create("x"))}, new Atom[] {Atom.create(D, Variable.create("y"))}),
				TGD.create(new Atom[] {Atom.create(R, Variable.create("z"),Variable.create("x"))}, new Atom[] {Atom.create(S, Variable.create("x"),Variable.create("y1")),Atom.create(T, Variable.create("x"),Variable.create("y2"))}),
				EGD.create(new Atom[] {Atom.create(R, Variable.create("x"),Variable.create("y1")),Atom.create(S, Variable.create("x"),Variable.create("y2"))}, new Atom[]{Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("y1"),Variable.create("y2"))}),
				EGD.create(new Atom[] {Atom.create(R, Variable.create("x"),Variable.create("y1")),Atom.create(T, Variable.create("x"),Variable.create("y2"))}, new Atom[]{Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("y1"),Variable.create("y2"))}),
		};
		Schema s = new Schema(r,d);
		List<Atom> facts = new ArrayList<>();
		facts.add(Atom.create(D, new Term[]{TypedConstant.create("a_1")}));
		facts.add(Atom.create(C, new Term[]{TypedConstant.create("a_5")}));
		for (int i=1; i <=10; i++) facts.add(Atom.create(R, new Term[]{TypedConstant.create("a_"+(i-1)),TypedConstant.create("a_"+i)}));
		try {
			this.state = new DatabaseChaseInstance(facts, new DatabaseConnection(new DatabaseParameters(), s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Schema:" + s);
		Set<Atom> newfacts = Sets.newHashSet(this.state.getFacts());
		Iterator<Atom> iterator = newfacts.iterator();
		System.out.println("Initial facts:");
		while(iterator.hasNext()) {
			Atom fact = iterator.next();
			System.out.println(fact);
		}
		this.chaser.reasonUntilTermination(this.state, d);
		System.out.println("\n\nAfter resoning:");
		
		newfacts = Sets.newHashSet(this.state.getFacts());
		iterator = newfacts.iterator();
		List<String> set = new ArrayList<>();
		while(iterator.hasNext()) {
			Atom fact = iterator.next();
			set.add(fact.toString());
		}
		Collections.sort(set, String.CASE_INSENSITIVE_ORDER);
		for(String line:set) System.out.println(line);
		System.out.println("TestRestrictedChaser.efiTests1() finished.");
	}
	
	@Test
	public void efiTests2a() {
//Create the following unit tests for getMatches
		
//a. conjunctive query is Q(x,y) = A(x,x), B(x,y), C(y,z,'TypedConstant1') D(z,z)
//
//in each unit test create facts in the database such that two conditions hold:
//(i) each relation has 10000 facts in the database and (ii) there are only five
//answer tuples in the answer. You could do this by adding junk tuples and
//manually creating tuples that participate in the match.
	
		Relation A = Relation.create("A", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation D = Relation.create("D", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"), Attribute.create(String.class, "attribute2"),Attribute.create(Integer.class, "InstanceID") });
		Relation E = Relation.create("E", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"), Attribute.create(String.class, "attribute2"),Attribute.create(Integer.class, "InstanceID") });
		Relation r[] = new Relation[] { A,B,C,D,E };
		Schema s = new Schema(r,new Dependency[0]);
		List<Atom> facts = new ArrayList<>();
		final int MAX_NUMBER_OF_TUPLES = 10000;
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(A, new Term[]{UntypedConstant.create("a_"+(i-1)),UntypedConstant.create("a_"+i)}));
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(B, new Term[]{UntypedConstant.create("b_"+(i-1)),UntypedConstant.create("b_"+i)}));
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(D, new Term[]{UntypedConstant.create("d_"+(i-1)),UntypedConstant.create("d_"+i)}));
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(C, new Term[]{UntypedConstant.create("c_1_"+(i-1)),UntypedConstant.create("c_2_"+(i-1)),UntypedConstant.create("c_3_"+i)}));
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(E, new Term[]{UntypedConstant.create("e_1_"+(i-1)),UntypedConstant.create("e_2_"+(i-1)),UntypedConstant.create("e_3_"+i)}));
		
		for (int i=1; i <=5; i++) facts.add(Atom.create(A, new Term[]{TypedConstant.create("x"+i),TypedConstant.create("x"+i)}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(B, new Term[]{TypedConstant.create("x"+i),TypedConstant.create("y"+i)}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(C, new Term[]{TypedConstant.create("y"+i),TypedConstant.create("z"+i),TypedConstant.create("c_constant_3")}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(D, new Term[]{TypedConstant.create("z"+i),TypedConstant.create("z"+i)}));
		//for (int i=1; i <=5; i++) facts.add(Atom.create(E, new Term[]{TypedConstant.create("e_constant_1_"+(i-1)),TypedConstant.create("e_constant_2_"+(i-1)),UntypedConstant.create("e_constant_3_"+i)}));
		
		try {
			this.state = new DatabaseChaseInstance(facts, new DatabaseConnection(new DatabaseParameters(), s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		ConjunctiveQuery query1; // Q(x,y) = A(x,x), B(x,y), C(y,z,'TypedConstant1') D(z,z)
		query1 = ConjunctiveQuery.create(new Variable[] {Variable.create("x"),Variable.create("y")}, (Conjunction)Conjunction.of(
				Atom.create(A, Variable.create("x"),Variable.create("x")),
				Atom.create(B, Variable.create("x"),Variable.create("y")),
				Atom.create(C, Variable.create("y"),Variable.create("z"), TypedConstant.create("c_constant_3")),
				Atom.create(D, Variable.create("z"),Variable.create("z"))
				));
		
		List<Match> matches = this.state.getMatches(query1, LimitToThisOrAllInstances.THIS);
//		System.out.println("Facts:");
//		System.out.println(state.getFacts());
//		System.out.println("\\Facts");
		System.out.println(matches);
		System.out.println("\n\n\n");
	}
	
	@Test
	public void efiTests2b() {
//Create the following unit tests for getMatches
//b. conjunctive query is Q(x,y,z) = A('TypedConstant2',y,z,w), B(x,y,z,w),
//C(y,z,'TypedConstant1') D(x,y), E(x,y,'TypedConstant1')
//
//in each unit test create facts in the database such that two conditions hold:
//(i) each relation has 10000 facts in the database and (ii) there are only five
//answer tuples in the answer. You could do this by adding junk tuples and
//manually creating tuples that participate in the match.
//In the last unit test the query is boolean so you should only get true
		Relation A = Relation.create("A", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(String.class, "attribute2"),Attribute.create(String.class, "attribute3"),Attribute.create(Integer.class, "InstanceID")});
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(String.class, "attribute2"),Attribute.create(String.class, "attribute3"),Attribute.create(Integer.class, "InstanceID")});
		Relation D = Relation.create("D", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"), Attribute.create(String.class, "attribute2"),Attribute.create(Integer.class, "InstanceID") });
		Relation E = Relation.create("E", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"), Attribute.create(String.class, "attribute2"),Attribute.create(Integer.class, "InstanceID") });
		Relation r[] = new Relation[] { A,B,C,D,E };
		Schema s = new Schema(r,new Dependency[0]);
		List<Atom> facts = new ArrayList<>();
		final int MAX_NUMBER_OF_TUPLES = 10000;
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(A, new Term[]{UntypedConstant.create("a_"+(i-1)),UntypedConstant.create("a_"+i)}));
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(B, new Term[]{UntypedConstant.create("b_"+(i-1)),UntypedConstant.create("b_"+i)}));
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(D, new Term[]{UntypedConstant.create("d_"+(i-1)),UntypedConstant.create("d_"+i)}));
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(C, new Term[]{UntypedConstant.create("c_1_"+(i-1)),UntypedConstant.create("c_2_"+(i-1)),UntypedConstant.create("c_3_"+i)}));
//		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(E, new Term[]{UntypedConstant.create("e_1_"+(i-1)),UntypedConstant.create("e_2_"+(i-1)),UntypedConstant.create("e_3_"+i)}));
		
		for (int i=1; i <=5; i++) facts.add(Atom.create(A, new Term[]{TypedConstant.create("TC2"),TypedConstant.create("y"+i),TypedConstant.create("z"+i),TypedConstant.create("w"+i)}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(B, new Term[]{TypedConstant.create("x"+i),TypedConstant.create("y"+i),TypedConstant.create("z"+i),TypedConstant.create("w"+i)}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(C, new Term[]{TypedConstant.create("y"+i),TypedConstant.create("z"+i),TypedConstant.create("TC1")}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(D, new Term[]{TypedConstant.create("x"+i),TypedConstant.create("y"+i)}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(E, new Term[]{TypedConstant.create("x"+i),TypedConstant.create("y"+i),TypedConstant.create("TC1")}));
		
		try {
			this.state = new DatabaseChaseInstance(facts, new DatabaseConnection(new DatabaseParameters(), s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		ConjunctiveQuery query1;  // Q(x,y,z) = A('TypedConstant2',y,z,w), B(x,y,z,w), C(y,z,'TypedConstant1') D(x,y), E(x,y,'TypedConstant1')
		query1 = ConjunctiveQuery.create(new Variable[] {Variable.create("x"),Variable.create("y"),Variable.create("z")}, (Conjunction)Conjunction.of(
				Atom.create(A, TypedConstant.create("TC2"),Variable.create("y"),Variable.create("z"),Variable.create("w")),
				Atom.create(B, Variable.create("x"),Variable.create("y"), Variable.create("z"),Variable.create("w")),
				Atom.create(C, Variable.create("y"),Variable.create("z"), TypedConstant.create("TC1")),
				Atom.create(D, Variable.create("x"),Variable.create("y")),
				Atom.create(C, Variable.create("x"),Variable.create("y"), TypedConstant.create("TC1"))
				));
		
		List<Match> matches = this.state.getMatches(query1, LimitToThisOrAllInstances.THIS);
		System.out.println("Facts:");
		System.out.println(state.getFacts());
		System.out.println("\\Facts");
		System.out.println(matches);
		System.out.println("\n\n\n");
	}
	@Test
	public void efiTests2c() {
//Create the following unit tests for getMatches
//c. conjunctive query is Q = A('TypedConstant2',y,z,w), B(x,y,z,w),
//C(y,z,'TypedConstant1') D(x,y), E('TypedConstant2',y,y)
//
//in each unit test create facts in the database such that two conditions hold:
//(i) each relation has 10000 facts in the database and (ii) there are only five
//answer tuples in the answer. You could do this by adding junk tuples and
//manually creating tuples that participate in the match.
//In the last unit test the query is boolean so you should only get true
		Relation A = Relation.create("A", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(String.class, "attribute2"),Attribute.create(String.class, "attribute3"),Attribute.create(Integer.class, "InstanceID")});
		Relation B = Relation.create("B", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(String.class, "attribute2"),Attribute.create(String.class, "attribute3"),Attribute.create(Integer.class, "InstanceID")});
		Relation D = Relation.create("D", new Attribute[] { Attribute.create(String.class, "attribute0"),Attribute.create(String.class, "attribute1"),Attribute.create(Integer.class, "InstanceID")});
		Relation C = Relation.create("C", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"), Attribute.create(String.class, "attribute2"),Attribute.create(Integer.class, "InstanceID") });
		Relation E = Relation.create("E", new Attribute[] { Attribute.create(String.class, "attribute0"), Attribute.create(String.class, "attribute1"), Attribute.create(String.class, "attribute2"),Attribute.create(Integer.class, "InstanceID") });
		Relation r[] = new Relation[] { A,B,C,D,E };
		Schema s = new Schema(r,new Dependency[0]);
		List<Atom> facts = new ArrayList<>();
		final int MAX_NUMBER_OF_TUPLES = 100;
		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(A, new Term[]{TypedConstant.create("a_"+(i-1)),TypedConstant.create("a_"+i),TypedConstant.create("a_"+i),TypedConstant.create("a_"+i)}));
		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(B, new Term[]{TypedConstant.create("b_"+(i-1)),TypedConstant.create("b_"+i),TypedConstant.create("b_"+i),TypedConstant.create("b_"+i)}));
		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(D, new Term[]{TypedConstant.create("d_"+(i-1)),TypedConstant.create("d_"+i)}));
		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(C, new Term[]{TypedConstant.create("c_1_"+(i-1)),TypedConstant.create("c_2_"+(i-1)),TypedConstant.create("c_3_"+i)}));
		for (int i=1; i <=MAX_NUMBER_OF_TUPLES; i++) facts.add(Atom.create(E, new Term[]{TypedConstant.create("e_1_"+(i-1)),TypedConstant.create("e_2_"+(i-1)),TypedConstant.create("e_3_"+i)}));
		
		for (int i=1; i <=5; i++) facts.add(Atom.create(A, new Term[]{TypedConstant.create("TC2"),TypedConstant.create("y"+i),TypedConstant.create("z"+i),TypedConstant.create("w"+i)}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(B, new Term[]{TypedConstant.create("x"+i),TypedConstant.create("y"+i),TypedConstant.create("z"+i),TypedConstant.create("w"+i)}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(C, new Term[]{TypedConstant.create("y"+i),TypedConstant.create("z"+i),TypedConstant.create("TC1")}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(D, new Term[]{TypedConstant.create("x"+i),TypedConstant.create("y"+i)}));
		for (int i=1; i <=5; i++) facts.add(Atom.create(E, new Term[]{TypedConstant.create("TC2"),TypedConstant.create("y"+i),TypedConstant.create("y"+i)}));
		
		try {
			this.state = new DatabaseChaseInstance(facts, new DatabaseConnection(new DatabaseParameters(), s));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		ConjunctiveQuery query1; // Q = A('TypedConstant2',y,z,w), B(x,y,z,w), C(y,z,'TypedConstant1') D(x,y), E('TypedConstant2',y,y)
		query1 = ConjunctiveQuery.create(new Variable[] {}, (Conjunction)Conjunction.of(
				Atom.create(A, TypedConstant.create("TC2"),Variable.create("y"),Variable.create("z"),Variable.create("w")),
				Atom.create(B, Variable.create("x"),Variable.create("y"), Variable.create("z"),Variable.create("w")),
				Atom.create(C, Variable.create("y"),Variable.create("z"), TypedConstant.create("TC1")),
				Atom.create(D, Variable.create("x"),Variable.create("y")),
				Atom.create(E, TypedConstant.create("TC2"),Variable.create("y"), Variable.create("y"))
				));
		
		List<Match> matches = this.state.getMatches(query1, LimitToThisOrAllInstances.THIS);
//		System.out.println("Facts:");
//		System.out.println(state.getFacts());
//		System.out.println("\\Facts");
		System.out.println(matches);
		System.out.println("\n\n\n");
	}
		
	public void tearDown() throws Exception {
		getConnection().close();
		state.close();
	}

	public DatabaseConnection getConnection() {
		return connection;
	}

	public void setConnection(DatabaseConnection connection) {
		this.connection = connection;
	}
}
