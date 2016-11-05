package org.nectarframework.www.templates;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.nectarframework.base.service.template.ITemplate;
import org.nectarframework.base.service.template.TemplateService;
import org.nectarframework.base.service.xml.Element;

@SuppressWarnings("unused")
public class fr_index implements ITemplate {
	@Override
	public void run(TemplateService templateService, OutputStream os, Element actionElement, Element sessionElement) throws IOException {
		os.write("<html><head><title>??fr..-/common.title??</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></meta><link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"../fileservice_root/s/style.css\"></link><script type=\"text/javascript\" src=\"../fileservice_root/s/script.js\"></script><script src=\"https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js?autoload=true&skin=default&lang=css\" defer=\"defer\"></script></head><body onload=\"main.load()\"><div class=\"wrap\"><div class=\"header\"><div class=\"title\"><img class=\"title\" src=\"/s/icon_bird_black.png\" height=\"24\" width=\"24\"></img><span>??fr..-/common.header.title??</span></div><div class=\"slogan\">??fr..-/common.header.slogan??</div></div><div class=\"navbar\"><img class=\"navbar\" src=\"/s/icon_about.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/\">??fr..-/common.leftLinks.home??</a><img class=\"navbar\" src=\"/s/icon_about.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/overview\">??fr..-/common.leftLinks.overview??</a><img class=\"navbar\" src=\"/s/icon_about.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/documentation\">??fr..-/common.leftLinks.documentation??</a><img class=\"navbar\" src=\"/s/icon_about.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/download\">??fr..-/common.leftLinks.download??</a><img class=\"navbar\" src=\"/s/icon_about.png\" height=\"24\" width=\"24\"></img><a class=\"navbar\" href=\"/about\">??fr..-/common.leftLinks.about??</a></div><div class=\"content\"><h2>What is Nectar Framework?</h2><p><b>Nectar Framework is a Web Applications Framework written in Java.</b>It is based on Model-View-Controller push-based architecture, and it's core services include an HTTP Server, a MySQL database connector, a data scaffolding / abstraction layer, a caching service, a thread management service. It is designed to be both strict and light-weight, which allows for great performance and scalability.</p>".getBytes());
		for (Element bp : actionElement.getChildren("index").getFirst().getChildren("blogPost")) {
		os.write("<div class=\"blog\"><h3>Blog Title</h3><div>Blog Message</div></div>".getBytes());
		}
		os.write("</div><div class=\"sidebar\"><p>??fr..-/common.sidebar.nectar_uses??</p><p>-<a href=\"http://www.simpleframework.org\">??fr..-/common.sidebar.simple??</a><br></br>-<a href=\"http://hbase.apache.org/\">??fr..-/common.sidebar.hbase??</a><br></br>-<a href=\"http://www.thymeleaf.org/\">??fr..-/common.sidebar.thymeleaf??</a><br></br>-<a href=\"https://maven.apache.org/\">??fr..-/common.sidebar.maven??</a><br></br>-<a href=\"https://xerces.apache.org/xerces2-j/\">??fr..-/common.sidebar.xerces??</a></p></div><div class=\"footer\">(c)2003-2016 nectarframework.org (schuttek@gmail.com) - LGPL v3 -<a href=\"/download\">??fr..-/common.leftLinks.download??</a><a href=\"/about\">??fr..-/common.leftLinks.about??</a></div></div></body></html>".getBytes());

	}
}
