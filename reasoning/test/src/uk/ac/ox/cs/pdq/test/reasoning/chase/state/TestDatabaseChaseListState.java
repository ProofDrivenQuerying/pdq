package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.db.EGD;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseListState;
import uk.ac.ox.cs.pdq.reasoning.homomorphism.DatabaseHomomorphismManager;
import uk.ac.ox.cs.pdq.reasoning.utility.Match;

import com.google.common.collect.Lists;


public class TestDatabaseChaseListState {

	protected DatabaseChaseListState state;
	@Mock protected DatabaseHomomorphismManager manager;
	
	private Atom R1 = new Atom(new Predicate("R1",3), 
			Lists.newArrayList(new Variable("x"),new Variable("y"),new Variable("z")));
	private Atom R2 = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Variable("y"),new Variable("z")));
	private Atom R2p = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Variable("y"),new Variable("w")));
	
	private TGD tgd = new TGD(Conjunction.of(R1),Conjunction.of(R2));
	private EGD egd = new EGD(Conjunction.of(R2,R2p), Conjunction.of(new Equality(new Variable("z"),new Variable("w"))));

//	private Atom f20 = new Atom(new Predicate("R2",2), 
//			Lists.newArrayList(new Skolem("c"),new Skolem("c1")));
//	
//	private Atom f21 = new Atom(new Predicate("R2",2), 
//			Lists.newArrayList(new Skolem("c"),new Skolem("c2")));
//	
//	private Atom f22 = new Atom(new Predicate("R2",2), 
//			Lists.newArrayList(new Skolem("c"),new Skolem("c3")));
//	
//	private Atom f23 = new Atom(new Predicate("R2",2), 
//			Lists.newArrayList(new Skolem("c"),new Skolem("c4")));
//	
//	private Atom f24 = new Atom(new Predicate("R2",2), 
//			Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("John"))));
//	
//	private Atom f25 = new Atom(new Predicate("R2",2), 
//			Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("Michael"))));

	private Atom f0 = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Skolem("c"),new Skolem("c1")));
	
	private Atom f1 = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("John"))));
	
	private Atom f2 = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Skolem("c"),new Skolem("k")));
	
	private Atom f3 = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Skolem("c3"),new TypedConstant(new String("John"))));
	
	private Atom f4 = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Skolem("c2"),new Skolem("c4")));
			
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test 
	public void test1() {
		
		this.state = new DatabaseChaseListState(this.manager, Lists.<Atom>newArrayList(this.f0, this.f1, this.f2, this.f3, this.f4));
		Map<Variable, Constant> map1 = new HashMap<>();
		map1.put(new Variable("y"), new Skolem("c"));
		map1.put(new Variable("z"), new Skolem("c1"));
		map1.put(new Variable("w"), new Skolem("c2"));
		
		Map<Variable, Constant> map2 = new HashMap<>();
		map2.put(new Variable("y"), new Skolem("c"));
		map2.put(new Variable("z"), new Skolem("c1"));
		map2.put(new Variable("w"), new Skolem("c3"));
		
		Map<Variable, Constant> map3 = new HashMap<>();
		map3.put(new Variable("y"), new Skolem("c"));
		map3.put(new Variable("z"), new Skolem("c2"));
		map3.put(new Variable("w"), new Skolem("c3"));
		
		Map<Variable, Constant> map4 = new HashMap<>();
		map4.put(new Variable("y"), new Skolem("c"));
		map4.put(new Variable("z"), new Skolem("c3"));
		map4.put(new Variable("w"), new Skolem("c4"));
		
		Map<Variable, Constant> map5 = new HashMap<>();
		map5.put(new Variable("y"), new Skolem("c"));
		map5.put(new Variable("z"), new Skolem("c3"));
		map5.put(new Variable("w"), new TypedConstant(new String("John")));
		
		Collection<Match> matches = new LinkedHashSet<>();
		matches.add(new Match(this.egd,map5));
		matches.add(new Match(this.egd,map1));
		matches.add(new Match(this.egd,map4));
		matches.add(new Match(this.egd,map3));
		matches.add(new Match(this.egd,map2));
		
		boolean _isFailed;
		_isFailed = this.state.chaseStep(matches);
		Assert.assertEquals(false, _isFailed);
		Assert.assertEquals(1, this.state.getConstantClasses().size());
		
		Map<Variable, Constant> map6 = new HashMap<>();
		map6.put(new Variable("y"), new Skolem("c"));
		map6.put(new Variable("z"), new Skolem("c2"));
		map6.put(new Variable("w"), new TypedConstant(new String("Michael")));
		
		_isFailed = this.state.chaseStep(new Match(this.egd,map6));
		Assert.assertEquals(true, _isFailed);
	}
	
}
