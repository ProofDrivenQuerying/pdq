package uk.ac.ox.cs.pdq.test.algebra;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTermAsLogic;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.databasemanagement.DatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the toLogic function of the RelationalTerm class and the RelationalTermAsLogic class.
 * @author gabor
 *
 */
public class RelationalTermAsLogicTest extends PdqTest {
	/**
	 * This test creates a plan like: 
	 * Project{[c1]Join{[(#0=#3&#2=#5)]Rename{[c1,c2,c3]Access{R2.mt_0[]}},Rename{[c1,c4,c3]Access{R2.mt_1[#0=a]}}}}
	 * And evaluates asserts the toLogic results are correct.
	 * 
	 * @throws DatabaseException
	 */
	@Test
	public void testToLogic() throws DatabaseException {
		DatabaseManager dm = new ExternalDatabaseManager(DatabaseParameters.Postgres);
		Schema s = getScenario1().getSchema();
		dm.initialiseDatabaseForSchema(s);
		Attribute[] ra1 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c2"),
				Attribute.create(String.class, "c3") };
		RelationalTerm a1 = new AccessTerm(s.getRelation("R0").getAccessMethod("mt_0"));
		RelationalTerm r1 = new RenameTerm(ra1, a1);

		Attribute[] ra2 = new Attribute[] { Attribute.create(String.class, "c1"), Attribute.create(String.class, "c4"),
				Attribute.create(String.class, "c3") };
		Map<Attribute, TypedConstant> map2 = new HashMap<>();
		TypedConstant constant = TypedConstant.create("a");
		map2.put(s.getRelation("R1").getAttribute(0), constant);
		RelationalTerm a2 = new AccessTerm(s.getRelation("R1").getAccessMethod("mt_1"), map2);
		RelationalTerm c2 = new RenameTerm(ra2, a2);

		RelationalTerm plan = new JoinTerm(r1, c2);
		Attribute c1 = Attribute.create(String.class, "c1") ;
		RelationalTerm plan2 = new ProjectionTerm(new Attribute[] { c1}, plan);
		
		RelationalTermAsLogic asLogic = plan2.toLogic();
		Assert.assertNotNull(asLogic.getFormula());
		Assert.assertNotNull(asLogic.getMapping());
		Assert.assertTrue(asLogic.getMapping().keySet().contains(c1));
		constant.equals(asLogic.getFormula().getTerms()[0]);
		Assert.assertTrue(Arrays.asList(asLogic.getFormula().getTerms()).contains(constant));
		
	}

}
