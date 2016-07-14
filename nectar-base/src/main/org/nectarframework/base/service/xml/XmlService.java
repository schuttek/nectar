package org.nectarframework.base.service.xml;

// enough imports for ya?
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.mysql.MysqlService;
import org.nectarframework.base.service.mysql.ResultRow;
import org.nectarframework.base.service.mysql.ResultTable;
import org.nectarframework.base.tools.ByteArray;
import org.nectarframework.base.tools.StringTools;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Structured data. docs/Data Transport.txt
 * 
 * @author skander
 *
 */
public class XmlService extends Service {

	private FileService fileService;
	private HashMap<String, String> languageMap = null;
	private MysqlService mysqlService;

	@Override
	public void checkParameters() throws ConfigurationException {
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		fileService = (FileService) dependancy(FileService.class);
		mysqlService = (MysqlService) dependancy(MysqlService.class);
		return true;
	}

	@Override
	protected boolean init() {

		return true;
	}

	@Override
	protected boolean run() {
		return true;
	}

	@Override
	protected boolean shutdown() {
		return true;
	}

	public byte[] translateXsl(String xsl, String language) throws SAXException, IOException {

		if (languageMap == null) {
			loadLanguageMap();
		}

		InputStream xslis = fileService.getFileAsInputStream(xsl);

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Log.fatal(e);
		}

		Document doc = documentBuilder.parse(xslis);

		org.w3c.dom.Element rootXElm = doc.getDocumentElement();

		NodeList langnl = rootXElm.getElementsByTagName("lang");

		for (int t = 0; t < langnl.getLength(); t++) {
			Node n = langnl.item(t);
			String langKey = language + "/" + ((org.w3c.dom.Element) n).getAttribute("key");
			String translatedText = languageMap.get(langKey);

			Node parent = n.getParentNode();
			if (translatedText == null) {
				parent.insertBefore(doc.createTextNode("!!" + langKey + "!!"), n);
				parent.removeChild(n);
				Log.warn("missing translation: " + langKey + " in file " + xsl);
			} else {
				String[] slicedTextsa = translatedText.split("\\?\\?");
				Vector<String> slicedText = new Vector<String>();
				for (String st : slicedTextsa) {
					slicedText.add(st);
				}
				int k = 0;
				NodeList childNodes = n.getChildNodes();
				while (slicedText.size() > 1) {
					parent.insertBefore(doc.createTextNode(slicedText.remove(0)), n);
					for (; k < childNodes.getLength(); k++) {
						Node cn = childNodes.item(k);
						if (cn.getNodeType() == Node.ELEMENT_NODE) {
							parent.insertBefore(cn.cloneNode(true), n);
						}
					}
				}
				parent.insertBefore(doc.createTextNode(slicedText.remove(0)), n);
				parent.removeChild(n);
			}
		}

