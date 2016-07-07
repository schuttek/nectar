package org.nectarframework.www.action;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.www.data.Article;

public class ArticleViewAction extends BaseAction {

	@Override
	public Element _execute() {

		Integer id = form.getInt("id");

		Element elm = new Element("arcticleView");
		Article article;
		try {
			article = Article.load(id);
		} catch (Exception e) {
			Log.warn(e);
			return null;
		}

		if (article == null) {
			return elm;
		}
		
		elm.add("name", article.getName());
		elm.add("content", article.getContent());

		return elm;
	}

}
