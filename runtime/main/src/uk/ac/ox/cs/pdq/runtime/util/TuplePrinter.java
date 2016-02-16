package uk.ac.ox.cs.pdq.runtime.util;

import java.io.PrintStream;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.eventbus.Subscribe;


// TODO: Auto-generated Javadoc
/**
 * Prints tuple to the given print stream, if provided, log.info otherwise.
 * 
 * @author Julien Leblay
 */
public class TuplePrinter implements EventHandler {

	/** TuplePrinterTest logger. */
	private static Logger log = Logger.getLogger(TuplePrinter.class);

	/** PrintStream where to print tuples. */
	private final PrintStream out;

	/**
	 * Default constructor.
	 *
	 * @param out the out
	 */
	public TuplePrinter(PrintStream out) {
		this.out = out;
	}
	
	/**
	 * Empty constructor, causes the tuple to be printed to log.info.
	 */
	public TuplePrinter() {
		this(null);
	}

	/**
	 * Prints the given tuple of the default print stream, or log.info if null.
	 *
	 * @param tuple the tuple
	 */
	@Subscribe
	public void print(Tuple tuple) {
		if (this.out != null) {
			//this.out.println(String.valueOf(tuple));
			String s = "";
			int i = 0;
			for(Object value:tuple.getValues()) {
				s += value==null ? "": value.toString();
				if(i < tuple.size() - 1) {
					s += "�";
				}
				i++;
			}
			this.out.println(s);
		} else {
			log.info(tuple);
		}
	}
}
