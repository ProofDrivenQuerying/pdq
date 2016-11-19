package uk.ac.ox.cs.pdq.test.reasoning.homomorphism;

import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismManager;
import uk.ac.ox.cs.pdq.db.homomorphism.HomomorphismProperty;
import uk.ac.ox.cs.pdq.db.homomorphism.TriggerProperty;
import uk.ac.ox.cs.pdq.db.sql.MySQLStatementBuilder;
import uk.ac.ox.cs.pdq.db.sql.SQLStatementBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;

import com.google.common.collect.Lists;

/**
 * Tests the getMatches method of the DatabaseHomomorphismManager class 
 * @author Efthymia Tsamoura
 *
 */
public class TestDatabaseHomomorphismManager {	
	protected HomomorphismManager manager;
	
	private Relation rel1;
	private Relation rel2;
	private Relation rel3;
	
	private Dependency tgd;
	private Dependency tgd2;
	private Dependency egd;

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
		
		Attribute at31 = new Attribute(String.class, "at31");
		Attribute at32 = new Attribute(String.class, "at32");
		this.rel3 = new Relation("R3", Lists.newArrayList(at31, at32)) {};
		
		Atom R1 = new Atom(this.rel1, 
				Lists.newArrayList(new Variable("x"),new Variable("y"),new Variable("z")));
		Atom R2 = new Atom(this.rel2, 
				Lists.newArrayList(new Variable("y"),new Variable("z")));
		Atom R2p = new Atom(this.rel2, 
				Lists.newArrayList(new Variable("y"),new Variable("w")));
		
		Atom R3 = new Atom(this.rel3, 
				Lists.newArrayList(new Variable("y"),new Variable("w")));
		
		this.tgd = new TGD(Conjunction.of(R1),Conjunction.of(R2));
		this.tgd2 = new TGD(Conjunction.of(R1),Conjunction.of(R3));
		this.egd = new EGD(Conjunction.of(R2,R2p), Conjunction.of(new Atom(new Predicate(QNames.EQUALITY.toString(), 2), new Variable("z"),new Variable("w"))));

		this.schema = new Schema(Lists.<Relation>newArrayList(this.rel1, this.rel2, this.rel3), Lists.<Dependency>newArrayList(this.tgd,this.tgd2, this.egd));
		
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
		SQLStatementBuilder builder = new MySQLStatementBuilder();
		this.manager = new DatabaseHomomorphismManager(driver, url, database, username, password, builder, this.schema);
		this.manager.initialize();
	}
	
	@Test 
	public void test_getMatches1() {
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

		Atom f25 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k6"), new UntypedConstant("c"),new TypedConstant(new String("Michael"))));
		
		this.manager.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25));
		List<Match> matches = this.manager.getTriggers(Lists.newArrayList(this.tgd),TriggerProperty.ACTIVE);
		Assert.assertEquals(6, matches.size());
	}
	
	@Test 
	public void test_getMatches2() {
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new UntypedConstant("c1")));

		Atom f21 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new UntypedConstant("c2")));

		Atom f22 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("k"),new UntypedConstant("c3")));

		Atom f23 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new UntypedConstant("c4")));

		Atom f24 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new TypedConstant(new String("John"))));

		Atom f25 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new TypedConstant(new String("Michael"))));
		
		this.manager.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25));
		List<Match> matches = this.manager.getTriggers(Lists.newArrayList(this.egd),TriggerProperty.ACTIVE);
		Assert.assertEquals(4, matches.size());
	}
	
	@Test 
	public void test_getMatches3() {
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new UntypedConstant("c1")));

		Atom f21 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new UntypedConstant("c2")));

		Atom f22 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("k"),new UntypedConstant("c3")));

		Atom f23 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new UntypedConstant("c4")));

		Atom f24 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new TypedConstant(new String("John"))));

		Atom f25 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new TypedConstant(new String("Michael"))));
		
		Atom eq1 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2), new UntypedConstant("c1"), new UntypedConstant("c2"));
		Atom eq2 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2), new UntypedConstant("c1"), new UntypedConstant("c3"));
		
		this.manager.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, eq1,eq2));
		List<Match> matches = this.manager.getTriggers(Lists.newArrayList(this.egd),TriggerProperty.ALL);
		Assert.assertEquals(4, matches.size());
	}	
	
	@Test 
	public void test_getMatches4() {
		Atom f20 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new UntypedConstant("c1")));

		Atom f21 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new UntypedConstant("c2")));

		Atom f22 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("k"),new UntypedConstant("c3")));

		Atom f23 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new UntypedConstant("c4")));

		Atom f24 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new TypedConstant(new String("John"))));

		Atom f25 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("p"),new TypedConstant(new String("Michael"))));
		
		Atom eq1 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2), new UntypedConstant("c1"), new UntypedConstant("c2"));
		Atom eq2 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2), new UntypedConstant("c1"), new UntypedConstant("c3"));
		
		this.manager.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, eq1,eq2));
		List<Match> matches = this.manager.getTriggers(Lists.newArrayList(this.egd),TriggerProperty.ACTIVE);
		Assert.assertEquals(3, matches.size());
	}	
	
	@Test 
	public void test_getMatches5() {
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

		Atom f25 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k6"), new UntypedConstant("c"),new TypedConstant(new String("Michael"))));
		
		Atom f26 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new UntypedConstant("c1")));

		Atom f27 = new Atom(this.rel2, 
				Lists.newArrayList(new UntypedConstant("c"),new UntypedConstant("c2")));
		
		this.manager.addFacts(Lists.newArrayList(f20,f21,f22,f23,f24,f25, f26, f27));
		List<Match> matches = this.manager.getTriggers(Lists.newArrayList(this.tgd), TriggerProperty.ACTIVE);
		Assert.assertEquals(4, matches.size());
	}
	
	@Test 
	public void test_getMatches6() {
		Atom f20 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k1"), new UntypedConstant("r1"),new UntypedConstant("c1")));

		Atom f21 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k2"), new UntypedConstant("r2"),new UntypedConstant("c2")));

		Atom f22 = new Atom(this.rel1, 
				Lists.newArrayList(new UntypedConstant("k3"), new UntypedConstant("r3"),new UntypedConstant("c3")));
		
		Atom f26 = new Atom(this.rel3, 
				Lists.newArrayList(new UntypedConstant("r1"),new UntypedConstant("skolem1")));

		Atom f27 = new Atom(this.rel3, 
				Lists.newArrayList(new UntypedConstant("r2"),new UntypedConstant("skolem2")));
		
		this.manager.addFacts(Lists.newArrayList(f20,f21,f22,f26,f27));
		List<Match> matches = this.manager.getTriggers(Lists.newArrayList(this.tgd2), TriggerProperty.ACTIVE);
		Assert.assertEquals(1, matches.size());
	}
	
}
