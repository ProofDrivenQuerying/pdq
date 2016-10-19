package uk.ac.ox.cs.pdq.test.reasoning.chase.state;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Conjunction;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.reasoning.chase.state.DatabaseChaseInstance;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Tests the chaseStep method of the DatabaseChaseInstance class 
 * @author Efthymia Tsamoura
 *
 */
public class TestDatabaseChaseListState {

	protected DatabaseChaseInstance state;
	
	private Atom R2 = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Variable("y"),new Variable("z")));
	private Atom R2p = new Atom(new Predicate("R2",2), 
			Lists.newArrayList(new Variable("y"),new Variable("w")));
	
	private EGD egd = new EGD(Conjunction.of(R2,R2p), Conjunction.of(new Equality(new Variable("z"),new Variable("w"))));
			
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test 
	public void test_chaseStep() {
//		Atom f0 = new Atom(new Predicate("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new Skolem("c1")));
//		
//		Atom f1 = new Atom(new Predicate("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("John"))));
//		
//		Atom f2 = new Atom(new Predicate("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new Skolem("k")));
//		
//		Atom f3 = new Atom(new Predicate("R2",2), 
//				Lists.newArrayList(new Skolem("c3"),new TypedConstant(new String("John"))));
//		
//		Atom f4 = new Atom(new Predicate("R2",2), 
//				Lists.newArrayList(new Skolem("c2"),new Skolem("c4")));
//		
//		try {
//			this.state = new DatabaseChaseInstance(Sets.<Atom>newHashSet(f0, f1, f2, f3, f4), null, null, null, null, null, null, null);
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}
//		Map<Variable, Constant> map1 = new HashMap<>();
//		map1.put(new Variable("y"), new Skolem("c"));
//		map1.put(new Variable("z"), new Skolem("c1"));
//		map1.put(new Variable("w"), new Skolem("c2"));
//		
//		Map<Variable, Constant> map2 = new HashMap<>();
//		map2.put(new Variable("y"), new Skolem("c"));
//		map2.put(new Variable("z"), new Skolem("c1"));
//		map2.put(new Variable("w"), new Skolem("c3"));
//		
//		Map<Variable, Constant> map3 = new HashMap<>();
//		map3.put(new Variable("y"), new Skolem("c"));
//		map3.put(new Variable("z"), new Skolem("c2"));
//		map3.put(new Variable("w"), new Skolem("c3"));
//		
//		Map<Variable, Constant> map4 = new HashMap<>();
//		map4.put(new Variable("y"), new Skolem("c"));
//		map4.put(new Variable("z"), new Skolem("c3"));
//		map4.put(new Variable("w"), new Skolem("c4"));
//		
//		Map<Variable, Constant> map5 = new HashMap<>();
//		map5.put(new Variable("y"), new Skolem("c"));
//		map5.put(new Variable("z"), new Skolem("c3"));
//		map5.put(new Variable("w"), new TypedConstant(new String("John")));
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
//		Atom n0 = new Atom(new Predicate("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new TypedConstant(new String("John"))));
//		
//		Atom n1 = new Atom(new Predicate("R2",2), 
//				Lists.newArrayList(new Skolem("c"),new Skolem("k")));
//		
//		Atom n2 = new Atom(new Predicate("R2",2), 
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
//		map6.put(new Variable("y"), new Skolem("c"));
//		map6.put(new Variable("z"), new Skolem("c2"));
//		map6.put(new Variable("w"), new TypedConstant(new String("Michael")));
//		
//		_isFailed = this.state.chaseStep(new Match(this.egd,map6));
//		Assert.assertEquals(true, !_isFailed);
	}
	
}
