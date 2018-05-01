package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TotalNumberOfOutputTuplesPerAccessCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

public class TestOrderDependentCostEstimators {
	protected AccessMethodDescriptor method0 = AccessMethodDescriptor.create(new Integer[]{});
	protected AccessMethodDescriptor method1 = AccessMethodDescriptor.create(new Integer[]{0});
	protected AccessMethodDescriptor method2 = AccessMethodDescriptor.create(new Integer[]{0,1});
	protected AccessMethodDescriptor method3 = AccessMethodDescriptor.create(new Integer[]{1});
	
	protected Attribute a = Attribute.create(String.class, "a");
	protected Attribute b = Attribute.create(String.class, "b");
	protected Attribute c = Attribute.create(String.class, "c");
	protected Attribute d = Attribute.create(String.class, "d");
	protected Attribute e = Attribute.create(String.class, "e");
	protected Attribute InstanceID = Attribute.create(String.class, "InstanceID");
	protected Attribute i = Attribute.create(String.class, "i");
    
	protected Relation R;
	protected Relation S;
	protected Relation T;
	protected Relation U;
	protected Relation access;
	
	@Mock
	protected SimpleCatalog catalog;
    
	@Before public void setup() {
		Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);                
	}
	
	//We create a bushy plan and a left deep one
	//We estimate its cost with two order aware cost estimators
	//the textbook one and the one that counts the total number of output tuples per input
	//We want to see that bushy plans perform better
	//We do not use constants to access relations
	//Plans have no selections
	@Test public void test1() {
        this.R = Relation.create("R", new Attribute[]{a,b,c,InstanceID}, new AccessMethodDescriptor[]{this.method0});
        this.S = Relation.create("S", new Attribute[]{b,c,InstanceID}, new AccessMethodDescriptor[]{this.method1});
        this.T = Relation.create("T", new Attribute[]{a,d,e,InstanceID}, new AccessMethodDescriptor[]{this.method0});
        this.U = Relation.create("U", new Attribute[]{d,e,InstanceID}, new AccessMethodDescriptor[]{this.method2});
        this.access = Relation.create("Accessible", new Attribute[]{i,InstanceID});
        AccessTerm access0 = AccessTerm.create(this.R, this.method0);
        AccessTerm access1 = AccessTerm.create(this.S, this.method1);
        DependentJoinTerm join0 = DependentJoinTerm.create(access0, access1);
        
        AccessTerm access2 = AccessTerm.create(this.T, this.method0);
        AccessTerm access3 = AccessTerm.create(this.U, this.method2);
        DependentJoinTerm join1 = DependentJoinTerm.create(access2, access3);
        JoinTerm bushy = JoinTerm.create(join0, join1);
		when(this.catalog.getCardinality(this.R)).thenReturn(10);
		when(this.catalog.getCardinality(this.S)).thenReturn(10000);
		when(this.catalog.getCardinality(this.T)).thenReturn(100);
		when(this.catalog.getCardinality(this.U)).thenReturn(10000);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.R, this.method0)).thenReturn(10);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.S, this.method1)).thenReturn(1);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.T, this.method0)).thenReturn(100);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.U, this.method2)).thenReturn(10);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		TotalNumberOfOutputTuplesPerAccessCostEstimator estimator2 = new TotalNumberOfOutputTuplesPerAccessCostEstimator(null, this.catalog);
		
		double output = cardinalityEstimator.getCardinalityMetadata(bushy).getOutputCardinality();
		DependentJoinTerm leftdeep = DependentJoinTerm.create(JoinTerm.create(join0, access2), access3);
		double output2 = cardinalityEstimator.getCardinalityMetadata(leftdeep).getOutputCardinality();
		
		
		Cost cost1 = estimator.cost(bushy); 
		Cost cost3 = estimator.cost(leftdeep);
		
		Cost cost2 = estimator2.cost(bushy);
		Cost cost4 = estimator2.cost(leftdeep);
		
		Assert.assertNotEquals(cost1,cost3);
		Assert.assertEquals(cost2,cost4); // busy or left deep doesn't matter, this counts accesses.
		Assert.assertEquals(output,output2,0.0001);
	}
	//We create a bushy plan and a left deep one
	//We estimate its cost with two order aware cost estimators
	//the textbook one and the one that counts the total number of output tuples per input
	//We want to see that bushy plans perform better
	//We use constants to access relations
	//Plans have no selections
	@Test public void test2() {
        this.R = Relation.create("R", new Attribute[]{a,b,c,InstanceID}, new AccessMethodDescriptor[]{this.method0});
        this.S = Relation.create("S", new Attribute[]{b,c,InstanceID}, new AccessMethodDescriptor[]{this.method1});
        this.T = Relation.create("T", new Attribute[]{a,d,e,InstanceID}, new AccessMethodDescriptor[]{this.method0});
        this.U = Relation.create("U", new Attribute[]{d,e,InstanceID}, new AccessMethodDescriptor[]{this.method1});
        this.access = Relation.create("Accessible", new Attribute[]{i,InstanceID});
        AccessTerm access0 = AccessTerm.create(this.R, this.method0);
        AccessTerm access1 = AccessTerm.create(this.S, this.method1);
        DependentJoinTerm join0 = DependentJoinTerm.create(access0, access1);
        
        Map<Integer, TypedConstant> inputs = new LinkedHashMap<>();
        inputs.put(0, TypedConstant.create("dummy"));
        
        AccessTerm access2 = AccessTerm.create(this.T, this.method0);
        AccessTerm access3 = AccessTerm.create(this.U, this.method1, inputs);
        JoinTerm join1 = JoinTerm.create(access2, access3);
        JoinTerm bushy = JoinTerm.create(join0, join1);
		when(this.catalog.getCardinality(this.R)).thenReturn(10);
		when(this.catalog.getCardinality(this.S)).thenReturn(10000);
		when(this.catalog.getCardinality(this.T)).thenReturn(100);
		when(this.catalog.getCardinality(this.U)).thenReturn(10000);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.R, this.method0)).thenReturn(10);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.S, this.method1)).thenReturn(1);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.T, this.method0)).thenReturn(100);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.U, this.method1,inputs)).thenReturn(1);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		TotalNumberOfOutputTuplesPerAccessCostEstimator estimator2 = new TotalNumberOfOutputTuplesPerAccessCostEstimator(null, this.catalog);
		
		Cost cost1 = estimator.cost(bushy); 
		Cost cost2 = estimator2.cost(bushy);
		
		JoinTerm leftdeep = JoinTerm.create(JoinTerm.create(join0, access2), access3);
		
		Cost cost3 = estimator.cost(leftdeep); 
		Cost cost4 = estimator2.cost(leftdeep);
		
		Assert.assertNotEquals(cost1,cost2);
		Assert.assertNotEquals(cost1,cost3);
		
		Assert.assertNotEquals(cost3,cost4);
		
		Assert.assertEquals(cost2,cost4); // busy or left deep doesn't matter, this counts accesses.
	}
	
	//We create a bushy plan and a left deep one
	//We estimate its cost with two order aware cost estimators
	//the textbook one and the one that counts the total number of output tuples per input
	//We want to see that bushy plans perform better
	//We do not use constants to access relations
	//Plans have selections
	@Test public void test3() {
        this.R = Relation.create("R", new Attribute[]{a,b,c,InstanceID}, new AccessMethodDescriptor[]{this.method0});
        this.S = Relation.create("S", new Attribute[]{b,c,InstanceID}, new AccessMethodDescriptor[]{this.method0});
        this.T = Relation.create("T", new Attribute[]{a,d,e,InstanceID}, new AccessMethodDescriptor[]{this.method0});
        this.U = Relation.create("U", new Attribute[]{d,e,InstanceID}, new AccessMethodDescriptor[]{this.method0});
        this.access = Relation.create("Accessible", new Attribute[]{i,InstanceID});
        SelectionTerm access0 = SelectionTerm.create(ConstantEqualityCondition.create(2, TypedConstant.create("dummy1")), AccessTerm.create(this.R, this.method0));
        SelectionTerm access1 = SelectionTerm.create(ConstantEqualityCondition.create(1, TypedConstant.create("dummy2")), AccessTerm.create(this.S, this.method0));
        JoinTerm join0 = JoinTerm.create(access0, access1);
        
        SelectionTerm access2 = SelectionTerm.create(ConstantEqualityCondition.create(2, TypedConstant.create("dummy3")), AccessTerm.create(this.T, this.method0));
        SelectionTerm access3 = SelectionTerm.create(ConstantEqualityCondition.create(1, TypedConstant.create("dummy4")), AccessTerm.create(this.U, this.method0));
        JoinTerm join1 = JoinTerm.create(access2, access3);
        JoinTerm bushy = JoinTerm.create(join0, join1);
		when(this.catalog.getCardinality(this.R)).thenReturn(10);
		when(this.catalog.getCardinality(this.S)).thenReturn(10000);
		when(this.catalog.getCardinality(this.T)).thenReturn(100);
		when(this.catalog.getCardinality(this.U)).thenReturn(10000);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.R, this.method0)).thenReturn(100);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.S, this.method0)).thenReturn(10000);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.T, this.method0)).thenReturn(100);
		when(this.catalog.getTotalNumberOfOutputTuplesPerInputTuple(this.U, this.method0)).thenReturn(10000);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		TotalNumberOfOutputTuplesPerAccessCostEstimator estimator2 = new TotalNumberOfOutputTuplesPerAccessCostEstimator(null, this.catalog);
		
		Cost cost1 = estimator.cost(bushy); 
		Cost cost2 = estimator2.cost(bushy);
		
		JoinTerm leftdeep = JoinTerm.create(JoinTerm.create(join0, access2), access3);
		
		Cost cost3 = estimator.cost(leftdeep); 
		Cost cost4 = estimator2.cost(leftdeep);
		
		Assert.assertNotEquals(cost1,cost2);
		Assert.assertNotEquals(cost1,cost3);
		Assert.assertNotEquals(cost3,cost4);
		Assert.assertEquals(cost2,cost4); // busy or left deep doesn't matter, this counts accesses.
	}
}
