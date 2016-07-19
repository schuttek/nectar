package org.nectarframework.base.service.thread;

public abstract class ThreadServiceTask implements Comparable<ThreadServiceTask> {
	private Throwable exception = null;
	private boolean completed = false;
	private long executeTime = -1;

	public abstract void execute() throws Exception;

	public void setException(Throwable e) {
		this.exception = e;
	}

	public Throwable getException() {
		return exception;
	}

	public synchronized void setComplete(boolean b) {
		completed = b;
		this.notify();
	}

	public boolean isComplete() {
		return completed;
	}

	public long getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(long executeTime) {
		this.executeTime = executeTime;
	}

	public int compareTo(ThreadServiceTask otherTask) {
		long diff = this.getExecuteTime() - otherTask.getExecuteTime();
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		}
		return 0;
	}

}
