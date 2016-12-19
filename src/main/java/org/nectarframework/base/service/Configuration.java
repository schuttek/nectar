package org.nectarframework.base.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.nectarframework.base.element.Element;
import org.nectarframework.base.exception.ConfigurationException;

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

		// FIXME: make default locale configurable
		Locale.setDefault(Locale.UK);

		for (Element node : configElm.getChildren("node")) {
			String nodeGroupName = node.get("group");
			if (nodeGroupName == "") {
				Log.warn(
						"A node element in the configuration file didn't have a group attribute. This node will be ignored.");
				continue;
			}

			LinkedList<Service> serviceList = new LinkedList<Service>();
			for (Element serviceElm : node.getChildren("service")) {
				String className = serviceElm.get("class");
				if (className == "") {
					Log.warn(
							"A service element in the configuration file didn't have a class attribute. This service will be ignored.");
					continue;
				}
				Service service = null;
				try {
					service = (Service) Class.forName(className).newInstance();
				} catch (ClassCastException e) {
					Log.warn("While instantiating the Service " + className
							+ " in the configuration, the indicated class wasn't a subtype of Service!", e);
				} catch (InstantiationException e) {
					Log.warn("While instantiating the Service " + className + " in the configuration...", e);
				} catch (IllegalAccessException e) {
					Log.warn("While instantiating the Service " + className + "in the configuration...", e);
				} catch (ClassNotFoundException e) {
					Log.warn("While instantiating the Service " + className
							+ " in the configuration, the indicated class couldn't be found!", e);
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
		Log.trace("Full Config WITHOUT Inheritance:" + configElm.toString());

		// let's first build a node map:
		HashMap<String, Element> nodeMap = new HashMap<>();
		for (Element node : configElm.getChildren("node")) {
			nodeMap.put(node.get("group"), node.copy());
		}

		Element newConfig = new Element("config");

		
		// FIXME: loop this to do multiple inheritance see mantis issue 38
		for (Element inheritingNode : configElm.getChildren("node")) {
			String inherits = inheritingNode.get("inherits");
			if (inherits != null) {
				if (!nodeMap.containsKey(inherits)) {
					// make sure that nodes that inherit for another node
					// actually exist.
					throw new ConfigurationException("Node: group=" + inheritingNode.get("group")
							+ " has an inherits tag (" + inherits + ") that doesn't cannot be found.");
				} else {
					// this node will now inherit from the grandparent
					Element parentNode = nodeMap.get(inherits).copy();
					inheritingNode.add("inherits", parentNode.get("inherits"));

					// only copy over the services from the parent that the
					// child doesn't have
					for (Element inheritedServices : parentNode.getChildren("service")) {
						boolean found = false;
						for (Element customServices : inheritingNode.getChildren("service")) {
							if (inheritedServices.get("class").equals(customServices.get("class"))) {
								found = true;
								break;
							}
						}
						if (!found) {
							inheritingNode.add(inheritedServices);
						}
					}
					// add the new / bigger child to the fresh config.
					newConfig.add(inheritingNode);
				}
			} else {
				newConfig.add(inheritingNode);
			}
		}
		Log.trace("Final Full Config with inheritance" + newConfig.toString());

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
