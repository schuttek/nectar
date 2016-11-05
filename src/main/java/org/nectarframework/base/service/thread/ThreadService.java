package org.nectarframework.base.service.thread;

/**
 * The ThreadService manages worker threads that perform tasks asynchronously.
 */

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;

public class ThreadService extends Service {

	private MasterThread masterThread = null;

	private boolean keepThreadsRunning = false;

	private Stack<ThreadServiceWorker> threads = new Stack<ThreadServiceWorker>();
	private Stack<ThreadServiceWorker> idleWorkers = new Stack<ThreadServiceWorker>();
	private HashSet<ThreadServiceWorker> busyWorkers = new HashSet<ThreadServiceWorker>();
	private PriorityQueue<ThreadServiceTask> taskQueue = new PriorityQueue<ThreadServiceTask>();

	private int maxQueueLength = 1000;

	private int minWorkerThreads = 2;
	private int maxWorkerThreads = 50;

	public boolean establishDependencies() {
		return true;
	}

	public boolean isRunning() {
		return (keepThreadsRunning);
	}

	@Override
	protected boolean init() {
		keepThreadsRunning = true;
		masterThread = new MasterThread(this);

		for (int i = 0; i < minWorkerThreads; i++) {
			ThreadServiceWorker tsw = new ThreadServiceWorker(this);
			threads.add(tsw);
		}
		masterThread.start();
		for (ThreadServiceWorker tsw : threads) {
			tsw.start();
			idleWorkers.add(tsw);
		}
		return true;
	}

	@Override
	protected synchronized boolean run() {
		return true;
	}

	/**
	 * a ThreadServiceTask turns in it's ThreadServiceWorker once it's
	 * completed.
	 * 
	 * @param worker
	 */
	public synchronized void threadReturn(ThreadServiceWorker worker) {
		if (!taskQueue.isEmpty()) {
			ThreadServiceTask task = taskQueue.poll();
			this.notify();
			worker.setTask(task);
		} else {
			worker.setTask(null);
			busyWorkers.remove(worker);
			idleWorkers.add(worker);
		}
	}

	/**
	 * Execute the given task as soon as possible
	 * 
	 * @param task
	 */

	public synchronized void execute(ThreadServiceTask task) {
		if (!keepThreadsRunning) {
			Log.warn("ThreadService was asked to execute a task while shut down, request ignored.");
		} else if (idleWorkers.empty()) {
			if (threads.size() < maxWorkerThreads) {
				ThreadServiceWorker tsw = new ThreadServiceWorker(this);
				tsw.start();
				threads.add(tsw);
				tsw.setTask(task);
				synchronized (tsw) {
					tsw.notify();
				}
				busyWorkers.add(tsw);
			} else {
				while (taskQueue.size() >= this.maxQueueLength) {
					// taskQueue is full.
					// let's just wait a while
					// Log.trace("ThreadService task queue is full. waiting a
					// bit.");
					
					// TODO: Benchmark difference between Thread.wait(1); and Thread.yield();
						Thread.yield();
				}
				taskQueue.add(task);
			}
		} else {
			ThreadServiceWorker worker = idleWorkers.pop();
			worker.setTask(task);
			synchronized (worker) {
				worker.notify();
			}
			busyWorkers.add(worker);
		}
	}

	@Override
	protected synchronized boolean shutdown() {
		keepThreadsRunning = false;
		this.masterThread.notify();
		try {
			this.masterThread.join(1000);
		} catch (InterruptedException e1) {
			Log.warn(e1);
		}
		for (ThreadServiceWorker tsw : threads) {
			try {
				tsw.notify();
				tsw.join(1000);
			} catch (InterruptedException e) {
				Log.warn(e);
			}
		}
		this.busyWorkers.clear();
		this.idleWorkers.clear();
		this.threads.clear();
		return true;
	}

	public void reportException(ThreadServiceWorker threadServiceWorker, ThreadServiceTask task, Throwable e) {
		Log.fatal(task.getClass().getName(), e);
	}

	@Override
	public void checkParameters(ServiceParameters sp) {
		minWorkerThreads = sp.getInt("minWorkerThreads", 1, 1000, 10);
		maxWorkerThreads = sp.getInt("maxWorkerThreads", 1, 10000, 20);
		maxQueueLength = sp.getInt("maxQueueLength", 1, Integer.MAX_VALUE, 10000);
	}

	public void waitOn(ThreadServiceTask task) {
		synchronized (task) {
			while (!task.isComplete()) {
				try {
					task.wait();
				} catch (InterruptedException e) {
					Log.fatal(e);
				}
			}
		}
	}

	/**
	 * Execute the given task after delay milliseconds at the earliest. The task
	 * will NOT be executed before delay milliseconds.
	 * 
	 * @param task
	 * @param delay
	 */

	public void executeLater(ThreadServiceTask task, long delay) {
		this.executeAtTime(task, System.currentTimeMillis() + delay);
	}

	/**
	 * Execute the given task at the given time (milliseconds since Epoch). Any
	 * positive number will queue the task behind any tasks added by execute().
	 * 
	 * @param task
	 * @param time
	 */

	public void executeAtTime(ThreadServiceTask task, long time) {
		task.setExecuteTime(time);
		this.masterThread.addTask(task);
	}

	/**
	 * PLACEHOLDER METHOD / NOT IMPLEMENTED! 
	 * 
	 * Executes the given task immediately, then at least every delay
	 * milliseconds afterwards. It will not execute task again unless the
	 * previous call to task has finished.
	 * 
	 * @param task
	 * @param delay
	 */	
	public synchronized void executeRepeat(ThreadServiceTask task, long delay) {
		//TODO: implement me!!
	}
}
