package org.nectarframework.base.exception;

public abstract class LockedConfig {
	private boolean locked = false;
	
	public void _lock() {
		this.locked = true;
		this.lock();
	}
	
	protected abstract void lock(); 
	
	protected void checkLock() throws LockedConfigException {
		if (this.locked) {
			throw new LockedConfigException();
		}
	}
}
