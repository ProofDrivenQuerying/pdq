// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Test for the NaiveCardinalityEstimator class. Mainly uses the yashoo example
 * as input.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

public class TestNaiveCardinalityEstimator extends PdqTest {
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
	 * 
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
		// Create the mock catalog object
		when(this.catalog.getCardinality(this.R)).thenReturn(100);
		when(this.catalog.getCardinality(this.S)).thenReturn(100);
	}

	/**
	 * Asserts the input and output cardinalities of the plan:
	 * 
	 * <pre>
	 * DependentJoin{[(#5=#9)]
	 * 		DependentJoin{[(#2=#4)]
	 * 			Rename{[dummy1,dummy2,c1]
	 * 				Access{YahooPlaceCode.mt_2[#0=iso,#1=FR]}},
	 * 			Rename{[dummy3,c1,c2,c3,c4,c5]
	 * 				Access{YahooPlaceRelationship.mt_2[#0=children,#1=a1]}}},
	 * 		Rename{[c2,c6,c7,c8,c9,c10,c11,c12,c13,c14,c15,c16,c17,c18,c19,c20,c21,c22,c23,c24,c25]
	 * 			Access{YahooWeather.mt_1[#0=a0]}}}
	 * </pre>
	 * Hand counted expected results: 
	 *  - input 0
	 *  - output 1.0E15
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

		cardinalityEstimator.estimateCardinality(plan2);
		double input = cardinalityEstimator.getCardinalityMetadata(plan2).getInputCardinality();
		double output = cardinalityEstimator.getCardinalityMetadata(plan2).getOutputCardinality();

		Assert.assertEquals(0.0, input, 0.0001);
		Assert.assertEquals(1.0E15, output, 0.0001);

	}

	/**
	 * Asserts the input and output cardinalities of the plan:
	 * 
	 * <pre>
	 * Access{YahooPlaces.mt_3[#1=Eiffel Tower]}
	 * </pre>
	 * Hand counted expected results: 
	 *  - input 0
	 *  - output 1000000
	 * 
	 */
	@Test
	public void test2() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(1, TypedConstant.create("Eiffel Tower"));
		AccessTerm access1 = AccessTerm.create(this.YahooPlaces, this.method3, inputConstants1);
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);

		cardinalityEstimator.estimateCardinality(access1);
		double input = cardinalityEstimator.getCardinalityMetadata(access1).getInputCardinality();
		double output = cardinalityEstimator.getCardinalityMetadata(access1).getOutputCardinality();

		Assert.assertEquals(0.0, input, 0.0001);
		Assert.assertEquals(1000000.0, output, 0.0001);

	}

	/**
	 * Asserts the input and output cardinalities of the plan:
	 * <pre>
	 * DependentJoin{[(#1=#3&#2=#4)]
	 * 		Access{R.mt_0[]},
	 * 		Access{S.mt_1[#0=b]}}
	 * </pre>
	 * 
	 * And input accesses:
	 * <pre>
	 * Access{R.mt_0[]}
	 * Access{S.mt_1[#0=b]}
	 * </pre>
	 * 
	 * Asserts if we get back the cost given in the catalog. 
	 */
	@Test
	public void test3() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		cardinalityEstimator.estimateCardinalityIfNeeded(plan1);

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(access1).getInputCardinality());
		Assert.assertEquals((Double)(100.0), cardinalityEstimator.getCardinalityMetadata(access1).getOutputCardinality());

		Assert.assertEquals((Double)(100.0), cardinalityEstimator.getCardinalityMetadata(access2).getInputCardinality());
		Assert.assertEquals((Double)(10.0), cardinalityEstimator.getCardinalityMetadata(access2).getOutputCardinality());

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(plan1).getInputCardinality());
		Assert.assertEquals((Double)(100.0), cardinalityEstimator.getCardinalityMetadata(plan1).getOutputCardinality());
	}

	/**
	 * Asserts the input and output cardinalities of the plan:
	 * 
	 * <pre>
	 * DependentJoin{[(#1=#3&#2=#4)]
	 * 		Access{R.mt_0[]},
	 * 		Access{S.mt_2[#0=b,#1=c]}}
	 * </pre>
	 * Asserts if we get back the cost given in the catalog. 
	 */
	@Test
	public void test4() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		cardinalityEstimator.estimateCardinalityIfNeeded(plan1);

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(access1).getInputCardinality());
		Assert.assertEquals((Double)(100.0), cardinalityEstimator.getCardinalityMetadata(access1).getOutputCardinality());

		Assert.assertEquals((Double)(100.0), cardinalityEstimator.getCardinalityMetadata(access2).getInputCardinality());
		Assert.assertEquals((Double)(1.0), cardinalityEstimator.getCardinalityMetadata(access2).getOutputCardinality());

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(plan1).getInputCardinality());
		Assert.assertEquals((Double)(100.0), cardinalityEstimator.getCardinalityMetadata(plan1).getOutputCardinality());
	}

	/**
	 * Asserts the input and output cardinalities of the plan:
	 * 
	 * <pre>
	 * DependentJoin{[(#1=#3&#2=#4)]
	 * 		Select{[#0=1]
	 * 			Access{R.mt_0[]}},
	 * 		Access{S.mt_2[#0=b,#1=c]}}
	 * </pre>
	 * Asserts if we get back the cost given in the catalog. 
	 */
	@Test
	public void test5() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method2);
		SelectionTerm selectionTerm = SelectionTerm.create(ConstantEqualityCondition.create(0, TypedConstant.create(1)), access1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(selectionTerm, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		cardinalityEstimator.estimateCardinalityIfNeeded(plan1);

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(access1).getInputCardinality());
		Assert.assertEquals((Double)(100.0), cardinalityEstimator.getCardinalityMetadata(access1).getOutputCardinality());

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(selectionTerm).getInputCardinality());
		Assert.assertEquals((Double)(10.0), cardinalityEstimator.getCardinalityMetadata(selectionTerm).getOutputCardinality());

		Assert.assertEquals((Double)(10.0), cardinalityEstimator.getCardinalityMetadata(access2).getInputCardinality());
		Assert.assertEquals((Double)(1.0), cardinalityEstimator.getCardinalityMetadata(access2).getOutputCardinality());

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(plan1).getInputCardinality());
		Assert.assertEquals((Double)(10.0), cardinalityEstimator.getCardinalityMetadata(plan1).getOutputCardinality());
	}

	/**
	 * Asserts the input and output cardinalities of the plan:
	 * 
	 * <pre>
	 * Access{R.mt_1[#0=1]}
	 * </pre>
	 * Asserts if we get back the cost given in the catalog. 
	 */
	@Test
	public void test6() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(0, TypedConstant.create(TypedConstant.create(1)));
		AccessTerm access1 = AccessTerm.create(this.R, this.method1, inputConstants1);
		AccessTerm access2 = AccessTerm.create(this.S, this.method1);
		DependentJoinTerm plan1 = DependentJoinTerm.create(access1, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		cardinalityEstimator.estimateCardinalityIfNeeded(plan1);

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(access1).getInputCardinality());
		Assert.assertEquals((Double)(10.0), cardinalityEstimator.getCardinalityMetadata(access1).getOutputCardinality());
	}

	/**
	 * Asserts the input and output cardinalities of the plan:
	 * 
	 * <pre>
	 * Join{[(#1=#3&#2=#4)]
	 * 		Access{R.mt_0[]},
	 * 		Access{S.mt_0[]}}
	 * </pre>
	 * Asserts if we get back the cost given in the catalog. 
	 */
	@Test
	public void test7() {
		AccessTerm access1 = AccessTerm.create(this.R, this.method0);
		AccessTerm access2 = AccessTerm.create(this.S, this.method0);
		JoinTerm plan1 = JoinTerm.create(access1, access2);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		cardinalityEstimator.estimateCardinalityIfNeeded(plan1);

		Assert.assertEquals((Double)(0.0), cardinalityEstimator.getCardinalityMetadata(plan1).getInputCardinality());
		Assert.assertEquals((Double)(100.0), cardinalityEstimator.getCardinalityMetadata(plan1).getOutputCardinality());
	}

}
