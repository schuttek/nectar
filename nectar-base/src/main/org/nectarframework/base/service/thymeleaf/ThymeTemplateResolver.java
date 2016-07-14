package org.nectarframework.base.service.thymeleaf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.nectarframework.base.service.file.FileService;
import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.tools.StringTools;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.cache.TTLCacheEntryValidity;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;

public class ThymeTemplateResolver implements ITemplateResolver {

	private ThymeleafService thymeleafService;

	// private HashMap<String, ThymeTemplateResource> templateResoureMap = new
	// HashMap<String, ThymeTemplateResource>();

	private FileService fileService;

	public ThymeTemplateResolver(ThymeleafService thymeleafService, FileService fileService) {
		this.thymeleafService = thymeleafService;
		this.fileService = fileService;
	}

	public String getName() {
		return "nectar.thyme.templateResolver";
	}

	public Integer getOrder() {
		return 100;
	}

	public TemplateResolution resolveTemplate(IEngineConfiguration config, String ownerTemplate, String template,
			Map<String, Object> templateResolutionAttributes) {
		Log.trace("ThymeTemplateResolver.resolveTemplate() " + config.toString() + " " + ownerTemplate + " " + template
				+ " " + StringTools.mapToString(templateResolutionAttributes));

		ThymeTemplateResource templateResource;
		try {
			templateResource = getTemplateResource(template);
		} catch (IOException e) {
			Log.fatal(e);
			return null;
		}

		TemplateResolution tr = new TemplateResolution(templateResource, true, TemplateMode.HTML, false,
				new TTLCacheEntryValidity(10000));
		return tr;
	}

	public ThymeTemplateResource getTemplateResource(String templateName) throws IOException {
		File f = fileService.getFile(thymeleafService.getTemplatesDirectory() + "/" + templateName + ".html");
		ThymeTemplateResource templateResource = new ThymeTemplateResource(this, templateName,
				Files.readAllBytes(f.toPath()));
		return templateResource;
	}

}
