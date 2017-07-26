package uk.ac.ox.cs.pdq.test.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * Utility unit test.
 *
 * @author Julien Leblay
 */
public class UtilityTest {


	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Test to typed constant.
	 */
	@Test public void testToTypedConstant() {
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
}
