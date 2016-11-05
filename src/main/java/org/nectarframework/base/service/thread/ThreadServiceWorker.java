package org.nectarframework.base.service.thread;

public class ThreadServiceWorker extends Thread {
	private ThreadService ts = null;
	private ThreadServiceTask task = null;

	public ThreadServiceWorker(ThreadService ts) {
		super("ThreadServiceWorker");
		this.ts = ts;
	}

	public void run() {
		while (ts.isRunning()) {
			while (task != null) {
				try {
					task.execute();
				} catch (Throwable e) {
					task.setException(e);
					ts.reportException(this, task, e);
				}
				task.setComplete(true);
				synchronized(task) {
					task.notifyAll();
				}
				ts.threadReturn(this);
			}
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
			}
		}

	}

	public void setTask(ThreadServiceTask task) {
		this.task = task;
	}

}
