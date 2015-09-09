package uk.ac.ox.cs.pdq.endpoint.util;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.runtime.Runtime;
import uk.ac.ox.cs.pdq.util.Tuple;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

/**
 * Prints tuple to the given print stream, if provided, log.info otherwise.
 * 
 * @author Julien Leblay
 */
public class HTMLTuplePrinter implements EventHandler {

	/** Runner's logger. */
	private static Logger log = Logger.getLogger(Runtime.class);

	/** PrintWriter where to print tuples. */
	private final JspWriter out;

	/**
	 * Default constructor
	 * @param out
	 */
	public HTMLTuplePrinter(JspWriter out) {
		Preconditions.checkArgument(out != null);
		this.out = out;
	}

	/**
	 * Prints the given tuple of the default print stream, or log.info if null.
	 * @param tuple
	 */
	@Subscribe
	public void print(Tuple tuple) {
		if (this.out != null) {
			try {
				this.out.print("<tr>");
				for (Object o: tuple.getValues()) {
					this.out.print("<td>" + o + "</td>");
				}
				this.out.println("</tr>");
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalStateException(e);
			}
		} else {
			log.info(tuple);
		}
	}
}
