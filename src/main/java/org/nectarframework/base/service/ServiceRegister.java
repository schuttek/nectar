package org.nectarframework.base.service;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.nectarframework.base.element.Element;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.exception.ServiceUnavailableRuntimeException;

public final class ServiceRegister {
	private Configuration config = null;
	private Element configElement = null;
	/** only one instance of this class allowed!! */
	private static ServiceRegister instance = null;

	private RUN_STATE runState = RUN_STATE.none;

	/**
	 * if you're not on the register, you're not a real Service...
	 */
	private HashMap<Class<? extends Service>, Service> registerByClass;

	private HashMap<Service, List<Service>> dependancies;

	public ServiceRegister() {
		if (instance != null) { // there can only be one!!
			throw new IllegalStateException("Cannot instantiate ServiceRegister more than once.");
		}
		instance = this;
		registerByClass = new HashMap<Class<? extends Service>, Service>();
		dependancies = new HashMap<Service, List<Service>>();
	}

	/**
	 * RUN_STATE determines the state of Nectar.
	 * 
	 * none - pre-initialization stage. configChecked - the config.xml for
	 * nectar.base.config.Configuration has been found, and seems to be sane.
	 * initialized - all services have passed init() and returned true. running
	 * - all services have passed run() and returned true. restarting - some
	 * services are undergoing a reinit. ... to be impelemented shutdown - all
	 * services have fully shut down, releasing all I/O safely.
	 * 
	 *
	 */
	public enum RUN_STATE {
		none, configChecked, initialized, running, restarting, shutdown
	}

	private HashMap<String, Service> serviceDirectory = new HashMap<String, Service>();

	public void setConfiguration(Configuration config) {
		this.config = config;
	}

	/**
	 * Let's start Nectar in Mode: Alone
	 * 
	 * @return
	 */
	public boolean initAlone() {
		// this.runMode = RUN_MODE.alone;
		return this.init();
	}

	/**
	 * Let's start Nectar in Mode: Master
	 * 
	 * @return
	 */

	public boolean initMaster() {
		// this.runMode = RUN_MODE.master;
		return this.init();
	}

	/**
	 * Let's start Nectar in Mode: Node
	 * 
	 * @return
	 */

	public boolean initNode() {
		// this.runMode = RUN_MODE.node;
		return this.init();
	}

