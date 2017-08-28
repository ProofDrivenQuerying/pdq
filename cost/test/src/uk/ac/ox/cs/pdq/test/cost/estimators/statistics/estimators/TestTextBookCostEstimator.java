package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Utility;

public class TestTextBookCostEstimator {
	protected Attribute[] attributes3Old = new Attribute[3];
	protected Attribute[] attributes6Old = new Attribute[6];
	protected Attribute[] attributes18Old = new Attribute[18];
	protected Attribute[] attributes21Old = new Attribute[21];
	protected Attribute[] newAttributes = new Attribute[30];
	
	protected AccessMethod method0 = AccessMethod.create(new Integer[]{});
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected AccessMethod method3 = AccessMethod.create(new Integer[]{1});
	
	protected Relation YahooPlaceCode;
	protected Relation YahooPlaceRelationship;
	protected Relation YahooWeather;
	protected Relation YahooPlaces;
	
	protected Attribute a = Attribute.create(Integer.class, "a");
	protected Attribute b = Attribute.create(Integer.class, "b");
	protected Attribute c = Attribute.create(Integer.class, "c");
	protected Attribute d = Attribute.create(Integer.class, "d");
    
	protected Relation R;
	protected Relation S;	
	
	@Mock
	protected SimpleCatalog catalog;
    
	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);
        
        for(int index = 0; index < attributes3Old.length; ++index) 
        	this.attributes3Old[index] = Attribute.create(String.class, "a" + index);
        
        for(int index = 0; index < attributes6Old.length; ++index) 
        	this.attributes6Old[index] = Attribute.create(String.class, "a" + index);
        
        for(int index = 0; index < attributes18Old.length; ++index) 
        	this.attributes18Old[index] = Attribute.create(String.class, "a" + index);
        
        for(int index = 0; index < attributes21Old.length; ++index) 
        	this.attributes21Old[index] = Attribute.create(String.class, "a" + index);
        
        for(int index = 0; index < newAttributes.length; ++index) 
        	this.newAttributes[index] = Attribute.create(String.class, "c" + index);
        
    
        this.YahooPlaceCode = Relation.create("YahooPlaceCode", this.attributes3Old, new AccessMethod[]{this.method2});
        this.YahooPlaceRelationship = Relation.create("YahooPlaceRelationship", this.attributes6Old, new AccessMethod[]{this.method2});
        this.YahooWeather = Relation.create("YahooWeather", this.attributes21Old, new AccessMethod[]{this.method1});
        this.YahooPlaces = Relation.create("YahooPlaces", this.attributes18Old, new AccessMethod[]{this.method3});
    	
        //Create the mock catalog object
        when(this.catalog.getCardinality(this.YahooPlaceCode)).thenReturn(10000000);
        when(this.catalog.getCardinality(this.YahooPlaceRelationship)).thenReturn(10000000);
        when(this.catalog.getCardinality(this.YahooWeather)).thenReturn(100000000);
        when(this.catalog.getCardinality(this.YahooPlaces)).thenReturn(10000000);
        
        this.R = Relation.create("R", new Attribute[]{a,b,c}, new AccessMethod[]{this.method0, this.method2});
        this.S = Relation.create("S", new Attribute[]{b,c}, new AccessMethod[]{this.method0, this.method1, this.method2});
        //Create the mock catalog object
        when(this.catalog.getCardinality(this.R)).thenReturn(100);
        when(this.catalog.getCardinality(this.S)).thenReturn(100);
	}
	
	@Test public void test1() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create("iso"));
		inputConstants1.put(1, TypedConstant.create("FR"));
		AccessTerm access1 = AccessTerm.create(this.YahooPlaceCode, this.method2, inputConstants1);
		Map<Integer, TypedConstant> inputConstants2 = new HashMap<>();
		inputConstants2.put(0, TypedConstant.create("children"));
		AccessTerm access2 = AccessTerm.create(this.YahooPlaceRelationship, this.method2, inputConstants2);
		AccessTerm access3 = AccessTerm.create(this.YahooWeather, this.method1);
		
		Attribute[] renamings1 = new Attribute[]{Attribute.create(String.class, "dummy1"), Attribute.create(String.class, "dummy2"), Attribute.create(String.class, "c1")};
		Attribute[] renamings2 = new Attribute[6];
		System.arraycopy(this.newAttributes, 1, renamings2, 1, 5);
		renamings2[0] = Attribute.create(String.class, "dummy3");
		Attribute[] renamings3 = new Attribute[21];
		System.arraycopy(this.newAttributes, 6, renamings3, 1, 20);
		renamings3[0] = Attribute.create(String.class, "c2");
		
		RenameTerm rename1 = RenameTerm.create(renamings1, access1);
		RenameTerm rename2 = RenameTerm.create(renamings2, access2);
		RenameTerm rename3 = RenameTerm.create(renamings3, access3);
		
		DependentJoinTerm plan1 = DependentJoinTerm.create(rename1, rename2);
		DependentJoinTerm plan2 = DependentJoinTerm.create(plan1, rename3);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan2);
		
		//TODO add assertions for cost  
	}
	
	@Test public void test2() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(1, TypedConstant.create("Eiffel Tower"));
		AccessTerm access1 = AccessTerm.create(this.YahooPlaces, this.method3, inputConstants1);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(access1);
		
		//TODO add assertions for cost 
	}
	
	@Test public void test3() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		
		//TODO add assertions for cost 
	}
	
	
	@Test public void test4() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);		
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);					
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		
		//TODO add assertions for cost 
	}
	
	@Test public void test5() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);
		SelectionTerm selectionTerm = SelectionTerm.create(ConstantEqualityCondition.create(0, TypedConstant.create(new Integer(1))), access1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(selectionTerm, access2);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);		
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		
		//TODO add assertions for cost
	}
	
	@Test public void test6() {	
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(TypedConstant.create(new Integer(1))));
		AccessTerm access1 = AccessTerm.create(this.R, this.method1, inputConstants1);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);		
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		
		//TODO add assertions for cost
	}
	
	@Test public void test7() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method0);
		JoinTerm plan1 = JoinTerm.create(access1, access2);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);		
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		
		//TODO add assertions for cost
	}
	
}
