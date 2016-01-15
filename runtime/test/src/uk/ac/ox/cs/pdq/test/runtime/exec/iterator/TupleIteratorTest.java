package uk.ac.ox.cs.pdq.test.runtime.exec.iterator;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Julien Leblay
 */
public abstract class TupleIteratorTest {
	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}

	protected abstract TupleIterator getIterator();
	
	@Test public void getColumn() {
		Iterator<Typed> columns = getIterator().getColumns().iterator();
		for (int k = 0; columns.hasNext(); k++) {
			Assert.assertEquals("Indexed column accessor must match full list",
					columns.next(), getIterator().getColumn(k));
		}
	}
	
	@Test public void getInputType() {
		Assert.assertEquals("Iterator input type must match that given by input columns",
				TupleType.DefaultFactory.createFromTyped(getIterator().getInputColumns()),
				getIterator().getInputType());
	}

	@Test public void getType() {
		Assert.assertEquals("Iterator type must match that given by columns",
				TupleType.DefaultFactory.createFromTyped(getIterator().getColumns()),
				getIterator().getType());
	}
	
	@Test(expected=UnsupportedOperationException.class) 
	public void remove() {
		getIterator().remove();
	}
	
	@Test public void interruptOnOpenedIterator() {
		getIterator().open();
		getIterator().interrupt();
		Assert.assertTrue(getIterator().isInterrupted());
	}
	
	@Test public void interruptSuccessful() {
		getIterator().open();
		getIterator().interrupt();
		Assert.assertTrue(getIterator().isInterrupted());
	}
	
	@Test(expected=IllegalStateException.class) 
	public void interruptOnUnopenedIterator() {
		getIterator().interrupt();
	}
	
	@Test(expected=IllegalStateException.class) 
	public void interruptOnClosedIterator() {
		getIterator().open();
		getIterator().close();
		getIterator().interrupt();
	}

	@Test public void isInterrupted() {
		getIterator().open();
		getIterator().interrupt();
		Assert.assertTrue(getIterator().isInterrupted());
	}

	@Test public void isNotInterrupted() {
		getIterator().open();
		Assert.assertFalse(getIterator().isInterrupted());
	}
	
	@Test public void getColumnsDisplay() {
		Assert.assertEquals(getIterator().getColumns().size(), getIterator().getColumnsDisplay().size());
	}
	
	@Test public void open() {
		getIterator().open();
		Assert.assertFalse(getIterator().isInterrupted());
	}
	
	@Test(expected=IllegalStateException.class) 
	public void openOnClosed() {
		getIterator().open();
		getIterator().close();
		getIterator().open();
	}
	
	@Test(expected=IllegalStateException.class) 
	public void resetOnClosed() {
		getIterator().open();
		getIterator().close();
		getIterator().reset();
	}
	
	@Test(expected=IllegalStateException.class) 
	public void resetOnInterrupted() {
		getIterator().open();
		getIterator().interrupt();
		getIterator().reset();
	}
	
	@Test(expected=IllegalStateException.class) 
	public void resetOnUnopened() {
		getIterator().reset();
	}
	
	@Test public void closeSuccessful() {
		getIterator().open();
		getIterator().close();
	}
	
	@Test(expected=IllegalStateException.class) 
	public void closeOnUnopened() {
		getIterator().close();
	}
}
