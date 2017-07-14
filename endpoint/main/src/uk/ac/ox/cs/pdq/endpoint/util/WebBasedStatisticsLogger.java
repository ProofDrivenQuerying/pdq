package uk.ac.ox.cs.pdq.endpoint.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.logging.ChainedStatistics;

// TODO: Auto-generated Javadoc
/**
 * Statistics logger that works by appending logs from a sequence of delegate
 * loggers.
 * 
 * @author Julien Leblay
 */
public final class WebBasedStatisticsLogger extends ChainedStatistics implements BufferedProgressLogger {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4623356935725604341L;

	/** The log. */
	protected static Logger log = Logger.getLogger(WebBasedStatisticsLogger.class);

	/** The buffer. */
	protected StringBuilder buffer = new StringBuilder();

	/** The baos. */
	private ByteArrayOutputStream baos;
	
	/**
	 * Default constructor.
	 */
	public WebBasedStatisticsLogger() {
		super();
		this.baos = new ByteArrayOutputStream();
		this.out = new HTMLOutputStream(this.baos);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.logging.performance.ChainedStatistics#close()
	 */
	@Override
	public void close() {
		super.close();
		try {
			this.baos.close();
		} catch (IOException e) {
			log.warn(e);
		}
	}

	/**
	 * The Class HTMLOutputStream.
	 */
	private class HTMLOutputStream extends PrintStream {

		/** The started. */
		boolean started = false;
		
		/**
		 * Instantiates a new HTML output stream.
		 *
		 * @param os the os
		 */
		public HTMLOutputStream(OutputStream os) {
			super(os);
		}

		/* (non-Javadoc)
		 * @see java.io.PrintStream#println(java.lang.String)
		 */
		@Override
		public void println(String s) {
			String w = s;
			if (s.startsWith("#")) {
				w = "<tr><th>" + s.substring(1).replace("" + ChainedStatistics.FIELD_SEPARATOR, "</th><th>") + "</th></tr>";
				this.started = true;
				WebBasedStatisticsLogger.this.buffer.append(w);
				super.println(w);
			} else if (this.started) {
				w = "<tr><td>" + s.replace("" + ChainedStatistics.FIELD_SEPARATOR, "</td><td>") + "</td></tr>";
				WebBasedStatisticsLogger.this.buffer.append(w);
				super.println(w);
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.endpoint.util.BufferedProgressLogger#getLog()
	 */
	@Override
	public String getLog() {
		return this.buffer.toString();
	}
}
