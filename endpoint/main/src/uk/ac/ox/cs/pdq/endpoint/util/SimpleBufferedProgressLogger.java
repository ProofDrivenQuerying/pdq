package uk.ac.ox.cs.pdq.endpoint.util;


// TODO: Auto-generated Javadoc
/**
 * This logger simply output dots, and can be used to observed the progress
 * of a process.
 * 
 * @author Julien Leblay
 *
 */
public class SimpleBufferedProgressLogger implements BufferedProgressLogger {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6510577946621945145L;

	/** The out. */
	protected StringBuilder out = new StringBuilder();
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.logging.ProgressLogger#log()
	 */
	@Override
	public void log() {
		this.out.append(".");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.AutoCloseable#close()
	 */
	@Override
	public void close() {
		this.out.append("\n");
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.endpoint.util.BufferedProgressLogger#getLog()
	 */
	@Override
	public String getLog() {
		return this.out.toString();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.logging.ProgressLogger#log(java.lang.String)
	 */
	@Override
	public void log(String suffix) {
		this.out.append(".").append(suffix);
	}
}
