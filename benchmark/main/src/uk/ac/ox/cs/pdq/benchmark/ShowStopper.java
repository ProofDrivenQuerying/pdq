package uk.ac.ox.cs.pdq.benchmark;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;


// TODO: Auto-generated Javadoc
/**
 * This is aimed at forcing the end of a test, thus by-passing the internal 
 * search timeout mechanism.
 * 
 * @author Julien Leblay
 *
 */
public class ShowStopper extends Thread {
	
	/** The log. */
	private static Logger log = Logger.getLogger(ShowStopper.class);

	/** The timeout. */
	private long timeout = -1L;
	
	/** The logger. */
	private final IntervalEventDrivenLogger logger;
	
	/**
	 * Instantiates a new show stopper.
	 *
	 * @param timeout the timeout
	 */
	public ShowStopper(long timeout) {
		this(timeout, null);
	}
	
	/**
	 * Instantiates a new show stopper.
	 *
	 * @param timeout the timeout
	 * @param logger the logger
	 */
	public ShowStopper(long timeout, IntervalEventDrivenLogger logger) {
		super("ShowStopper");
		this.setDaemon(true);
		this.timeout = timeout;
		this.logger = logger;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		if (this.timeout > 0l) {
			try {
				Thread.sleep(this.timeout);
				if (this.logger != null) {
					this.logger.forceLog("# TIMEOUT EXPIRED - Forcing termination ");
				}
				Runtime.getRuntime().exit(-3);
			} catch (InterruptedException e) {
				log.error(e.getMessage(),e);
			}
		}
	}
}