		// FIXME: doc.toString() ???
		return null;

	}

	private void loadLanguageMap() {
		try {
			ResultTable rt = mysqlService.select("SELECT key,lang,translatedText from w_lang");
			languageMap = new HashMap<String, String>();
			for (ResultRow rr : rt) {
				String key = rr.getString("lang") + "/" + rr.getString("key");
				languageMap.put(key, rr.getString("translatedText"));
			}
		} catch (SQLException e) {
			Log.fatal(e);
			languageMap = null;
		}
	}

	public void transformToOS(Element elm, String xsl, OutputStream os) throws IOException {
		TransformerFactory tFactory = TransformerFactory.newInstance();

		ByteArrayOutputStream xmlbaos = new ByteArrayOutputStream();
		outputXml(elm, null, xmlbaos, true);
		ByteArrayInputStream xmlbais = new ByteArrayInputStream(xmlbaos.toByteArray());

		InputStream xslis = fileService.getFileAsInputStream(xsl);

		Transformer transformer;
		try {
			transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xslis));
		} catch (TransformerConfigurationException e) {
			Log.fatal("TransformerConfigurationException", e);
			throw new IOException(e);
		}
		try {
			transformer.transform(new javax.xml.transform.stream.StreamSource(xmlbais),
					new javax.xml.transform.stream.StreamResult(os));
		} catch (TransformerException e) {
			Log.fatal("TransformerException", e);
			throw new IOException(e);
		}

	}

	public static void toNDOJson(Element elm, OutputStream os) throws IOException {
		os.write("{".getBytes());
		recurseOutputElmJson(elm, os);
		os.write("}".getBytes());
	}

	private static void recurseOutputElmJson(Element elm, OutputStream os) throws IOException {
		Map<String, String> attribs = elm.getAttributes();
		LinkedList<Element> children = elm.getChildren();
		os.write(("\"n\":\"" + elm.getName() + "\"").getBytes());
		if (attribs.size() > 0) {
			os.write(",\"a\":{".getBytes());
			boolean first = true;
			for (String key : attribs.keySet()) {
				if (!first) {
					os.write(",".getBytes());
				}
				first = false;
				os.write(
						("\"" + StringTools.jsonEncode(key) + "\":\"" + StringTools.jsonEncode(attribs.get(key)) + "\"")
								.getBytes());
			}
			os.write("}".getBytes());
		}
		if (!children.isEmpty()) {
			os.write(",".getBytes());
			boolean first = true;
			os.write("\"c\":[".getBytes());
			for (Element child : children) {
				if (!first) {
					os.write(",".getBytes());
				}
				first = false;
				os.write(("{").getBytes());
				recurseOutputElmJson(child, os);
				os.write("}".getBytes());
			}
			os.write("]".getBytes());
		}
	}

	public static void outputXml(Element elm, String xsl, OutputStream os, boolean writeHeader) throws IOException {
		if (writeHeader) {
			os.write(("<?xml version=\"1.0\" encoding=\"" + Charset.defaultCharset().toString() + "\"?>\n").getBytes());
		}
		// TODO: manage encodings.

		if (xsl != null) {
			os.write(("<?xml-stylesheet type=\"text/xsl\" href=\"" + xsl + "\"?>\n").getBytes());
		}
		recurseOutputElmXml(elm, os);
	}

	private static void recurseOutputElmXml(Element elm, OutputStream os) throws IOException {
		Map<String, String> attribs = elm.getAttributes();
		LinkedList<Element> children = elm.getChildren();
		os.write(("<" + elm.getName()).getBytes());
		for (String key : attribs.keySet()) {
			os.write((" " + key + "=\"" + StringTools.xmlEncode(attribs.get(key)) + "\"").getBytes());
		}
		if (children.isEmpty()) {
			os.write("/>".getBytes());
		} else {
			os.write(">".getBytes());
			for (Element child : children) {
				recurseOutputElmXml(child, os);
			}
			os.write(("</" + elm.getName() + ">").getBytes());
		}
	}

	public static void toNDOJson(Element elm, StringBuffer sb) {
		sb.append("{");
		recurseToNDOJson(elm, sb);
		sb.append("}");
	}

	private static void recurseToNDOJson(Element elm, StringBuffer sb) {
		Map<String, String> attribs = elm.getAttributes();
		LinkedList<Element> children = elm.getChildren();
		sb.append("\"n\":\"" + elm.getName() + "\"");
		if (attribs.size() > 0) {
			sb.append(",\"a\":{");
			boolean first = true;
			for (String key : attribs.keySet()) {
				if (!first) {
					sb.append(",");
				}
				first = false;
				sb.append(("\"" + StringTools.jsonEncode(key) + "\":\"" + StringTools.jsonEncode(attribs.get(key))
						+ "\""));
			}
			sb.append("}");
		}
		if (!children.isEmpty()) {
			sb.append(",");
			boolean first = true;
			sb.append("\"c\":[");
			for (Element child : children) {
				if (!first) {
					sb.append(",");
				}
				first = false;
				sb.append(("{"));
				recurseToNDOJson(child, sb);
				sb.append("}");
			}
			sb.append("]");
		}
	}

	public static void toXml(Element elm, OutputStream os) throws IOException {

		os.write(("<" + elm.getName()).getBytes());
		for (String key : elm.getAttributes().keySet()) {
			os.write((" " + key + "=\"" + StringTools.xmlEncode(elm.getAttributes().get(key)) + "\"").getBytes());
		}
		if (elm.getChildren().isEmpty()) {
			os.write("/>".getBytes());
		} else {
			os.write(">".getBytes());
			for (Element child : elm.getChildren()) {
				toXml(child, os);
			}
			os.write(("</" + elm.getName() + ">").getBytes());
		}
	}
	
	public static void toXml(Element elm, StringBuffer sb) {

		sb.append("<" + elm.getName());
		for (String key : elm.getAttributes().keySet()) {
			sb.append(" " + key + "=\"" + StringTools.xmlEncode(elm.getAttributes().get(key)) + "\"");
		}
		if (elm.getChildren().isEmpty()) {
			sb.append("/>");
		} else {
			sb.append(">");
			for (Element child : elm.getChildren()) {
				toXml(child, sb);
			}
			sb.append("</" + elm.getName() + ">");
		}
	}

	public static void toXml(Element elm, ByteArray ba) {

		ba.add("<" + elm.getName());
		for (String key : elm.getAttributes().keySet()) {
			ba.add(" " + key + "=\"" + StringTools.xmlEncode(elm.getAttributes().get(key)) + "\"");
		}
		if (elm.getChildren().isEmpty()) {
			ba.add("/>");
		} else {
			ba.add(">");
			for (Element child : elm.getChildren()) {
				toXml(child, ba);
			}
			ba.add("</" + elm.getName() + ">");
		}
	}

	public static byte[] toXmlBytes(Element elm) {
		// TODO: throughly test this with weird UTF-8 characters
		ByteArray ba = new ByteArray();
		toXml(elm, ba);
		return ba.getBytes();
	}

	public static String toXmlString(Element elm) {
		StringBuffer sb = new StringBuffer();
		toXml(elm, sb);
		return sb.toString();
	}

	public static Element fromXml(File file) throws SAXException, IOException {
		return fromXml(new FileInputStream(file));
	}

	public static Element fromXml(StringBuffer elementXml) throws SAXException {
		return fromXml(elementXml.toString());
	}

	public static Element fromXml(String elementXml) throws SAXException {
		String s = xmlHeader() + elementXml;
		ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());

		try {
			return fromXml(bais);
		} catch (IOException e) {
			// really can't happen with a ByteArrayInputStream,
			Log.fatal(e);
			throw new SAXException(e);
		}
	}

	public static Element fromXml(byte[] elementXml) throws SAXException {
		try {
			return fromXml(new ByteArrayInputStream(elementXml));
		} catch (IOException e) {
			// really can't happen with a ByteArrayInputStream,
			Log.fatal(e);
			throw new SAXException(e);
		}
	}

	public static String xmlHeader() {
		return "<?xml version=\"1.0\" encoding=\"" + Charset.defaultCharset().toString() + "\"?>\n";
	}

	private static Element fromXmlRecurse(org.w3c.dom.Element xe) {
		Element e = new Element(xe.getTagName());

		NamedNodeMap nnm = xe.getAttributes();
		int nnmLen = nnm.getLength();
		for (int t = 0; t < nnmLen; t++) {
			Node n = nnm.item(t);
			if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
				e.add(n.getNodeName(), n.getNodeValue());
			}
		}

		NodeList nl = xe.getChildNodes();
		int nlLen = nl.getLength();
		for (int t = 0; t < nlLen; t++) {
			Node n = nl.item(t);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				e.add(fromXmlRecurse((org.w3c.dom.Element) n));
			}
		}

		return e;
	}

	public static Element generateTestElement(int childCount, int attrCount, int depth) {
		Element e = new Element(StringTools.randomStringLowerCase(4, 8));
		Random rand = new Random();
		int n = rand.nextInt(attrCount - 1) + 1;
		for (int t = 0; t < n; t++) {
			e.add(StringTools.randomStringLowerCase(4, 8), StringTools.randomStringLowerCase(4, 8));
		}
		if (depth > 0) {
			n = rand.nextInt(childCount - 1) + 1;
			for (int t = 0; t < n; t++) {
				e.add(generateTestElement(childCount, attrCount, depth - 1));
			}
		}

		return e;
	}

	public static Element fromXml(InputStream is) throws SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			Log.fatal(e);
		}

		Document doc = documentBuilder.parse(is);
		org.w3c.dom.Element rootXElm = doc.getDocumentElement();

		Element root = fromXmlRecurse(rootXElm);

		return root;
	}

}
