package org.nectarframework.www.action;

import org.nectarframework.base.service.log.Log;
import org.nectarframework.base.service.xml.Element;
import org.nectarframework.www.data.Article;

public class ArticleViewAction extends BaseAction {

	@Override
	public Element _execute() {
		// get the ID from the Form Object. we can trust that it exists and is
		// greater or equal to zero because of pathConfig.xml settings
		Integer id = form.getInt("id");

		// Article is is a DataStoreObject, so the below is all you need to load
		// it from the database
		Article article;
		try {
			// lots of magic happens in the load method, see DataStoreService
			// documentation!
			article = Article.load(id);
		} catch (Exception e) {
			// save the error in the logging system.
			Log.warn(e);
			// returning null will tell the RequestService not to proceed, and
			// spit out an error message instead.
			return null;
		}

		// Creating the output Element. As a tradition it's named after the
		// Action name, but that's not obligatory.
		Element elm = new Element("arcticleView");

		// Article ID wasn't found in the database, so display an empty page.
		if (article == null) {
			return elm;
		}

		// copy the DataStoreObject's data into the output Element
		elm.add("name", article.getName());
		elm.add("content", article.getContent());
		
	
		return elm;
	}

}

