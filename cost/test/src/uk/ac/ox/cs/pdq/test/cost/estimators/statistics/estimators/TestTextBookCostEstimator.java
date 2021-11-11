// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.util.PdqTest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Tests the TextBookCostEstimator mainly using the yahoo example schema.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

public class TestTextBookCostEstimator extends PdqTest {
	protected Attribute[] attributes3Old = new Attribute[3];
	protected Attribute[] attributes6Old = new Attribute[6];
	protected Attribute[] attributes18Old = new Attribute[18];
	protected Attribute[] attributes21Old = new Attribute[21];
	protected Attribute[] newAttributes = new Attribute[30];

	protected Relation YahooPlaceCode;
	protected Relation YahooPlaceRelationship;
	protected Relation YahooWeather;
	protected Relation YahooPlaces;

	@Mock
	protected SimpleCatalog catalog;

	/**
	 * Setup.
	 * @throws Exception 
	 */
	@Before
	public void setup() throws Exception {
		super.setup();
		for (int index = 0; index < attributes3Old.length; ++index)
			this.attributes3Old[index] = Attribute.create(String.class, "a" + index);

		for (int index = 0; index < attributes6Old.length; ++index)
			this.attributes6Old[index] = Attribute.create(String.class, "a" + index);

		for (int index = 0; index < attributes18Old.length; ++index)
			this.attributes18Old[index] = Attribute.create(String.class, "a" + index);

		for (int index = 0; index < attributes21Old.length; ++index)
			this.attributes21Old[index] = Attribute.create(String.class, "a" + index);

		for (int index = 0; index < newAttributes.length; ++index)
			this.newAttributes[index] = Attribute.create(String.class, "c" + index);

		this.YahooPlaceCode = Relation.create("YahooPlaceCode", this.attributes3Old, new AccessMethodDescriptor[] { this.method2 });
		this.YahooPlaceRelationship = Relation.create("YahooPlaceRelationship", this.attributes6Old, new AccessMethodDescriptor[] { this.method2 });
		this.YahooWeather = Relation.create("YahooWeather", this.attributes21Old, new AccessMethodDescriptor[] { this.method1 });
		this.YahooPlaces = Relation.create("YahooPlaces", this.attributes18Old, new AccessMethodDescriptor[] { this.method3 });

		// Create the mock catalog object
		when(this.catalog.getCardinality(this.YahooPlaceCode)).thenReturn(10000000);
		when(this.catalog.getCardinality(this.YahooPlaceRelationship)).thenReturn(10000000);
		when(this.catalog.getCardinality(this.YahooWeather)).thenReturn(100000000);
		when(this.catalog.getCardinality(this.YahooPlaces)).thenReturn(10000000);

		this.R = Relation.create("R", new Attribute[] { a, b, c }, new AccessMethodDescriptor[] { this.method0, this.method2 });
		this.S = Relation.create("S", new Attribute[] { b, c }, new AccessMethodDescriptor[] { this.method0, this.method1, this.method2 });
		// Create the mock catalog object
		when(this.catalog.getCardinality(this.R)).thenReturn(100);
		when(this.catalog.getCardinality(this.S)).thenReturn(100);
	}

