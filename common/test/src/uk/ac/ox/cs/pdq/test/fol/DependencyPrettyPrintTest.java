package uk.ac.ox.cs.pdq.test.fol;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

// @author Mark Ridler
public class DependencyPrettyPrintTest {
	@Before
	public void setup() {
		PdqTest.assertsEnabled();
	}

	// Dependencies are created as either Dependency or TGD
	// Body and head terms consist of variable xi and yi respectively
	// Calls Dependency.toString(), TGD.toString()
	// - Dependency: (forall[x0,x1](body_pred(x0,x1) --> (exists[y0,y1]head_pred(y0,y1))))
	// - TGD: body_pred(x0,x1)→(exists[y0,y1]head_pred(y0,y1))
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
