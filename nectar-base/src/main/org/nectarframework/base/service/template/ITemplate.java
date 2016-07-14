package org.nectarframework.base.service.template;

import java.io.IOException;
import java.io.OutputStream;

import org.nectarframework.base.service.xml.Element;

public interface ITemplate {
	public void run(TemplateService templateService, OutputStream os, Element actionElement, Element sessionElement) throws IOException;
}
