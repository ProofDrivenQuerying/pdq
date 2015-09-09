package uk.ac.ox.cs.pdq.endpoint.util;


/**
 * This logger simply output dots, and can be used to observed the progress
 * of a process.
 * 
 * @author Julien Leblay
 *
 */
public class SimpleBufferedProgressLogger implements BufferedProgressLogger {

	/** */
	private static final long serialVersionUID = -6510577946621945145L;

	protected StringBuilder out = new StringBuilder();
	
	@Override
	public void log() {
		this.out.append(".");
	}
	
	@Override
	public void close() {
		this.out.append("\n");
	}

	@Override
	public String getLog() {
		return this.out.toString();
	}

	@Override
	public void log(String suffix) {
		this.out.append(".").append(suffix);
	}
}
