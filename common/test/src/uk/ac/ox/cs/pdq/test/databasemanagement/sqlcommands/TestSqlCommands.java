package uk.ac.ox.cs.pdq.test.databasemanagement.sqlcommands;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.BasicSelect;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.BulkInsert;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Command;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.CreateDatabase;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.CreateTable;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Delete;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.DifferenceQuery;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.DropDatabase;
import uk.ac.ox.cs.pdq.databasemanagement.sqlcommands.Insert;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/** This class has a test case for each command object in the sqlcommands package.
 * @author Gabor
 *
 */
public class TestSqlCommands extends PdqTest {
	private final String databaseNameKeyWord = "databaseNameKeyWord";
	/**
	 * <pre>
	 * Schema has 3 relations : R0(a,b,c) where a,b,c are integer attributes, with a
	 * free access method <br>
	 * R1(a,b,c) where a,b,c are integer attributes, with one access method that
	 * needs input on the second attribute<br>
	 * R2(a,b,c) where a,b,c are integer attributes, with one access method that
	 * needs input on the third attribute.<br>
	 * In this scenario there are no dependencies. <br>
	 * The query is Q(x,y,z) = R0(x,y1,z1) R1(x,y,z2) R2(x1,y,z)
	 * @throws DatabaseException 
	 * 
	 */
	@Test
	public void testBasicSelect() throws DatabaseException {
		TestScenario sc = getScenario1();
		BasicSelect bs = new BasicSelect(sc.getSchema(),sc.getQuery());
		
		Assert.assertEquals(sc.getQuery(), bs.getFormula());
		Assert.assertEquals(Arrays.asList(sc.getQuery().getFreeVariables()), Arrays.asList(bs.getResultTerms()));
		Assert.assertTrue(bs.toDerbyStatement("123").size()==1);
		Assert.assertTrue(bs.toDerbyStatement("123").get(0).length()>50);
	
	}
	
