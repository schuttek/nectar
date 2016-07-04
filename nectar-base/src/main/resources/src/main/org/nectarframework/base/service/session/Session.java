package org.nectarframework.base.service.session;

import java.util.HashMap;
import java.util.Locale;

public class Session {
	private Long sessionId;

	private HashMap<String, String> sessionVars = new HashMap<String, String>();
	private Locale locale;
	
	public Session(Long sessionId) {
		this.sessionId = sessionId;
		locale = new Locale("en", "UK");
	}

	public Long getSessionId() {
		return sessionId;
	}
	
	public HashMap<String, String> getVars() {
		return this.sessionVars;
	}

	/** is the session variable in key equal to value?
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean is(String key, String value) {
		String s = sessionVars.get(key);
		if (s == null && value == null || s != null && s.compareTo(value) == 0) {
			return true;
		}
		return false;
	}

	public Locale getLocale() {
		return locale;
	}

	public boolean isLoggedIn() {
		return false;
	}

}
