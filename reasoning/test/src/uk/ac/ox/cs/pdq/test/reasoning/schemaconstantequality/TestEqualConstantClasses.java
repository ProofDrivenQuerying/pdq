package uk.ac.ox.cs.pdq.test.reasoning.schemaconstantequality;


import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.QNames;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.reasoning.chase.schemaconstantequality.EqualConstantsClasses;
import uk.ac.ox.cs.pdq.test.util.PdqTest;

/**
 * Tests the add method of the EqualConstantsClasses class 
 * @author Efthymia Tsamoura
 * @author Gabor
 */
public class TestEqualConstantClasses extends PdqTest {
	
	EqualConstantsClasses classes = new EqualConstantsClasses();
	
	/**
	 * Tests adding equality classes. Asserts if we successfully added the new class and if we have the right amount after merging the new one in.
	 */
	@Test 
	public void test_add1() {
		Atom eq5 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c3"), TypedConstant.create(new String("John")));
		Atom eq1 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c1"), UntypedConstant.create("c2"));
		Atom eq4 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c3"), UntypedConstant.create("c4"));
		Atom eq3 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c2"), UntypedConstant.create("c3"));
		Atom eq2 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c1"), UntypedConstant.create("c3"));
		
		boolean _isFailed;
		_isFailed = this.classes.add(eq5);
		Assert.assertEquals(false, !_isFailed);
		Assert.assertEquals(1, this.classes.size());
		_isFailed = this.classes.add(eq1);
		Assert.assertEquals(false, !_isFailed);
		Assert.assertEquals(2, this.classes.size());
		_isFailed = this.classes.add(eq4);
		Assert.assertEquals(false, !_isFailed);
		Assert.assertEquals(2, this.classes.size());
		_isFailed = this.classes.add(eq3);
		Assert.assertEquals(false, !_isFailed);
		Assert.assertEquals(1, this.classes.size());
		_isFailed = this.classes.add(eq2);
		Assert.assertEquals(false, !_isFailed);
		Assert.assertEquals(1, this.classes.size());
		Assert.assertEquals(TypedConstant.create(new String("John")), this.classes.getClass(UntypedConstant.create("c3")).getRepresentative());
		
		Atom eq6 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c3"), TypedConstant.create(new String("Michael")));
		_isFailed = this.classes.add(eq6);
		Assert.assertEquals(true, !_isFailed);
	}
	
	/**
	 * Tests adding equality classes. Asserts if we successfully added the new class and if we have the right amount after merging the new one in.
	 */
	@Test 
	public void test_add2() {
		Atom eq1 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c2"), UntypedConstant.create("c1"));
		Atom eq2 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c3"), UntypedConstant.create("c1"));
		Atom eq3 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c4"), UntypedConstant.create("c1"));
		Atom eq4 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),TypedConstant.create(new String("John")), UntypedConstant.create("c1"));
		Atom eq12 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c3"), UntypedConstant.create("c2"));
		Atom eq13 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c4"), UntypedConstant.create("c2"));
		Atom eq14 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),TypedConstant.create(new String("John")), UntypedConstant.create("c2"));
		Atom eq21 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),UntypedConstant.create("c4"), UntypedConstant.create("c3"));
		Atom eq22 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),TypedConstant.create(new String("John")), UntypedConstant.create("c3"));
		Atom eq31 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),TypedConstant.create(new String("John")), UntypedConstant.create("c4"));
		
		boolean _isSuccessful;
		_isSuccessful = this.classes.add(eq1);
		Assert.assertTrue(_isSuccessful);
		_isSuccessful = this.classes.add(eq2);
		Assert.assertTrue(_isSuccessful);
		_isSuccessful = this.classes.add(eq3);
		Assert.assertTrue(_isSuccessful);
		_isSuccessful = this.classes.add(eq4);
		Assert.assertTrue(_isSuccessful);
		
		_isSuccessful = this.classes.add(eq12);
		Assert.assertTrue(_isSuccessful);
		_isSuccessful = this.classes.add(eq13);
		Assert.assertTrue(_isSuccessful);
		_isSuccessful = this.classes.add(eq14);
		Assert.assertTrue(_isSuccessful);
		
		_isSuccessful = this.classes.add(eq21);
		Assert.assertTrue(_isSuccessful);
		_isSuccessful = this.classes.add(eq22);
		Assert.assertTrue(_isSuccessful);
		_isSuccessful = this.classes.add(eq31);
		Assert.assertTrue(_isSuccessful);
		
		Assert.assertEquals(1, this.classes.size());
		Assert.assertEquals(TypedConstant.create(new String("John")), this.classes.getClass(UntypedConstant.create("c3")).getRepresentative());		
	}
	
	/**
	 * Tests adding an equality class with two typed constants that are different. Asserts that the adding of this illegal equality class fails.
	 */
	@Test 
	public void test_add3() {
		Atom eq31 = Atom.create(Predicate.create(QNames.EQUALITY.toString(), 2, true),TypedConstant.create(new String("John")), TypedConstant.create(new String("Michael")));
		
		boolean _isFailed;
		_isFailed = this.classes.add(eq31);
		
		Assert.assertEquals(true, !_isFailed);		
	}
	
}
