package org.nectarframework.base.service;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.nectarframework.base.Main;
import org.nectarframework.base.exception.ConfigurationException;
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



	public boolean parseFullConfig(Element configElm) throws ConfigurationException {
		// open config.xml file, parse each service and parameters.
		
		configElm = expandInheritance(configElm);
		
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
					Log.warn("While instantiating the Service "+className+" in the configuration, the indicated class wasn't a subtype of Service!", e);
				} catch (InstantiationException e) {
					Log.warn("While instantiating the Service "+className+" in the configuration...", e);
				} catch (IllegalAccessException e) {
					Log.warn("While instantiating the Service "+className+"in the configuration...", e);
				} catch (ClassNotFoundException e) {
					Log.warn("While instantiating the Service "+className+" in the configuration, the indicated class couldn't be found!", e);
				}

				ServiceParameters serviceParams = ServiceParameters.parsesp(serviceElm);

				service.setParameters(serviceParams);

				serviceList.add(service);
			}
			this.serviceListByNodeGroup.put(nodeGroupName, serviceList);
		}

		return true;
	}

	private Element expandInheritance(Element configElm) throws ConfigurationException {
		// let's first build a node map: 
		Log.trace("Full Config WITHOUT Inheritance:" + configElm.toString());
		HashMap<String, Element> nodeMap = new HashMap<>();
		for (Element node : configElm.getChildren("node")) {
			nodeMap.put(node.get("group"), node.copy());
		}
		
		Element newConfig = new Element("config");
		
		// make sure that nodes that inherit for another node actually exist.
		// FIXME: node should be able to inherit from a whole line, this only does it once. 
		// 
		for (Element node : configElm.getChildren("node")) {
			String inherits = node.get("inherits");
			if (inherits != null) {
				if (!nodeMap.containsKey(inherits)) {
					throw new ConfigurationException("Node: group="+node.get("group")+" has an inherits tag ("+inherits+") that doesn't cannot be found.");
				} else {
					Element parentNode = nodeMap.get(inherits);
					Element newNode = parentNode.copy();
					newNode.addAll(node.getChildren());
					newNode.add("group", node.get("group"));
					newConfig.add(newNode);
				}
			} else {
				newConfig.add(node);
			}
		}
		
		
		Log.trace("Full Config with Inheritance:" + newConfig.toString());
		
		return newConfig;
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
