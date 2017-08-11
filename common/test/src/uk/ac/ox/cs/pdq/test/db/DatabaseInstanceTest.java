package uk.ac.ox.cs.pdq.test.db;

import java.io.File;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.pdq.db.DatabaseConnection;
import uk.ac.ox.cs.pdq.db.DatabaseInstance;
import uk.ac.ox.cs.pdq.db.DatabaseParameters;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
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
			Schema schema = IOManager.importSchema(new File("test\\src\\uk\\ac\\ox\\cs\\pdq\\test\\io\\jaxb\\schema.xml"));
			
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
			Collection<Atom> facts = createTestFacts(schema.getRelation(0));
			di.addFacts(facts);
			di.deleteFacts(facts);
			di.addFacts(facts);
			ConjunctiveQuery q=null;
			//di.answerQuery(q);
			di.setupQueryIndices(q);
			// di.executeQueries(queries);
			TGD tgd = (TGD) schema.getDependencies()[0];
		//	List<Match> matches = this.chaseState.getTriggers(new Dependency[]{tgd},TriggerProperty.ACTIVE,LimitToThisOrAllInstances.THIS);
			
			
		}catch(Throwable t ) {
			t.printStackTrace();
			Assert.fail(t.getMessage());
		}
	}
	
	private Collection<Atom> createTestFacts(Relation rel) {
//		Attribute at11 = Attribute.create(String.class, "at11");
//		Attribute at12 = Attribute.create(String.class, "at12");
//		Attribute at13 = Attribute.create(String.class, "at13");
//		Relation rel = Relation.create("R1", new Attribute[]{at11, at12, at13});
		
		Atom f20 = Atom.create(rel, new Term[] { UntypedConstant.create("k1"), UntypedConstant.create("c"), UntypedConstant.create("c1") });
		Atom f21 = Atom.create(rel, new Term[] { UntypedConstant.create("k2"), UntypedConstant.create("c"), UntypedConstant.create("c2") });
		Atom f22 = Atom.create(rel, new Term[] { UntypedConstant.create("k3"), UntypedConstant.create("c"), UntypedConstant.create("c3") });
		Atom f23 = Atom.create(rel, new Term[] { UntypedConstant.create("k4"), UntypedConstant.create("c"), UntypedConstant.create("c4") });
		Atom f24 = Atom.create(rel, new Term[] { UntypedConstant.create("k5"), UntypedConstant.create("c"), TypedConstant.create(new String("John")) });
		Atom f25 = Atom.create(rel, new Term[] { UntypedConstant.create("k6"), UntypedConstant.create("c"), TypedConstant.create(new String("Michael")) });
		return Lists.newArrayList(f20, f21, f22, f23, f24, f25);
	}

}
