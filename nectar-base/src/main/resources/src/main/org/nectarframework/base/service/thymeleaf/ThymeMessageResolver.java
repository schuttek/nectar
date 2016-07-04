package org.nectarframework.base.service.thymeleaf;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.IMessageResolver;

public class ThymeMessageResolver implements IMessageResolver {
	private ThymeleafService thymeleafService;

	public ThymeMessageResolver(ThymeleafService thymeleafService) {
		this.thymeleafService = thymeleafService;
	}

	public String getName() {
		return "nectar.thyme.messageResolver";
	}

	public Integer getOrder() {
		return 100;
	}


	public String createAbsentMessageRepresentation(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
		return thymeleafService.getTranslationService().get(context.getLocale(), "", key, messageParameters);
	}

	public String resolveMessage(ITemplateContext context, Class<?> origin, String key, Object[] messageParameters) {
		return thymeleafService.getTranslationService().get(context.getLocale(), "", key, messageParameters);
	}

}
