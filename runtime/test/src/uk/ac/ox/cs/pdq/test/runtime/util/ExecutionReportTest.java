package uk.ac.ox.cs.pdq.test.runtime.util;

import org.junit.Before;
import org.junit.Ignore;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;
import uk.ac.ox.cs.pdq.util.Utility;

import com.google.common.eventbus.Subscribe;

/**
 * Prints a report of a physical operator tree usage.
 * 
 * @author Julien Leblay
 */
@Ignore
public class ExecutionReportTest implements EventHandler {

	
	/**
	 * Makes sure assertions are enabled
	 */
	@Before 
	public void setup() {
		Utility.assertsEnabled();
	}
	
	
}
