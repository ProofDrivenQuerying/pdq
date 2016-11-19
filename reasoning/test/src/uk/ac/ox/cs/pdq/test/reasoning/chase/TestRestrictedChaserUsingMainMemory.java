package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.logging.performance.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseState;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

/**
 * Tests the reasonUntilTermination method of the RestrictedChaser class 
 * @author Efthymia Tsamoura
 *
 */
public class TestRestrictedChaserUsingMainMemory {

	protected HomomorphismManager manager;
	protected DatabaseChaseState state;
	protected RestrictedChaser chaser;
	
	private Relation rel1;
	private Relation rel2;
	
	private TGD tgd;
	private EGD egd;

	private Schema schema;
	
	@Before
	public void setup() throws SQLException {
		Attribute at11 = new Attribute(String.class, "at11");
		Attribute at12 = new Attribute(String.class, "at12");
		Attribute at13 = new Attribute(String.class, "at13");
		this.rel1 = new Relation("R1", Lists.newArrayList(at11, at12, at13)) {};
		
		Attribute at21 = new Attribute(String.class, "at21");
		Attribute at22 = new Attribute(String.class, "at22");
		this.rel2 = new Relation("R2", Lists.newArrayList(at21, at22)) {};
		
		Atom R1 = new Atom(this.rel1, 
				Lists.newArrayList(new Variable("x"),new Variable("y"),new Variable("z")));
		Atom R2 = new Atom(this.rel2, 
				Lists.newArrayList(new Variable("y"),new Variable("z")));
		Atom R2p = new Atom(this.rel2, 
				Lists.newArrayList(new Variable("y"),new Variable("w")));
		
		this.tgd = new TGD(Conjunction.of(R1),Conjunction.of(R2));
		this.egd = new EGD(Conjunction.of(R2,R2p), Conjunction.of(new Atom(new Predicate(QNames.EQUALITY.toString(), 2), new Variable("z"),new Variable("w"))));

		this.schema = new Schema(Lists.<Relation>newArrayList(this.rel1, this.rel2), Lists.<Dependency>newArrayList(this.tgd,this.egd));
		this.schema.updateConstants(Lists.<TypedConstant<?>>newArrayList(new TypedConstant(new String("John"))));
		/** The driver. */
		String driver = null;
		/** The url. */
		String url = "jdbc:mysql://localhost/";
		/** The database. */
		String database = "pdq_chase";
		/** The username. */
		String username = "root";
		/** The password. */
		String password ="root";
		this.manager = new MainMemoryHomomorphismManager(this.schema);
		this.manager.initialize();
		
		this.chaser = new RestrictedChaser(new StatisticsCollector(true, new EventBus()));
	}
	
	@Test 
	public void test_reasonUntilTermination1() {
		Atom f20 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k1"), new UntypedConstant("c"),new UntypedConstant("c1")));

		Atom f21 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k2"), new UntypedConstant("c"),new UntypedConstant("c2")));

		Atom f22 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k3"), new UntypedConstant("c"),new UntypedConstant("c3")));

		Atom f23 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k4"), new UntypedConstant("c"),new UntypedConstant("c4")));

		Atom f24 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k5"), new UntypedConstant("c"),new TypedConstant(new String("John"))));
		this.state = new DatabaseChaseState(this.manager, Sets.newHashSet(f20,f21,f22,f23,f24));
		this.chaser.reasonUntilTermination(this.state, Lists.<Dependency>newArrayList(this.tgd,this.egd));
		Assert.assertEquals(false, this.state.isFailed());
		
		
		Atom n00 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k5"), new UntypedConstant("c"),new TypedConstant(new String("John"))));
		
		Atom n01 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k4"), new UntypedConstant("c"),new TypedConstant(new String("John"))));
		
		Atom n02 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k3"), new UntypedConstant("c"),new TypedConstant(new String("John"))));
		
		Atom n03 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k1"), new UntypedConstant("c"),new TypedConstant(new String("John"))));
		
		Atom n04 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k2"), new UntypedConstant("c"),new TypedConstant(new String("John"))));
		
		Atom n1 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new TypedConstant(new String("John"))));
	
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
	
	
}