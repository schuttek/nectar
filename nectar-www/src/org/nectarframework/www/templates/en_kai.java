package org.nectarframework.www.templates;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.nectarframework.base.service.template.ITemplate;
import org.nectarframework.base.service.template.TemplateService;
import org.nectarframework.base.service.xml.Element;

public class en_kai implements ITemplate {
	@Override
	public void run(TemplateService templateService, OutputStream os, Element actionElement, Element sessionElement) throws IOException {
		os.write("<html>\r\n\r\n<head>\r\n<title>Nectar Framework</title>\r\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></meta>\r\n<meta charset=\"utf-8\"></meta>\r\n<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"/s/style.css\"></link>\r\n<script type=\"text/javascript\" src=\"/s/script.js\"></script>\r\n<script src=\"https://cdn.rawgit.com/google/code-prettify/master/loader/run_prettify.js?autoload=true&skin=default&lang=css\" defer=\"defer\"></script>\r\n</head>\r\n<body>\r\n\r\nbla\r\n</body>\r\n</html>".getBytes());

	}
}
