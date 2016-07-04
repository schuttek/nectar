package org.nectarframework.base.service;

import org.nectarframework.base.exception.ConfigurationException;

/**
 * Services tie together common functionality throughout Nectar. While most the
 * base package Services provide some of Nectar's most essential tools (the
 * RequestService for example), specific projects should group common
 * functionality into one or more Services that provide Action implementations
 * easy access to shared resources.
 * 
 */
public abstract class Service {
	protected ServiceParameters serviceParameters;

	protected enum STATE {
		none, initialized, running, shutdown
	}

	STATE state = STATE.none;

	public void setParameters(ServiceParameters sp) {
		serviceParameters = sp;
	}

	public ServiceParameters getParameters() {
		return this.serviceParameters;
	}
	
	/**
	 * This method is the first step setting up a Service.
	 * 
	 * This method should look into the serviceParameters property, and look for
	 * parameters this Service is interested in.
	 * 
	 * If those parameters are fatally incorrect, a Configuration Exception
	 * should be thrown.
	 * 
	 * The parameters, once validated, should be stored as private or protected
	 * fields in the implementing Service class.
	 * 
	 * By validated I mean passing min/max checks or a regex check, but not
	 * something that can send an Exception unexpectedly, like a host name
	 * lookup. In this phase of the startup, all input/output, and all other
	 * Services should be considered unavailable.
	 * 
	 * All we're doing is checking if our configuration file is just remotely
	 * sane, and setting some default values.
	 * 
	 * @throws ConfigurationException
	 */
	public abstract void checkParameters() throws ConfigurationException;


	/**
	 * Stage 2: If your Service will make use of another Service, make sure
	 * 
	 * 
	 * @return
	 * @throws ServiceUnavailableException
	 */
	public abstract boolean establishDependancies() throws ServiceUnavailableException;

	/**
	 * This is the third stage towards starting up a service. At this point, if
	 * your service has declared a dependancy, that Service has performed it's
	 * init() method
	 * 
	 * @return
	 */
	protected abstract boolean init();
	
	protected Service dependancy(Class<? extends Service> serviceClass) throws ServiceUnavailableException {
		Service service = ServiceRegister.addServiceDependancy(this, serviceClass);
		if (service == null) {
			throw new ServiceUnavailableException(this.getClass().getName() + " requires the Service " + serviceClass.getName());
		}
		return service;
	}
	

	public final boolean _run() {
		if (state != STATE.initialized)
			throw new IllegalStateException("Can't run while in state: " + state.name());
		if (run()) {
			this.state = STATE.running;
			return true;
		}
		return false;
	}

	protected abstract boolean run();

	public final boolean _shutdown() {
		if (state != STATE.running)
			throw new IllegalStateException();
		if (ServiceRegister.shutdownDependancies(this) && shutdown()) {
			this.state = STATE.shutdown;
			return true;
		}
		return false;
	}

	protected abstract boolean shutdown();
}
