package org.nectarframework.base.service.thread;

import java.util.SortedSet;
import java.util.TreeSet;

import org.nectarframework.base.service.Log;

public class MasterThread extends Thread {

	private ThreadService ts = null;

	private SortedSet<ThreadServiceTask> taskSet = null;

	public MasterThread(ThreadService ts) {
		super("ThreadService-MasterThread");
		this.ts = ts;
		taskSet = new TreeSet<ThreadServiceTask>(new ThreadServiceTaskComparator());
	}

	public synchronized void addTask(ThreadServiceTask task) {
		taskSet.add(task);
		notify();
		// Log.trace("WaitingThread add " + task.toString());
	}

	public synchronized void run() {
		while (ts.isRunning()) {
			if (taskSet.isEmpty()) {
				// nothing to do, wait endlessly.
				try {
					// Log.trace("WaitingThread wait endlessly");
					wait();
				} catch (InterruptedException e) {
					Log.trace(e); // ignored
				}
			} else {
				// when is the next task due?
				long nowTime = System.currentTimeMillis();
				ThreadServiceTask firstTask = taskSet.first();
				long nextTime = firstTask.getExecuteTime();
				if (nowTime >= nextTime) { // task is overdue
					taskSet.remove(firstTask);
					// Log.trace("WaitingThread executing
					// "+firstTask.toString());
					ts.execute(firstTask);
				} else {
					// wait til due date
					long waitTime = nextTime - nowTime;
					try {
						// Log.trace("WaitingThread wait for "+waitTime);
						wait(waitTime);
					} catch (InterruptedException e) {
						Log.trace(e); // ignored
					}
				}
			}
		}
	}
	/*
	 * public static void main(String[] args) { SortedSet<ThreadServiceTask>
	 * taskSet = new TreeSet<ThreadServiceTask>(new
	 * ThreadServiceTaskComparator()); for (int i=0; i<10; i++) {
	 * ReconnectLaterTask rlt = new ReconnectLaterTask(null);
	 * rlt.setExecuteTime(System.currentTimeMillis() + i*100); taskSet.add(rlt);
	 * rlt = new ReconnectLaterTask(null);
	 * rlt.setExecuteTime(System.currentTimeMillis() - i*100); taskSet.add(rlt);
	 * }
	 * 
	 * int k=0; while (!taskSet.isEmpty()) { ThreadServiceTask tst =
	 * taskSet.first(); System.out.println(++k + ". " +tst.getExecuteTime());
	 * taskSet.remove(tst); } }
	 */

}
