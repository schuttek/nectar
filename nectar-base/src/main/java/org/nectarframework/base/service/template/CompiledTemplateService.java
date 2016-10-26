package org.nectarframework.base.service.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.EntityRef;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedElement;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.output.XMLOutputter;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.exception.ServiceUnavailableException;
import org.nectarframework.base.service.Log;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.pathfinder.IPathFinder;
import org.nectarframework.base.service.translation.TranslationService;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.nectarframework.base.tools.Stopwatch;
import org.nectarframework.base.tools.StringTools;
import org.xml.sax.SAXException;

public class CompiledTemplateService extends TemplateService {

	private HashMap<String, ITemplate> loadedTemplates = new HashMap<String, ITemplate>();

	private LinkedList<Locale> localeList = new LinkedList<Locale>();
	private String templatePackageName;
	private String rawTemplatesRootDir;

	private TranslationService translationService;
	private IPathFinder directoryService;

	private static Namespace NS = Namespace.getNamespace("th", "http://www.nectarframework.org");

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		LinkedList<String> localeStrList = sp.getSet("locale");
		if (localeStrList == null) {
			throw new ConfigurationException("TempalteService needs at least one Locale.");
		}
		for (String localeStr : localeStrList) {
			Locale l = null;
			String[] vs = localeStr.split("_");
			if (vs.length == 1) {
				l = new Locale(vs[0]);
			} else if (vs.length == 2) {
				l = new Locale(vs[0], vs[1]);
			} else if (vs.length == 3) {
				l = new Locale(vs[0], vs[1], vs[2]);
			}
			localeList.add(l);
		}

		templatePackageName = sp.getString("templatePackageName", null);
		if (templatePackageName == null) {
			throw new ConfigurationException("TemplateService needs templatePackageName config.");
		}

