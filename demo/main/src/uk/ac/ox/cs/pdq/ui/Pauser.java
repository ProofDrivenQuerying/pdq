package uk.ac.ox.cs.pdq.ui;

/**
 * This class aims at pausing atomic task from the interface such as planner
 * search and plan execution.
 * 
 * @author Julien Leblay
 *
 */
public class Pauser implements Runnable {

	private boolean isPaused;
	
	private final int interval;
	
	private final Object monitored;

	public Pauser(Object o, int refreshInterval) {
		this.monitored = o;
		this.interval = refreshInterval;
	}
	
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
	
	public void pause() {
		synchronized (this.monitored) {
			this.isPaused = true;
		}
	}
	
	public void resume() {
		synchronized (this.monitored) {
			this.isPaused = false;
		}
	}
}
