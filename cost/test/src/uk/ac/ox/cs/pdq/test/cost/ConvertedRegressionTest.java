package uk.ac.ox.cs.pdq.test.cost;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * These test cases are converted from regression tests
 * 
 * @author Gabor
 *
 */
public class ConvertedRegressionTest extends PdqTest {

	@Mock
	protected SimpleCatalog catalog;

	/**
	 * This test is the same as the regression test:
	 * test\dag\fast\tpch\derby\fk_inputs\case_002. <br>
	 * The textbook cost calculation should result 322831.63820324163 (manually
	 * checked)
	 * 
	 * <pre>
	 * Tables: 
	 *	part(p_partkey,p_name,p_mfgr,p_brand,p_type,p_size,p_container,p_retailprice,p_comment)
	 *	partsupp(ps_partkey,ps_suppkey,ps_availqty,ps_supplycost,ps_comment)
	 *
	 * Dependency:
	 *  [_0, _1, _2, _3, _4]partsupp(_0,_1,_2,_3,_4)→[_6, _7, _8, _9, _10, _11, _12, _13](exists[_6,_7,_8,_9,_10,_11,_12,_13]part(_0,_6,_7,_8,_9,_10,_11,_12,_13))
	 *  
	 *  
	 * Plan:
	 * 
	 *  Project{[c20,c22,c25]DependentJoin{[(#0=#9)]Rename{[c13,c18,c19,c20,c21,c22,c23,c24,c25]Access{part.m2[]}},Rename{[c13,c14,c15,c16,c17]Access{partsupp.m4[#0=ps_partkey]}}}}
	 * </pre>
	 * 
	 */
	@Test
	public void fkInputsCase2() {
		Attribute attr[] = new Attribute[] { Attribute.create(Integer.class, "ps_partkey"), Attribute.create(Integer.class, "ps_suppkey"),
				Attribute.create(Integer.class, "ps_availqty"), Attribute.create(java.math.BigDecimal.class, "ps_supplycost"), Attribute.create(String.class, "ps_comment") };

		AccessMethod mthds1[] = new AccessMethod[] { AccessMethod.create("m4", new Integer[] { 0 }), AccessMethod.create("m5", new Integer[] { 1 }),
				AccessMethod.create("m6", new Integer[] { 0, 1 }) };
		Relation partsupp = Relation.create("partsupp", attr, mthds1);
		Attribute attr2[] = new Attribute[] { Attribute.create(Integer.class, "p_partkey"), Attribute.create(String.class, "p_name"), Attribute.create(String.class, "p_mfgr"),
				Attribute.create(String.class, "p_brand"), Attribute.create(String.class, "p_type"), Attribute.create(Integer.class, "p_size"),
				Attribute.create(String.class, "p_container"), Attribute.create(java.math.BigDecimal.class, "p_retailprice"), Attribute.create(String.class, "p_comment") };
		AccessMethod mthds2[] = new AccessMethod[] { AccessMethod.create("m2", new Integer[] {}) };
		Relation part = Relation.create("part", attr2, mthds2);
		TGD tgd = TGD.create(
				new Atom[] {
						Atom.create(partsupp, new Term[] { Variable.create("_0"), Variable.create("_1"), Variable.create("_2"), Variable.create("_3"), Variable.create("_4") }) },
				new Atom[] { Atom.create(part, new Term[] { Variable.create("_0"), Variable.create("_6"), Variable.create("_7"), Variable.create("_8"), Variable.create("_9"),
						Variable.create("_10"), Variable.create("_11"), Variable.create("_12"), Variable.create("_13") }) });
		Schema s = new Schema(new Relation[] { part, partsupp }, new Dependency[] { tgd });
		System.out.println("Schema : " + s);

		AccessTerm la = AccessTerm.create(part, mthds2[0]);
		AccessTerm ra = AccessTerm.create(partsupp, mthds1[0]);
		Attribute[] renamings1 = new Attribute[] { Attribute.create(String.class, "c13"), Attribute.create(String.class, "c18"), Attribute.create(String.class, "c19"),
				Attribute.create(String.class, "c20"), Attribute.create(String.class, "c21"), Attribute.create(String.class, "c22"), Attribute.create(String.class, "c23"),
				Attribute.create(String.class, "c24"), Attribute.create(String.class, "c25") };
		RenameTerm lr = RenameTerm.create(renamings1, la);
		Attribute[] renamings2 = new Attribute[] { Attribute.create(String.class, "c13"), Attribute.create(String.class, "c14"), Attribute.create(String.class, "c15"),
				Attribute.create(String.class, "c16"), Attribute.create(String.class, "c17") };
		RenameTerm rr = RenameTerm.create(renamings2, ra);
		DependentJoinTerm djt = DependentJoinTerm.create(lr, rr);
		Attribute[] projections = new Attribute[] { Attribute.create(String.class, "c20"), Attribute.create(String.class, "c22"), Attribute.create(String.class, "c25") };
		ProjectionTerm pt = ProjectionTerm.create(projections, djt);

		// Create the mock catalog object
		when(this.catalog.getCardinality(part)).thenReturn(200);
		when(this.catalog.getCardinality(partsupp)).thenReturn(700);
		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(pt);
		Assert.assertEquals(322831.63820324163, (double) cost.getValue(), 0.0001);

	}

