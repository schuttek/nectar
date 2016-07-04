package org.nectarframework.base.service.thymeleaf;

import java.util.Map;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;

public class ThymeTemplateResolver implements ITemplateResolver {

	private ThymeleafService thymeleafService;
	
	public ThymeTemplateResolver(ThymeleafService thymeleafService) {
		this.thymeleafService = thymeleafService;
	}

	public String getName() {
		return "nectar.thyme.templateResolver";
	}

	public Integer getOrder() {
		return 100;
	}

	public TemplateResolution resolveTemplate(IEngineConfiguration config, String arg1, String arg2, Map<String, Object> arg3) {
		// TODO Auto-generated method stub
		return null;
	}

}
