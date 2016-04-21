package uk.ac.ox.cs.pdq.test.reasoning.utility;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Skolem;
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
		Equality eq5 = new Equality(new Skolem("c3"), new TypedConstant(new String("John")));
		Equality eq1 = new Equality(new Skolem("c1"), new Skolem("c2"));
		Equality eq4 = new Equality(new Skolem("c3"), new Skolem("c4"));
		Equality eq3 = new Equality(new Skolem("c2"), new Skolem("c3"));
		Equality eq2 = new Equality(new Skolem("c1"), new Skolem("c3"));
		
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
		Assert.assertEquals(new TypedConstant(new String("John")), this.classes.getClass(new Skolem("c3")).getRepresentative());
		
		Equality eq6 = new Equality(new Skolem("c3"), new TypedConstant(new String("Michael")));
		_isFailed = this.classes.add(eq6);
		Assert.assertEquals(true, !_isFailed);
	}
	
	@Test 
	public void test_add2() {
		Equality eq1 = new Equality(new Skolem("c2"), new Skolem("c1"));
		Equality eq2 = new Equality(new Skolem("c3"), new Skolem("c1"));
		Equality eq3 = new Equality(new Skolem("c4"), new Skolem("c1"));
		Equality eq4 = new Equality(new TypedConstant(new String("John")), new Skolem("c1"));
		Equality eq12 = new Equality(new Skolem("c3"), new Skolem("c2"));
		Equality eq13 = new Equality(new Skolem("c4"), new Skolem("c2"));
		Equality eq14 = new Equality(new TypedConstant(new String("John")), new Skolem("c2"));
		Equality eq21 = new Equality(new Skolem("c4"), new Skolem("c3"));
		Equality eq22 = new Equality(new TypedConstant(new String("John")), new Skolem("c3"));
		Equality eq31 = new Equality(new TypedConstant(new String("John")), new Skolem("c4"));
		
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
		Assert.assertEquals(new TypedConstant(new String("John")), this.classes.getClass(new Skolem("c3")).getRepresentative());		
	}
	
}
