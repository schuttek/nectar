package org.nectarframework.base.service.template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.nectarframework.base.element.Element;
import org.nectarframework.base.service.Service;

public abstract class TemplateService extends Service {

	public void outputTemplate(StringBuffer sb, String templateName, Locale locale, Element actionElement,
			Element sessionElement) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		outputTemplate(baos, templateName, locale, actionElement, sessionElement);

		sb.append(new String(baos.toByteArray()));
	}

	public abstract void outputTemplate(OutputStream os, String templateName, Locale locale, Element actionElement,
			Element sessionElement) throws IOException;
}
