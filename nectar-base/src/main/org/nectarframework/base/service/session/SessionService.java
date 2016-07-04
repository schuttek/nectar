package org.nectarframework.base.service.session;

import java.util.HashMap;
import java.util.Random;

import org.nectarframework.base.service.Service;

/**
 * 
 * The SessionService manages data used and shared by several Request objects.
 * 
 */
public class SessionService extends Service {

	private HashMap<Long, Session> sessionStore;

	@Override
	protected boolean init() {
		sessionStore = new HashMap<Long, Session>();
		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		sessionStore.clear();
		return true;
	}

	@Override
	public boolean establishDependancies() {
		return true;
	}

	public Session getSessionById(Long sessionId) {
		Session session = sessionStore.get(sessionId);
		return session;
	}

	public Session createSession() {
		Random rand = new Random();
		Long sessionId = new Long(rand.nextLong());
		Session session = new Session(sessionId);
		sessionStore.put(sessionId, session);
		return session;
	}

	@Override
	public void checkParameters() {
	}

	public void forgetSession(Session session) {
		sessionStore.remove(session.getSessionId());
	}
	
}