	@Test
	public void testBulkInsert() throws DatabaseException {
		TestScenario sc = getScenario1();
		BulkInsert bi = new BulkInsert(sc.getExampleAtoms1(),sc.getSchema());
		Assert.assertNotNull(bi);
		Assert.assertEquals(sc.getExampleAtoms1().size(), bi.toDerbyStatement("123").size());
		Assert.assertEquals(3, bi.toMySqlStatement("123").size());
		Assert.assertTrue(bi.toDerbyStatement("123").get(0).length()>20);
	}
	@Test
	public void testCommand() throws DatabaseException {
		Command c = new Command("hello |" + Command.DATABASENAME + "| end.");
		List<List<String>> statements = new ArrayList<>();
		statements.add(c.toDerbyStatement(databaseNameKeyWord));
		statements.add(c.toMySqlStatement(databaseNameKeyWord));
		statements.add(c.toPostgresStatement(databaseNameKeyWord));
		for (List<String> st:statements) {
			Assert.assertEquals(1,st.size());
			Assert.assertTrue(st.get(0).contains(databaseNameKeyWord));
			Assert.assertTrue(st.get(0).contains("|"+databaseNameKeyWord+"|"));
			Assert.assertTrue(st.get(0).contains("hello"));
			Assert.assertTrue(st.get(0).contains("end."));
		}
		
	}
	@Test
	public void testCreateDatabase() {
		CreateDatabase d = new CreateDatabase(getScenario1().getSchema());
		List<List<String>> statements = new ArrayList<>();
		Assert.assertEquals(1,d.toDerbyStatement(databaseNameKeyWord).size());
		statements.add(d.toMySqlStatement(databaseNameKeyWord));
		statements.add( d.toPostgresStatement(databaseNameKeyWord));
		for (List<String> st:statements) {
			Assert.assertTrue(st.size() > 1);
			Assert.assertTrue(st.get(0).contains(databaseNameKeyWord));
		}
	}
	@Test
	public void testCreateTable() throws DatabaseException {

		CreateTable cr = new CreateTable(new Relation[] {R,S,T});
		
		// have to have 3 table + 3 constraints
		Assert.assertEquals(6,cr.toDerbyStatement(databaseNameKeyWord ).size());
		// have to have 3 table + 3 constraints
		Assert.assertEquals(6,cr.toMySqlStatement(databaseNameKeyWord ).size());
		// have to have 3 table + 3 constraints
		Assert.assertEquals(6,cr.toPostgresStatement(databaseNameKeyWord ).size());
		Assert.assertTrue(cr.toDerbyStatement(databaseNameKeyWord ).get(0).contains(databaseNameKeyWord));
	}
	@Test
	public void testDelete() throws DatabaseException {
		Delete d = new Delete(Atom.create(getScenario1().getSchema().getRelation(0), TypedConstant.create(123), TypedConstant.create(223),
				TypedConstant.create(323)),getScenario1().getSchema());
		List<List<String>> statements = new ArrayList<>();
		statements.add(d.toDerbyStatement(databaseNameKeyWord));
		statements.add(d.toMySqlStatement(databaseNameKeyWord));
		statements.add( d.toPostgresStatement(databaseNameKeyWord));
		for (List<String> st:statements) {
			Assert.assertEquals(1,st.size());
			Assert.assertTrue(st.get(0).contains(databaseNameKeyWord));
			Assert.assertTrue(st.get(0).contains("123"));
			Assert.assertTrue(st.get(0).contains("223"));
			Assert.assertTrue(st.get(0).contains("323"));
		}
	}
	@Test
	public void testDifferenceQuery() throws DatabaseException {
		TestScenario sc = getScenario1();
		BasicSelect bs = new BasicSelect(sc.getSchema(),sc.getQuery());
		
		DifferenceQuery dq = new DifferenceQuery(bs,bs,sc.getSchema());
		
		Assert.assertEquals(sc.getQuery(), dq.getFormula());
		Assert.assertEquals(Arrays.asList(sc.getQuery().getFreeVariables()), Arrays.asList(dq.getResultTerms()));
		Assert.assertTrue(dq.toDerbyStatement("123").size()==1);
		Assert.assertTrue(dq.toDerbyStatement("123").get(0).length()>50);
	}
	@Test
	public void testDropDatabase() {
		DropDatabase dd = new DropDatabase(getScenario1().getSchema());
		
		// derby does not drop the schema but it does drop the tables.
		Assert.assertEquals(5,dd.toDerbyStatement(databaseNameKeyWord).size());
		Assert.assertTrue(dd.toDerbyStatement(databaseNameKeyWord).get(0).contains(databaseNameKeyWord));
		
		Assert.assertTrue(dd.toMySqlStatement(databaseNameKeyWord).size()>0);
		Assert.assertTrue(dd.toPostgresStatement(databaseNameKeyWord).size()>0);
		
		Assert.assertTrue(dd.toMySqlStatement(databaseNameKeyWord).get(0).contains(databaseNameKeyWord));
		Assert.assertTrue(dd.toPostgresStatement(databaseNameKeyWord).get(0).contains(databaseNameKeyWord));
		
	}
	@Test
	public void testInsert() throws DatabaseException {
		Insert d = new Insert(Atom.create(getScenario1().getSchema().getRelation(0), TypedConstant.create(123), TypedConstant.create(223),
				TypedConstant.create(323)),getScenario1().getSchema());
		List<List<String>> statements = new ArrayList<>();
		statements.add(d.toDerbyStatement(databaseNameKeyWord));
		statements.add(d.toMySqlStatement(databaseNameKeyWord));
		statements.add( d.toPostgresStatement(databaseNameKeyWord));
		for (List<String> st:statements) {
			Assert.assertEquals(1,st.size());
			Assert.assertTrue(st.get(0).contains(databaseNameKeyWord));
			Assert.assertTrue(st.get(0).contains("123"));
			Assert.assertTrue(st.get(0).contains("223"));
			Assert.assertTrue(st.get(0).contains("323"));
		}
	}
}
