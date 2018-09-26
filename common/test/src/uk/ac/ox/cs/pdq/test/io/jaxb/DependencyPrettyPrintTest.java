package uk.ac.ox.cs.pdq.test.io.jaxb;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.CartesianProductTerm;
import uk.ac.ox.cs.pdq.algebra.Condition;
import uk.ac.ox.cs.pdq.algebra.ConstantEqualityCondition;
import uk.ac.ox.cs.pdq.algebra.DependentJoinTerm;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.algebra.RenameTerm;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.util.Utility;

// @author Mark Ridler
public class DependencyPrettyPrintTest {
	@Before
	public void setup() {
		Utility.assertsEnabled();
	}

	// Calls Dependency.toString(), TGD.toString()
	@Test
	public void testDependencyPrettyPrint() {
		
		int n = 3;
		for (int i = 0; i < n; i++)
		{
			Predicate headpred = Predicate.create("head_pred", i);
			Predicate bodypred = Predicate.create("body_pred", i);
		
			Term[] bodyterms = new Term[i];
			for (int j = 0, l = bodyterms.length; j < l; j++) {
				bodyterms[j] = Variable.create("x" + j);
			};
			Term[] headterms = new Term[i];
			for (int j = 0, l = headterms.length; j < l; j++) {
				headterms[j] = Variable.create("y" + j);
			};
			Atom[] head = new Atom[1]; head[0] = Atom.create(headpred, headterms);
			Atom[] body = new Atom[1]; body[0] = Atom.create(bodypred, bodyterms);
			Dependency dependency1 = Dependency.create(body, head);
			TGD dependency2 = TGD.create(body, head);
			System.out.println("Dependency: " + dependency1.toString());
			System.out.println("TGD: " + dependency2.toString());
		}
	}

}
