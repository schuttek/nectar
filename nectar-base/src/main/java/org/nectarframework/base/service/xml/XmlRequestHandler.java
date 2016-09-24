package org.nectarframework.base.service.xml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nectarframework.base.action.Action;
import org.nectarframework.base.form.Form;
import org.nectarframework.base.service.directory.DirAction;
import org.nectarframework.base.service.directory.DirPath;
import org.nectarframework.base.service.directory.DirRedirect;
import org.nectarframework.base.service.directory.DirectoryService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.session.Session;
import org.nectarframework.base.service.thread.ThreadServiceTask;

public class XmlRequestHandler extends ThreadServiceTask {

	private XmlServerService xmlServerService;
	private Connection connection;
	private Session session;
	private Element element;
	private DirectoryService directoryService;

	public XmlRequestHandler(XmlServerService xmlServerService, DirectoryService directoryService,
			Connection connection, Session session, Element element) {
		this.xmlServerService = xmlServerService;
		this.connection = connection;
		this.session = session;
		this.element = element;
		this.directoryService = directoryService;
	}

	@Override
	public void execute() throws Exception {
	
		long startTime = System.nanoTime();
		
		String actionStr = element.get("action");
		
		HashMap<String, List<String>> parameters = paramterMap(element.getChildren().getFirst());

		
		DirPath dirPath = null;
		
		while (dirPath == null || !(dirPath instanceof DirAction)) {
			dirPath = directoryService.lookupPath(actionStr);
		
			if (dirPath == null) {
					Log.warn("no action "+actionStr);
					return;
				}
		
			if (dirPath instanceof DirRedirect) {
				parameters.putAll(((DirRedirect)dirPath).variables);
				dirPath = directoryService.lookupPath(actionStr);
			}
		}
		
		DirAction dirAction = (DirAction)dirPath;

		
		Form form = new Form(dirAction.form, parameters); 
		
		form.setSession(session);
		
		Action action = (Action) ClassLoader.getSystemClassLoader().loadClass(dirAction.className).newInstance();
		
		action._init(form);
		Element response = action.execute();
		
		xmlServerService.sendResponse(connection, element.get("id"), response);

		long duration = System.nanoTime() - startTime;
		
		Log.accessLog(actionStr, form.getElement(), element.getChildren().getFirst(), response, duration / 1000, connection.getRemoteIp(), session);
	}

	protected static HashMap<String, List<String>> paramterMap(Element e) {
		HashMap<String, List<String>> para = new HashMap<String, List<String>>();
		for (String key : e.getAttributes().keySet()) {
			LinkedList<String> list = new LinkedList<String>();
			list.add(e.get(key));
			para.put(key, list);
		}

		for (Element child : e.getChildren()) {

			if (para.containsKey(child.getName())) {
				LinkedList<String> list = new LinkedList<String>();
				list.add(e.get("value"));
				para.put(e.getName(), list);
			} else {
				List<String> list = para.get(e.getName());
				list.add(e.get("value"));
			}

		}

		return para;
	}

}
