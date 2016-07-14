package org.nectarframework.base.service.thymeleaf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

import org.nectarframework.base.Main;
import org.nectarframework.base.exception.ConfigurationException;
import org.nectarframework.base.service.Service;
import org.nectarframework.base.service.ServiceUnavailableException;
import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.translation.TranslationService;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.base.service.xml.XmlService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.exceptions.TemplateEngineException;

public class ThymeleafService extends Service {

	private TemplateEngine templateEngine;
	private FileService fileService;
	private TranslationService translationService;
	
	private String templatesDirectory;

	@Override
	public void checkParameters() throws ConfigurationException {
		templatesDirectory = this.serviceParameters.getString("templatesDirectory", null);
	}
	
	protected String getTemplatesDirectory() {
		return templatesDirectory;
	}

	@Override
	public boolean establishDependancies() throws ServiceUnavailableException {
		this.dependancy(XmlService.class);
		fileService = (FileService) this.dependancy(FileService.class);
		translationService = (TranslationService) this.dependancy(TranslationService.class);
		return true;
	}

	@Override
	protected boolean init() {
		templateEngine = new NectarTemplateEngine();

		ThymeTemplateResolver templateResolver = new ThymeTemplateResolver(this, fileService);
		templateEngine.setTemplateResolver(templateResolver);

		ThymeMessageResolver messageResolver = new ThymeMessageResolver(this);
		templateEngine.setMessageResolver(messageResolver);

		ThymeCacheManager cacheManager = new ThymeCacheManager(this);
		templateEngine.setCacheManager(cacheManager);


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

	public void output(Locale locale, String packageName, String templateName, Element elm, StringBuffer sb) throws TemplateEngineException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		output(locale, packageName, templateName, elm, baos);
		sb.append(new String(baos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8));
	}

	public void output(Locale locale, String packageName, String templateName, Element elm, OutputStream os) throws TemplateEngineException {
		ThymeContext context = new ThymeContext(locale, elm, null, null);

		OutputStreamWriter osw = new OutputStreamWriter(os);
		templateEngine.process(templateName, context, osw);
		try {
			osw.flush();
			osw.close();
		} catch (IOException e) {
			Log.fatal(e);
			Main.exit();
		}
	}
	
	public FileService getFileService() {
		return fileService;
	}

	public TranslationService getTranslationService() {
		return translationService;
	}

}
