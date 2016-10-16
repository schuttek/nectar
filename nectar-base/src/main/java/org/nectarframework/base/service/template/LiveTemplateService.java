package org.nectarframework.base.service.template;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.ServiceParameters;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.pathfinder.IPathFinder;
import org.nectarframework.base.service.translation.TranslationService;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;

public class LiveTemplateService extends TemplateService {

	private String rawTemplatesRootDir;
	private TranslationService translationService;
	private IPathFinder directoryService;

	@Override
	public void outputTemplate(OutputStream os, String templateName, Locale locale, Element actionElement,
			Element sessionElement) throws IOException {

		
		
	}

	@Override
	public void checkParameters(ServiceParameters sp) throws ConfigurationException {
		rawTemplatesRootDir = sp.getString("rawTemplatesRootDir", null);
		if (rawTemplatesRootDir == null) {
			throw new ConfigurationException("TemplateService needs rawTemplatesRootDir config.");
		}
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		dependancy(XmlService.class);
		translationService = (TranslationService) dependancy(TranslationService.class);
		directoryService = (IPathFinder) dependancy(IPathFinder.class);
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

}
