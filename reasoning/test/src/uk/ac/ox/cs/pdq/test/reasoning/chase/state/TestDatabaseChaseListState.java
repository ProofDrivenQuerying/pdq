package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;


/**
 * Tests the chaseStep method of the DatabaseChaseInstance class 
 * @author Efthymia Tsamoura
 *
 */
public class TestDatabaseChaseListState {

	protected DatabaseChaseInstance state;
	
	private Atom R2 = Atom.create(Predicate.create("R2",2), 
			new Term[]{Variable.create("y"),Variable.create("z")});
	private Atom R2p = Atom.create(Predicate.create("R2",2), 
			new Term[]{Variable.create("y"),Variable.create("w")});
	
	private EGD egd = EGD.create(Conjunction.of(R2,R2p), Conjunction.of(
			Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), 
			Variable.create("z"),Variable.create("w"))));
			
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test 
	public void test_chaseStep() {
//		Atom f0 = Atom.create(Predicate.create("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new Skolem("c1")));
//		
//		Atom f1 = Atom.create(Predicate.create("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("John"))));
//		
//		Atom f2 = Atom.create(Predicate.create("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new Skolem("k")));
//		
//		Atom f3 = Atom.create(Predicate.create("R2",2), 
//				Lists.newArrayList(new Skolem("c3"),new TypedConstant(new String("John"))));
//		
//		Atom f4 = Atom.create(Predicate.create("R2",2), 
//				Lists.newArrayList(new Skolem("c2"),new Skolem("c4")));
//		
//		try {
//			this.state = new DatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), null, null, null, null, null, null, null);
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}
//		Map<Variable, Constant> map1 = new HashMap<>();
//		map1.put(Variable.create("y"), new Skolem("c"));
//		map1.put(Variable.create("z"), new Skolem("c1"));
//		map1.put(Variable.create("w"), new Skolem("c2"));
//		
//		Map<Variable, Constant> map2 = new HashMap<>();
//		map2.put(Variable.create("y"), new Skolem("c"));
//		map2.put(Variable.create("z"), new Skolem("c1"));
//		map2.put(Variable.create("w"), new Skolem("c3"));
//		
//		Map<Variable, Constant> map3 = new HashMap<>();
//		map3.put(Variable.create("y"), new Skolem("c"));
//		map3.put(Variable.create("z"), new Skolem("c2"));
//		map3.put(Variable.create("w"), new Skolem("c3"));
//		
//		Map<Variable, Constant> map4 = new HashMap<>();
//		map4.put(Variable.create("y"), new Skolem("c"));
//		map4.put(Variable.create("z"), new Skolem("c3"));
//		map4.put(Variable.create("w"), new Skolem("c4"));
//		
//		Map<Variable, Constant> map5 = new HashMap<>();
//		map5.put(Variable.create("y"), new Skolem("c"));
//		map5.put(Variable.create("z"), new Skolem("c3"));
//		map5.put(Variable.create("w"), new TypedConstant(new String("John")));
//		
//		Collection<Match> matches = new LinkedHashSet<>();
//		matches.add(new Match(this.egd,map5));
//		matches.add(new Match(this.egd,map1));
//		matches.add(new Match(this.egd,map4));
//		matches.add(new Match(this.egd,map3));
//		matches.add(new Match(this.egd,map2));
//		
//		boolean _isFailed;
//		_isFailed = this.state.chaseStep(matches);
//		Assert.assertEquals(false, !_isFailed);
//		Assert.assertEquals(1, this.state.getConstantClasses().size());
//		Assert.assertNotNull(this.state.getConstantClasses().getClass(new Skolem("c1")));
//		Assert.assertNotNull(this.state.getConstantClasses().getClass(new Skolem("c2")));
//		Assert.assertNotNull(this.state.getConstantClasses().getClass(new Skolem("c3")));
//		Assert.assertNotNull(this.state.getConstantClasses().getClass(new Skolem("c4")));
//		Assert.assertNotNull(this.state.getConstantClasses().getClass(new TypedConstant(new String("John"))));
//		
//		Atom n0 = Atom.create(Predicate.create("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("John"))));
//		
//		Atom n1 = Atom.create(Predicate.create("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new Skolem("k")));
//		
//		Atom n2 = Atom.create(Predicate.create("R2",2), 
//				Lists.newArrayList(new TypedConstant(new String("John")),new TypedConstant(new String("John"))));
//		
//		Atom n3 = new Equality( 
//				Lists.newArrayList(new Skolem("c3"),new TypedConstant(new String("John"))));
//		
//		Atom n4 = new Equality( 
//				Lists.newArrayList(new Skolem("c1"),new Skolem("c2")));
//		
//		Atom n5 = new Equality( 
//				Lists.newArrayList(new Skolem("c3"),new Skolem("c4")));
//		
//		Atom n6 = new Equality( 
//				Lists.newArrayList(new Skolem("c2"),new Skolem("c3")));
//		
//		Atom n7 = new Equality( 
//				Lists.newArrayList(new Skolem("c1"),new Skolem("c3")));
//		
//		Assert.assertEquals(Sets.newHashSet(n0,n1,n2,n3,n4,n5,n6,n7), this.state.getFacts());
//		
//		Map<Variable, Constant> map6 = new HashMap<>();
//		map6.put(Variable.create("y"), new Skolem("c"));
//		map6.put(Variable.create("z"), new Skolem("c2"));
//		map6.put(Variable.create("w"), new TypedConstant(new String("Michael")));
//		
//		_isFailed = this.state.chaseStep(new Match(this.egd,map6));
//		Assert.assertEquals(true, !_isFailed);
	}
	
}
