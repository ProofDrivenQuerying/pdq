package uk.ac.ox.cs.pdq.test.reasoning.chase;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.logging.StatisticsCollector;
import uk.ac.ox.cs.pdq.reasoning.chase.RestrictedChaser;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

/**
 * Tests the reasonUntilTermination method of the RestrictedChaser class 
 * @author Efthymia Tsamoura
 *
 */
public class TestRestrictedChaser {

	protected DatabaseChaseInstance state;
	protected RestrictedChaser chaser;

	private Relation rel1;
	private Relation rel2;

	private TGD tgd;
	private EGD egd;

	private Schema schema;
//	private ReasoningParameters reasoningParams;
	private DatabaseConnection connection;

	@Before
	public void setup() throws SQLException {
		Attribute at11 = Attribute.create(String.class, "at11");
		Attribute at12 = Attribute.create(String.class, "at12");
		Attribute at13 = Attribute.create(String.class, "at13");
		Attribute fact= Attribute.create(Integer.class, "Fact");
		this.rel1 = new Relation("R1", new Attribute[]{at11, at12, at13, fact}) {
			private static final long serialVersionUID = 1L;};

		Attribute at21 = Attribute.create(String.class, "at21");
		Attribute at22 = Attribute.create(String.class, "at22");
		this.rel2 = new Relation("R2", new Attribute[]{at21, at22, fact}) {
			private static final long serialVersionUID = 1L;};

		Atom R1 = Atom.create(this.rel1, 
				new Term[]{Variable.create("x"),Variable.create("y"),Variable.create("z")});
		Atom R2 = Atom.create(this.rel2, 
				new Term[]{Variable.create("y"),Variable.create("z")});
		Atom R2p = Atom.create(this.rel2, 
				new Term[]{Variable.create("y"),Variable.create("w")});

		this.tgd = TGD.create(Conjunction.of(R1),Conjunction.of(R2));
		this.egd = EGD.create(Conjunction.of(R2,R2p), Conjunction.of(Atom.create(new Predicate(QNames.EQUALITY.toString(), 2, true), 
				Variable.create("z"),Variable.create("w"))));

		this.schema = new Schema(new Relation[]{this.rel1, this.rel2}, new Dependency[]{this.tgd,this.egd});
		this.schema.addConstants(Lists.<TypedConstant>newArrayList(TypedConstant.create(new String("John"))));

		this.connection = new DatabaseConnection(new DatabaseParameters(), this.schema);
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
			this.state = new DatabaseChaseInstance(Sets.<Atom>newHashSet(f20,f21,f22,f23,f24),connection);
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


}
