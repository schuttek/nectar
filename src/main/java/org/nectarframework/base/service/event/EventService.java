package org.nectarframework.base.service.event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.thread.ThreadService;
import org.nectarframework.base.service.thread.ThreadServiceTask;

/**
 * The EventService allows the spreading of an Event object to other Services
 * that are listening.
 * 
 * 
 * @author skander
 *
 */
public final class EventService extends Service {

	protected ConcurrentHashMap<EventChannel, CopyOnWriteArraySet<EventListener>> channelListeners;
	protected CopyOnWriteArraySet<EventListener> globalListeners;
	protected ThreadService threadService;

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {

	}

	@Override
	public boolean establishDependencies() {
		threadService = (ThreadService) ServiceRegister.getService(ThreadService.class);
		return true;
	}

	@Override
	protected boolean init() {
		channelListeners = new ConcurrentHashMap<>();
		globalListeners = new CopyOnWriteArraySet<>();
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
	public synchronized void publishEvent(NectarEvent event) {
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
		CopyOnWriteArraySet<EventListener> set = channelListeners.get(channel);
		if (set == null) {
			set = new CopyOnWriteArraySet<EventListener>();
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

	/**
	 * Reference to https://en.wikipedia.org/wiki/CQ_(call)
	 * 
	 * synonym for isChannelActive(EventChannel).
	 * 
	 * @param channel
	 * @return
	 */

	public boolean cq(EventChannel channel) {
		return isChannelActive(channel);
	}

	/**
	 * is anyone listening on this channel? do I even need to bother building an
	 * event?
	 * 
	 * This method ignores global listeners.
	 * 
	 * @param channel
	 * @return
	 */
	public boolean isChannelActive(EventChannel channel) {
		return channelListeners.contains(channel);
	}

	/**
	 * is anyone listening?
	 * 
	 * @return
	 */
	public boolean isActive() {
		if (!globalListeners.isEmpty())
			return true;
		return false;
	}

	public boolean isActive(EventChannel channel) {
		if (isActive()) {
			return true;
		}
		if (channelListeners.containsKey(channel)) {
			return true;
		}
		return false;
	}
}
