package uk.ac.ox.cs.pdq.ui;

// TODO: Auto-generated Javadoc
/**
 * This class aims at pausing atomic task from the interface such as planner
 * search and plan execution.
 * 
 * @author Julien Leblay
 *
 */
public class Pauser implements Runnable {

	/** The is paused. */
	private boolean isPaused;
	
	/** The interval. */
	private final int interval;
	
	/** The monitored. */
	private final Object monitored;

	/**
	 * Instantiates a new pauser.
	 *
	 * @param o the o
	 * @param refreshInterval the refresh interval
	 */
	public Pauser(Object o, int refreshInterval) {
		this.monitored = o;
		this.interval = refreshInterval;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(0, this.interval);
				if (!this.isPaused) {
					synchronized (this.monitored) {
						this.monitored.notifyAll();
					}
				}
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	/**
	 * Pause.
	 */
	public void pause() {
		synchronized (this.monitored) {
			this.isPaused = true;
		}
	}
	
	/**
	 * Resume.
	 */
	public void resume() {
		synchronized (this.monitored) {
			this.isPaused = false;
		}
	}
}