	/**
	 * The second stage to the booting process.
	 * 
	 * Here we look at the config, and instantiate services...
	 * 
	 * then we make sure dependendancies are available.
	 * 
	 * We switch to RUN_STATE.initialized
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean init() {
		Log.info("ServiceRegister is initializing services for "+config.getNodeName()+"@"+config.getNodeGroup());
		if (runState != RUN_STATE.configChecked && runState != RUN_STATE.restarting) {
			throw new IllegalStateException();
		}

		List<Service> serviceList = config.getServiceList(this.config.getNodeGroup());
		if (serviceList == null) {
			Log.fatal("No services configured for nodeGroup " + config.getNodeGroup());
			return false;
		}
		for (Service s : serviceList) {
			Log.trace("Registering " + s.getClass().getName());
			registerByClass.put(s.getClass(), s);
			Class<?> superClass = s.getClass().getSuperclass();
			while (!superClass.equals(Service.class)) {
				Log.trace("Registering " + superClass.getName());
				registerByClass.put((Class<? extends Service>) superClass, s);
				superClass = superClass.getSuperclass();
			}
		}
		for (Service s : serviceList) {
			if (s.getRunState() == Service.State.none) {
				try {
					if (!initService(s))
						return false;
				} catch (ServiceUnavailableException e) {
					Log.fatal(e);
					return false;
				}
			} else if (s.getRunState() != Service.State.initialized) {
				Log.fatal("ServiceRegister.init() has Service " + s.getClass().getName() + " in irregular state: "
						+ s.getRunState());
				return false;
			}
		}

		this.runState = RUN_STATE.initialized;
		return true;
	}

	private boolean initService(Service s) throws ServiceUnavailableException {
		if (!s.establishDependencies()) {
			// Log.fatal("ServiceResgster.init: Service " +
			// s.getClass().getName() +
			// " returned false on Service.establishDependancies().");
			throw new ServiceUnavailableException("ServiceResgister.init: Service " + s.getClass().getName()
					+ " returned false on Service.establishDependancies().");
		}

		if (s.init()) {
			s.setRunState(Service.State.initialized);
		} else {
			Log.fatal("Service: " + s.getClass().toString() + " failed to initialize!");
			return false;
		}
		Class<?> cl = s.getClass().getSuperclass();
		while (cl.getName().compareTo("nectar.base.service.Service") != 0 && cl.getName() != "java.lang.Object") {
			cl = cl.getClass().getSuperclass();
		}

		return true;
	}

	public boolean shutdown() {
		Log.info("[ServiceRegister] shutdown triggered.");
		for (Service s : serviceDirectory.values()) {
			shutdownDependancies(s);
		}

		for (Service s : serviceDirectory.values()) {
			s.__rootServiceShutdown();
		}
		if (runState != RUN_STATE.restarting) {
			runState = RUN_STATE.shutdown;
		}
		serviceDirectory.clear();
		instance = null;
		Log.info("[ServiceRegister] Nectar is closed for business.");
		return true;
	}

	public boolean restart(File configFile) {
		// FIXME: this isn't reading / refreshing the config file...
		Log.info("ServiceRegister is restarting services...");
		if (runState != RUN_STATE.running) {
			throw new IllegalStateException();
		}
		runState = RUN_STATE.restarting;

		Configuration newConfig = new Configuration(this);

		boolean newConfigSafe = false;

		try {
			newConfigSafe = newConfig.parseFullConfig(configElement);
		} catch (ConfigurationException e) {
			Log.fatal("Configuration Problem detected, restart aborted", e);
		}

		if (newConfigSafe) {
			if (shutdown()) {
				config = newConfig;
				if (init()) {
					begin();
				}
			}
		}
		return false;
	}

	public boolean configCheck() {

		if (Charset.defaultCharset().name().compareTo("UTF-8") != 0) {
			Log.warn("Nectar should really only run with UTF-8 Charset. default charset is: " + Charset.defaultCharset()
					+ ". This might cause weird problems. Please check your java installation / runtime options. ");
		}

		try {
			boolean configValid = config.parseFullConfig(configElement);
			if (configValid) {
				this.runState = RUN_STATE.configChecked;
				return true;
			}
			Log.warn("Configuration Parse Failed.");
			return false;
		} catch (ConfigurationException e) {
			Log.fatal("Configuration Problem detected", e);
		}
		return false;
	}

	public boolean begin() {
		Log.info("ServiceRegister is running services...");
		List<Service> serviceList = config.getServiceList(this.config.getNodeGroup());
		if (serviceList == null) {
			Log.fatal("Null services configured for node group " + config.getNodeGroup());
			return false;
		}
		if (serviceList.isEmpty()) {
			Log.fatal("No services configured for node group " + config.getNodeGroup());
			return false;
		}
		for (Service s : serviceList) {
			Log.trace("running Service: " + s.getClass().getName());
			if (!s.rootServiceRun()) {
				Log.info("ServiceRegister: Service " + s.getClass().getName() + " failed to run()");
				System.exit(-1);
			}
		}
		this.runState = RUN_STATE.running;
		Log.info("ServiceRegister: All services are up and running.");
		return true;
	}

	public static ServiceRegister getInstance() {
		return instance;
	}

	public Configuration getConfiguration() {
		return config;
	}

	public void setConfigElement(Element configElement) {
		this.configElement = configElement;
	}

	public static Service addServiceDependancy(Service service, Class<? extends Service> serviceClass)
			throws ServiceUnavailableException {
		return instance._addServiceDependancy(service, serviceClass);
	}

	private Service _addServiceDependancy(Service service, Class<? extends Service> serviceClass)
			throws ServiceUnavailableException {
		Service dependant = instance.registerByClass.get(serviceClass);
		if (dependant == null) {
			return null;
		}

		if (dependant.getRunState() == Service.State.none) {
			if (!initService(dependant)) {
				return null;
			}
		}

		if (dependancies.containsKey(service)) {
			dependancies.get(service).add(dependant);
		} else {
			ArrayList<Service> list = new ArrayList<Service>();
			list.add(dependant);
			dependancies.put(service, list);
		}

		return dependant;
	}

	public static boolean shutdownDependancies(Service service) {
		return instance._shutdownDependancies(service);
	}

	public boolean _shutdownDependancies(Service service) {
		List<Service> list = dependancies.get(service);

		for (Service dependant : list) {
			if (dependant.getRunState() == Service.State.running) {
				if (!_shutdownDependancies(dependant)) {
					return false;
				}
				if (!dependant.rootServiceRun()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get the instance of the Service implemented by the given serviceClass.
	 * 
	 * 
	 * 
	 * @param serviceClass
	 * @return
	 * @throws ServiceUnavailableRuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Service> T getService(Class<T> serviceClass) {
		if (instance == null) {
			throw new ServiceUnavailableRuntimeException("Nectar is not running, or not available.");
		}
		switch (instance.runState) {
		case none:
			throw new ServiceUnavailableRuntimeException("Nectar is not started.");
		case restarting:
			throw new ServiceUnavailableRuntimeException("Nectar is restarting.");
		case configChecked:
		case initialized:
		case running:
			Service service = instance.registerByClass.get(serviceClass);
			if (service == null)
				return null;
			return (T) service;
		case shutdown:
			throw new ServiceUnavailableRuntimeException("Nectar is shut down.");
		}
		// unreachable code since all switch cases are addressed.
		throw new ServiceUnavailableRuntimeException(
				"Nectar is in an unknown RUN_STATE, and has no idea what's going on.");
	}
}
