package uk.ac.ox.cs.pdq.test.cost;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.cost.estimators.FixedCostPerAccessCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;

/**
 * @author Gabor
 *
 */
public class SimpleCatalogTest {

	@Test
	public void case1() {
		AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {0});
		AccessMethod am2 = AccessMethod.create("access_method2",new Integer[] {0});
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "r1_attribute")},
				new AccessMethod[] {am1});
		Relation relation2 = Relation.create("R2", new Attribute[] {Attribute.create(Integer.class, "r2_attribute")},
				new AccessMethod[] {am2});
		Schema schema = new Schema(new Relation[] {relation1,relation2});
		try {
			schema.addConstants(Arrays.asList(new TypedConstant[] {TypedConstant.create("C1"),TypedConstant.create("C1")}));
			SimpleCatalog catalog = new SimpleCatalog(schema, "test//catalog.properties");
			Assert.assertNotNull(catalog);
			Assert.assertTrue(13.0 == catalog.getCost(relation1, am1));
			Assert.assertTrue(15.0 == catalog.getCost(relation2, am2));
			
			
			AccessTerm at = AccessTerm.create(relation1, am1);
			AccessTerm at1 = AccessTerm.create(relation2, am2);
			
			FixedCostPerAccessCostEstimator est = new FixedCostPerAccessCostEstimator(null, catalog);
			Assert.assertTrue(13.0 == est.cost(at).getCost());
			Assert.assertTrue(15.0 == est.cost(at1).getCost());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	@Test
	public void case2() {
		AccessMethod am1 = AccessMethod.create("access_method1",new Integer[] {0});
		AccessMethod am2 = AccessMethod.create("access_method2",new Integer[] {0});
		Relation relation1 = Relation.create("R1", new Attribute[] {Attribute.create(Integer.class, "r1_attribute")},
				new AccessMethod[] {am1});
		Relation relation2 = Relation.create("R2", new Attribute[] {Attribute.create(Integer.class, "r2_attribute")},
				new AccessMethod[] {am2});
		Schema schema = new Schema(new Relation[] {relation1,relation2});
		try {
			schema.addConstants(Arrays.asList(new TypedConstant[] {TypedConstant.create("C1"),TypedConstant.create("C1")}));
			SimpleCatalog catalog = new SimpleCatalog(schema, "test//catalog.properties");
			Assert.assertNotNull(catalog);
			Assert.assertTrue(13.0 == catalog.getCost(relation1, am1));
			Assert.assertTrue(15.0 == catalog.getCost(relation2, am2));
			
			AccessTerm at = AccessTerm.create(relation1, am1);
			AccessTerm at1 = AccessTerm.create(relation2, am2);
			
			FixedCostPerAccessCostEstimator est = new FixedCostPerAccessCostEstimator(null, catalog);
			Assert.assertTrue(13.0 == est.cost(at).getCost());
			Assert.assertTrue(15.0 == est.cost(at1).getCost());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
}
