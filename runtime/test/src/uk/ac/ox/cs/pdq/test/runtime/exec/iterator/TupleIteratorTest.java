package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.utility.TupleType;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class TupleIteratorTest.
 *
 * @author Julien Leblay
 */
public abstract class TupleIteratorTest {
	
	/**
	 * Makes sure assertions are enabled.
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	/**
	 * Gets the iterator.
	 *
	 * @return the iterator
	 */
	protected abstract TupleIterator getIterator();
	
	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	@Test public void getColumn() {
		Iterator<Typed> columns = getIterator().getColumns().iterator();
		for (int k = 0; columns.hasNext(); k++) {
			Assert.assertEquals("Indexed column accessor must match full list",
					columns.next(), getIterator().getColumn(k));
		}
	}
	
	/**
	 * Gets the input type.
	 *
	 * @return the input type
	 */
	@Test public void getInputType() {
		Assert.assertEquals("Iterator input type must match that given by input columns",
				TupleType.DefaultFactory.createFromTyped(getIterator().getInputColumns()),
				getIterator().getInputType());
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	@Test public void getType() {
		Assert.assertEquals("Iterator type must match that given by columns",
				TupleType.DefaultFactory.createFromTyped(getIterator().getColumns()),
				getIterator().getOutputType());
	}
	
	/**
	 * Removes the.
	 */
	@Test(expected=UnsupportedOperationException.class) 
	public void remove() {
		getIterator().remove();
	}
	
	/**
	 * Interrupt on opened iterator.
	 */
	@Test public void interruptOnOpenedIterator() {
		getIterator().open();
		getIterator().interrupt();
		Assert.assertTrue(getIterator().isInterrupted());
	}
	
	/**
	 * Interrupt successful.
	 */
	@Test public void interruptSuccessful() {
		getIterator().open();
		getIterator().interrupt();
		Assert.assertTrue(getIterator().isInterrupted());
	}
	
	/**
	 * Interrupt on unopened iterator.
	 */
	@Test(expected=IllegalStateException.class) 
	public void interruptOnUnopenedIterator() {
		getIterator().interrupt();
	}
	
	/**
	 * Interrupt on closed iterator.
	 */
	@Test(expected=IllegalStateException.class) 
	public void interruptOnClosedIterator() {
		getIterator().open();
		getIterator().close();
		getIterator().interrupt();
	}

	/**
	 * Checks if is interrupted.
	 */
	@Test public void isInterrupted() {
		getIterator().open();
		getIterator().interrupt();
		Assert.assertTrue(getIterator().isInterrupted());
	}

	/**
	 * Checks if is not interrupted.
	 */
	@Test public void isNotInterrupted() {
		getIterator().open();
		Assert.assertFalse(getIterator().isInterrupted());
	}
	
	/**
	 * Gets the columns display.
	 *
	 * @return the columns display
	 */
	@Test public void getColumnsDisplay() {
		Assert.assertEquals(getIterator().getColumns().size(), getIterator().getColumnsDisplay().size());
	}
	
	/**
	 * Open.
	 */
	@Test public void open() {
		getIterator().open();
		Assert.assertFalse(getIterator().isInterrupted());
	}
	
	/**
	 * Open on closed.
	 */
	@Test(expected=IllegalStateException.class) 
	public void openOnClosed() {
		getIterator().open();
		getIterator().close();
		getIterator().open();
	}
	
	/**
	 * Reset on closed.
	 */
	@Test(expected=IllegalStateException.class) 
	public void resetOnClosed() {
		getIterator().open();
		getIterator().close();
		getIterator().reset();
	}
	
	/**
	 * Reset on interrupted.
	 */
	@Test(expected=IllegalStateException.class) 
	public void resetOnInterrupted() {
		getIterator().open();
		getIterator().interrupt();
		getIterator().reset();
	}
	
	/**
	 * Reset on unopened.
	 */
	@Test(expected=IllegalStateException.class) 
	public void resetOnUnopened() {
		getIterator().reset();
	}
	
	/**
	 * Close successful.
	 */
	@Test public void closeSuccessful() {
		getIterator().open();
		getIterator().close();
	}
	
	/**
	 * Close on unopened.
	 */
	@Test(expected=IllegalStateException.class) 
	public void closeOnUnopened() {
		getIterator().close();
	}
}
