package uk.ac.ox.cs.pdq.test.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.collect.Lists;

/**
 * Utility unit test
 *
 * @author Julien Leblay
 */
public class UtilityTest {
	
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	@Test public void testToTypedConstant() {
		TypedConstant<?> t1 = new TypedConstant<>("str");
		Attribute t2 = new Attribute(Integer.class, "1");
		List<Typed> typed = Lists.newArrayList(t1, t2);
		List<TypedConstant<?>> constants = Utility.toTypedConstants(typed);
		Assert.assertSame(constants.get(0), t1);
		Assert.assertEquals(constants.get(1), new TypedConstant<>(1));
	}
}
