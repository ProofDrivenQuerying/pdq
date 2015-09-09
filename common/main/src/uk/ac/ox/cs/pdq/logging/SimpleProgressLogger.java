package uk.ac.ox.cs.pdq.logging;

import java.io.PrintStream;


/**
 * This logger simply outputs dots, and can be used to observed the progress
 * of a process.
 *
 * @author Julien Leblay
 */
public class SimpleProgressLogger implements ProgressLogger {

	private PrintStream out;

	/**
	 * Constructor for SimpleProgressLogger.
	 * @param out PrintStream
	 */
	public SimpleProgressLogger(PrintStream out) {
		this.out = out;
	}

	/**
	 * @see uk.ac.ox.cs.pdq.logging.ProgressLogger#log()
	 */
	@Override
	public void log() {
		this.out.print('.');
	}

	/**
	 * @param suffix String
	 * @see uk.ac.ox.cs.pdq.logging.ProgressLogger#log(String)
	 */
	@Override
	public void log(String suffix) {
		this.out.print('.' + suffix);
	}

	/**
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		this.out.print('\n');
	}

}
