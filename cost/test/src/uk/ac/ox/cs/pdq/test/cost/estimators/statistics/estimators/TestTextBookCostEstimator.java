package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
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
	protected Attribute[] attributes21Old = new Attribute[21];
	protected Attribute[] newAttributes = new Attribute[30];
	
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected Relation YahooPlaceCode;
	protected Relation YahooPlaceRelationship;
	protected Relation YahooWeather;
	
	@Mock
	protected SimpleCatalog catalog;
    
	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);
        
        for(int index = 0; index < attributes3Old.length; ++index) {
        	this.attributes3Old[index] = Attribute.create(String.class, "a" + index);
        }
        
        for(int index = 0; index < attributes6Old.length; ++index) {
        	this.attributes6Old[index] = Attribute.create(String.class, "a" + index);
        }
        
        for(int index = 0; index < attributes21Old.length; ++index) {
        	this.attributes21Old[index] = Attribute.create(String.class, "a" + index);
        }
        
        for(int index = 0; index < newAttributes.length; ++index) {
        	this.newAttributes[index] = Attribute.create(String.class, "c" + index);
        }
    
        this.YahooPlaceCode = Relation.create("YahooPlaceCode", this.attributes3Old, new AccessMethod[]{this.method2});
        this.YahooPlaceRelationship = Relation.create("YahooPlaceRelationship", this.attributes6Old, new AccessMethod[]{this.method2});
        this.YahooWeather = Relation.create("YahooWeather", this.attributes21Old, new AccessMethod[]{this.method1});
    	
        //Create the mock catalog object
        when(this.catalog.getCardinality(this.YahooPlaceCode)).thenReturn(10000000);
        when(this.catalog.getCardinality(this.YahooPlaceRelationship)).thenReturn(10000000);
        when(this.catalog.getCardinality(this.YahooWeather)).thenReturn(100000000);    
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
		
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, new NaiveCardinalityEstimator(this.catalog));
		Cost cost = estimator.cost(plan2);
		System.out.println(cost);
	}
	
}
