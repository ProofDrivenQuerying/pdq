package uk.ac.ox.cs.pdq.test.cost.estimators.statistics.estimators;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.estimators.NaiveCardinalityEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.PerInputCostEstimator;
import uk.ac.ox.cs.pdq.cost.estimators.TextBookCostEstimator;
import uk.ac.ox.cs.pdq.cost.statistics.SimpleCatalog;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

public class YahooTestCases {

	private Schema schema;
	private SimpleCatalog catalog;

	@Before
	public void setup() throws FileNotFoundException, JAXBException {
		this.schema = IOManager.importSchema(new File("test//resources//Yahoo//schema.xml"));
		this.catalog = new SimpleCatalog(schema,"test//resources//Yahoo//catalog.properties");
	}
	
	
	/**
	 * Clone of the regression test: \pdq\regression\test\linear\fast\demo\derby\case_002\
	 * Tests only the cost package.  
	 */
	@Test
	public void case2() {
		Map<Integer, TypedConstant> inputConstants1 = new HashMap<>();
		inputConstants1.put(2, TypedConstant.create("Eiffel Tower"));
		Relation places = this.schema.getRelation("YahooPlaces");
		AccessTerm access1 = AccessTerm.create(places , places.getAccessMethod("yh_geo_name"), inputConstants1);
		TextBookCostEstimator estimator = new TextBookCostEstimator(null, new NaiveCardinalityEstimator(this.catalog));
		Cost cost = estimator.cost(access1);
		PerInputCostEstimator est = new PerInputCostEstimator(null, catalog);
		Cost cost1 = est.cost(access1);
		System.out.println("TextBookCostEstimator:" + cost);
		System.out.println("PerInputCostEstimator:" + cost1);
		NaiveCardinalityEstimator nce = new NaiveCardinalityEstimator(catalog);
		nce.estimateCardinality(access1);
		System.out.println("NaiveCardinalityEstimator:" + new NaiveCardinalityEstimator(catalog).getCardinalityMetadata(access1).getOutputCardinality());
	}
}
