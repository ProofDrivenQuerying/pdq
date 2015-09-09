package uk.ac.ox.cs.pdq.endpoint.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.logging.performance.ChainedStatistics;

/**
 * Statistics logger that works by appending logs from a sequence of delegate
 * loggers.
 * 
 * @author Julien Leblay
 */
public final class WebBasedStatisticsLogger extends ChainedStatistics implements BufferedProgressLogger {

	/** */
	private static final long serialVersionUID = -4623356935725604341L;

	protected static Logger log = Logger.getLogger(WebBasedStatisticsLogger.class);

	protected StringBuilder buffer = new StringBuilder();

	private ByteArrayOutputStream baos;
	
	/**
	 * Default constructor
	 */
	public WebBasedStatisticsLogger() {
		super();
		this.baos = new ByteArrayOutputStream();
		this.out = new HTMLOutputStream(this.baos);
	}
	
	@Override
	public void close() {
		super.close();
		try {
			this.baos.close();
		} catch (IOException e) {
			log.warn(e);
		}
	}

	private class HTMLOutputStream extends PrintStream {

		boolean started = false;
		
		public HTMLOutputStream(OutputStream os) {
			super(os);
		}

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

	@Override
	public String getLog() {
		return this.buffer.toString();
	}
}
