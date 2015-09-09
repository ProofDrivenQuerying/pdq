package uk.ac.ox.cs.pdq.benchmark;

import uk.ac.ox.cs.pdq.planner.logging.IntervalEventDrivenLogger;


/**
 * This is aimed at forcing the end of a test, thus by-passing the internal 
 * search timeout mechanism.
 * 
 * @author Julien Leblay
 *
 */
public class ShowStopper extends Thread {

	private long timeout = -1L;
	private final IntervalEventDrivenLogger logger;
	
	public ShowStopper(long timeout) {
		this(timeout, null);
	}
	
	public ShowStopper(long timeout, IntervalEventDrivenLogger logger) {
		super("ShowStopper");
		this.setDaemon(true);
		this.timeout = timeout;
		this.logger = logger;
	}
	
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
				e.printStackTrace();
			}
		}
	}
}
