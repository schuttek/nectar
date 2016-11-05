package org.nectarframework.base.service.thread;

/**
 * Tickets can be issued for a task or set of tasks in progress. 
 * @author skander
 *
 */

public class Ticket {

	private boolean ready;

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean b) {
		ready = b;
	}

	public synchronized void waitForReady() {
		if (!ready) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
	}
	
}
