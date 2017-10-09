package uk.ac.ox.cs.pdq.test.planner.accessibleschema;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TGD;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibilityAxiom;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the AccessibleSchema class by creating an example schemas and
 * validating it's accessible version.
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

public class TestAccessibleSchema extends PdqTest {

	/**
	 * Create a schema, then make a call to create its accessible version and then
	 * test if the accessible version is what it should be.
	 * 
	 * For schema it uses the R,S,T tables from PdqTest, and adds these
	 * dependencies:
	 * <pre>
	 * TGD: T(x,y,'constant1'), S(x,'constant2') -> R(x,y,z') S(y,x)
	 * <br>
	 * EGD: R(x,y,z), R(x,y,z') -> z=z'
	 * </pre>
	 * Asserts the accessible axioms.
	 */
	@Test
	public void test1() {
		Atom atom1 = Atom.create(this.T_s, new Term[] { Variable.create("x"), Variable.create("y"), TypedConstant.create("constant1") });
		Atom atom2 = Atom.create(this.S_s, new Term[] { Variable.create("x"), TypedConstant.create("constant2") });
		Atom atom3 = Atom.create(this.R_s, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom atom4 = Atom.create(this.S_s, new Term[] { Variable.create("y"), Variable.create("x") });
		Atom atom5 = Atom.create(this.R_s, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("z") });
		Atom atom6 = Atom.create(this.R_s, new Term[] { Variable.create("x"), Variable.create("y"), Variable.create("zp") });
		Atom atom7 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("z"), Variable.create("zp"));

		TGD tgd = TGD.create(new Atom[] { atom1, atom2 }, new Atom[] { atom3, atom4 });
		EGD egd = EGD.create(new Atom[] { atom5, atom6 }, new Atom[] { atom7 });

		Schema schema = new Schema(new Relation[] { this.R_s, this.S_s, this.T_s }, new Dependency[] { tgd, egd });

		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		Assert.assertNotNull(accessibleSchema);

		// constants
		Assert.assertEquals(2, accessibleSchema.getConstants().size());
		Assert.assertEquals(TypedConstant.create("constant1"), accessibleSchema.getConstant("constant1"));
		Assert.assertEquals(TypedConstant.create("constant2"), accessibleSchema.getConstant("constant2"));
		// accessibility axioms
		Assert.assertNotNull(accessibleSchema.getAccessibilityAxioms());
		Assert.assertEquals(8, accessibleSchema.getAccessibilityAxioms().length);
		int abc = 0;
		int bc = 0;
		int bcd = 0;
		for (AccessibilityAxiom axiom : accessibleSchema.getAccessibilityAxioms()) {
			if (axiom.getBoundVariables().length == 2) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("c"));
				bc++;
			} else if (axiom.getBoundVariables().length == 3 && axiom.getBoundVariables()[0].equals(Variable.create("a"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("a"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("c"));
				abc++;
			} else if (axiom.getBoundVariables().length == 3 && axiom.getBoundVariables()[0].equals(Variable.create("b"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("c"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("d"));
				bcd++;
			} else {
				Assert.fail();
			}
		}
		Assert.assertEquals(2, abc);
		Assert.assertEquals(3, bc);
		Assert.assertEquals(3, bcd);

		Assert.assertNotNull(accessibleSchema.getRelations());
		Assert.assertEquals(6, accessibleSchema.getRelations().length);
		Dependency[] infAccAxioms = accessibleSchema.getInferredAccessibilityAxioms();
		Assert.assertNotNull(infAccAxioms);
		Assert.assertEquals(1, infAccAxioms.length);
		Assert.assertNotNull(infAccAxioms[0]);
		Assert.assertTrue(infAccAxioms[0] instanceof TGD);
		TGD inferredAccessibilityAxiom = (TGD) infAccAxioms[0];
		Assert.assertNotEquals(tgd.getHead(), inferredAccessibilityAxiom.getHead());
		Assert.assertEquals("InferredAccessibleT", inferredAccessibilityAxiom.getBodyAtom(0).getPredicate().getName());
		Assert.assertEquals("InferredAccessibleS", inferredAccessibilityAxiom.getBodyAtom(1).getPredicate().getName());
	}

}
