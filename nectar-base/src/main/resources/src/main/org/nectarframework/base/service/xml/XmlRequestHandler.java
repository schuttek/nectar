package org.nectarframework.base.service.xml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nectarframework.base.action.Action;
import org.nectarframework.base.form.Form;
import org.nectarframework.base.service.directory.DirAction;
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

	public XmlRequestHandler(XmlServerService xmlServerService, DirectoryService directoryService, Connection connection, Session session, Element element) {
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

		int prefixIdx = actionStr.lastIndexOf('/');
		String prefix = "";
		String actionPath = "";
		if (prefixIdx > 0) {
			prefix = actionStr.substring(0, prefixIdx);
			actionPath = actionStr.substring(prefixIdx +1, actionStr.length());
		} else {
			actionPath = actionStr;
		}
		
		
		DirAction dirAction = directoryService.lookupAction(prefix, actionPath);
		
		if (dirAction == null) {
			Log.warn("no action "+prefix+" "+actionPath);
			return;
		}
		
		Form form = new Form(dirAction.form, paramterMap(element.getChildren().getFirst())); 
		
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
