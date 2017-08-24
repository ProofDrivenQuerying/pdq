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
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
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
	public void efiAskedForThis() {
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
		facts.add(Atom.create(D, new Term[]{UntypedConstant.create("a_1")}));
		facts.add(Atom.create(C, new Term[]{UntypedConstant.create("a_5")}));
		for (int i=1; i <=10; i++) facts.add(Atom.create(R, new Term[]{UntypedConstant.create("a_"+(i-1)),UntypedConstant.create("a_"+i)}));
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
