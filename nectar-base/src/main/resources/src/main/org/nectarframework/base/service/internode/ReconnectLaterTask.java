package org.nectarframework.base.service.internode;

import org.nectarframework.base.service.thread.ThreadServiceTask;

public class ReconnectLaterTask extends ThreadServiceTask {

	private InternodeService is;

	public ReconnectLaterTask(InternodeService is) {
		this.is = is;
	}

	@Override
	public void execute() throws Exception {
		is.connectToController();
	}

}
