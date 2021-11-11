// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.cs.pdq.fol.*;
import uk.ac.ox.cs.pdq.util.PdqTest;

// @author Mark Ridler
public class DependencyPrettyPrintTest {
	String[] expectedDependencyPrettyPrint;
	private String[] expectedTgdPrettyPrint;

	@Before
	public void setup() {

		PdqTest.assertsEnabled();
		expectedDependencyPrettyPrint = new String[]{"forall[] (body_pred --> head_pred)", "forall[x0] (body_pred(x0) --> exists[y0] head_pred(y0))", "forall[x0,x1] (body_pred(x0,x1) --> exists[y0,y1] head_pred(y0,y1))"};
		expectedTgdPrettyPrint = new String[]{"body_pred → head_pred","body_pred(x0) → exists[y0] head_pred(y0)","body_pred(x0,x1) → exists[y0,y1] head_pred(y0,y1)"};

	}

	// Calls Dependency.toString
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
			Assert.assertEquals(expectedDependencyPrettyPrint[i], dependency1.toString());
			Assert.assertEquals(expectedTgdPrettyPrint[i], dependency2.toString());
		}
	}

}
