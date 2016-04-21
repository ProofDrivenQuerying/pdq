package uk.ac.ox.cs.pdq.test.materialize.utility;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Equality;
import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.materialize.utility.EqualConstantsClasses;


public class TestEqualConstantClasses {
	
	EqualConstantsClasses classes = new EqualConstantsClasses();
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test 
	public void test1() {
		
		Equality eq5 = new Equality(new Skolem("c3"), new TypedConstant(new String("John")));
		Equality eq1 = new Equality(new Skolem("c1"), new Skolem("c2"));
		Equality eq4 = new Equality(new Skolem("c3"), new Skolem("c4"));
		Equality eq3 = new Equality(new Skolem("c2"), new Skolem("c3"));
		Equality eq2 = new Equality(new Skolem("c1"), new Skolem("c3"));
		
		boolean _isFailed;
		_isFailed = this.classes.add(eq5);
		_isFailed = this.classes.add(eq1);
		_isFailed = this.classes.add(eq4);
		_isFailed = this.classes.add(eq3);
		_isFailed = this.classes.add(eq2);
		
		Equality eq6 = new Equality(new Skolem("c3"), new TypedConstant(new String("Michael")));
		_isFailed = this.classes.add(eq6);
		
	}
	
}
