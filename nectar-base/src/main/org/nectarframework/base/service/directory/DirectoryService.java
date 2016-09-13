package org.nectarframework.base.service.directory;

/**
 * The DirectoryService maps request paths to Actions.
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nectarframework.base.form.Form;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.pathfinder.IPathFinder;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.StringTools;
import org.xml.sax.SAXException;

/**
 * The DirectoryService is mainly a pathfinder: it maps requests for a given
 * host, port and path, to the Action class that is configured to handle it.
 * 
 * It also deals with configured Forms, redirects and path rewriting.
 * 
 */
public class DirectoryService extends IPathFinder {

	private String configFilePath = "config/pathConfig.xml";

	private HashMap<String, DirPath> pathMap;

	private Element pathConfigElement;

	@Override
	public void checkParameters() {
		configFilePath = this.serviceParameters.getValue("configFilePath");
	}

	@Override
	protected boolean init() {
		pathMap = new HashMap<String, DirPath>();
		boolean ret = buildFromFile(configFilePath);

		if (Log.isTrace() && ret) {
			Log.trace(configToString());
		}

		return ret;
	}

	private boolean buildFromFile(String cfp) {
		pathConfigElement = null;
		try {
			pathConfigElement = XmlService.fromXml(new FileInputStream(cfp));
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

		if (!pathConfigElement.isName("pathConfig")) {
			Log.fatal(cfp + " doesn't contain a valid path config");
			return false;
		}

		for (Element project : pathConfigElement.getChildren("project")) {
			String namespace = project.get("namespace");

			HashMap<String, DirForm> formMap = new HashMap<String, DirForm>();

			for (Element form : project.getChildren("form")) {
				DirFormvar[] fva = new DirFormvar[form.getChildren().size()];
				int t = 0;
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

			for (Element path : project.getChildren("path")) {
				pathMap.put(getAbsolutePath(namespace, path.get("path")), actionMap.get(path.get("action")));
			}

			for (Element redirect : project.getChildren("redirect")) {
				DirRedirect dirRedirect = new DirRedirect();
				dirRedirect.path = redirect.get("path");
				dirRedirect.toPath = redirect.get("toPath");
				dirRedirect.variables = new HashMap<String, List<String>>();
				for (Element rv : redirect.getChildren("var")) {
					if (dirRedirect.variables.containsKey(rv.get("name"))) {
						dirRedirect.variables.get(rv.get("name")).add(rv.get("value"));
					} else {
						LinkedList<String> ls = new LinkedList<String>();
						ls.add(rv.get("value"));
						dirRedirect.variables.put(rv.get("name"), ls);
					}
				}
				pathMap.put(getAbsolutePath(namespace, dirRedirect.path), dirRedirect);
			}

			for (Element proxy : project.getChildren("proxy")) {
				DirProxy dirProxy = new DirProxy();
				dirProxy.path = proxy.get("path");

				dirProxy.protocol = proxy.get("protocol");
				dirProxy.port = proxy.get("port");
				dirProxy.host = proxy.get("host");
				dirProxy.requestPath = proxy.get("requestPath");

				pathMap.put(getAbsolutePath(namespace, dirProxy.path), dirProxy);
			}

			for (Element staticElm : project.getChildren("static")) {
				DirStatic dirStatic = new DirStatic();
				dirStatic.path = staticElm.get("path");
				dirStatic.toPath = staticElm.get("toPath");
				pathMap.put(getAbsolutePath(namespace, dirStatic.path), dirStatic);
			}

		}

		return true;
	}

	private String getAbsolutePath(String namespace, String path) {
		if (namespace.startsWith("/")) {
			namespace = namespace.substring(1);
		}
		if (!namespace.endsWith("/")) {
			namespace += "/";
		}
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}

		return "/" + namespace + "/" + path;
	}

	private String configToString() {
		StringBuffer sb = new StringBuffer("Path Configuration:");

		for (String ac : pathMap.keySet()) {
			DirPath dp = pathMap.get(ac);
			if (dp instanceof DirAction) {
				sb.append("Path: '" + ac + "' -> '" + ((DirAction) dp).name + "' (" + ((DirAction) dp).className + ") '"
						+ ((DirAction) dp).formName + "'\n");
			} else if (dp instanceof DirRedirect) {
				sb.append("Redirect: '" + ac + "' -> '" + ((DirRedirect) dp).path + "' (" + ((DirRedirect) dp).toPath
						+ ") '" + StringTools.mapToString(((DirRedirect) dp).variables) + "'\n");
			} else if (dp instanceof DirProxy) {
				sb.append("Proxy: '" + ac + "' -> '" + ((DirProxy) dp).path + "' (" + ((DirProxy) dp).protocol + " "
						+ ((DirProxy) dp).host + ":" + ((DirProxy) dp).port + " -> " + ((DirProxy) dp).requestPath
						+ ")\n");
			} else if (dp instanceof DirStatic) {
				sb.append(
						"Static: '" + ac + "' -> '" + ((DirStatic) dp).path + "' (" + ((DirStatic) dp).toPath + ")\n");
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

	public Element getPathConfigElement() {
		return pathConfigElement;
	}

	public DirPath lookupPath(String path) {
		return this.pathMap.get(path);
	}
}
