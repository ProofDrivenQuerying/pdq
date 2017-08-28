package uk.ac.ox.cs.pdq.test.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.ForeignKey;
import uk.ac.ox.cs.pdq.db.PrimaryKey;
import uk.ac.ox.cs.pdq.db.Reference;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.EGD;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * Utility unit test.
 *
 * @author Julien Leblay
 */
public class UtilityTest {
	
	protected AccessMethod method0 = AccessMethod.create(new Integer[]{});
	protected AccessMethod method1 = AccessMethod.create(new Integer[]{0});
	protected AccessMethod method2 = AccessMethod.create(new Integer[]{0,1});
	protected AccessMethod method3 = AccessMethod.create(new Integer[]{1});

	protected Attribute a = Attribute.create(String.class, "a");
	protected Attribute b = Attribute.create(String.class, "b");
	protected Attribute c = Attribute.create(String.class, "c");
	protected Attribute d = Attribute.create(String.class, "d");
    
	protected Relation R;
	protected Relation S;
	protected Relation T;

	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
        this.R = Relation.create("R", new Attribute[]{a,b,c}, new AccessMethod[]{this.method0, this.method2});
        this.S = Relation.create("S", new Attribute[]{a,c}, new AccessMethod[]{this.method0, this.method1, this.method2});
        this.T = Relation.create("T", new Attribute[]{a,b,c,d}, new AccessMethod[]{this.method0, this.method1, this.method2});
        this.R.setKey(PrimaryKey.create(new Attribute[]{this.a, this.b}));
        this.S.setKey(PrimaryKey.create(new Attribute[]{this.a, this.c}));
        this.T.setKey(PrimaryKey.create(new Attribute[]{this.a, this.b,this.c}));
        ForeignKey fk = new ForeignKey();
        fk.addReference(new Reference(this.a, this.a));
        fk.addReference(new Reference(this.b, this.b));
        fk.setForeignRelation(this.R);
        this.T.addForeignKey(new ForeignKey());
	}

	/**
	 * Test to typed constant.
	 */
	@Test 
	public void testToTypedConstant() {
		TypedConstant t1 = TypedConstant.create("str");
		Attribute t2 = Attribute.create(Integer.class, "1");
		Typed[] typed = new Typed[]{t1, t2};
		TypedConstant[] constants = toTypedConstants(typed);
		Assert.assertSame(constants[0], t1);
		Assert.assertEquals(constants[1], TypedConstant.create(1));
	}

	protected TypedConstant[] toTypedConstants(Typed[] typed) {
		TypedConstant[] result = new TypedConstant[typed.length];
		for (int typedIndex = 0; typedIndex < typed.length; ++typedIndex) {
			Typed t = typed[typedIndex];
			if (t instanceof TypedConstant) 
				result[typedIndex] = (TypedConstant) t;
			else 
				result[typedIndex] = TypedConstant.create(Utility.cast(t.getType(), String.valueOf(t)));

		}
		return result;
	}
	
	@Test
	public void testGetEGDs1() {
		EGD egds = Utility.getEGDs(this.R, this.R.getKey().getAttributes());
		//assert 
	}
	
	@Test
	public void testGetEGDs2() {
		EGD egds = Utility.getEGDs(this.S, this.S.getKey().getAttributes());
		//assert 
	}
	
	@Test
	public void testGetEGDs3() {
		EGD egds = Utility.getEGDs(this.T, this.T.getKey().getAttributes());
		//assert 
	}
	
}
