package uk.ac.ox.cs.pdq.test.db;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Match;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.db.sql.FromCondition;
import uk.ac.ox.cs.pdq.db.sql.SelectCondition;
import uk.ac.ox.cs.pdq.db.sql.WhereCondition;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Constant;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.util.Utility;

public class DatabaseInstanceTest {
	
	@Before
	public void setup() {
		Utility.assertsEnabled();	
	}
	
	@Test
	public void mySqlStatementBuioderTest () {
		try {
			Schema schema = IOManager.importSchema(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\db\\schema.xml"));
			
			DatabaseConnection dc = new DatabaseConnection(new DatabaseParameters(), schema);
			Assert.assertNotNull(dc);
			Assert.assertNotNull(dc.getSQLStatementBuilder());
			Assert.assertNotNull(dc.getSynchronousConnections());
			Assert.assertTrue(dc.getSynchronousConnections().size()>0);
			
			DatabaseInstance di = new DatabaseInstance(dc) {
				
				@Override
				public Collection<Atom> getFacts() {
					// TODO Auto-generated method stub
					return null;
				}
			};
			Collection<Atom> facts = createTestFacts(schema.getRelation(2));
			di.addFacts(facts);
			di.deleteFacts(facts);
			di.addFacts(facts);
//			Atom f20 = Atom.create(schema.getRelation(2), new Term[] { UntypedConstant.create("k1"), Variable.create("c"), UntypedConstant.create("c1"), TypedConstant.create(new Integer(13001))});
			Atom f20 = Atom.create(schema.getRelation(2), new Term[] { UntypedConstant.create("k1"), Variable.create("c"), UntypedConstant.create("c1")});
			ConjunctiveQuery q=ConjunctiveQuery.create(new Variable[] {Variable.create("c")}, f20);
			
			
			Pair<String, LinkedHashMap<String, Variable>> pair = createSQLQuery(q, dc, q.getSubstitutionOfFreeVariablesToCanonicalConstants());
			Queue<Triple<Formula, String, LinkedHashMap<String, Variable>>> queries = new ConcurrentLinkedQueue<>();  
			queries.add(Triple.of((Formula)q, pair.getLeft(), pair.getRight()));
//			List<Match> asd = di.answerQueries(queries);
	//		System.out.println(asd);
			//di.answerQuery(q);
			//di.setupQueryIndices( q);
			//di.executeQueries(q);
			//TGD tgd = (TGD) schema.getDependencies()[0];
		//	List<Match> matches = this.chaseState.getTriggers(new Dependency[]{tgd},TriggerProperty.ACTIVE,LimitToThisOrAllInstances.THIS);
			
			
		}catch(Throwable t ) {
			t.printStackTrace();
			Assert.fail(t.getMessage());
		}
	}
	public Pair<String,LinkedHashMap<String,Variable>> createSQLQuery(ConjunctiveQuery source, DatabaseConnection databaseConnection, Map<Variable, Constant> finalProjectionMapping) {
		String query = "";
		FromCondition from = databaseConnection.getSQLStatementBuilder().createFromStatement(source.getAtoms());
		//TOCOMMENT: Rename appropriately and comment each of the following methods.
		SelectCondition projections = databaseConnection.getSQLStatementBuilder().createProjections(source.getAtoms());
		WhereCondition where = new WhereCondition();
		WhereCondition equalities = databaseConnection.getSQLStatementBuilder().createAttributeEqualities(source.getAtoms());
		WhereCondition constantEqualities = databaseConnection.getSQLStatementBuilder().createEqualitiesWithConstants(source.getAtoms());
		WhereCondition equalitiesWithProjectedVars = databaseConnection.getSQLStatementBuilder().createEqualitiesRespectingInputMapping(source.getAtoms(), finalProjectionMapping);

		WhereCondition factproperties = null;
			factproperties = new WhereCondition();
		
		where.addCondition(equalities);
		where.addCondition(constantEqualities);
		where.addCondition(equalitiesWithProjectedVars);
		where.addCondition(factproperties);


		query = databaseConnection.getSQLStatementBuilder().buildSQLQuery(projections, from, where);
		return Pair.of(query, projections.getInternalMap());
	}
	
	
	private Collection<Atom> createTestFacts(Relation rel) {
//		Attribute at11 = Attribute.create(String.class, "at11");
//		Attribute at12 = Attribute.create(String.class, "at12");
//		Attribute at13 = Attribute.create(String.class, "at13");
//		Relation rel = Relation.create("R1", new Attribute[]{at11, at12, at13});
		
//		Atom f20 = Atom.create(rel, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), UntypedConstant.create("c1"), TypedConstant.create(new Integer(13001))});
//		Atom f21 = Atom.create(rel, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), UntypedConstant.create("c2"), TypedConstant.create(new Integer(13002))});
//		Atom f22 = Atom.create(rel, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), UntypedConstant.create("c3"), TypedConstant.create(new Integer(13003))});
//		Atom f23 = Atom.create(rel, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), UntypedConstant.create("c4"), TypedConstant.create(new Integer(13004))});
//		Atom f24 = Atom.create(rel, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John")), TypedConstant.create(new Integer(13005))});
//		Atom f25 = Atom.create(rel, new Term[] { UntypedConstant.create("k6"), UntypedConstant.create("c"), TypedConstant.create(new String("Michael")), TypedConstant.create(new Integer(13006))});
		Atom f20 = Atom.create(rel, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), UntypedConstant.create("c1")});
		Atom f21 = Atom.create(rel, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), UntypedConstant.create("c2")});
		Atom f22 = Atom.create(rel, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), UntypedConstant.create("c3")});
		Atom f23 = Atom.create(rel, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), UntypedConstant.create("c4")});
		Atom f24 = Atom.create(rel, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John"))});
		Atom f25 = Atom.create(rel, new Term[] { UntypedConstant.create("k6"), UntypedConstant.create("c"), TypedConstant.create(new String("Michael"))});
		return Lists.newArrayList(f20, f21, f22, f23, f24, f25);
	}

}
