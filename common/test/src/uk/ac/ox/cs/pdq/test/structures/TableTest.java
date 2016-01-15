package uk.ac.ox.cs.pdq.test.structures;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien Leblay
 */
@Ignore
public class TableTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	@Test public void getType() {}

	@Test public void appendRow() {}

	@Test public void appendRows() {}

	@Test public void isEmpty() {}

	@Test public void removeDuplicates() {}

	@Test public void getColumn() {}

	@Test public void contains() {}

	@Test public void howDifferent() {}

	@Test public void size() {}

	@Test public void columns() {}

	@Test public void iterator() {}

	@Test public void getHeader() {}

	@Test public void setHeader() {}

	@Test public void hasHeader() {}

	@Test public void getData() {}

	@Test public void diff() {}
}