		rawTemplatesRootDir = sp.getString("rawTemplatesRootDir", null);
		if (rawTemplatesRootDir == null) {
			throw new ConfigurationException("TemplateService needs rawTemplatesRootDir config.");
		}
	}

	@Override
	public boolean establishDependencies() throws ServiceUnavailableException {
		dependency(XmlService.class);
		translationService = (TranslationService) dependency(TranslationService.class);
		directoryService = (IPathFinder) dependency(IPathFinder.class);
		return true;
	}

	@Override
	protected boolean init() {
		return loadTemplates();
	}

	private boolean loadTemplates() {
		Element directoryServiceConfigElement = directoryService.getPathConfigElement();

		for (Locale l : this.localeList) {
			for (Element project : directoryServiceConfigElement.getChildren("project")) {
				for (Element action : project.getChildren("action")) {
					if (action.get("defaultOutput") != null && action.get("defaultOutput").equals("template")) {
						String templateName = action.get("templateName");
						String className = templatePackageName + "." + l.getLanguage() + "_" + templateName;
						ITemplate templateInstance = loadTemplate(className);
						loadedTemplates.put(className, templateInstance);
						className = templatePackageName + "." + l.getLanguage() + "_" + l.getCountry() + "_"
								+ templateName;
						templateInstance = loadTemplate(className);
						loadedTemplates.put(className, templateInstance);
						className = templatePackageName + "." + l.getLanguage() + "_" + l.getCountry() + "_"
								+ l.getVariant() + "_" + templateName;
						templateInstance = loadTemplate(className);
						loadedTemplates.put(className, templateInstance);
					}
				}
			}
		}
		return true;
	}

	private ITemplate loadTemplate(String className) {
		Class<?> clanon = null;
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		try {
			clanon = cl.loadClass(className);
		} catch (ClassNotFoundException e) {
		}

		if (clanon != null) {
			ITemplate templateInstance;
			try {
				templateInstance = (ITemplate) clanon.newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassCastException e) {
				Log.warn(e);
				return null;
			}
			return templateInstance;
		}
		return null;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	public void outputTemplate(OutputStream os, String templateName, Locale locale, Element actionElement,
			Element sessionElement) throws IOException {
		String locLang = locale.getLanguage();
		String locCountry = locale.getCountry();
		String locVariant = locale.getVariant();

		ITemplate template = null;

		template = loadedTemplates
				.get(templatePackageName + "." + locLang + "_" + locCountry + "_" + locVariant + "_" + templateName);
		if (template == null) {
			template = loadedTemplates.get(templatePackageName + "." + locLang + "_" + locCountry + "_" + templateName);
		}
		if (template == null) {
			template = loadedTemplates.get(templatePackageName + "." + locLang + "_" + templateName);
		}

		if (template == null) {
			Log.warn("[TemplateService]: No template could be found for " + templateName + " locale:"
					+ locale.toString() + " searched... " + templatePackageName + "." + locLang + "_" + locCountry + "_"
					+ locVariant + "_" + templateName);
			return;
		}

		template.run(this, os, actionElement, sessionElement);
	}

	protected void buildTemplates(String pathConfig, String outputDir)
			throws IOException, TemplateParseException, SAXException, ParserConfigurationException, JDOMException {
		Element pathConfigElm = XmlService.fromXml(new FileInputStream(new File(pathConfig)));

		Log.trace("pathConfig: " + pathConfigElm.toString());

		for (Element projectElm : pathConfigElm.getChildren("project")) {
			for (Element actionElm : projectElm.getChildren("action")) {
				if (actionElm.get("defaultOutput").equalsIgnoreCase("template")) {
					String templateName = actionElm.get("templateName");
					Stopwatch sw = new Stopwatch();
					sw.start();
					buildTemplate(templateName, outputDir);
					sw.start();
					Log.trace("TemplateService: buildTemplate '"+templateName+"' in "+sw.toString());
				}
			}
		}

	}

	private void buildTemplate(String templateName, String outputDir)
			throws IOException, TemplateParseException, ParserConfigurationException, SAXException, JDOMException {

		Log.info("TemplateBuilder: building Template " + templateName + " to " + outputDir);

		for (Locale loc : localeList) {
			String className = loc.toString() + "_" + templateName;

			CompiledTemplate compT = compileTemplate(templateName, loc);

			File f = new File(outputDir + "/" + className + ".java");
			FileOutputStream fos = new FileOutputStream(f, false);
			PrintWriter pw = new PrintWriter(fos);

			// package
			pw.println("package " + this.templatePackageName + ";");
			pw.println("");
			// imports
			pw.println("import java.io.IOException;");
			pw.println("import java.io.OutputStream;");
			pw.println("import org.apache.commons.lang3.StringEscapeUtils;");
			pw.println("import org.nectarframework.base.service.template.ITemplate;");
			pw.println("import org.nectarframework.base.service.template.TemplateService;");
			pw.println("import org.nectarframework.base.service.xml.Element;");
			pw.println("");
			// class start
			pw.println("@SuppressWarnings(\"all\")");
			pw.println("public class " + className + " implements ITemplate {");
			pw.println("	@Override");
			pw.println(
					"	public void run(TemplateService templateService, OutputStream os, Element actionElement, Element sessionElement) throws IOException {");

			pw.println(compT.getJavaCode());

			// class end
			pw.println("	}");
			pw.println("}");

			pw.close();
		}

	}

	private Document readDocument(String filePath) throws JDOMException, IOException {
		SAXBuilder jdomBuilder = new SAXBuilder();
		jdomBuilder.setJDOMFactory(new LocatedJDOMFactory());
		return jdomBuilder.build(new File(filePath));
	}

	private CompiledTemplate compileTemplate(String templateName, Locale loc) throws FileNotFoundException, IOException,
			ParserConfigurationException, SAXException, JDOMException, TemplateParseException {

		
		
		CompiledTemplate compiledTemplate = new CompiledTemplate();
		
		// TODO: make doctype configurable
		
		compiledTemplate.addText("<!DOCTYPE html>");

		String filePath = rawTemplatesRootDir + "/" + templateName + ".html";
		Document jdomDoc = readDocument(filePath);

		// 1. Recombine fragments
		processFragments(jdomDoc.getRootElement(), filePath);

		// 2. process the tags
		processTags(jdomDoc.getRootElement(), loc, filePath);
		

		// 3. push to compiledTemplate
		// TODO: make configurable and locale sensitive
		compiledTemplate.addText("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">");
		jdomDoc.getRootElement().getChildren().forEach(e -> processCompiledTemplate(e, compiledTemplate));
		compiledTemplate.addText("</html>");

		return compiledTemplate;
	}

	private void processCompiledTemplate(org.jdom2.Element elm, CompiledTemplate ct) {

		// before
		for (Attribute attr : elm.getAttributes()) {
			if (attr.getNamespacePrefix().equals(NS.getPrefix())) {
				if (attr.getName().equals("javaBeforeTag")) {
					ct.addJava(attr.getValue());
				}
			}
		}
		
		// tag start
		ct.addText("<" + elm.getName());
		for (Attribute attr : elm.getAttributes()) {
			// ignore other th attributes
			if (!attr.getNamespacePrefix().equals(NS.getPrefix())) {
				ct.addText(" " + attr.getName() + "=\"" + attr.getValue() + "\"");
			}
		}
		
		ct.addText(">");

		// inside
		for (Attribute attr : elm.getAttributes()) {
			if (attr.getNamespacePrefix().equals(NS.getPrefix())) {
				if (attr.getName().equals("javaInsideTag")) {
					ct.addJava(attr.getValue());
				}
			}
		}

		XMLOutputter xo = new XMLOutputter();

		// recurse down
		for (Content c : elm.getContent()) {
			switch (c.getCType()) {
			case Element:
				processCompiledTemplate((org.jdom2.Element) c, ct);
				break;
			case CDATA:
				ct.addText(xo.outputString((CDATA) c));
				break;
			case Comment:
				ct.addText(xo.outputString((Comment) c));
				break;
			case DocType:
				ct.addText(xo.outputString((DocType) c));
				break;
			case EntityRef:
				ct.addText(xo.outputString((EntityRef) c));
				break;
			case ProcessingInstruction:
				ct.addText(xo.outputString((ProcessingInstruction) c));
				break;
			case Text:
				ct.addText(xo.outputString((Text) c));
				break;
			default:
				break;
			}
		}

		// end tag
		ct.addText("</" + elm.getName() + ">");
		
		// after
		for (Attribute attr : elm.getAttributes()) {
			if (attr.getNamespacePrefix().equals(NS.getPrefix())) {
				if (attr.getName().equals("javaAfterTag")) {
					ct.addJava(attr.getValue());
				}
			}
		}
	}

	private void processTags(org.jdom2.Element elm, Locale loc, String filePath) {

		// search for attributes to process in current element
		List<Attribute> attrList = new LinkedList<Attribute>();
		attrList.addAll(elm.getAttributes());
		for (Attribute attr : attrList) {
			if (attr.getNamespacePrefix().equals(NS.getPrefix())) {
				processAttribute(elm, attr, loc, filePath);
			}
		}
		for (Content c : elm.getContent()) {
			if (c instanceof org.jdom2.Element) {
				processTags((org.jdom2.Element) c, loc, filePath);
			}
		}
	}

	public static final String variablePatternStr = "\\s*\\$\\{\\s*([\\w_\\.]+)\\s*}\\s*";
	public static final String messagePatternStr = "\\s*\\#\\{\\s*([\\w_\\.]+)\\s*}\\s*";
	public static final String urlPatternStr = "\\s*\\@\\{\\s*([\\w_\\/\\?\\=\\&\\.]+)\\s*}\\s*";
	public static final String localVarPatternStr = "\\s*([\\w_\\.]+)\\s*";
	public static final Pattern variablePattern = Pattern.compile(variablePatternStr);
	public static final Pattern messagePattern = Pattern.compile(messagePatternStr);
	public static final Pattern urlPattern = Pattern.compile(urlPatternStr);
	public static final Pattern localVarPattern = Pattern.compile(localVarPatternStr);
	public static final Pattern eachTagPattern = Pattern.compile("\\s*([\\w_]+)\\s*:" + variablePattern);

	public static final String[] fillInAttr = new String[] { "abbr", "accept", "accept-charset", "accesskey", "action",
			"align", "alt", "archive", "audio", "autocomplete", "axis", "background", "bgcolor", "border",
			"cellpadding", "cellspacing", "challenge", "charset", "cite", "class", "classid", "codebase", "codetype",
			"cols", "colspan", "compact", "content", "contenteditable", "contextmenu", "data", "datetime", "dir",
			"draggable", "dropzone", "enctype", "for", "form", "formaction", "formenctype", "formmethod", "formtarget",
			"frame", "frameborder", "headers", "height", "high", "href", "hreflang", "hspace", "http-equiv", "icon",
			"id", "keytype", "kind", "label", "lang", "list", "longdesc", "low", "manifest", "marginheight",
			"marginwidth", "max", "maxlength", "media", "method", "min", "name", "optimum", "pattern", "placeholder",
			"poster", "preload", "radiogroup", "rel", "rev", "rows", "rowspan", "rules", "sandbox", "scheme", "scope",
			"scrolling", "size", "sizes", "span", "spellcheck", "src", "srclang", "standby", "start", "step", "style",
			"summary", "tabindex", "target", "title", "type", "usemap", "value", "valuetype", "vspace", "width", "wrap",
			"xmlbase", "xmllang", "xmlspace" };

	private void processAttribute(org.jdom2.Element elm, Attribute attr, Locale loc, String filePath) {

		String aName = attr.getName();
		String aValue = attr.getValue();
		String attrLog = NS.getPrefix() + ":" + aName + "=\"" + aValue + "\"";
		String lineLog = " near " + filePath + ":" + ((LocatedElement) elm).getLine();

		// logic tags
		if (aName.equals("each")) {
			Matcher m = eachTagPattern.matcher(aValue);
			if (m.matches()) {
				String iter = m.group(1);
				String var = m.group(2);
				Log.trace("each: " + iter + " " + var);

				String evalVar = evaluateVarGetList(var);

				// for (Element $var : $evalVar) {
				elm.setAttribute("javaBeforeTag", "for (Element " + iter + " : " + evalVar + ") {", NS);
				elm.setAttribute("javaAfterTag", "}", NS);
				elm.removeAttribute(attr);
			} else {
				Log.warn(
						"[TemplateService] processAttribute: " + attrLog + " didn't match expected pattern " + lineLog);
			}
			return;
		}
		if (aName.equals("text")) {
			Matcher messageMatcher = messagePattern.matcher(aValue);
			Matcher variableMatcher = variablePattern.matcher(aValue);
			Matcher localVarMatcher = localVarPattern.matcher(aValue);
			if (messageMatcher.matches()) {
				String message = messageMatcher.group(1);
				Log.trace("text: " + message);
				elm.removeContent();
				elm.setContent(new Text(StringEscapeUtils.escapeXml10(translationService.get(loc, "", message, null))));
				elm.removeAttribute(attr);
			} else if (variableMatcher.matches()) {
				String variable = variableMatcher.group(1);
				Log.trace("var: " + variable);
				elm.removeContent();
				elm.setAttribute("javaInsideTag",
						"os.write(StringEscapeUtils.escapeXml10(" + evaluateVar(variable) + ").getBytes());", NS);
				elm.removeAttribute(attr);
			} else if (localVarMatcher.matches()) {
				String variable = localVarMatcher.group(1);
				Log.trace("localVar: " + variable);
				elm.removeContent();
				elm.setAttribute("javaInsideTag",
						"os.write(StringEscapeUtils.escapeXml10(" + evaluateLocalVar(variable) + ").getBytes());", NS);
				elm.removeAttribute(attr);
			} else {
				Log.warn(
						"[TemplateService] processAttribute: " + attrLog + " didn't match expected pattern " + lineLog);
			}
			return;

		}

		if (aName.equals("utext")) {
			Matcher messageMatcher = messagePattern.matcher(aValue);
			Matcher variableMatcher = variablePattern.matcher(aValue);
			Matcher localVarMatcher = localVarPattern.matcher(aValue);
			if (messageMatcher.matches()) {
				String message = messageMatcher.group(1);
				Log.trace("text: " + message);
				elm.removeContent();
				elm.setContent(new Text(translationService.get(loc, "", message, null)));
				elm.removeAttribute(attr);
			} else if (variableMatcher.matches()) {
				String variable = variableMatcher.group(1);
				Log.trace("var: " + variable);
				elm.removeContent();
				elm.setAttribute("javaInsideTag", "os.write(" + evaluateVar(variable) + ".getBytes());", NS);
				elm.removeAttribute(attr);
			} else if (localVarMatcher.matches()) {
				String variable = localVarMatcher.group(1);
				Log.trace("localVar: " + variable);
				elm.removeContent();
				elm.setAttribute("javaInsideTag", "os.write(" + evaluateLocalVar(variable) + ".getBytes());", NS);
				elm.removeAttribute(attr);
			} else {
				Log.warn(
						"[TemplateService] processAttribute: " + attrLog + " didn't match expected pattern " + lineLog);
			}
			return;
		}

		for (String fia : fillInAttr) {
			if (aName.equals(fia)) {
				Matcher messageMatcher = messagePattern.matcher(aValue);
				Matcher urlMatcher = urlPattern.matcher(aValue);
				Matcher variableMatcher = variablePattern.matcher(aValue);
				Matcher localVarMatcher = localVarPattern.matcher(aValue);
				if (messageMatcher.matches()) {
					String message = messageMatcher.group(1);
					elm.setAttribute(aName,
							StringEscapeUtils.escapeXml10(translationService.get(loc, "", message, null)));
					elm.removeAttribute(attr);
				} else if (urlMatcher.matches()) {
					String url = urlMatcher.group(1);
					elm.setAttribute(aName, url);
					elm.removeAttribute(attr);
				} else if (variableMatcher.matches()) {
					String variable = variableMatcher.group(1);
					elm.setAttribute("javaAttrTag", aName+":os.write(" + evaluateVar(variable) + ".getBytes());", NS);
					elm.removeAttribute(attr);
				} else if (localVarMatcher.matches()) {
					String variable = localVarMatcher.group(1);
					elm.setAttribute("javaAttrTag", aName+":os.write(" + evaluateVar(variable) + ".getBytes());", NS);
					elm.removeAttribute(attr);
				} else {
					Log.warn("[TemplateService] processAttribute: " + attrLog + " didn't match expected pattern "
							+ lineLog);
				}

			}
		}

	}

	public static final Pattern variableArrayIndexPattern = Pattern.compile("(\\w+)\\[(\\d+)\\]");

	private String evaluateVarGetList(String var) {
		Vector<String> vs = StringTools.slice(var, ".");
		String builtStr = "";

		for (int i = 1; i < vs.size(); i++) {
			if (i == vs.size() - 1) {
				// last element
				builtStr += ".getChildren(\"" + vs.get(i) + "\")";
			} else {
				builtStr += ".getChildren(\"" + vs.get(i) + "\").getFirst()";
			}
		}

		return "actionElement" + builtStr;
	}

	private String evaluateVar(String var) {
		Vector<String> vs = StringTools.slice(var, ".");
		String builtStr = "";

		for (int i = 1; i < vs.size(); i++) {
			if (i == vs.size() - 1) {
				// last element
				builtStr += ".get(\"" + vs.get(i) + "\")";
			} else {
				builtStr += ".getChildren(\"" + vs.get(i) + "\").getFirst()";
			}
		}

		return "actionElement" + builtStr;
	}

	private String evaluateLocalVar(String var) {
		Vector<String> vs = StringTools.slice(var, ".");
		String builtStr = vs.get(0);

		for (int i = 1; i < vs.size(); i++) {
			if (i == vs.size() - 1) {
				// last element
				builtStr += ".get(\"" + vs.get(i) + "\")";
			} else {
				builtStr += ".getChildren(\"" + vs.get(i) + "\").getFirst()";
			}
		}

		return builtStr;
	}

	private void processFragments(org.jdom2.Element elm, String filePath)
			throws TemplateParseException, FileNotFoundException, IOException, JDOMException {
		String replaceAttr = elm.getAttributeValue("replace", NS);
		if (replaceAttr != null && !replaceAttr.equals("")) {

			Pattern pattern = Pattern.compile("\\s*(\\S+)\\s*::\\s*(\\S+)\\s*");
			Matcher m = pattern.matcher(replaceAttr);
			String includeTemplate;
			String includeTag;
			if (m.matches()) {
				includeTemplate = m.group(1);
				includeTag = m.group(2);
			} else {
				throw new TemplateParseException("[TemplateService]: th:replace attribute " + replaceAttr
						+ " does not match expected pattern, check your syntax *near* line "
						+ ((LocatedElement) elm).getLine() + " in file " + filePath);
			}

			org.jdom2.Element targetNode = findFragment(includeTemplate, includeTag);

			if (targetNode == null) {
				throw new TemplateParseException(
						"[TemplateService]: Template fragment for " + replaceAttr + " could not be found *near* line "
								+ ((LocatedElement) elm).getLine() + " in file " + filePath);
			}

			elm.removeContent();
			elm.removeAttribute("replace", NS);
			for (Content cont : targetNode.getContent()) {
				elm.addContent(cont.clone());
			}
		} else { // recurse
			for (org.jdom2.Element c : elm.getChildren()) {
				processFragments(c, filePath);
			}
		}
	}

	private org.jdom2.Element findFragment(String includeTemplate, String includeTag)
			throws FileNotFoundException, IOException, JDOMException {
		String templateFile = rawTemplatesRootDir + "/" + includeTemplate + ".html";
		Document doc = readDocument(templateFile);
		return findFragmentRec(doc.getRootElement(), includeTag);
	}

	private org.jdom2.Element findFragmentRec(org.jdom2.Element elm, String includeTag) {
		String fragmentName = elm.getAttributeValue("fragment", NS);
		if (fragmentName != null && fragmentName.equals(includeTag)) {
			return elm;
		} else {
			for (org.jdom2.Element c : elm.getChildren()) {
				org.jdom2.Element ret = findFragmentRec(c, includeTag);
				if (ret != null) {
					return ret;
				}
			}
		}
		return null;
	}

}