	/**
	 * This test is the same as the regression test:
	 * test\dag\fast\tpch\derby\fk_inputs\case_003 <br>
	 * 
	 * Expected result: 7998.7450843781535 (manually checked)
	 * 
	 * <pre>
	 * Tables:
	 *  
	 * 		part(p_partkey,p_name,p_mfgr,p_brand,p_type,p_size,p_container,p_retailprice,p_comment) 
	 *  	region(r_regionkey,r_name,r_comment)	
	 *  	region(s_suppkey,s_name,s_address,s_nationkey,s_phone,s_acctbal,r_comment)
	 *  	region(n_nationkey,n_name,n_regionkey,n_comment)
	 *  	partsupp(ps_partkey,ps_suppkey,ps_availqty,ps_supplycost,ps_comment)
	 * 
	 * Dependency:
	 *  [_0, _1, _2, _3, _4]partsupp(_0,_1,_2,_3,_4)→[_6, _7, _8, _9, _10, _11, _12, _13](exists[_6,_7,_8,_9,_10,_11,_12,_13]part(_0,_6,_7,_8,_9,_10,_11,_12,_13))
	 *  
	 *  
	 * Plan:
	 * Project{[c23,c19,c17,c26]DependentJoin{[(#7=#15)]DependentJoin{[(#3=#10)]DependentJoin{[(#0=#5)]Rename{[c26,k36,k37]Access{region.mt_0[]}},Rename{[c21,c25,c26,c27]Access{region.m7[#2=n_regionkey]}}},Rename{[c15,c19,c20,c21,c22,c23,c24]Access{region.m3[#3=s_nationkey]}}},Rename{[c14,c15,c16,c17,c18]Access{partsupp.m5[#1=ps_suppkey]}}}}
	 * </pre>
	 */
	@Test
	public void fkInputsCase3() {
		Attribute attr[] = new Attribute[] { Attribute.create(Integer.class, "ps_partkey"), Attribute.create(Integer.class, "ps_suppkey"),
				Attribute.create(Integer.class, "ps_availqty"), Attribute.create(java.math.BigDecimal.class, "ps_supplycost"), Attribute.create(String.class, "ps_comment") };

		AccessMethod mthds1[] = new AccessMethod[] { AccessMethod.create("m4", new Integer[] { 0 }), AccessMethod.create("m5", new Integer[] { 1 }),
				AccessMethod.create("m6", new Integer[] { 0, 1 }) };
		Relation partsupp = Relation.create("partsupp", attr, mthds1);

		Attribute attrRegion[] = new Attribute[] { Attribute.create(Integer.class, "r_regionkey"), Attribute.create(String.class, "r_name"),
				Attribute.create(String.class, "r_comment") };
		Relation region = Relation.create("region", attrRegion, new AccessMethod[] { method0 });

		Attribute attrNation[] = new Attribute[] { Attribute.create(Integer.class, "n_nationkey"), Attribute.create(String.class, "n_name"),
				Attribute.create(Integer.class, "n_regionkey"), Attribute.create(String.class, "n_comment") };
		Relation nation = Relation.create("region", attrNation, new AccessMethod[] { AccessMethod.create("m7", new Integer[] { 2 }) });

		Attribute attrSupplier[] = new Attribute[] { Attribute.create(Integer.class, "s_suppkey"), Attribute.create(String.class, "s_name"),
				Attribute.create(String.class, "s_address"), Attribute.create(Integer.class, "s_nationkey"), Attribute.create(String.class, "s_phone"),
				Attribute.create(String.class, "s_acctbal"), Attribute.create(String.class, "r_comment") };
		Relation supplier = Relation.create("region", attrSupplier, new AccessMethod[] { AccessMethod.create("m3", new Integer[] { 3 }) });

		Attribute attr2[] = new Attribute[] { Attribute.create(Integer.class, "p_partkey"), Attribute.create(String.class, "p_name"), Attribute.create(String.class, "p_mfgr"),
				Attribute.create(String.class, "p_brand"), Attribute.create(String.class, "p_type"), Attribute.create(Integer.class, "p_size"),
				Attribute.create(String.class, "p_container"), Attribute.create(java.math.BigDecimal.class, "p_retailprice"), Attribute.create(String.class, "p_comment") };
		AccessMethod mthds2[] = new AccessMethod[] { AccessMethod.create("m2", new Integer[] {}) };
		Relation part = Relation.create("part", attr2, mthds2);
		TGD tgd = TGD.create(
				new Atom[] {
						Atom.create(partsupp, new Term[] { Variable.create("_0"), Variable.create("_1"), Variable.create("_2"), Variable.create("_3"), Variable.create("_4") }) },
				new Atom[] { Atom.create(part, new Term[] { Variable.create("_0"), Variable.create("_6"), Variable.create("_7"), Variable.create("_8"), Variable.create("_9"),
						Variable.create("_10"), Variable.create("_11"), Variable.create("_12"), Variable.create("_13") }) });
		Schema s = new Schema(new Relation[] { part, region, supplier, nation, partsupp }, new Dependency[] { tgd });
		System.out.println("Schema : " + s);
		AccessTerm ra = AccessTerm.create(partsupp, mthds1[1]);
		Attribute[] renamings2 = new Attribute[] { Attribute.create(String.class, "c14"), Attribute.create(String.class, "c15"), Attribute.create(String.class, "c16"),
				Attribute.create(String.class, "c17"), Attribute.create(String.class, "c18") };
		RenameTerm renamePartsupp = RenameTerm.create(renamings2, ra);
		Attribute[] renamingsSup = new Attribute[] { Attribute.create(String.class, "c15"), Attribute.create(String.class, "c19"), Attribute.create(String.class, "c20"),
				Attribute.create(String.class, "c21"), Attribute.create(String.class, "c22"), Attribute.create(String.class, "c23"), Attribute.create(String.class, "c24") };
		Attribute[] renamingsNation = new Attribute[] { Attribute.create(String.class, "c21"), Attribute.create(String.class, "c25"), Attribute.create(String.class, "c26"),
				Attribute.create(String.class, "c27") };

		Attribute[] renamingsRegion = new Attribute[] { Attribute.create(String.class, "c26"), Attribute.create(String.class, "k36"), Attribute.create(String.class, "k37") };

		RenameTerm renameSupplier = RenameTerm.create(renamingsSup, AccessTerm.create(supplier, supplier.getAccessMethod("m3")));
		RenameTerm renameNation = RenameTerm.create(renamingsNation, AccessTerm.create(nation, nation.getAccessMethod("m7")));
		RenameTerm renameRegion = RenameTerm.create(renamingsRegion, AccessTerm.create(region, region.getAccessMethod("mt_0")));

		DependentJoinTerm djt1 = DependentJoinTerm.create(renameRegion, renameNation);

		DependentJoinTerm djt2 = DependentJoinTerm.create(djt1, renameSupplier);
		DependentJoinTerm djt3 = DependentJoinTerm.create(djt2, renamePartsupp);

		Attribute[] projections = new Attribute[] { Attribute.create(String.class, "c23"), Attribute.create(String.class, "c19"), Attribute.create(String.class, "c17"),
				Attribute.create(String.class, "c26") };
		ProjectionTerm pt = ProjectionTerm.create(projections, djt3);
		// Create the mock catalog object
		when(this.catalog.getCardinality(part)).thenReturn(200);
		when(this.catalog.getCardinality(partsupp)).thenReturn(700);
		when(this.catalog.getCardinality(nation)).thenReturn(25);
		when(this.catalog.getCardinality(supplier)).thenReturn(10);
		when(this.catalog.getCardinality(region)).thenReturn(5);

		NaiveCardinalityEstimator cardinalityEstimator = new NaiveCardinalityEstimator(this.catalog);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, cardinalityEstimator);
		Cost cost = estimator.cost(pt);
		Assert.assertEquals(7998.7450843781535, (double) cost.getValue(), 0.0001);

	}

}
