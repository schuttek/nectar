package org.nectarframework.base.service.event;

import java.util.HashMap;
import java.util.HashSet;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.thread.ThreadServiceTask;

/**
 * The EventService allows the spreading of an Event object to other Services that are listening.
 * 
 * 
 * 
 * @author skander
 *
 */
public class EventService extends Service {

	protected HashMap<EventChannel, HashSet<EventListener>> channelListeners;
	protected HashSet<EventListener> globalListeners;
	protected ThreadService threadService;

	@Override
	public void checkParameters() throws ConfigurationException {

	}

	@Override
	public boolean establishDependancies() {
		threadService = (ThreadService) ServiceRegister.getService(ThreadService.class);
		return true;
	}

	@Override
	protected boolean init() {
		channelListeners = new HashMap<EventChannel, HashSet<EventListener>>();
		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	/**
	 * Share an event with anyone who's listening...
	 * 
	 * @param event
	 */
	public synchronized void publishEvent(Event event) {
		EventChannel ec = event.getChannel();
		for (EventListener el : this.channelListeners.get(ec)) {
			threadService.execute(new ThreadServiceTask() {
				@Override
				public void execute() throws Exception {
					el.handleListenerServiceEvent(event);
				}
			});
		}
		for (EventListener el : this.globalListeners) {
			threadService.execute(new ThreadServiceTask() {
				@Override
				public void execute() throws Exception {
					el.handleListenerServiceEvent(event);
				}
			});
		}
	}
	
	public synchronized void registerOnChannel(EventChannel channel, EventListener listener) {
		HashSet<EventListener> set = channelListeners.get(channel);
		if (set == null) {
			set = new HashSet<EventListener>();
			channelListeners.put(channel, set);
		}
		set.add(listener);
	}

	public synchronized void unregisterListener(EventListener listener) {
		for (EventChannel ec : channelListeners.keySet()) {
			unregisterListenerOnChannel(listener, ec);
		}

		for (EventListener el : globalListeners) {
			if (el == listener) {
				globalListeners.remove(el);
			}
		}
	}

	public void unregisterListenerOnChannel(EventListener listener, EventChannel channel) {
		for (EventListener el : channelListeners.get(channel)) {
			if (el == listener) {
				channelListeners.get(channel).remove(el);
			}
		}
	}
}
