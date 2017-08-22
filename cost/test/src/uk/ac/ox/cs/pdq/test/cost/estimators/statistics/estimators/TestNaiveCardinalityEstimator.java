package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
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

public class TestNaiveCardinalityEstimator {
	protected AccessMethod method0 = AccessMethod.create(new Integer[]{});
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected AccessMethod method3 = AccessMethod.create(new Integer[]{1});
	
    Attribute a = Attribute.create(Integer.class, "a");
    Attribute b = Attribute.create(Integer.class, "b");
    Attribute c = Attribute.create(Integer.class, "c");
    Attribute d = Attribute.create(Integer.class, "d");
    
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
        this.R = Relation.create("R", new Attribute[]{a,b,c}, new AccessMethod[]{this.method0, this.method2});
        this.S = Relation.create("S", new Attribute[]{b,c}, new AccessMethod[]{this.method0, this.method1, this.method2});
        //Create the mock catalog object
        when(this.catalog.getCardinality(this.R)).thenReturn(100);
        when(this.catalog.getCardinality(this.S)).thenReturn(100);
	}
	
	@Test public void test1() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
				
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);		
		cardinalityEstimator.estimateCardinalityIfNeeded(plan1);
		
		Assert.assertEquals(new Double(0.0), cardinalityEstimator.getCardinalityMetadata(access1).getInputCardinality());
		Assert.assertEquals(new Double(100.0), cardinalityEstimator.getCardinalityMetadata(access1).getOutputCardinality());
		
		Assert.assertEquals(new Double(100.0), cardinalityEstimator.getCardinalityMetadata(access2).getInputCardinality());
		Assert.assertEquals(new Double(10.0), cardinalityEstimator.getCardinalityMetadata(access2).getOutputCardinality());
		
		Assert.assertEquals(new Double(0.0), cardinalityEstimator.getCardinalityMetadata(plan1).getInputCardinality());
		Assert.assertEquals(new Double(100.0), cardinalityEstimator.getCardinalityMetadata(plan1).getOutputCardinality());
		
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		Assert.assertEquals(new Double(4800.0), cost);
	}
	
	
	@Test public void test2() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);
				
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);		
		cardinalityEstimator.estimateCardinalityIfNeeded(plan1);
		
		Assert.assertEquals(new Double(0.0), cardinalityEstimator.getCardinalityMetadata(access1).getInputCardinality());
		Assert.assertEquals(new Double(100.0), cardinalityEstimator.getCardinalityMetadata(access1).getOutputCardinality());
		
		Assert.assertEquals(new Double(100.0), cardinalityEstimator.getCardinalityMetadata(access2).getInputCardinality());
		Assert.assertEquals(new Double(1.0), cardinalityEstimator.getCardinalityMetadata(access2).getOutputCardinality());
		
		Assert.assertEquals(new Double(0.0), cardinalityEstimator.getCardinalityMetadata(plan1).getInputCardinality());
		Assert.assertEquals(new Double(100.0), cardinalityEstimator.getCardinalityMetadata(plan1).getOutputCardinality());
		
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		Assert.assertEquals(new Double(1200.0), cost);
	}
	
	@Test public void test3() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);
		
		SelectionTerm selectionTerm = SelectionTerm.create(ConstantEqualityCondition.create(0, TypedConstant.create("1")), access1);
	
		DependentJoinTerm plan1 = DependentJoinTerm.create(selectionTerm, access2);
		
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);		
		cardinalityEstimator.estimateCardinalityIfNeeded(plan1);
		
		Assert.assertEquals(new Double(0.0), cardinalityEstimator.getCardinalityMetadata(access1).getInputCardinality());
		Assert.assertEquals(new Double(100.0), cardinalityEstimator.getCardinalityMetadata(access1).getOutputCardinality());
		
		Assert.assertEquals(new Double(0.0), cardinalityEstimator.getCardinalityMetadata(selectionTerm).getInputCardinality());
		Assert.assertEquals(new Double(10.0), cardinalityEstimator.getCardinalityMetadata(selectionTerm).getOutputCardinality());
		
		Assert.assertEquals(new Double(10.0), cardinalityEstimator.getCardinalityMetadata(access2).getInputCardinality());
		Assert.assertEquals(new Double(1.0), cardinalityEstimator.getCardinalityMetadata(access2).getOutputCardinality());
		
		Assert.assertEquals(new Double(0.0), cardinalityEstimator.getCardinalityMetadata(plan1).getInputCardinality());
		Assert.assertEquals(new Double(10.0), cardinalityEstimator.getCardinalityMetadata(plan1).getOutputCardinality());
		
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		Assert.assertEquals(new Double(390), cost);
	}
	
}
