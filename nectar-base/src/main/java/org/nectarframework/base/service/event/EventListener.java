package org.nectarframework.base.service.event;

public interface EventListener {

	/**
	 * handle an Event sent by the EventService.
	 * 
	 * Note that some events may arrive *after* you've unregistered from the
	 * EventService due to multithreading, in that case it's your responsibility to ignore them.
	 * 
	 * @param e
	 */
	public void handleListenerServiceEvent(NectarEvent e);
}
