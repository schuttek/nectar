package org.nectarframework.base.service.directory;

/**
 * The DirectoryService maps request paths to Actions.
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.nectarframework.base.form.Form;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.xml.sax.SAXException;

/**
 * The DirectoryService is mainly a pathfinder: it maps requests for a given
 * host, port and path, to the Action class that is configured to handle it.
 * 
 * It also deals with configured Forms, redirects and path rewriting.
 * 
 */
public class DirectoryService extends Service {
	
	private String configFilePath = "config/pathConfig.xml";
	
	private HashMap<String, HashMap<String, DirAction>> pathMap;

	@Override
	public void checkParameters() {
		configFilePath = this.serviceParameters.getValue("configFilePath");
	}

	@Override
	protected boolean init() {
		pathMap = new HashMap<String, HashMap<String, DirAction>>();
		boolean ret = buildFromFile(configFilePath);

		if (Log.isTrace() && ret) {
			Log.trace(configToString());
		}
		
		return ret;
	}

	private boolean buildFromFile(String cfp) {
		Element configElement = null;
		try {
			configElement = XmlService.staticFromXml(new FileInputStream(cfp));
		} catch (FileNotFoundException e) {
			Log.fatal(e);
			return false;
		} catch (SAXException e) {
			Log.fatal(e);
			return false;
		} catch (IOException e) {
			Log.fatal(e);
			return false;
		}
		

		if (!configElement.isName("pathConfig")) {
			Log.fatal(cfp + " doesn't contain a valid path config");
			return false;
		}
		
		for (Element project : configElement.getChildren("project")) {
			String namespace = project.get("namespace");
			
			
			HashMap<String, DirForm> formMap = new HashMap<String, DirForm>();
			
			for (Element form : project.getChildren("form")) {
				DirFormvar[] fva = new DirFormvar[form.getChildren().size()];
				int t=0;
				for (Element formvar : form.getChildren("var")) {
					DirFormvar fv = new DirFormvar();
					fv.name = formvar.get("name");
					fv.type = Form.VarType.getByString(formvar.get("type"));
					fv.nullAllowed = formvar.isAttribute("null", "true");
					fva[t] = fv;
				}
				
				DirForm f = new DirForm();
				f.formvars = fva;
				f.name = form.get("name");
				formMap.put(f.name, f);
			}

			HashMap<String, DirAction> actionMap = new HashMap<String, DirAction>();
			
			for (Element action : project.getChildren("action")) {
				DirAction da = new DirAction();
				da.name = action.get("name");
				da.packageName = action.get("package");
				da.className = action.get("class");
				da.defaultOutput = action.get("defaultOutput");
				da.templateName = action.get("templateName");
				da.formName = action.get("form");
				
				da.form = formMap.get(da.formName);
				
				actionMap.put(da.name, da);
			}
			
			HashMap<String, DirAction> daMap = new HashMap<String, DirAction>();
			
			for (Element path : project.getChildren("path")) {
				daMap.put(path.get("path"), actionMap.get(path.get("action")));
			}
			
			if (!pathMap.containsKey(namespace)) {
				pathMap.put(namespace, new HashMap<String, DirAction>());
			}

			pathMap.get(namespace).putAll(daMap);
		}
		
		return true;
	}


	private String configToString() {
		StringBuffer sb = new StringBuffer("Path Configuration:");
		
		for (String k : pathMap.keySet()) {
			HashMap<String, DirAction> map = pathMap.get(k);
			sb.append("namespace: '"+k+"'\n");
			for (String ac : map.keySet()) {
				DirAction da = map.get(ac);
				sb.append("Path: '"+ac+ "' -> '"+da.name+"' ("+da.className+") '"+da.formName+"'\n");
			}
		}
		return sb.toString();
	}

	
	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		return true;
	}

	public DirAction lookupAction(String prefix, String path) {
		HashMap<String, DirAction> m = pathMap.get(prefix);
		if (m != null) {
			return m.get(path);
		}
		return null;
	}
}
