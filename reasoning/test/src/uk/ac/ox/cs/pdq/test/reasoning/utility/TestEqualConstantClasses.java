package uk.ac.ox.cs.pdq.test.reasoning.utility;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.UntypedConstant;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.reasoning.utility.EqualConstantsClasses;

/**
 * Tests the add method of the EqualConstantsClasses class 
 * @author Efthymia Tsamoura
 *
 */
public class TestEqualConstantClasses {
	
	EqualConstantsClasses classes = new EqualConstantsClasses();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test 
	public void test_add1() {
		Atom eq5 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c3"), new TypedConstant(new String("John")));
		Atom eq1 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c1"), new UntypedConstant("c2"));
		Atom eq4 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c3"), new UntypedConstant("c4"));
		Atom eq3 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c2"), new UntypedConstant("c3"));
		Atom eq2 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c1"), new UntypedConstant("c3"));
		
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
		Assert.assertEquals(new TypedConstant(new String("John")), this.classes.getClass(new UntypedConstant("c3")).getRepresentative());
		
		Atom eq6 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c3"), new TypedConstant(new String("Michael")));
		_isFailed = this.classes.add(eq6);
		Assert.assertEquals(true, !_isFailed);
	}
	
	@Test 
	public void test_add2() {
		Atom eq1 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c2"), new UntypedConstant("c1"));
		Atom eq2 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c3"), new UntypedConstant("c1"));
		Atom eq3 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c4"), new UntypedConstant("c1"));
		Atom eq4 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new TypedConstant(new String("John")), new UntypedConstant("c1"));
		Atom eq12 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c3"), new UntypedConstant("c2"));
		Atom eq13 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c4"), new UntypedConstant("c2"));
		Atom eq14 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new TypedConstant(new String("John")), new UntypedConstant("c2"));
		Atom eq21 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new UntypedConstant("c4"), new UntypedConstant("c3"));
		Atom eq22 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new TypedConstant(new String("John")), new UntypedConstant("c3"));
		Atom eq31 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new TypedConstant(new String("John")), new UntypedConstant("c4"));
		
		boolean _isFailed;
		_isFailed = this.classes.add(eq1);
		_isFailed = this.classes.add(eq2);
		_isFailed = this.classes.add(eq3);
		_isFailed = this.classes.add(eq4);
		
		_isFailed = this.classes.add(eq12);
		_isFailed = this.classes.add(eq13);
		_isFailed = this.classes.add(eq14);
		
		_isFailed = this.classes.add(eq21);
		_isFailed = this.classes.add(eq22);
		_isFailed = this.classes.add(eq31);
		
		Assert.assertEquals(false, !_isFailed);
		Assert.assertEquals(1, this.classes.size());
		Assert.assertEquals(new TypedConstant(new String("John")), this.classes.getClass(new UntypedConstant("c3")).getRepresentative());		
	}
	
	@Test 
	public void test_add3() {
		Atom eq31 = new Atom(new Predicate(QNames.EQUALITY.toString(), 2, true),new TypedConstant(new String("John")), new TypedConstant(new String("Michael")));
		
		boolean _isFailed;
		_isFailed = this.classes.add(eq31);
		
		Assert.assertEquals(true, !_isFailed);		
	}
	
}
