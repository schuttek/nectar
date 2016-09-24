package org.nectarframework.base.service.thread;

import java.util.Comparator;

public class ThreadServiceTaskComparator implements Comparator<ThreadServiceTask> {

	public int compare(ThreadServiceTask o1, ThreadServiceTask o2) {
		long diff = o1.getExecuteTime() - o2.getExecuteTime();
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		}
		return 0;
	}

}