	/**
	 * Creates plan
	 * <pre> 
	 * 	DependentJoin{[(#5=#9)]
	 * 		DependentJoin{[(#2=#4)]
	 * 			Rename{[dummy1,dummy2,c1]
	 * 				Access{YahooPlaceCode.mt_2[#0=iso,#1=FR]}},
	 * 			Rename{[dummy3,c1,c2,c3,c4,c5]
	 * 				Access{YahooPlaceRelationship.mt_2[#0=children,#1=a1]}}},
	 * 		Rename{[c2,c6,c7,c8,c9,c10,c11,c12,c13,c14,c15,c16,c17,c18,c19,c20,c21,c22,c23,c24,c25]
	 * 			Access{YahooWeather.mt_1[#0=a0]}}}
	 * </pre>
	 * And evaluates its cost using the TextBookCostEstimator.
	 * 
	 * The correct value was created by old regression tests in review package before algebra-changes (not sure)
	 *  
	 */
	@Test
	public void test1() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create("iso"));
		inputConstants1.put(1, TypedConstant.create("FR"));
		AccessTerm access1 = AccessTerm.create(this.YahooPlaceCode, this.method2, inputConstants1);
		Map<Integer, TypedConstant> inputConstants2 = new HashMap<>();
		inputConstants2.put(0, TypedConstant.create("children"));
		AccessTerm access2 = AccessTerm.create(this.YahooPlaceRelationship, this.method2, inputConstants2);
		AccessTerm access3 = AccessTerm.create(this.YahooWeather, this.method1);

		Attribute[] renamings1 = new Attribute[] { Attribute.create(String.class, "dummy1"), Attribute.create(String.class, "dummy2"), Attribute.create(String.class, "c1") };
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
		TextBookCostEstimator estimator = new TextBookCostEstimator(cardinalityEstimator);
		Cost cost = estimator.cost(plan2);
		Assert.assertEquals(3.3958007884803287E18, (double) cost.getValue(), 0.0001);
	}

	/**
	 * Creates simple access plan
	 * <pre>
	 * Access{YahooPlaces.mt_3[#1=Eiffel Tower]}
	 * </pre>
	 * And evaluates its cost using the TextBookCostEstimator.
	 * The correct value was created by old regression tests in review package before algebra-changes (not sure)
	 */
	@Test
	public void test2() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(1, TypedConstant.create("Eiffel Tower"));
		AccessTerm access1 = AccessTerm.create(this.YahooPlaces, this.method3, inputConstants1);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(cardinalityEstimator);
		Cost cost = estimator.cost(access1);

		Assert.assertEquals(2.4867919004335693E8, (double) cost.getValue(), 0.0001);
	}
	
	/**
	 * Creates plan
	 * <pre>
	 *  DependentJoin{[(#1=#3&#2=#4)]
	 *  	Access{R.mt_0[]},
	 *  	Access{S.mt_1[#0=b]}}
	 * </pre>
	 * And evaluates its cost using the TextBookCostEstimator.
	 * The correct value was created by old regression tests in review package before algebra-changes (not sure)
	 */
	@Test
	public void test3() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(cardinalityEstimator);
		Cost cost = estimator.cost(plan1);

		Assert.assertEquals(6186.72124178452, (double) cost.getValue(), 0.0001);
	}

	/**
	 * Creates plan
	 * <pre>
	 *  DependentJoin{[(#1=#3&#2=#4)]
	 *  	Access{R.mt_0[]},
	 *  	Access{S.mt_2[#0=b,#1=c]}}
	 * </pre>
	 * And evaluates its cost using the TextBookCostEstimator.
	 * The correct value was created by old regression tests in review package before algebra-changes (not sure)
	 */
	@Test
	public void test4() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(cardinalityEstimator);
		Cost cost = estimator.cost(plan1);

		Assert.assertEquals(1781.5510557964276, (double) cost.getValue(), 0.0001);
	}

	/**
	 * Creates plan
	 * <pre>
	 *  DependentJoin{[(#1=#3&#2=#4)]
	 *  	Select{[#0=1]
	 *  		Access{R.mt_0[]}},
	 *  	Access{S.mt_2[#0=b,#1=c]}}
	 * </pre>
	 * And evaluates its cost using the TextBookCostEstimator.
	 * The correct value was created by old regression tests in review package before algebra-changes (not sure)
	 */
	@Test
	public void test5() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);
		SelectionTerm selectionTerm = SelectionTerm.create(ConstantEqualityCondition.create(0, TypedConstant.create(1)), access1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(selectionTerm, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(cardinalityEstimator);
		Cost cost = estimator.cost(plan1);
		Assert.assertEquals(1431.5510557964276, (double) cost.getValue(), 0.0001);
	}

	/**
	 * Creates plan
	 * <pre>
	 *  DependentJoin{[(#1=#3&#2=#4)]
	 *  	Select{[#0=1]
	 *  		Access{R.mt_0[]}},
	 *  	Access{S.mt_2[#0=b,#1=c]}}
	 * </pre>
	 * And evaluates its cost using the TextBookCostEstimator.
	 * The correct value was created by old regression tests in review package before algebra-changes (not sure)
	 */
	@Test
	public void test6() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(TypedConstant.create(1)));
		AccessTerm access1 = AccessTerm.create(this.R, this.method1, inputConstants1);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(cardinalityEstimator);
		Cost cost = estimator.cost(plan1);

		Assert.assertEquals(549.5945713886306, (double) cost.getValue(), 0.0001);
	}

	/**
	 * Creates plan
	 * <pre>
	 *  Join{[(#1=#3&#2=#4)]
	 *  	Access{R.mt_0[]},
	 *  	Access{S.mt_0[]}}
	 * </pre>
	 * And evaluates its cost using the TextBookCostEstimator.
	 * The correct value was created by old regression tests in review package before algebra-changes (not sure)
	 */
	@Test
	public void test7() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method0);
		JoinTerm plan1 = JoinTerm.create(access1, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(cardinalityEstimator);
		Cost cost = estimator.cost(plan1);

		Assert.assertEquals(93684.95477555826, (double) cost.getValue(), 0.0001);
	}

	
}
