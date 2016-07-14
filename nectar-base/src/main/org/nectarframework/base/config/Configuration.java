package org.nectarframework.base.config;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.nectarframework.base.Main;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;

public class Configuration {
	ServiceRegister sr = null;

	HashMap<String, LinkedList<Service>> serviceListByNodeGroup = new HashMap<String, LinkedList<Service>>();

	private String nodeName;
	private String nodeGroup;
	private String command;

	public Configuration(ServiceRegister sr) {
		this.sr = sr;
	}

	public File parseArgs(Options options, CommandLine line) {

		boolean missingArgs = false;
		if (!line.hasOption("configFile")) {
			System.err.println("ERROR: configFile command line argument is required.");
			missingArgs = true;
		}
		if (!line.hasOption("nodeName")) {
			System.err.println("ERROR: nodeName command line argument is required.");
			missingArgs = true;
		}
		if (!line.hasOption("nodeGroup")) {
			System.err.println("ERROR: nodeGroup command line argument is required.");
			missingArgs = true;
		}

		if (missingArgs) {
			Main.runHelp(options);
			return null;
		}

		File configFile = new File(line.getOptionValue("configFile"));
		if (!configFile.exists()) {
			System.err.println("configuration file: " + line.getOptionValue("configFile") + " cannot be found.");
			return null;
		}
		if (!configFile.canRead()) {
			System.err.println("configuration file: " + line.getOptionValue("configFile") + " is not readable.");
			return null;
		}

		nodeName = line.getOptionValue("nodeName");
		nodeGroup = line.getOptionValue("nodeGroup");

		return configFile;
	}


	public boolean parseFullConfig(Element configElm) throws ConfigurationException {
		// open config.xml file, parse each service and parameters.
		
		//FIXME: make default locale configurable
		Locale.setDefault(Locale.UK);

		for (Element node : configElm.getChildren("node")) {
			String nodeGroupName = node.get("group");
			if (nodeGroupName == "") {
				Log.warn("A node element in the configuration file didn't have a group attribute. This node will be ignored.");
				continue;
			}

			LinkedList<Service> serviceList = new LinkedList<Service>();
			for (Element serviceElm : node.getChildren("service")) {
				String className = serviceElm.get("class");
				if (className == "") {
					Log.warn("A service element in the configuration file didn't have a class attribute. This service will be ignored.");
					continue;
				}
				Service service = null;
				try {
					service = (Service) Class.forName(className).newInstance();
				} catch (ClassCastException e) {
					Log.warn("While instantiating a Service in the configuration, the indicated class wasn't a subtype of Service!", e);
				} catch (InstantiationException e) {
					Log.warn("While instantiating a Service in the configuration...", e);
				} catch (IllegalAccessException e) {
					Log.warn("While instantiating a Service in the configuration...", e);
				} catch (ClassNotFoundException e) {
					Log.warn("While instantiating a Service in the configuration, the indicated class couldn't be found!", e);
				}

				ServiceParameters serviceParams = ServiceParameters.parseServiceParameters(serviceElm);

				service.setParameters(serviceParams);

				service.checkParameters();

				serviceList.add(service);
			}
			this.serviceListByNodeGroup.put(nodeGroupName, serviceList);
		}

		return true;
	}

	public List<Service> getServiceList(String nodeGroup) {
		return serviceListByNodeGroup.get(nodeGroup);
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getNodeGroup() {
		return nodeGroup;
	}

	public String getCommand() {
		return command;
	}

	public void setNodeGroup(String nodeGroup) {
		this.nodeGroup = nodeGroup;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
}
