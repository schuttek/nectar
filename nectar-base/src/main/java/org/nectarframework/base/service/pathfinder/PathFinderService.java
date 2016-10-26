package org.nectarframework.base.service.pathfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.nectarframework.base.action.Action;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.form.Form;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.StringTools;
import org.xml.sax.SAXException;

public class PathFinderService extends IPathFinder {

	private Element pathConfigElm;
	private boolean loadWarningsExist = false;

	private HashMap<String, VirtualHostResolution> virtualHostMap = new HashMap<>();

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		String pathConfigFile = sp.getString("pathConfigFile", "config/pathConfig.xml");
		try {
			pathConfigElm = XmlService.fromXml(new FileInputStream(new File(pathConfigFile)));
		} catch (SAXException | IOException e) {
			throw new ConfigurationException(e);
		}
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		return true;
	}

	@Override
	protected boolean init() {
		loadWarningsExist = false;
		return loadConfig();
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
	public Element getPathConfigElement() {
		return pathConfigElm;
	}

	public UriResolution resolveUri(String hostnamePort, String uri) {

		VirtualHostResolution vhr = virtualHostMap.get(hostnamePort);

		if (vhr == null) {
			return null;
		}

		// remove the / at the start if it exists.
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}

		ProjectResolution rootProject = vhr.resolveNamespace("");
		
		UriResolution ur = null;
		// is there a wild card ?
		if (rootProject != null) {
			ur = rootProject.getUriResolution("*");
			if (ur != null) {
				return ur;
			}
			// root document
			if (uri.length() == 0) {
				return rootProject.getUriResolution("");
			}
		}

		Vector<String> pathSlices = StringTools.slice(uri, "/");
		
		// find subproject...
		String path = "";
		ProjectResolution pr = null;
		int i=0;
		for (i=0; i < pathSlices.size(); i++) {
			pr = vhr.resolveNamespace(path);
			if (pr != null) {
				break;
			}
			if (i>0) 
				path += "/";
			path += pathSlices.get(i);
			Log.trace("path: " + path);
		}
		if (pr == null) {
			return null;
		}

		Log.trace("local path: " + pathSlices.get(i));

		ur = pr.getUriResolution(pathSlices.get(i));

		return ur;
	}

	private void loadWarn(String s) {
		loadWarningsExist = true;
		Log.warn("[PathFinderService]loadConfig: " + s);
	}

	private boolean loadConfig() {
		// TODO: line by line validation.
		if (!pathConfigElm.getName().equals("pathConfig")) {
			loadWarn(" root element should be called 'pathConfig'");
		}
		if (!pathConfigElm.isAttribute("version", "2.0")) {
			loadWarn(" PathFinderService requires version 2.0 of the pathConfig.xml");
		}

		HashMap<String, ProjectResolution> tempProjectMap = new HashMap<>();
		// projects
		for (Element projectE : pathConfigElm.getChildren("project")) {
			if (!projectE.hasAttribute("name")) {
				loadWarn(" pathConfig>project element requires name attribute.");
			}
			ProjectResolution pr = new ProjectResolution();
			pr.name = projectE.get("name");

			tempProjectMap.put(pr.name, pr);

			// forms
			HashMap<String, FormResolution> formMap = new HashMap<>();
			for (Element form : projectE.getChildren("form")) {
				FormvarResolution[] fva = new FormvarResolution[form.getChildren().size()];
				int t = 0;
				for (Element formvar : form.getChildren("var")) {
					FormvarResolution fv = new FormvarResolution();
					fv.name = formvar.get("name");
					fv.type = Form.VarType.getByString(formvar.get("type"));
					fv.nullAllowed = formvar.isAttribute("null", "true");
					fva[t] = fv;
				}

				FormResolution f = new FormResolution();
				f.formvars = fva;
				f.name = form.get("name");
				formMap.put(f.name, f);
			}

			// actions
			HashMap<String, ActionResolution> actionMap = new HashMap<>();
			for (Element action : projectE.getChildren("action")) {
				ActionResolution da = new ActionResolution();
				da.name = action.get("name");
				da.packageName = action.get("package");
				da.className = action.get("class");
				da.defaultOutput = ActionResolution.OutputType.lookup(action.get("defaultOutput"));
				da.templateName = action.get("templateName");
				da.formName = action.get("form");

				da.form = formMap.get(da.formName);
				if (da.form == null) {
					loadWarn(" Form " + da.formName + " for action " + da.name + " not found!");
				}
				Class<?> acClass = null;
				try {
					acClass = ClassLoader.getSystemClassLoader().loadClass(da.packageName + "." + da.className);
				} catch (ClassNotFoundException e) {
					loadWarn(" action Class Definition " + da.packageName + "." + da.className + " not found. "
							+ e.getMessage());
				}
				if (acClass != null) {
					try {
						da.actionClass = acClass.asSubclass(Action.class);
					} catch (ClassCastException e) {
						loadWarn(" action Class Definition " + da.packageName + "." + da.className
								+ " is not of Action type! " + e.getMessage());
					}
				}

				actionMap.put(da.name, da);
			}

			// paths
			for (Element path : projectE.getChildren("path")) {
				if (!path.hasAttribute("path")) {
					loadWarn(" pathConfig>project>path element requires name attribute.");
				}
				if (!path.hasAttribute("action")) {
					loadWarn(" pathConfig>project>path element requires action attribute.");
				}
				ActionResolution ar = actionMap.get(path.get("action"));

				pr.addPath(path.get("path"), ar);
			}

			// redirects
			for (Element redirect : projectE.getChildren("redirect")) {
				RedirectResolution red = new RedirectResolution();
				red.path = redirect.get("path");
				red.toUrl = redirect.get("toUrl");
				red.code = redirect.getAsInt("code");
				pr.addPath(red.path, red);
			}

			// alias
			for (Element alias : projectE.getChildren("alias")) {
				AliasResolution ali = new AliasResolution();
				ali.path = alias.get("path");
				ali.toPath = alias.get("toPath");
				ali.relative = (alias.isAttribute("relative", "true") ? true : false);
				ali.variables = new HashMap<String, List<String>>();
				for (Element rv : alias.getChildren("var")) {
					if (ali.variables.containsKey(rv.get("name"))) {
						ali.variables.get(rv.get("name")).add(rv.get("value"));
					} else {
						LinkedList<String> ls = new LinkedList<String>();
						ls.add(rv.get("value"));
						ali.variables.put(rv.get("name"), ls);
					}
				}
				pr.addPath(ali.path, ali);
			}

			// proxy
			for (Element proxy : projectE.getChildren("proxy")) {
				ProxyResolution pro = new ProxyResolution();
				pro.path = proxy.get("path");

				pro.protocol = proxy.get("protocol");
				pro.port = proxy.getAsInt("port");
				pro.host = proxy.get("host");
				pro.requestPath = proxy.get("requestPath");

				pr.addPath(pro.path, pro);
			}

			// static
			for (Element staticElm : projectE.getChildren("static")) {
				StaticResolution sta = new StaticResolution();
				sta.path = staticElm.get("path");
				sta.toPath = staticElm.get("toPath");
				pr.addPath(sta.path, sta);
			}
		}

		// virtualServer
		for (Element vhe : pathConfigElm.getChildren("virtualServer")) {
			if (!vhe.hasAttribute("hostname") || !vhe.hasAttribute("port")) {
				loadWarn(" pathConfig>virtualServer element requires hostname and port attributes.");
			}
			VirtualHostResolution vhr = new VirtualHostResolution();

			vhr.hostname = vhe.get("hostname");
			vhr.port = vhe.getAsInt("port");

			this.virtualHostMap.put(vhr.hostname + ":" + vhr.port, vhr);

			// projects
			for (Element projectElm : vhe.getChildren("project")) {
				if (!projectElm.hasAttribute("namespace") || !projectElm.hasAttribute("name")) {
					loadWarn(" pathConfig>virtualServer>project element requires namespace and name attributes.");
				}

				String namespace = projectElm.get("namespace");
				String name = projectElm.get("name");

				ProjectResolution pr = tempProjectMap.get(name);
				if (pr == null) {
					loadWarn(" pathConfig>virtualServer>project element " + name + " not found.");
				}
				vhr.put(namespace, pr);
			}
		}

		return !loadWarningsExist;
	}

	public String dumpConfig() {
		String s = "";
		for (String k : this.virtualHostMap.keySet()) {
			s += k+ " -> "+this.virtualHostMap.get(k).dumpConfig()+"\n\n";
		}
		return s;
	}
	
}
