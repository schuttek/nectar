package org.nectarframework.base.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.nectarframework.base.Main;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceRegister;
import org.nectarframework.base.service.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Configuration {
	ServiceRegister sr = null;

	HashMap<String, LinkedList<Service>> serviceListByNodeGroup = new HashMap<String, LinkedList<Service>>();

	File configFile;
	String nodeName;
	String nodeGroup;
	String command;

	public Configuration(ServiceRegister sr) {
		this.sr = sr;
	}

	public boolean parseArgs(Options options, CommandLine line) {

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
			return false;
		}

		configFile = new File(line.getOptionValue("configFile"));
		if (!configFile.exists()) {
			System.err.println("configuration file: " + line.getOptionValue("configFile") + " cannot be found.");
			return false;
		}
		if (!configFile.canRead()) {
			System.err.println("configuration file: " + line.getOptionValue("configFile") + " is not readable.");
			return false;
		}

		nodeName = line.getOptionValue("nodeName");
		nodeGroup = line.getOptionValue("nodeGroup");

		return true;
	}

	private Document parseDOM(File f) throws ConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(f);
		} catch (ParserConfigurationException e) {
			throw new ConfigurationException(e);
		} catch (SAXException e) {
			throw new ConfigurationException(e);
		} catch (IOException e) {
			throw new ConfigurationException(e);
		}
	}

	public boolean parseFullConfig() throws ConfigurationException {
		// open config.xml file, parse each service and parameters.

		Document dom = parseDOM(this.configFile);
		
		//FIXME: make default locale configurable
		Locale.setDefault(Locale.UK);

		NodeList nodeGroupNodes = dom.getElementsByTagName("node");
		for (int k = 0; k < nodeGroupNodes.getLength(); k++) {
			String nodeGroupName = ((Element) nodeGroupNodes.item(k)).getAttribute("group");
			if (nodeGroupName == "") {
				Log.warn("A node element in the configuration file didn't have a group attribute. This node will be ignored.");
				continue;
			}

			LinkedList<Service> serviceList = new LinkedList<Service>();

			NodeList serviceNodes = ((Element) nodeGroupNodes.item(k)).getElementsByTagName("service");
			for (int i = 0; i < serviceNodes.getLength(); i++) {
				Element serviceElement = (Element) serviceNodes.item(i);
				String className = serviceElement.getAttribute("class");
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

				ServiceParameters serviceParams = ServiceParameters.parseServiceParameters(serviceElement);

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

	public File getConfigFile() {
		return configFile;
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
}
