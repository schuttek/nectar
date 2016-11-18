package org.nectarframework.base.service;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;

/**
 * Services tie together common functionality throughout Nectar. While most the
 * base package Services provide some of Nectar's most essential tools (the
 * RequestService for example), specific projects should group common
 * functionality into one or more Services that provide Action implementations
 * easy access to shared resources.
 * 
 */
public abstract class Service {
	private ServiceParameters serviceParameters;

	protected enum State {
		none, initialized, running
	}

	private State runState = State.none;

	protected final void setParameters(ServiceParameters sp) throws ConfigurationException {
		if (runState == State.none) {
			checkParameters(sp);
			serviceParameters = sp;
		}
	}

	protected final State getRunState() {
		return runState;
	}

	protected final void setRunState(State rs) {
		this.runState = rs;
	}
	
	protected final ServiceParameters getParameters() {
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
	 * @param sp TODO
	 * 
	 * @throws ConfigurationException
	 */
	protected abstract void checkParameters(ServiceParameters sp) throws ConfigurationException;

	/**
	 * Stage 2: If your Service will make use of another Service, make sure you
	 * declare it in this method with the dependancy() method.
	 * 
	 * 
	 * @return true if every thing worked successfully, false will halt the
	 *         startup process and exit Nectar.
	 * @throws ServiceUnavailableException
	 */
	protected abstract boolean establishDependencies() throws ServiceUnavailableException;

	/**
	 * Stage 3: At this point, if your service has declared a dependancy, that
	 * Service has performed it's init() method, so those Services can now
	 * provide basic functionality.
	 * 
	 * @return true if every thing worked successfully, false will halt the
	 *         startup process and exit Nectar.
	 * 
	 */
	protected abstract boolean init();

	/**
	 * Declares the given serviceClass as a dependency for this Service.
	 * 
	 * @param serviceClass
	 * @return the shared instance of serviceClass
	 * @throws ServiceUnavailableException
	 *             if the serviceClass couldn't be found, or isn't configured to
	 *             be started.
	 */
	protected <T extends Service>T dependency(Class<T> serviceClass) throws ServiceUnavailableException {
		Service service = ServiceRegister.addServiceDependancy(this, serviceClass);
		if (service == null) {
			throw new ServiceUnavailableException(
					this.getClass().getName() + " requires the Service " + serviceClass.getName());
		}
		return serviceClass.cast(service);
	}

	protected final boolean rootServiceRun() {
		if (runState != State.initialized)
			throw new IllegalStateException("Can't run while in state: " + runState.name());
		if (run()) {
			this.runState = State.running;
			return true;
		}
		return false;
	}

	/**
	 * Stage 4: This is the final startup step. All dependencies have already
	 * passed this stage. By the end of this method, your Service should be
	 * fully operational.
	 * 
	 * @return true if every thing worked successfully, false will halt the
	 *         startup process and exit Nectar.
	 */
	protected abstract boolean run();

	protected final boolean __rootServiceShutdown() {
		if (runState != State.running)
			throw new IllegalStateException();
		if (ServiceRegister.shutdownDependancies(this) && shutdown()) {
			this.runState = State.none;
			return true;
		}
		return false;
	}

	/**
	 * This is called when Nectar shuts down or restarts. All resources should
	 * be released, Threads rejoined, connections closed, etc.
	 * 
	 * @return true if every thing worked successfully, false will halt a
	 *         restart and exit Nectar.
	 */
	protected abstract boolean shutdown();

}
