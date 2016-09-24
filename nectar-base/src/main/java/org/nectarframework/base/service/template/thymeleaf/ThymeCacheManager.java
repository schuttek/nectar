package org.nectarframework.base.service.template.thymeleaf;

import java.util.List;

import org.thymeleaf.cache.ExpressionCacheKey;
import org.thymeleaf.cache.ICache;
import org.thymeleaf.cache.ICacheManager;
import org.thymeleaf.cache.TemplateCacheKey;
import org.thymeleaf.engine.TemplateModel;

public class ThymeCacheManager implements ICacheManager {

	private ThymeleafService thymeleafService;

	public ThymeCacheManager(ThymeleafService thymeleafService) {
		this.thymeleafService = thymeleafService;
	}

	public void clearAllCaches() {
		// TODO Auto-generated method stub
		
	}

	public List<String> getAllSpecificCacheNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICache<ExpressionCacheKey, Object> getExpressionCache() {
		// TODO Auto-generated method stub
		return null;
	}

	public <K, V> ICache<K, V> getSpecificCache(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICache<TemplateCacheKey, TemplateModel> getTemplateCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
