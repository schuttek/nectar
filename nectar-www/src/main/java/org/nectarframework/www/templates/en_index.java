package org.nectarframework.www.templates;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.nectarframework.base.service.template.ITemplate;
import org.nectarframework.base.service.template.TemplateService;
import org.nectarframework.base.service.xml.Element;

@SuppressWarnings("all")
public class en_index implements ITemplate {
	@Override
	public void run(TemplateService templateService, OutputStream os, Element actionElement, Element sessionElement) throws IOException {
		os.write("<!DOCTYPE html><html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\"><head>\r\n<title>Nectar Framework</title>\r\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></meta>\r\n<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"/s/style.css\"></link>\r\n<script type=\"text/javascript\" src=\"/s/script.js\"></script>\r\n<script src=\"https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js?autoload=true&lang=css\" defer=\"defer\"></script>\r\n</head><body>\r\n\t<div class=\"wrap\">\r\n\t\t<div class=\"header\">\r\n\t\t<div class=\"title\">\r\n\t\t\t<img class=\"title\" src=\"/s/icon_bird_black.png\" height=\"24\" width=\"24\"></img><span>Nectar Framework</span>\r\n\t\t</div>\r\n\t\t<div class=\"slogan\">a simple and stupidly fast web application framework for massively scalable applications</div>\r\n\t</div>\r\n\t\t<div class=\"navbar\">\r\n\t\t<img class=\"navbar\" src=\"/s/icon_home.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/\">Home</a> <img class=\"navbar\" src=\"/s/icon_overview.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/overview\">Overview</a> <img class=\"navbar\" src=\"/s/icon_documentation.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/documentation\">Documentation</a>\r\n\r\n\t\t<img class=\"navbar\" src=\"/s/icon_download.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/download\">Download</a> <img class=\"navbar\" src=\"/s/icon_about.png\" height=\"24\" width=\"24\"></img> <a class=\"navbar\" href=\"/about\">About</a>\r\n\t</div>\r\n\r\n\t\t<div class=\"content\">\r\n\t\t\t<h2>What is Nectar Framework?</h2>\r\n\r\n\t\t\t<p>\r\n\t\t\t\t<b>Nectar Framework is a Web Applications Framework written in\r\n\t\t\t\t\tJava.</b> It is based on Model-View-Controller push-based architecture,\r\n\t\t\t\tand it's core services include an HTTP Server, a MySQL database\r\n\t\t\t\tconnector, a data scaffolding / abstraction layer, a caching\r\n\t\t\t\tservice, a thread management service. It is designed to be both\r\n\t\t\t\tstrict and light-weight, which allows for great performance and\r\n\t\t\t\tscalability.\r\n\t\t\t</p>\r\n\t\t\t\r\n\t\t\t\r\n\t\t\t".getBytes());
		for (Element bp : actionElement.getChildren("blogPost")) {
		os.write("<div class=\"blog\">\r\n\t\t\t\t<div class=\"blogHeader\">".getBytes());
		os.write(StringEscapeUtils.escapeXml10(bp.get("title")).getBytes());
		os.write("</div>\r\n\t\t\t\t<div class=\"blogByline\"><span>".getBytes());
		os.write(StringEscapeUtils.escapeXml10(bp.get("createdTimestamp")).getBytes());
		os.write("</span> by\r\n\t\t\t\t<span>".getBytes());
		os.write(StringEscapeUtils.escapeXml10(bp.get("authorName")).getBytes());
		os.write("</span></div>\r\n\t\t\t\t<div class=\"blogMessage\">".getBytes());
		os.write(bp.get("message").getBytes());
		os.write("</div>\r\n\t\t\t</div>".getBytes());
		}
		os.write("\r\n\t\t\t\r\n\t\t</div>\r\n\r\n\t\t<div class=\"footer\">\r\n\t\t(c)2003-2016 nectarframework.org (schuttek@gmail.com) - LGPL v3 - <a href=\"/download\">Download</a>\r\n\t\t<a href=\"/about\">About</a>\r\n\t</div>\r\n\r\n\t</div>\r\n</body></html>".getBytes());

	}
}
