package uk.ac.ox.cs.pdq.test.planner.accessibleschema;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.datasources.io.xml.QNames;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
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
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * 
 * @author Efthymia Tsamoura
 * @author Gabor
 */

public class TestAccessibleSchema {

	protected AccessMethod method0 = AccessMethod.create(new Integer[]{});
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected AccessMethod method3 = AccessMethod.create(new Integer[]{1});
	
	protected Attribute a = Attribute.create(String.class, "a");
	protected Attribute b = Attribute.create(String.class, "b");
	protected Attribute c = Attribute.create(String.class, "c");
	protected Attribute d = Attribute.create(String.class, "d");
	protected Attribute InstanceID = Attribute.create(String.class, "InstanceID");
    
	protected Relation R;
	protected Relation S;
	protected Relation T;
    
	/**
	 * Setup.
	 */
	@Before public void setup() {
		Utility.assertsEnabled();
        MockitoAnnotations.initMocks(this);                
        this.R = Relation.create("R", new Attribute[]{a,b,c,InstanceID}, new AccessMethod[]{this.method0, this.method2});
        this.S = Relation.create("S", new Attribute[]{b,c,InstanceID}, new AccessMethod[]{this.method0, this.method1, this.method2});
        this.T = Relation.create("T", new Attribute[]{b,c,d,InstanceID}, new AccessMethod[]{this.method0, this.method1, this.method2});
	}
	
	@Test public void test1() {
		//T(x,y,'constant1'), S(x,'constant2') -> R(x,y,z') S(y,x)  
		//R(x,y,z), R(x,y,z') -> z=z'
		Atom atom1 = Atom.create(this.T, new Term[]{Variable.create("x"), Variable.create("y"), TypedConstant.create("constant1")});
		Atom atom2 = Atom.create(this.S, new Term[]{Variable.create("x"), TypedConstant.create("constant2")});
		Atom atom3 = Atom.create(this.R, new Term[]{Variable.create("x"), Variable.create("y"), Variable.create("z")});
		Atom atom4 = Atom.create(this.S, new Term[]{Variable.create("y"), Variable.create("x")});
		Atom atom5 = Atom.create(this.R, new Term[]{Variable.create("x"), Variable.create("y"),Variable.create("z")});
		Atom atom6 = Atom.create(this.R, new Term[]{Variable.create("x"), Variable.create("y"),Variable.create("zp")});
		Atom atom7 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true), Variable.create("z"), Variable.create("zp"));
		
		TGD tgd = TGD.create(new Atom[]{atom1,atom2}, new Atom[]{atom3,atom4});
		EGD egd = EGD.create(new Atom[]{atom5,atom6}, new Atom[]{atom7});

		Schema schema = new Schema(new Relation[]{this.R,this.S, this.T}, new Dependency[]{tgd,egd});
		
		AccessibleSchema accessibleSchema = new AccessibleSchema(schema);
		Assert.assertNotNull(accessibleSchema);
		
		// constants
		Assert.assertEquals(2,accessibleSchema.getConstants().size());
		Assert.assertEquals(TypedConstant.create("constant1"),accessibleSchema.getConstant("constant1"));
		Assert.assertEquals(TypedConstant.create("constant2"),accessibleSchema.getConstant("constant2"));
		
		// accessibility axioms
		Assert.assertNotNull(accessibleSchema.getAccessibilityAxioms());
		Assert.assertEquals(8,accessibleSchema.getAccessibilityAxioms().length);
		int abc=0;
		int bc=0;
		int bcd=0;
		for (AccessibilityAxiom axiom:accessibleSchema.getAccessibilityAxioms()) {
			if (axiom.getBoundVariables().length==2) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("c"));
				bc++;
			} else
			if (axiom.getBoundVariables().length==3 && axiom.getBoundVariables()[0].equals(Variable.create("a"))) {
				Assert.assertEquals(axiom.getBoundVariables()[0], Variable.create("a"));
				Assert.assertEquals(axiom.getBoundVariables()[1], Variable.create("b"));
				Assert.assertEquals(axiom.getBoundVariables()[2], Variable.create("c"));
				abc++;
			} else
			if (axiom.getBoundVariables().length==3 && axiom.getBoundVariables()[0].equals(Variable.create("b"))) {
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
	}
	
}
