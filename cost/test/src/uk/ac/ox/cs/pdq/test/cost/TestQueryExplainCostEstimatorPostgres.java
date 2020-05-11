package uk.ac.ox.cs.pdq.test.cost;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.cost.DoubleCost;
import uk.ac.ox.cs.pdq.cost.estimators.QueryExplainCostEstimator;
import uk.ac.ox.cs.pdq.cost.io.jaxb.CostIOManager;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseManager;
import uk.ac.ox.cs.pdq.reasoningdatabase.DatabaseParameters;
import uk.ac.ox.cs.pdq.reasoningdatabase.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the QueryExplainCostEstimator class by creating ad hoc plans and queries.  
 * @author gabor
 *
 */
public class TestQueryExplainCostEstimatorPostgres extends PdqTest {

	/**
	 * This test creates a plan like: Join
	 * {[(#0=#3)]Rename{[c1,c2,c3]Access{R2.mt_0[]}},Rename{[c1,c4,c5]Access{R2.mt_1[#0=a]}}}
	 * And evaluates its cost.
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void getCostOfPlanTest1() throws DatabaseException {
		DatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		Schema s = getScenario1().getSchema();
		dm.initialiseDatabaseForSchema(s);
		QueryExplainCostEstimator estimator = new QueryExplainCostEstimator(dm);

		Attribute[] ra1 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c2"),
				Attribute.create(String.class, "c3") };
		RelationalTerm a1 = AccessTerm.create(s.getRelation("R2"), s.getRelation("R0").getAccessMethod("mt_0"));
		RelationalTerm r1 = RenameTerm.create(ra1, a1);

		Attribute[] ra2 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c4"),
				Attribute.create(String.class, "c3") };
		Map<Integer, TypedConstant> map2 = new HashMap<>();
		map2.put(0, TypedConstant.create("a"));
		RelationalTerm a2 = AccessTerm.create(s.getRelation("R2"), s.getRelation("R1").getAccessMethod("mt_1"), map2);
		RelationalTerm c2 = RenameTerm.create(ra2, a2);

		RelationalTerm plan = JoinTerm.create(r1, c2);
		RelationalTerm plan2 = ProjectionTerm.create(new Attribute[] { Attribute.create(String.class, "c1") }, plan);

		DoubleCost res = (DoubleCost) estimator.cost(plan2);
		Assert.assertEquals(21.26, res.getCost(), 0.01);
	}

	@Test
	public void getCostOfPlanTest1b() throws DatabaseException {
		DatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		Schema s = getScenario1().getSchema();
		dm.initialiseDatabaseForSchema(s);
		QueryExplainCostEstimator estimator = new QueryExplainCostEstimator(dm);

		RelationalTerm a1 = AccessTerm.create(s.getRelation("R0"), s.getRelation("R0").getAccessMethod("mt_0"));

		Map<Integer, TypedConstant> map2 = new HashMap<>();
		map2.put(0, TypedConstant.create("a"));
		RelationalTerm a2 = AccessTerm.create(s.getRelation("R1"), s.getRelation("R1").getAccessMethod("mt_1"), map2);

		RelationalTerm plan = JoinTerm.create(a1, a2);
		RelationalTerm plan2 = ProjectionTerm.create(new Attribute[] { s.getRelation("R1").getAttributes()[0] }, plan);

		DoubleCost res = (DoubleCost) estimator.cost(plan2);
		Assert.assertEquals(21.26, res.getCost(), 0.01);
	}

	private Schema convertTypesToString(Schema schema) {
		List<Dependency> dep = new ArrayList<>();
		dep.addAll(Arrays.asList(schema.getNonEgdDependencies()));
		dep.addAll(Arrays.asList(schema.getKeyDependencies()));
		Relation[] rels = schema.getRelations();
		for (int i = 0; i < rels.length; i++) {
			rels[i] = createDatabaseRelation(rels[i]);
		}
		return new Schema(rels, dep.toArray(new Dependency[dep.size()]));
	}

	/**
	 * Creates the db relation. Currently codes in the position numbers into the
	 * names, but this should change
	 *
	 * @param relation
	 *            the relation
	 * @return a new database relation with attributes x0,x1,...,x_{N-1}, Fact where
	 *         x_i maps to the i-th relation's attribute
	 */
	private Relation createDatabaseRelation(Relation relation) {
		Attribute[] attributes = new Attribute[relation.getArity()];
		for (int index = 0; index < relation.getArity(); index++) {
			Attribute attribute = relation.getAttribute(index);
			attributes[index] = Attribute.create(String.class, attribute.getName());
		}
		return Relation.create(relation.getName(), attributes, relation.getAccessMethods(), relation.isEquality());
	}

	@Test
	public void getCostOfPlanTest2() throws DatabaseException, JAXBException, FileNotFoundException {
		File planFile = new File("..//regression//test//planner//dag//fast//benchmark//case_001//expected-plan.xml");
		Schema s = IOManager
				.importSchema(new File("..//regression//test//planner//dag//fast//benchmark//case_001//schema.xml"));
		Schema sc = convertTypesToString(s);
		DatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		dm.initialiseDatabaseForSchema(sc);
		QueryExplainCostEstimator estimator = new QueryExplainCostEstimator(dm);

		RelationalTerm plan = CostIOManager.readRelationalTermFromRelationaltermWithCost(planFile, s);

		DoubleCost res = (DoubleCost) estimator.cost(plan);

		Assert.assertEquals(42.63, res.getCost(), 0.02);
	}

	@Test
	public void getCostOfCqTest() throws DatabaseException {
		DatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		dm.initialiseDatabaseForSchema(getScenario1().getSchema());
		QueryExplainCostEstimator estimator = new QueryExplainCostEstimator(dm);
		ConjunctiveQuery cq = getScenario1().getQuery();
		DoubleCost res = estimator.costQuery(cq);
		Assert.assertEquals(34.13, res.getCost(), 0.02);
	}

}
